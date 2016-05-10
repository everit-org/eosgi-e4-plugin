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
package org.everit.osgi.dev.e4.plugin.core.launcher;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.everit.osgi.dev.e4.plugin.core.server.EOSGILaunchConfigurationDelegate;
import org.everit.osgi.dev.e4.plugin.ui.EOSGiLog;
import org.everit.osgi.dev.e4.plugin.ui.EOSGiPluginActivator;
import org.everit.osgi.dev.eosgi.dist.schema.util.LaunchConfigurationDTO;

/**
 * Builder class for launch configurations.
 */
public class LaunchConfigurationBuilder {

  private String buildDirectory;

  private String environmentId;

  private EOSGiLog eosgiLog;

  private LaunchConfigurationDTO launchConfiguration;

  private final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();

  private String projectName;

  private ILaunchConfigurationWorkingCopy wc;

  /**
   * Constructor.
   *
   * @param projectName
   *          name of the project.
   * @param environmentId
   *          id of the environment
   * @param buildDirectory
   *          build directory of the project.
   */
  public LaunchConfigurationBuilder(final String projectName, final String environmentId,
      final String buildDirectory) {
    super();
    ILog log = EOSGiPluginActivator.getDefault().getLog();
    eosgiLog = new EOSGiLog(log);
    this.projectName = projectName;
    this.environmentId = environmentId;
    this.buildDirectory = buildDirectory;
  }

  public LaunchConfigurationBuilder addEnvironmentConfigurationDTO(
      final LaunchConfigurationDTO environmentConfigurationDTO) {
    this.launchConfiguration = environmentConfigurationDTO;
    return this;
  }

  public LaunchConfigurationBuilder addLauncherConfigurationWorkingCopy(
      final ILaunchConfigurationWorkingCopy configurationWorkingCopy) {
    this.wc = configurationWorkingCopy;
    return this;
  }

  /**
   * Build a launcher configuration instance.
   *
   * @return {@link ILaunchConfiguration} instance.
   */
  public ILaunchConfiguration build() {
    ILaunchConfigurationType type = manager
        .getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);

    if (launchConfiguration == null) {
      return null;
    }

    if (wc == null) {
      try {
        wc = type.newInstance(null, environmentId + "_" + projectName);
      } catch (CoreException e) {
        eosgiLog.error("Could not create laucher working copy", e);
      }
    }
    if (wc == null) {
      return null;
    }

    String workingDirectory = buildDirectory + File.separator + "eosgi-dist" + File.separator
        + environmentId;
    wc = updateCurrentLauncherConfigurationWorkingCopy(workingDirectory,
        launchConfiguration);

    try {
      return wc.doSave();
    } catch (CoreException e) {
      eosgiLog.error("Could not save new launch configuration.", e);
      return null;
    }
  }

  private String createArgumentsString(
      final List<String> argumentList) {
    final StringBuilder stringBuilder = new StringBuilder();

    if (argumentList == null) {
      return stringBuilder.toString();
    }

    argumentList.forEach(argument -> {
      stringBuilder.append(' ' + argument);
    });

    String argumentsString = stringBuilder.toString();
    return argumentsString;
  }

  private List<String> createClasspathList(final String rootDirectory, final String classpath)
      throws CoreException {
    List<String> classpathEntryList = new ArrayList<>();

    if ("*".equals(classpath)) {
      List<String> jarFiles = fetchAllJarFileFromRootDirectory(rootDirectory);
      classpathEntryList.addAll(jarFiles);
    } else if (classpath != null) {
      classpathEntryList.addAll(Arrays.asList(classpath.split(":")));
    }

    List<String> classpathMementoEntryList = new ArrayList<>();
    for (String classpathEntry : classpathEntryList) {
      classpathMementoEntryList.add(toMemento(rootDirectory, classpathEntry));
    }
    return classpathMementoEntryList;
  }

  private String createVmArgs(final List<String> vmArgumentList) {
    StringBuilder stringBuilder = new StringBuilder();

    if (vmArgumentList == null) {
      return stringBuilder.toString();
    }

    vmArgumentList.forEach((vmOption) -> {
      if (vmOption.indexOf("Xrunjdwp") == -1) {
        stringBuilder.append(" " + vmOption);
      }
    });
    return stringBuilder.toString();
  }

  private List<String> fetchAllJarFileFromRootDirectory(final String rootDirectory) {
    File rootFolder = new File(rootDirectory);
    if (rootFolder.isDirectory()) {
      File[] files = rootFolder.listFiles((FileFilter) pathname -> {
        return !pathname.isDirectory() && pathname.getName().endsWith(".jar");
      });
      return Arrays.asList(files).stream().map(file -> file.getName()).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  private String toMemento(final String rootDirectory, final String classpath)
      throws CoreException {
    IPath path = new Path(rootDirectory + "/" + classpath);
    IRuntimeClasspathEntry classpathEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(path);
    // IRuntimeClasspathEntry2 newRuntimeClasspathEntry =
    // LaunchingPlugin.getDefault().newRuntimeClasspathEntry("");
    classpathEntry.setExternalAnnotationsPath(path);
    return classpathEntry.getMemento();
  }

  private ILaunchConfigurationWorkingCopy updateCurrentLauncherConfigurationWorkingCopy(
      final String workingDirectory,
      final LaunchConfigurationDTO environmentConfigurationDTO) {

    String argumentsString = createArgumentsString(environmentConfigurationDTO.programArguments);
    String vmArgsList = createVmArgs(environmentConfigurationDTO.vmArguments);

    List<String> classPathList = new ArrayList<>();
    try {
      classPathList = createClasspathList(workingDirectory,
          environmentConfigurationDTO.classpath);
    } catch (CoreException e) {
      eosgiLog.error("Could not resolv classpath entries.", e);
    }

    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
        environmentConfigurationDTO.mainClass);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, workingDirectory);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, argumentsString);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classPathList);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArgsList);

    wc.setAttribute(EOSGILaunchConfigurationDelegate.LAUNCHER_ATTR_ENVIRONMENT_ID, environmentId);

    return wc;
  }

}
