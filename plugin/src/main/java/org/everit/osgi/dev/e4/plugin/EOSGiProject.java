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

import org.apache.maven.plugin.MojoExecution;
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
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

/**
 * {@link EOSGiProject} base implementation.
 */
public class EOSGiProject {

  private static final DAGFlattener<DependencyNode> DEPENDENCY_TREE_FLATTENER =
      new DAGFlattener<>(new DependencyNodeComparator(), new DependencyNodeChildResolver());

  public static final String EOSGI_ARTIFACT_ID = "eosgi-maven-plugin";

  public static final String EOSGI_GROUP_ID = "org.everit.osgi.dev";

  public static final String[] EOSGI_SORTED_ACCEPTED_GOAL_ARRAY =
      new String[] { "dist", "integration-test" };

  public static final VersionRange EOSGI_VERSION_RANGE = new VersionRange("[4.0.0,5.0)");

  private final EOSGiVMManager eosgiVMManager;

  private ExecutableEnvironmentContainer executableEnvironmentContainer;

  private IMavenProjectFacade mavenProjectFacade;

  public EOSGiProject(final IMavenProjectFacade mavenProjectFacade,
      final EOSGiVMManager eosgiVMManager, final IProgressMonitor monitor) {
    this.eosgiVMManager = eosgiVMManager;
    refresh(mavenProjectFacade, monitor);
  }

  public void dispose() {
    // TODO stop launched vms
  }

  public void dist(final ExecutableEnvironment executableEnvironment,
      final IProgressMonitor monitor) {
    try {
      packModifiedDeps(executableEnvironment.getEnvironmentId(),
          executableEnvironment.getMojoExecution().getExecutionId(), monitor);

      EOSGiEclipsePlugin.getDefault().getProjectPackageUtil().packageProject(mavenProjectFacade,
          monitor);

      MavenExecutionContextModifiers modifiers = new MavenExecutionContextModifiers();
      modifiers.systemPropertiesReplacer = (originalProperties) -> {
        Properties systemProperties = new Properties();
        systemProperties.putAll(originalProperties);
        systemProperties.put(DistConstants.PLUGIN_PROPERTY_DIST_ONLY, Boolean.TRUE.toString());
        return systemProperties;
      };

      modifiers.executionRequestDataModifier =
          (data) -> data.put(DistConstants.MAVEN_EXECUTION_REQUEST_DATA_KEY_ATTACH_API_CLASSLOADER,
              EOSGiVMManager.class.getClassLoader());

      ProjectPackager packageUtil = EOSGiEclipsePlugin.getDefault().getProjectPackageUtil();
      modifiers.workspaceReaderReplacer = (original) -> packageUtil.createWorkspaceReader(original);

      M2EUtil.executeInContext(mavenProjectFacade, modifiers, (context, monitor1) -> {
        SubMonitor.convert(monitor1, "Calling \"mvn eosgi:dist\" on project", 0);

        MavenPlugin.getMaven().execute(mavenProjectFacade.getMavenProject(),
            executableEnvironment.getMojoExecution(), monitor1);

        mavenProjectFacade.getProject().refreshLocal(IProject.DEPTH_INFINITE, monitor1);
        return null;
      }, monitor);
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }

  public List<ExecutableEnvironment> getDefaultExecutableEnvironmentList(
      final MojoExecution mojoExecution) {
    List<ExecutableEnvironment> defaultExecutableEnvironments = new ArrayList<>();
    defaultExecutableEnvironments.add(new ExecutableEnvironment("equinox", mojoExecution, this,
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

    if (EOSGI_GROUP_ID.equals(mojoExecutionKey.getGroupId())
        && EOSGI_ARTIFACT_ID.equals(mojoExecutionKey.getArtifactId())
        && Arrays.binarySearch(EOSGI_SORTED_ACCEPTED_GOAL_ARRAY, mojoExecutionKey.getGoal()) >= 0) {

      String versionString = mojoExecutionKey.getVersion().replace('-', '.');
      Version version = new Version(versionString);
      return EOSGI_VERSION_RANGE.includes(version);
    }
    return false;
  }

  public void launch(final ExecutableEnvironment executableEnvironment, final String mode,
      final IProgressMonitor monitor) {

    dist(executableEnvironment, monitor);

    String launchUniqueId = UUID.randomUUID().toString();

    File distFolder =
        new File(resolveDistFolder(executableEnvironment.getMojoExecution(), monitor));

    ILaunchConfiguration launchConfiguration = new LaunchConfigurationBuilder().build(
        mavenProjectFacade.getProject(), executableEnvironment.getEnvironmentId(),
        distFolder, launchUniqueId);

    try {
      ILaunch launch = launchConfiguration.launch(mode, monitor);
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
      final IProgressMonitor monitor)
      throws CoreException {
    Objects.requireNonNull(environmentId, "environmentName must be not null!");

    DependencyNode rootNode =
        MavenPlugin.getMavenModelManager().readDependencyTree(null,
            mavenProjectFacade.getMavenProject(monitor), null, monitor);

    List<DependencyNode> flattenedDependencyTree = DEPENDENCY_TREE_FLATTENER.flatten(rootNode);

    packageDependenciesWhereNecessary(flattenedDependencyTree, monitor);
  }

  public synchronized void refresh(final IMavenProjectFacade mavenProjectFacade,
      final IProgressMonitor monitor) {

    monitor.subTask("Resolving OSGi environments");

    this.mavenProjectFacade = mavenProjectFacade;
    Set<ExecutableEnvironment> executableEnvironments = new TreeSet<>();
    Set<MojoExecution> executions = resolveEOSGiExecutions(monitor);
    for (MojoExecution mojoExecution : executions) {
      executableEnvironments.addAll(resolveExecutableEnvironments(mojoExecution));
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
    for (MojoExecutionKey mojoExecutionKey : executionKeys) {
      if (isEOSGiExecution(mojoExecutionKey)) {
        try {
          MojoExecution mojoExecution =
              mavenProjectFacade.getMojoExecution(mojoExecutionKey, monitor);

          eosgiExecutions.add(mojoExecution);
        } catch (CoreException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return eosgiExecutions;
  }

  private Collection<ExecutableEnvironment> resolveExecutableEnvironments(
      final MojoExecution mojoExecution) {
    Xpp3Dom configuration = mojoExecution.getConfiguration();
    Xpp3Dom environmentsNode = configuration.getChild("environments");
    if (environmentsNode == null) {
      return getDefaultExecutableEnvironmentList(mojoExecution);
    }
    Set<ExecutableEnvironment> result = new LinkedHashSet<>();
    Xpp3Dom[] environmentsChildNodes = environmentsNode.getChildren();

    if (environmentsChildNodes.length == 0) {
      return getDefaultExecutableEnvironmentList(mojoExecution);
    }

    for (Xpp3Dom environmentNode : environmentsChildNodes) {
      Xpp3Dom environmentIdNode = environmentNode.getChild("id");
      if (environmentIdNode != null) {
        result.add(new ExecutableEnvironment(environmentIdNode.getValue(), mojoExecution, this,
            resolveShutdownTimeout(environmentNode)));
      }

    }
    return result;
  }

  private long resolveShutdownTimeout(final Xpp3Dom environmentNode) {
    Xpp3Dom shutdownTimeoutNode = environmentNode.getChild("shutdownTimeout");
    if (shutdownTimeoutNode == null) {
      return DistConstants.DEFAULT_SHUTDOWN_TIMEOUT;
    }
    return Long.parseLong(shutdownTimeoutNode.getValue());
  }

}
