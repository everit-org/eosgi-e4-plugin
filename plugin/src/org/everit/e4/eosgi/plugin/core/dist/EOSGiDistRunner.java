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
package org.everit.e4.eosgi.plugin.core.dist;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.console.MessageConsoleStream;
import org.everit.e4.eosgi.plugin.core.EventType;
import org.everit.e4.eosgi.plugin.core.ModelChangeEvent;
import org.everit.e4.eosgi.plugin.core.util.DistUtils;
import org.everit.e4.eosgi.plugin.ui.EOSGiLog;
import org.everit.e4.eosgi.plugin.ui.EOSGiPluginActivator;
import org.rzo.yajsw.os.OperatingSystem;
import org.rzo.yajsw.os.Process;
import org.rzo.yajsw.os.ProcessManager;
import org.rzo.yajsw.os.ms.win.w32.WindowsXPProcess;
import org.rzo.yajsw.os.posix.linux.LinuxProcess;

/**
 * A {@link DistRunner} implementation that used the YAJSW {@link Process} for running a dist
 * instance.
 */
public class EOSGiDistRunner extends Observable implements DistRunner {

  /**
   * A shutdown hook that stops the started OSGi container.
   */
  private class ShutdownHook extends Thread {

    private final Process process;

    private final int shutdownTimeout;

    public ShutdownHook(final Process process, final int shutdownTimeout) {
      this.process = process;
      this.shutdownTimeout = shutdownTimeout;
    }

    @Override
    public void run() {
      shutdownProcess(process, shutdownTimeout, 0);
    }
  }

  private AutoCloseable closeable;

  private String directory;

  private String environmentName;

  private EOSGiLog log;

  private Process process;

  private AtomicBoolean running = new AtomicBoolean(false);

  /**
   * Constructor with build directory and environment id.
   *
   * @param directory
   *          build directory String.
   * @param environmentId
   *          environment id.
   */
  public EOSGiDistRunner(final String directory, final String environmentId) {
    super();
    this.directory = directory;
    environmentName = environmentId;
    log = new EOSGiLog(EOSGiPluginActivator.getDefault().getLog());
  }

  private Process createDistProcess() {
    process = createOsSpecificProcess();
    process.setTitle(environmentName);

    String distStartCommand = DistUtils.getDistStartCommand(directory, environmentName);

    process.setCommand(distStartCommand);
    process.setVisible(false);
    process.setTeeName(null);
    process.setPipeStreams(true, false);
    // process.setLogger(Logger.getLogger(EOSGiDistRunner.class.getName()));

    // TODO put environment settings too
    return process;
  }

  private Process createOsSpecificProcess() {
    OperatingSystem operatingSystem = OperatingSystem.instance();
    String lowerCaseOperatingSystemName = operatingSystem.getOperatingSystemName()
        .toLowerCase(Locale.getDefault());
    Process process;
    if (lowerCaseOperatingSystemName.contains("linux")
        || lowerCaseOperatingSystemName.startsWith("mac os x")) {
      process = new LinuxProcess();
    } else {
      ProcessManager processManager = operatingSystem.processManagerInstance();
      process = processManager.createProcess();
    }
    return process;
  }

  private void createRedirecter(final Process process) {
    MessageConsoleStream messageConsoleStream = EOSGiPluginActivator.getDefault()
        .getConsoleWithName(environmentName);

    InputStream inputStream = process.getInputStream();
    OutputStream[] outputStreams = new OutputStream[] { messageConsoleStream };
    DaemonStreamRedirector daemonStreamRedirector = new DaemonStreamRedirector(inputStream,
        outputStreams, log);

    try {
      daemonStreamRedirector.start();
    } catch (IOException e) {
      try {
        daemonStreamRedirector.close();
      } catch (IOException e1) {
        e.addSuppressed(e1);
      }
      log.error("Could not start stream redirector.", e);
    }
    closeable = new Closeable() {

      @Override
      public void close() throws IOException {
        daemonStreamRedirector.close();
      }
    };
  }

  @Override
  public void forcedStop() {
    // TODO implement it!
  }

  @Override
  public boolean isRunning() {
    return running.get();
  }

  private void registerShutdownHook(final Process process) {
    ShutdownHook shutdownHook = new ShutdownHook(process, 5000);
    Runtime.getRuntime().addShutdownHook(shutdownHook);
  }

  private void shutdownProcess(final Process process, final int shutdownTimeout,
      final int code) {
    int pid = process.getPid();
    if (process.isRunning()) {
      if (process instanceof WindowsXPProcess) {
        // In case of windows xp process we must kill the process with a command as there is no
        // visible
        // window and kill tree command of YAJSW does not work. Hopefully this is a temporary
        // solution.

        OperatingSystem operatingSystem = OperatingSystem.instance();
        ProcessManager processManagerInstance = operatingSystem.processManagerInstance();
        Process killProcess = processManagerInstance.createProcess();
        String killCommand = "taskkill /F /T /PID " + process.getPid();
        log.warning("Killing windows process with command: " + killCommand + "");
        killProcess.setCommand(killCommand);
        killProcess.setVisible(false);
        killProcess.start();
        process.waitFor(shutdownTimeout);
      } else {
        boolean stopped = process.stop(shutdownTimeout, code);
        if (!stopped) {
          log.warning("Could not stop process with PID " + pid);
        }
      }
    }
  }

  @Override
  public void start(final IProgressMonitor monitor) {
    Process process = createDistProcess();
    boolean started = process.start();
    if (started) {
      registerShutdownHook(process);
      createRedirecter(process);
      running.set(true);
      setChanged();
    } else {
      log.error("Could not start dist process.");
    }
    notifyObservers(new ModelChangeEvent()
        .eventType(EventType.ENVIRONMENT).arg(environmentName));
  }

  @Override
  public void stop(final IProgressMonitor monitor) {
    shutdownProcess(process, 10000, 9);
    if (process.isRunning()) {
      try {
        closeable.close();
      } catch (Exception e) {
        log.error("Could not close process stream(s).", e);
      }
    }
    running.set(false);
    setChanged();
    notifyObservers(new ModelChangeEvent()
        .eventType(EventType.ENVIRONMENT).arg(environmentName));
  }

}
