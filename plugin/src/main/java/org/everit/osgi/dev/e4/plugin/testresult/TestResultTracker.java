/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.osgi.dev.e4.plugin.testresult;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.junit.JUnitCore;
import org.everit.osgi.dev.dist.util.attach.EOSGiVMManager;
import org.everit.osgi.dev.dist.util.attach.EnvironmentRuntimeInfo;
import org.everit.osgi.dev.e4.plugin.EOSGiEclipsePlugin;
import org.everit.osgi.dev.e4.plugin.EOSGiLog;
import org.everit.osgi.dev.e4.plugin.ExecutableEnvironment;
import org.everit.osgi.dev.e4.plugin.util.DeleteOnCloseTempFileWrapper;
import org.xml.sax.SAXException;

/**
 * Tracks test results of launched environments by checking test result folder changes and shows
 * them in the JUnit view of Eclipse.
 */
public class TestResultTracker implements Closeable {

  /**
   * Information of a tracked JVM.
   */
  private static class TrackedJVMInfo {

    long lastDistTimestamp;

    String projectName;

    File testResultFolder;

    WatchKey watchKey;

  }

  private static final long WATCH_EVENT_CHECK_PERIOD = 100;

  private final AtomicBoolean closed = new AtomicBoolean(false);

  private final EOSGiLog eosgiLog = EOSGiEclipsePlugin.getDefault().getEOSGiLog();

  private final EOSGiVMManager eosgiVMManager;

  private final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

  private final Map<File, TrackedJVMInfo> trackedJVMsByResultFolder = new HashMap<>();

  private final Runnable vmStateChangeListener;

  private final WatchService watchService;

