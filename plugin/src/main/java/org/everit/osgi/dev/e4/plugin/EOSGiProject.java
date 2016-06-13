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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import org.apache.maven.plugin.MojoExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.MojoExecutionKey;
import org.everit.osgi.dev.dist.util.attach.EOSGiVMManager;
import org.everit.osgi.dev.dist.util.configuration.DistributedEnvironmentConfigurationProvider;
import org.everit.osgi.dev.dist.util.configuration.LaunchConfigurationDTO;
import org.everit.osgi.dev.dist.util.configuration.schema.EnvironmentType;
import org.everit.osgi.dev.dist.util.configuration.schema.UseByType;
import org.everit.osgi.dev.e4.plugin.core.launcher.LaunchConfigurationBuilder;
import org.everit.osgi.dev.e4.plugin.m2e.Messages;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

/**
 * {@link EOSGiProject} base implementation.
 */
public class EOSGiProject {

  private static final List<String> DEFAULT_ENVIRONMENT_LIST = Arrays.asList("equinox");

  public static final String EOSGI_ARTIFACT_ID = "eosgi-maven-plugin";

  public static final String EOSGI_GROUP_ID = "org.everit.osgi.dev";

  public static final String[] EOSGI_SORTED_ACCEPTED_GOAL_ARRAY =
      new String[] { "dist", "integration-test" };

  public static final VersionRange EOSGI_VERSION_RANGE = new VersionRange("[4.0.0,5.0)");

  private String buildDirectory;

  private final EOSGiVMManager eosgiVMManager;

  private Map<String, Map<String, MojoExecution>> executionsByEnvironmentIds;

  private IMavenProjectFacade mavenProjectFacade;

  public EOSGiProject(final IMavenProjectFacade mavenProjectFacade,
      final EOSGiVMManager eosgiVMManager, final IProgressMonitor monitor) {
    this.eosgiVMManager = eosgiVMManager;
    refresh(mavenProjectFacade, monitor);
  }

  private void createLauncherForEnvironment(final String environmentId,
      final LaunchConfigurationDTO launchConfigurationDTO,
      final IProgressMonitor monitor) throws CoreException {
    if (monitor != null) {
      monitor.setTaskName(Messages.monitorCreateServer);
    }

    new LaunchConfigurationBuilder(mavenProjectFacade.getProject().getName(), environmentId,
        buildDirectory).addEnvironmentConfigurationDTO(launchConfigurationDTO).build();
  }

  public void dispose() {
    removeLaunchers();
  }

  public Collection<String> getEnvironmentIds() {
    return executionsByEnvironmentIds.keySet();
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

  private LaunchConfigurationDTO loadEnvironmentConfiguration(final String environmentId) {
    String distXmlFilePath = buildDirectory + File.separator + "eosgi-dist" //$NON-NLS-1$
        + File.separator + environmentId;
    DistributedEnvironmentConfigurationProvider distSchemaProvider =
        new DistributedEnvironmentConfigurationProvider();

    EnvironmentType distributedEnvironment = distSchemaProvider
        .getOverriddenDistributedEnvironmentConfig(new File(distXmlFilePath), UseByType.IDE);

    // EnvironmentConfigurationDTO environmentConfigurationDTO = distSchemaProvider
    // .getEnvironmentConfiguration(new File(distXmlFilePath), UseByType.IDE);
    return distSchemaProvider.getLaunchConfiguration(distributedEnvironment);
  }

  public void packDepsAndExecuteDist(final String environmentId, final IProgressMonitor monitor)
      throws CoreException {
    Objects.requireNonNull(environmentId, "environmentName must be not null!");

    // Environment environment = environments.get(environmentId);
    // if (environment == null) {
    // throw new NullPointerException();
    // }
    //
    // M2EGoalExecutor executor = new M2EGoalExecutor(mavenProjectFacade.getProject(),
    // environmentId);
    // if (!executor.execute(monitor)) {
    // return;
    // }
    //
    // environment.setGenerated();
    //
    // if (monitor != null) {
    // monitor.setTaskName(Messages.monitorLoadDistXML);
    // }
    //
    // LaunchConfigurationDTO launchConfigurationDTO = loadEnvironmentConfiguration(
    // environmentId);
    //
    // if (launchConfigurationDTO != null) {
    // createLauncherForEnvironment(environmentId, launchConfigurationDTO, monitor);
    // }
  }

  public synchronized void refresh(final IMavenProjectFacade mavenProjectFacade,
      final IProgressMonitor monitor) {

    monitor.subTask("Resolving OSGi environments");

    this.mavenProjectFacade = mavenProjectFacade;
    this.executionsByEnvironmentIds = new TreeMap<>();
    Set<MojoExecution> executions = resolveEOSGiExecutions(monitor);
    for (MojoExecution mojoExecution : executions) {
      Collection<String> environmentIds = resolveEnvironmentIds(mojoExecution);
      for (String environmentId : environmentIds) {
        Map<String, MojoExecution> executionMap = executionsByEnvironmentIds.get(environmentId);
        if (executionMap == null) {
          executionMap = new TreeMap<>();
          executionsByEnvironmentIds.put(environmentId, executionMap);
        }
        executionMap.put(mojoExecution.getExecutionId(), mojoExecution);
      }
    }
  }

  private void removeLaunchers() {
    // TODO Auto-generated method stub

  }

  private Collection<String> resolveEnvironmentIds(final MojoExecution mojoExecution) {
    Xpp3Dom configuration = mojoExecution.getConfiguration();
    Xpp3Dom environmentsNode = configuration.getChild("environments");
    if (environmentsNode == null) {
      return DEFAULT_ENVIRONMENT_LIST;
    }
    Set<String> result = new LinkedHashSet<>();
    Xpp3Dom[] environmentsChildNodes = environmentsNode.getChildren();

    if (environmentsChildNodes.length == 0) {
      return DEFAULT_ENVIRONMENT_LIST;
    }

    for (Xpp3Dom environmentNode : environmentsChildNodes) {
      Xpp3Dom environmentIdNode = environmentNode.getChild("id");
      if (environmentIdNode != null) {
        result.add(environmentIdNode.getValue());
      }
    }
    return result;
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

}
