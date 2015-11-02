package org.everit.e4.eosgi.plugin.core.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import org.everit.e4.eosgi.plugin.core.server.EOSGILaunchConfigurationDelegate;
import org.everit.e4.eosgi.plugin.ui.EOSGiLog;
import org.everit.e4.eosgi.plugin.ui.EOSGiPluginActivator;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.EnvironmentConfigurationType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.VmOptionsType;

/**
 * Builder class for launch configurations.
 */
public class LaunchConfigurationBuilder {

  private String buildDirectory;

  private EnvironmentConfigurationType environmentConfigurationType;

  private String environmentId;

  private EOSGiLog eosgiLog;

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

  public LaunchConfigurationBuilder addEnvironmentConfigurationType(
      final EnvironmentConfigurationType launcherConfiguration) {
    this.environmentConfigurationType = launcherConfiguration;
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

    if (environmentConfigurationType == null) {
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
    wc = updateCurrentLauncherConfigurationWorkingCopy(projectName, workingDirectory,
        environmentConfigurationType);

    try {
      return wc.doSave();
    } catch (CoreException e) {
      eosgiLog.error("Could not save new launch configuration.", e);
      return null;
    }
  }

  // private String createArgumentsString(
  // final EnvironmentConfigurationType environmentConfigurationType)
  // {
  // final StringBuilder stringBuilder = new StringBuilder();
  //
  // CommandArguments commandArguments = environmentConfigurationType.getCommandArguments();
  // if ((commandArguments != null) && (commandArguments.getCommandArgument() != null)) {
  // commandArguments.getCommandArgument().forEach((argument) -> {
  // stringBuilder.append(" " + argument);
  // });
  // }
  //
  // String argumentsString = stringBuilder.toString();
  // return argumentsString;
  // }

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

  private String createVmArgs(final EnvironmentConfigurationType environmentConfigurationType) {
    StringBuilder stringBuilder = new StringBuilder();

    if (environmentConfigurationType == null) {
      return stringBuilder.toString();
    }

    VmOptionsType vmOptions = environmentConfigurationType.getVmOptions();
    if (vmOptions == null || vmOptions.getVmOption() == null) {
      return stringBuilder.toString();
    }

    vmOptions.getVmOption().forEach((vmOption) -> {
      stringBuilder.append(" " + vmOption);
    });
    return stringBuilder.toString();
  }

  private ILaunchConfigurationWorkingCopy updateCurrentLauncherConfigurationWorkingCopy(
      final String projectName, final String workingDirectory,
      final EnvironmentConfigurationType environmentConfigurationType) {
    String mainClass = environmentConfigurationType.getMainClass();
    String argumentsString = "";// createArgumentsString(environmentConfigurationType);

    List<String> classPathList = new ArrayList<>();
    try {
      classPathList = createClassPathList(workingDirectory,
          environmentConfigurationType.getMainJar());
    } catch (CoreException e) {
      eosgiLog.error("Could not resolv classpath entries.", e);
    }

    String vmArgsList = createVmArgs(environmentConfigurationType);

    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainClass);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, workingDirectory);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
        argumentsString);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH,
        classPathList);
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArgsList);

    wc.setAttribute(EOSGILaunchConfigurationDelegate.LAUNCHER_ATTR_ENVIRONMENT_ID, environmentId);

    return wc;
  }

}
