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
package org.everit.e4.eosgi.plugin.core.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.everit.e4.eosgi.plugin.core.server.EOSGILaunchConfigurationDelegate;
import org.everit.e4.eosgi.plugin.ui.EOSGiLog;
import org.everit.e4.eosgi.plugin.ui.EOSGiPluginActivator;
import org.everit.osgi.dev.maven.jaxb.dist.definition.CommandArguments;
import org.everit.osgi.dev.maven.jaxb.dist.definition.DistributionPackageType;
import org.everit.osgi.dev.maven.jaxb.dist.definition.LauncherConfiguration;
import org.everit.osgi.dev.maven.jaxb.dist.definition.ObjectFactory;

public class LauncherConfigurationFactory {

  private final JAXBContext distConfigJAXBContext;

  private EOSGiLog eosgiLog;

  private final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();

  private ILaunchConfigurationWorkingCopy wc;

  public LauncherConfigurationFactory() {
    super();
    ILog log = EOSGiPluginActivator.getDefault().getLog();
    eosgiLog = new EOSGiLog(log);
    try {
      distConfigJAXBContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(),
          ObjectFactory.class.getClassLoader());
    } catch (JAXBException e) {
      throw new RuntimeException(
          "Could not create JAXB Context for distribution configuration file", e);
    }
  }

  public LauncherConfigurationFactory(final ILaunchConfigurationWorkingCopy wc) {
    this();
    this.wc = wc;
  }

  /**
   * Create a new {@link ILaunchConfiguration} with the given params.
   *
   * @param projectName
   *          name of the project.
   * @param environmentId
   *          id of the environment.
   * @param buildDirectory
   *          build directory.
   * @return launch configuration or <code>null</code>.
   */
  public final ILaunchConfiguration create(final String projectName,
      final String environmentId, final String buildDirectory) {

    // File distXmlFile = new File(rootDirectory);
    // DistributionPackageType distributionPackageType =
    // readDistConfig(project.getLocation().toFile());

    /*
     * A '..' nem fog kelleni, csak fejlesztes idejere kell, amig nincs 4.0.0-as maven plugin.
     */
    DistributionPackageType distributionPackageType = readDistConfig(
        new File(buildDirectory + File.separator + "..")); /// target/eosgi-dist/equinoxtest

    ILaunchConfiguration configuration = null;
    if (distributionPackageType == null) {
      eosgiLog.error("Could not load dist xml file.");
    } else {
      List<LauncherConfiguration> launcherConfigurations = distributionPackageType
          .getLauncherConfiguration();

      if (launcherConfigurations.size() > 0) {
        LauncherConfiguration launcherConfiguration = launcherConfigurations.get(0);

        Objects.requireNonNull(launcherConfiguration, "launcherConfiguration must be not null");
        Objects.requireNonNull(launcherConfiguration.getEnvironmentName(),
            "launcherConfiguration.environmentName must be not null");
        Objects.requireNonNull(launcherConfiguration.getMainClass(),
            "launcherConfiguration.mainClass must be not null");
        Objects.requireNonNull(launcherConfiguration.getMainJar(),
            "launcherConfiguration.mainJar must be not null");

        configuration = createConfigurationFromLauncherConfiguration(projectName,
            environmentId,
            launcherConfiguration, buildDirectory);
      }
    }
    return configuration;
  }

  private String createArgumentsString(final LauncherConfiguration launcherConfiguration) {
    final StringBuilder stringBuilder = new StringBuilder();
    CommandArguments commandArguments = launcherConfiguration.getCommandArguments();
    if ((commandArguments != null) && (commandArguments.getCommandArgument() != null)) {
      commandArguments.getCommandArgument().forEach((argument) -> {
        stringBuilder.append(" " + argument);
      });
    }

    String argumentsString = stringBuilder.toString();
    return argumentsString;
  }

  private List<String> createClassPathList(final String rootDirectory, final String mainJar)
      throws CoreException {
    List<String> classPathList = new ArrayList<>();
    IPath path = new Path(rootDirectory + "/" + mainJar);
    IRuntimeClasspathEntry classpathEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(path);
    classpathEntry.setExternalAnnotationsPath(new Path(rootDirectory));
    classpathEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
    classPathList.add(classpathEntry.getMemento());
    return classPathList;
  }

  private ILaunchConfiguration createConfigurationFromLauncherConfiguration(
      final String projectName, final String environmentId,
      final LauncherConfiguration launcherConfiguration, final String rootDir) {

    ILaunchConfigurationType type = manager
        .getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);

    if (wc == null) {
      try {
        wc = type.newInstance(null, launcherConfiguration.getEnvironmentName() + "_" + projectName);
      } catch (CoreException e) {
        eosgiLog.error("Could not create laucher working copy", e);
      }
    }
    if (wc == null) {
      return null;
    }

    String workingDirectory = rootDir + File.separator + "eosgi-dist" + File.separator
        + environmentId;
    wc = updateCurrentLauncherConfigurationWorkingCopy(projectName, workingDirectory,
        launcherConfiguration);

    try {
      return wc.doSave();
    } catch (CoreException e) {
      eosgiLog.error("Could not save new launch configuration.", e);
      return null;
    }
  }

  private String createVmArgs(final LauncherConfiguration launcherConfiguration) {
    StringBuilder stringBuilder = new StringBuilder();
    if ((launcherConfiguration.getVmArguments() != null)
        && (launcherConfiguration.getVmArguments().getVmArgument() != null)) {
      launcherConfiguration.getVmArguments().getVmArgument().forEach((vmArg) -> {
        stringBuilder.append(" " + vmArg);
      });
    }
    return stringBuilder.toString();
  }

  private String getDistRootDir(final IProject project, final String environmentId) {
    IMavenProjectFacade mavenProjectFacade = MavenPlugin.getMavenProjectRegistry()
        .getProject(project);
    if (mavenProjectFacade != null) {
      MavenProject mavenProject;
      try {
        mavenProject = mavenProjectFacade.getMavenProject(new NullProgressMonitor());
        String buildDirectory = mavenProject.getBuild().getDirectory();
        return buildDirectory + File.separator + "eosgi-dist" + File.separator + environmentId;
      } catch (CoreException e) {
        eosgiLog.error("Could not resolv build directory for project", e);
      }
    }
    return null;
  }

  private DistributionPackageType readDistConfig(final File distFolderFile) {
    File distConfigFile = new File(distFolderFile, "/eosgi.dist.xml");
    if (distConfigFile.exists()) {
      try {
        Unmarshaller unmarshaller = distConfigJAXBContext.createUnmarshaller();
        Object distributionPackage = unmarshaller.unmarshal(distConfigFile);
        if (distributionPackage instanceof JAXBElement) {

          @SuppressWarnings("unchecked")
          JAXBElement<DistributionPackageType> jaxbDistPack = (JAXBElement<DistributionPackageType>) distributionPackage;
          distributionPackage = jaxbDistPack.getValue();
        }
        if (distributionPackage instanceof DistributionPackageType) {
          return (DistributionPackageType) distributionPackage;
        } else {
          throw new RuntimeException(
              "The root element in the provided distribution configuration file "
                  + "is not the expected DistributionPackage element");
        }
      } catch (JAXBException e) {
        throw new RuntimeException(
            "Failed to process already existing distribution configuration file: "
                + distConfigFile.getAbsolutePath(),
            e);
      }
    } else {
      return null;
    }
  }

  private ILaunchConfigurationWorkingCopy updateCurrentLauncherConfigurationWorkingCopy(
      final String projectName, final String workingDirectory,
      final LauncherConfiguration launcherConfiguration) {
    String mainClass = launcherConfiguration.getMainClass();
    String argumentsString = createArgumentsString(launcherConfiguration);

    List<String> classPathList = new ArrayList<>();
    try {
      classPathList = createClassPathList(workingDirectory, launcherConfiguration.getMainJar());
    } catch (CoreException e) {
      eosgiLog.error("Could not resolv classpath entries.", e);
    }

    String vmArgsList = createVmArgs(launcherConfiguration);

    wc.setAttribute(EOSGILaunchConfigurationDelegate.LAUNCHER_ATTR_ENVIRONMENT_ID,
        launcherConfiguration.getEnvironmentName());
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainClass);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, workingDirectory);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
        argumentsString);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH,
        classPathList);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArgsList);

    return wc;
  }
}