  /**
   * Constructor that initializes the folder and file watch service.
   */
  public TestResultTracker(final EOSGiVMManager eosgiVMManager) {
    this.eosgiVMManager = eosgiVMManager;
    try {
      watchService = FileSystems.getDefault().newWatchService();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    vmStateChangeListener = () -> checkJVMRunningChanges();

    eosgiVMManager.addStateChangeListener(vmStateChangeListener);
    checkJVMRunningChanges();

    new Thread(this::watchTestResultFolders).start();
  }

  private void addContentOfTestResultFileToSummary(final File testResultFile,
      final TestResultSummarizer summarizedTestResult) {

    SAXParser saxParser;
    try {
      saxParser = saxParserFactory.newSAXParser();
    } catch (ParserConfigurationException | SAXException e) {
      throw new RuntimeException(e);
    }

    try {
      saxParser.parse(testResultFile, new TestResultSAXHandler(summarizedTestResult));
    } catch (SAXException | IOException e) {
      eosgiLog.error("Cannot parse test file: " + testResultFile, e);
    }

  }

  private synchronized void checkJVMRunningChanges() {
    Set<EnvironmentRuntimeInfo> runtimeInformations = eosgiVMManager.getRuntimeInformations();
    Set<File> testResultFoldersWithoutRunningJVM =
        new HashSet<>(trackedJVMsByResultFolder.keySet());

    for (EnvironmentRuntimeInfo info : runtimeInformations) {
      Properties systemProperties = info.systemProperties;
      if (EOSGiEclipsePlugin.ECLIPSE_INSTANCE
          .equals(systemProperties.get(EOSGiEclipsePlugin.SYSPROP_ECLIPSE_INSTANCE))) {

        String testResultFolder =
            systemProperties.getProperty(EOSGiEclipsePlugin.SYSPROP_TEST_RESULT_FOLDER);

        File testResultFolderFile = new File(testResultFolder);
        if (!testResultFoldersWithoutRunningJVM.remove(testResultFolderFile)) {
          startWatchingTestResultFolder(testResultFolderFile, systemProperties);
        }
      }
    }

    for (File folder : testResultFoldersWithoutRunningJVM) {
      TrackedJVMInfo trackedJVMInfo = trackedJVMsByResultFolder.remove(folder);
      trackedJVMInfo.watchKey.cancel();
    }
    trackedJVMsByResultFolder.keySet().removeAll(testResultFoldersWithoutRunningJVM);
  }

  private synchronized void checkTestResultFolder(
      final Map<TrackedJVMInfo, Long> iterationIdByTrackedJVM) {

    long currentIterationId = System.nanoTime();

    for (Iterator<Entry<File, TrackedJVMInfo>> iterator =
        trackedJVMsByResultFolder.entrySet().iterator(); iterator.hasNext();) {

      Entry<File, TrackedJVMInfo> trackedEntry = iterator.next();
      File resultFolder = trackedEntry.getKey();
      TrackedJVMInfo trackedJVMInfo = trackedEntry.getValue();
      if (trackedJVMInfo.watchKey.isValid()) {
        List<WatchEvent<?>> events = trackedJVMInfo.watchKey.pollEvents();
        if (!events.isEmpty()) {
          iterationIdByTrackedJVM.put(trackedJVMInfo, currentIterationId);
        }
      } else {
        handleInvalidWatchKey(iterator, resultFolder, trackedJVMInfo);
      }
    }

    for (Iterator<Entry<TrackedJVMInfo, Long>> iterator =
        iterationIdByTrackedJVM.entrySet().iterator(); iterator
            .hasNext();) {

      Entry<TrackedJVMInfo, Long> entry = iterator.next();
      if (entry.getValue().longValue() != currentIterationId) {
        iterator.remove();
        importTestFilesOfTrackedJVMIntoEclipseJUnitView(entry.getKey());
      }
    }
  }

  @Override
  public synchronized void close() {
    if (closed.getAndSet(true)) {
      return;
    }
    try {
      watchService.close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } finally {
      eosgiVMManager.removeStateChangeListener(vmStateChangeListener);
    }
  }

  private WatchKey createWatchKeyForTestResultFolder(final File testResultFolderFile)
      throws IOException {
    return testResultFolderFile.toPath().register(watchService,
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW);
  }

  private void handleInvalidWatchKey(final Iterator<Entry<File, TrackedJVMInfo>> iterator,
      final File resultFolder, final TrackedJVMInfo trackedJVMInfo) {

    // Probably someone deleted the folder so we try to re-create it
    if (resultFolder.exists() || resultFolder.mkdirs()) {
      try {
        trackedJVMInfo.watchKey = createWatchKeyForTestResultFolder(resultFolder);
      } catch (IOException e) {
        eosgiLog.error("Cannot watch test result folder", e);
        iterator.remove();
      }
    } else {
      eosgiLog.error("Cannot create test result folder:" + resultFolder);
      iterator.remove();
    }
  }

  private void importTestFilesOfTrackedJVMIntoEclipseJUnitView(
      final TrackedJVMInfo trackedJVMInfo) {

    Optional<TestResultSummarizer> optionalSummarizedTestResult =
        summarizeTestResults(trackedJVMInfo);

    if (!optionalSummarizedTestResult.isPresent()) {
      return;
    }

    TestResultSummarizer summarizedTestResult = optionalSummarizedTestResult.get();

    String sumFileContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<testrun name=\""
        + trackedJVMInfo.projectName + "\" project=\"" + trackedJVMInfo.projectName + "\" tests=\""
        + summarizedTestResult.tests + "\" started=\"" + summarizedTestResult.started
        + "\" failures=\"" + summarizedTestResult.failures + "\" errors=\""
        + summarizedTestResult.errors + "\" ignored=\"" + summarizedTestResult.ignored + "\">\n"
        + summarizedTestResult.xmlBody.toString() + "</testrun>\n";

    File sumFile = null;
    try (DeleteOnCloseTempFileWrapper sumTempFile =
        new DeleteOnCloseTempFileWrapper("eosgi-testresult-", ".xml")) {

      sumFile = sumTempFile.getTempFile();
      try (Writer writer =
          new OutputStreamWriter(new FileOutputStream(sumFile), StandardCharsets.UTF_8)) {

        writer.write(sumFileContent);
      } catch (IOException e) {
        eosgiLog.error("Cannot create new summarized test result file: " + sumFile, e);
      }

      JUnitCore.importTestRunSession(sumFile);
    } catch (CoreException e) {
      eosgiLog.error("Cannot import test results from sum file '" + sumFile + "' with content:\n"
          + sumFileContent, e);
    }
  }

  private List<File> resolveNewerNonEmptyXMLFilesThanLastDist(final TrackedJVMInfo trackedJVMInfo) {
    File[] testResultFiles = trackedJVMInfo.testResultFolder.listFiles();
    List<File> newerFilesThanLastDist = new ArrayList<>(testResultFiles.length);
    for (File file : testResultFiles) {
      if (file.lastModified() >= trackedJVMInfo.lastDistTimestamp && file.getName().endsWith(".xml")
          && file.length() > 0) {
        newerFilesThanLastDist.add(file);
      }
    }
    return newerFilesThanLastDist;
  }

  private void startWatchingTestResultFolder(final File testResultFolderFile,
      final Properties systemProperties) {

    if (!testResultFolderFile.exists() && !testResultFolderFile.mkdirs()) {
      throw new UncheckedIOException(new IOException(
          "Failed to create non-existent test result folder: " + testResultFolderFile));
    }

    String projectName =
        systemProperties.getProperty(EOSGiEclipsePlugin.SYSPROP_ECLIPSE_PROJECT_NAME);

    long startTimestamp =
        Long.parseLong(systemProperties.getProperty(EOSGiEclipsePlugin.SYSPROP_START_TIMESTAMP));

    WatchKey watchKey;
    try {
      watchKey =
          createWatchKeyForTestResultFolder(testResultFolderFile);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    TrackedJVMInfo info = new TrackedJVMInfo();
    info.testResultFolder = testResultFolderFile;
    info.lastDistTimestamp = startTimestamp;
    info.projectName = projectName;
    info.watchKey = watchKey;
    trackedJVMsByResultFolder.put(testResultFolderFile, info);
  }

  private Optional<TestResultSummarizer> summarizeTestResults(final TrackedJVMInfo trackedJVMInfo) {

    List<File> testResultFiles = resolveNewerNonEmptyXMLFilesThanLastDist(trackedJVMInfo);

    if (testResultFiles.isEmpty()) {
      return Optional.empty();
    }

    TestResultSummarizer testResultSummarizer = new TestResultSummarizer();
    testResultSummarizer.projectName = trackedJVMInfo.projectName;

    for (File testResultFile : testResultFiles) {
      addContentOfTestResultFileToSummary(testResultFile, testResultSummarizer);
    }

    return Optional.of(testResultSummarizer);
  }

  /**
   * Must be called if a new distribution is done on an executable environment as in that case only
   * test results will be processed that are after this call.
   *
   * @param executableEnvironment
   *          The executable environment that might be tracked for test results.
   */
  public synchronized void updateDistTimestampOfEnvironment(
      final ExecutableEnvironment executableEnvironment) {

    File testResultFolder = executableEnvironment.getTestResultFolder();
    TrackedJVMInfo trackedJVMInfo = trackedJVMsByResultFolder.get(testResultFolder);
    if (trackedJVMInfo != null) {
      trackedJVMInfo.lastDistTimestamp = System.currentTimeMillis();
    }
  }

  private void watchTestResultFolders() {
    Map<TrackedJVMInfo, Long> iterationIdByTrackedJVM = new HashMap<>();
    while (!closed.get()) {
      checkTestResultFolder(iterationIdByTrackedJVM);

      try {
        Thread.sleep(WATCH_EVENT_CHECK_PERIOD);
      } catch (InterruptedException e) {
        close();
        Thread.currentThread().interrupt();
      }
    }
  }

}
