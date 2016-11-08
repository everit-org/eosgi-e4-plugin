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
package org.everit.osgi.dev.e4.plugin;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.maven.lifecycle.MavenExecutionPlan;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.embedder.IMavenExecutionContext;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.m2e.core.project.configurator.MojoExecutionKey;
import org.everit.osgi.dev.dist.util.DistConstants;
import org.everit.osgi.dev.dist.util.attach.EOSGiVMManager;
import org.everit.osgi.dev.e4.plugin.core.launcher.LaunchConfigurationBuilder;
import org.everit.osgi.dev.e4.plugin.m2e.M2EUtil;
import org.everit.osgi.dev.e4.plugin.m2e.MavenExecutionContextModifiers;
import org.everit.osgi.dev.e4.plugin.m2e.packaging.ProjectPackager;
import org.everit.osgi.dev.e4.plugin.util.DAGFlattener;
import org.everit.osgi.dev.e4.plugin.util.DependencyNodeChildResolver;
import org.everit.osgi.dev.e4.plugin.util.DependencyNodeComparator;
import org.osgi.framework.Bundle;

/**
 * {@link EOSGiProject} base implementation.
 */
public class EOSGiProject {

  private static final DAGFlattener<DependencyNode> DEPENDENCY_TREE_FLATTENER =
      new DAGFlattener<>(new DependencyNodeComparator(), new DependencyNodeChildResolver());

  public static final String[] EOSGI_SORTED_ACCEPTED_GOAL_ARRAY =
      new String[] { "dist", "integration-test" };

  private final EOSGiVMManager eosgiVMManager;

  private ExecutableEnvironmentContainer executableEnvironmentContainer;

  private IMavenProjectFacade mavenProjectFacade;

  public EOSGiProject(final IMavenProjectFacade mavenProjectFacade,
      final EOSGiVMManager eosgiVMManager, final IProgressMonitor monitor) {
    this.eosgiVMManager = eosgiVMManager;
    refresh(mavenProjectFacade, monitor);
  }

  private void checkExecutionResultExceptions(final IMavenExecutionContext context) {
    List<Throwable> exceptions = context.getSession().getResult().getExceptions();
    if (!exceptions.isEmpty()) {
      Throwable throwable = exceptions.get(0);
      if (exceptions instanceof RuntimeException) {
        throw (RuntimeException) throwable;
      } else if (exceptions instanceof Error) {
        throw (Error) throwable;
      } else {
        throw new RuntimeException(throwable);
      }
    }
  }

  public void clean(final ExecutableEnvironment executableEnvironment,
      final IProgressMonitor monitor) {

    try {
      FileUtils.deleteDirectory(executableEnvironment.getRootFolder());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public void dispose() {
    // TODO stop launched vms
  }

  public void dist(final ExecutableEnvironment executableEnvironment,
      final IProgressMonitor monitor) {
    try {
      packModifiedDeps(executableEnvironment.getEnvironmentId(),
          executableEnvironment.getExecutionId(), monitor);

      EOSGiEclipsePlugin eosgiEclipsePlugin = EOSGiEclipsePlugin.getDefault();
      eosgiEclipsePlugin.getProjectPackageUtil().packageProject(mavenProjectFacade,
          monitor);

      MavenExecutionContextModifiers modifiers = new MavenExecutionContextModifiers();
      modifiers.systemPropertiesReplacer = (originalProperties) -> {
        Properties systemProperties = new Properties();
        systemProperties.putAll(originalProperties);
        systemProperties.put(DistConstants.PLUGIN_PROPERTY_ENVIRONMENT_ID,
            executableEnvironment.getEnvironmentId());
        return systemProperties;
      };

      modifiers.executionRequestDataModifier =
          (data) -> data.put(DistConstants.MAVEN_EXECUTION_REQUEST_DATA_KEY_ATTACH_API_CLASSLOADER,
              EOSGiVMManager.class.getClassLoader());

      Bundle bundle = eosgiEclipsePlugin.getBundle();

      modifiers.systemPropertiesReplacer = (properties) -> {
        Properties newProps = new Properties(properties);
        newProps.setProperty("eosgi.analytics.referer",
            bundle.getSymbolicName() + "_" + bundle.getVersion());
        return newProps;
      };

      ProjectPackager packageUtil = eosgiEclipsePlugin.getProjectPackageUtil();
      modifiers.workspaceReaderReplacer = (original) -> packageUtil.createWorkspaceReader(original);

      M2EUtil.executeInContext(mavenProjectFacade, modifiers, (context, monitor1) -> {
        SubMonitor.convert(monitor1, "Calling \"mvn eosgi:dist\" on project", 0);

        String executionId = executableEnvironment.getExecutionId();
        String goal = "eosgi:dist" + "@" + executionId;

        MavenProject mavenProject = mavenProjectFacade.getMavenProject(monitor1);
        packageUtil.setArtifactsOnMavenProject(mavenProject, mavenProjectFacade.getProject());

        MavenExecutionPlan executionPlan =
            MavenPlugin.getMaven().calculateExecutionPlan(mavenProject, Arrays.asList(goal), true,
                monitor);

        executeExecutionPlan(mavenProject, executionPlan, monitor1);

        checkExecutionResultExceptions(context);

        return null;
      }, monitor);
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }

  private void executeExecutionPlan(final MavenProject mavenProject,
      final MavenExecutionPlan executionPlan,
      final IProgressMonitor monitor) {

    List<MojoExecution> mojoExecutions = executionPlan.getMojoExecutions();

    IMaven maven = MavenPlugin.getMaven();
    for (MojoExecution mojoExecution : mojoExecutions) {
      try {
        maven.execute(mavenProject, mojoExecution, monitor);
        mavenProjectFacade.getProject().refreshLocal(IProject.DEPTH_INFINITE, monitor);
      } catch (CoreException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public List<ExecutableEnvironment> getDefaultExecutableEnvironmentList(
      final MojoExecution mojoExecution, final boolean defaultExecution,
      final IProgressMonitor monitor) {
    List<ExecutableEnvironment> defaultExecutableEnvironments = new ArrayList<>();
    String distFolder = resolveDistFolder(mojoExecution, monitor);
    File environmentRootFolder = new File(distFolder, DistConstants.DEFAULT_ENVIRONMENT_ID);

    defaultExecutableEnvironments
        .add(new ExecutableEnvironment(DistConstants.DEFAULT_ENVIRONMENT_ID,
            mojoExecution.getExecutionId(), defaultExecution, this, environmentRootFolder,
            DistConstants.DEFAULT_SHUTDOWN_TIMEOUT));
    return defaultExecutableEnvironments;

  }

  public ExecutableEnvironmentContainer getExecutableEnvironmentContainer() {
    return executableEnvironmentContainer;
  }

  public IMavenProjectFacade getMavenProjectFacade() {
    return mavenProjectFacade;
  }

  private boolean isEOSGiExecution(final MojoExecutionKey mojoExecutionKey) {

    if (M2EUtil.EOSGI_GROUP_ID.equals(mojoExecutionKey.getGroupId())
        && M2EUtil.EOSGI_ARTIFACT_ID.equals(mojoExecutionKey.getArtifactId())
        && Arrays.binarySearch(EOSGI_SORTED_ACCEPTED_GOAL_ARRAY, mojoExecutionKey.getGoal()) >= 0) {

      String mojoVersion = mojoExecutionKey.getVersion();
      return M2EUtil.isEOSGiMojoVersionSupported(mojoVersion);
    }
    return false;
  }

  public void launch(final ExecutableEnvironment executableEnvironment, final String mode,
      final IProgressMonitor monitor) {

    SubMonitor subMonitor = SubMonitor.convert(monitor, "Analyzing dependency tree", 0);

    dist(executableEnvironment, subMonitor);

    String launchUniqueId = UUID.randomUUID().toString();

    ILaunchConfiguration launchConfiguration = new LaunchConfigurationBuilder().build(
        mavenProjectFacade.getProject(), executableEnvironment, launchUniqueId);

    try {
      ILaunch launch = launchConfiguration.launch(mode, subMonitor);
      IProcess[] processes = launch.getProcesses();
      if (processes.length == 0) {
        return;
      }

      launch.removeProcess(processes[0]);
      launch.addProcess(
          new GracefulShutdownProcessWrapper(processes[0], eosgiVMManager, launchUniqueId,
              executableEnvironment.getShutdownTimeout()));
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }

  private void packageDependenciesWhereNecessary(
      final List<DependencyNode> flattenedDependencyTree, final IProgressMonitor monitor) {

    ListIterator<DependencyNode> listIterator =
        flattenedDependencyTree.listIterator(flattenedDependencyTree.size());

    IMavenProjectRegistry mavenProjectRegistry = MavenPlugin.getMavenProjectRegistry();
    ProjectPackager projectPackageUtil = EOSGiEclipsePlugin.getDefault().getProjectPackageUtil();

    while (listIterator.hasPrevious()) {
      DependencyNode dependencyNode = listIterator.previous();
      Artifact artifact = dependencyNode.getArtifact();

      if (artifact != null) {
        IMavenProjectFacade dependencyMavenProject =
            mavenProjectRegistry.getMavenProject(artifact.getGroupId(), artifact.getArtifactId(),
                artifact.getVersion());

        if (dependencyMavenProject != null) {
          SubMonitor subMonitor = SubMonitor.convert(monitor);
          subMonitor.beginTask("Packaging dependency: " + artifact.getGroupId() + ":"
              + artifact.getArtifactId() + ":" + artifact.getVersion(), 1);
          try {
            projectPackageUtil.packageProject(dependencyMavenProject, subMonitor);
          } catch (CoreException e) {
            throw new RuntimeException(e);
          } finally {
            subMonitor.done();
          }
        }
      }
    }
  }

  public void packModifiedDeps(final String environmentId, final String executionId,
      final IProgressMonitor monitor) throws CoreException {
    Objects.requireNonNull(environmentId, "environmentName must be not null!");

    DependencyNode rootNode =
        MavenPlugin.getMavenModelManager().readDependencyTree(mavenProjectFacade,
            mavenProjectFacade.getMavenProject(monitor), null, monitor);

    List<DependencyNode> flattenedDependencyTree = DEPENDENCY_TREE_FLATTENER.flatten(rootNode);

    packageDependenciesWhereNecessary(flattenedDependencyTree, monitor);
  }

  public synchronized void refresh(final IMavenProjectFacade mavenProjectFacade,
      final IProgressMonitor monitor) {

    monitor.subTask("Resolving OSGi environments");

    this.mavenProjectFacade = mavenProjectFacade;
    Set<ExecutableEnvironment> executableEnvironments = new TreeSet<>();

    MojoExecution defaultMojoExecution = resolvePlainPluginConfigMojoExecution(monitor);
    Xpp3Dom defaultMojoExecutionConfiguration = null;
    if (defaultMojoExecution != null) {
      defaultMojoExecutionConfiguration = defaultMojoExecution.getConfiguration();
      executableEnvironments
          .addAll(resolveExecutableEnvironments(defaultMojoExecution, true, monitor));
    }

    Set<MojoExecution> executions = resolveEOSGiExecutions(monitor);
    for (MojoExecution mojoExecution : executions) {
      if (defaultMojoExecutionConfiguration == null
          || !defaultMojoExecutionConfiguration.equals(mojoExecution.getConfiguration())) {
        executableEnvironments.addAll(resolveExecutableEnvironments(mojoExecution, false, monitor));
      }
    }

    this.executableEnvironmentContainer =
        new ExecutableEnvironmentContainer(executableEnvironments);
  }

  private String resolveDistFolder(final MojoExecution mojoExecution,
      final IProgressMonitor monitor) {
    try {
      return M2EUtil.getParameterValue(mavenProjectFacade.getMavenProject(monitor), "distFolder",
          String.class, mojoExecution, monitor);
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }

  private Set<MojoExecution> resolveEOSGiExecutions(final IProgressMonitor monitor) {
    Map<MojoExecutionKey, List<IPluginExecutionMetadata>> mojoExecutionMapping =
        mavenProjectFacade.getMojoExecutionMapping();

    Set<MojoExecutionKey> executionKeys = mojoExecutionMapping.keySet();

    Set<MojoExecution> eosgiExecutions = new LinkedHashSet<>();
    IMaven maven = MavenPlugin.getMaven();

    MavenProject mavenProject;
    try {
      mavenProject = mavenProjectFacade.getMavenProject(monitor);
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }

    for (MojoExecutionKey mojoExecutionKey : executionKeys) {
      if (isEOSGiExecution(mojoExecutionKey)) {
        try {
          MavenExecutionPlan executionPlan = maven.calculateExecutionPlan(mavenProject,
              Arrays.asList("eosgi:dist@" + mojoExecutionKey.getExecutionId()), true, monitor);

          MojoExecution mojoExecution = executionPlan.getMojoExecutions().get(0);
          eosgiExecutions.add(mojoExecution);
        } catch (CoreException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return eosgiExecutions;
  }

  private Collection<ExecutableEnvironment> resolveExecutableEnvironments(
      final MojoExecution mojoExecution, final boolean defaultExecution,
      final IProgressMonitor monitor) {
    Xpp3Dom configuration = mojoExecution.getConfiguration();
    Xpp3Dom environmentsNode = configuration.getChild("environments");
    if (environmentsNode == null) {
      return getDefaultExecutableEnvironmentList(mojoExecution, defaultExecution, monitor);
    }
    Set<ExecutableEnvironment> result = new LinkedHashSet<>();
    Xpp3Dom[] environmentsChildNodes = environmentsNode.getChildren();

    if (environmentsChildNodes.length == 0) {
      return getDefaultExecutableEnvironmentList(mojoExecution, defaultExecution, monitor);
    }
    String distFolder = resolveDistFolder(mojoExecution, monitor);
    File distFolderFile = new File(distFolder);

    for (Xpp3Dom environmentNode : environmentsChildNodes) {
      Xpp3Dom environmentIdNode = environmentNode.getChild("id");
      if (environmentIdNode != null) {
        String environmentId = environmentIdNode.getValue();
        File environmentRootFolder = new File(distFolderFile, environmentId);
        result.add(
            new ExecutableEnvironment(environmentId, mojoExecution.getExecutionId(),
                defaultExecution, this, environmentRootFolder,
                resolveShutdownTimeout(environmentNode)));
      }

    }
    return result;
  }

  private MojoExecution resolvePlainPluginConfigMojoExecution(final IProgressMonitor monitor) {
    try {
      MavenProject mavenProject = this.mavenProjectFacade.getMavenProject(monitor);
      if (!M2EUtil.hasEOSGiMavenPlugin(mavenProject)) {
        return null;
      }

      IMaven maven = MavenPlugin.getMaven();
      MavenExecutionPlan executionPlan =
          maven.calculateExecutionPlan(mavenProject, Arrays.asList(new String[] { "eosgi:dist" }),
              true, monitor);

      List<MojoExecution> mojoExecutions = executionPlan.getMojoExecutions();
      if (mojoExecutions.isEmpty()) {
        return null;
      }
      return mojoExecutions.iterator().next();
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }

  private long resolveShutdownTimeout(final Xpp3Dom environmentNode) {
    Xpp3Dom shutdownTimeoutNode = environmentNode.getChild("shutdownTimeout");
    if (shutdownTimeoutNode == null) {
      return DistConstants.DEFAULT_SHUTDOWN_TIMEOUT;
    }
    return Long.parseLong(shutdownTimeoutNode.getValue());
  }

  public void syncBack(final ExecutableEnvironment executableEnvironment,
      final IProgressMonitor monitor) {

    MavenExecutionContextModifiers modifiers = new MavenExecutionContextModifiers();
    modifiers.systemPropertiesReplacer = (originalProperties) -> {
      Properties systemProperties = new Properties();
      systemProperties.putAll(originalProperties);
      systemProperties.put(DistConstants.PLUGIN_PROPERTY_ENVIRONMENT_ID,
          executableEnvironment.getEnvironmentId());
      return systemProperties;
    };

    modifiers.executionRequestDataModifier =
        (data) -> data.put(DistConstants.MAVEN_EXECUTION_REQUEST_DATA_KEY_ATTACH_API_CLASSLOADER,
            EOSGiVMManager.class.getClassLoader());

    ProjectPackager packageUtil = EOSGiEclipsePlugin.getDefault().getProjectPackageUtil();
    modifiers.workspaceReaderReplacer = (original) -> packageUtil.createWorkspaceReader(original);

    try {
      M2EUtil.executeInContext(mavenProjectFacade, modifiers, (context, monitor1) -> {
        SubMonitor.convert(monitor1, "Calling \"mvn eosgi:sync-back\" on project "
            + mavenProjectFacade.getProject().getName(), 0);

        String executionId = executableEnvironment.getExecutionId();
        String goal = "eosgi:sync-back" + "@" + executionId;

        MavenProject mavenProject = mavenProjectFacade.getMavenProject(monitor1);

        MavenExecutionPlan executionPlan =
            MavenPlugin.getMaven().calculateExecutionPlan(mavenProject, Arrays.asList(goal), true,
                monitor);

        executeExecutionPlan(mavenProject, executionPlan, monitor1);

        // TODO check execution result exceptions in execution plan and call refresh always.

        mavenProjectFacade.getProject().refreshLocal(IProject.DEPTH_INFINITE, monitor1);

        checkExecutionResultExceptions(context);

        return null;
      }, monitor);
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }

}
