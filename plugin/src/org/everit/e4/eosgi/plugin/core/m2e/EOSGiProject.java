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
package org.everit.e4.eosgi.plugin.core.m2e;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.everit.e4.eosgi.plugin.core.ContextChange;
import org.everit.e4.eosgi.plugin.core.EOSGiContext;
import org.everit.e4.eosgi.plugin.core.EventType;
import org.everit.e4.eosgi.plugin.core.ModelChangeEvent;
import org.everit.e4.eosgi.plugin.core.launcher.LaunchConfigurationBuilder;
import org.everit.e4.eosgi.plugin.core.m2e.model.Environment;
import org.everit.e4.eosgi.plugin.core.m2e.xml.ConfiguratorParser;
import org.everit.e4.eosgi.plugin.core.m2e.xml.EnvironmentsDTO;
import org.everit.e4.eosgi.plugin.core.server.EOSGiRuntime;
import org.everit.e4.eosgi.plugin.core.server.EOSGiServer;
import org.everit.e4.eosgi.plugin.ui.EOSGiLog;
import org.everit.e4.eosgi.plugin.ui.dto.EnvironmentNodeDTO;
import org.everit.osgi.dev.eosgi.dist.schema.util.DistSchemaProvider;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.DistributionPackageType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.EnvironmentConfigurationType;

/**
 * {@link EOSGiContext} base implementation.
 */
public class EOSGiProject extends Observable implements EOSGiContext {

  private String buildDirectory;

  private Map<String, Environment> environments = new HashMap<>();

  private final EOSGiLog log;

  private final IProject project;

  public EOSGiProject(final IProject project, final EOSGiLog log) {
    this.project = project;
    this.log = log;
  }

  private void createServerForEnvironment(final String environmentId,
      final EnvironmentConfigurationType environmentConfigurationType,
      final IProgressMonitor monitor) throws CoreException {
    if (monitor != null) {
      monitor.setTaskName("Creating Server...");
    }

    IRuntime runtime = EOSGiRuntime.createRuntime(environmentId, monitor);
    String serverId = generateServerId(environmentId);
    IServer server = EOSGiServer.findServerToEnvironment(serverId, runtime,
        monitor);

    ILaunchConfigurationWorkingCopy workingCopy = null;
    ILaunchConfiguration serverLaunchConfiguration = server.getLaunchConfiguration(true,
        monitor);
    workingCopy = serverLaunchConfiguration.getWorkingCopy();

    new LaunchConfigurationBuilder(project.getName(), environmentId, buildDirectory)
        .addLauncherConfigurationWorkingCopy(workingCopy)
        .addEnvironmentConfigurationType(environmentConfigurationType)
        .build();
  }

  @Override
  public void delegateObserver(final Observer observer) {
    addObserver(observer);
  }

  private void deleteServer(final String serverId) {
    try {
      EOSGiServer.deleteServer(serverId);
    } catch (CoreException e) {
      log.error("Could not delete server (" + serverId + ")", e);
    }
  }

  @Override
  public void dispose() {
    deleteObservers();
    environments.clear();
  }

  @Override
  public List<EnvironmentNodeDTO> fetchEnvironments() {
    final List<EnvironmentNodeDTO> environmentList = new ArrayList<>();
    environments.values().forEach((environment) -> {
      environmentList.add(
          new EnvironmentNodeDTO()
              .id(environment.getId())
              .outdated(environment.isOutdated())
              .observable(environment));
    });
    return environmentList;
  }

  @Override
  public void generate(final String environmentId, final IProgressMonitor monitor)
      throws CoreException {
    Objects.requireNonNull(environmentId, "environmentName must be not null!");

    String serverId = generateServerId(environmentId);
    deleteServer(serverId);

    Environment environment = environments.get(environmentId);
    if (environment == null) {
      log.error("Could not found environment with name '" + environmentId + "'");
      return;
    }

    M2EGoalExecutor executor = new M2EGoalExecutor(project, environmentId);
    if (!executor.execute(monitor)) {
      return;
    }

    environment.setGenerated();

    if (monitor != null) {
      monitor.setTaskName("Check and load dist.xml.");
    }

    EnvironmentConfigurationType environmentConfigurationType = loadEnvironmentConfiguration(
        environmentId);

    if (environmentConfigurationType != null) {
      createServerForEnvironment(environmentId, environmentConfigurationType, monitor);
    }
  }

  private String generateServerId(final String environmentId) {
    return environmentId + "/" + project.getName();
  }

  private EnvironmentConfigurationType loadEnvironmentConfiguration(final String environmentId) {
    DistSchemaProvider distSchemaProvider = new DistSchemaProvider();
    /// target/eosgi-dist/equinoxtest
    DistributionPackageType distributionPackageType = distSchemaProvider
        .readDistConfig(new File(buildDirectory + File.separator + ".."));
    return distributionPackageType.getEnvironmentConfiguration();
  }

  @Override
  public void mavenProjectChanged(final MavenProjectChangedEvent[] changedEvents,
      final IProgressMonitor monitor) {
    for (MavenProjectChangedEvent mavenProjectChangedEvent : changedEvents) {
      processEvents(mavenProjectChangedEvent);
    }
  }

  private void processEvents(final MavenProjectChangedEvent mavenProjectChangedEvent) {
    IMavenProjectFacade mavenProjectFacade = mavenProjectChangedEvent.getMavenProject();
    // IMavenProjectFacade oldMavenProjectFacade = mavenProjectChangedEvent.getOldMavenProject();

    IProject project = null;
    MavenProject mavenProject = null;
    // boolean projectRemoved = false;
    if (mavenProjectFacade != null) {
      project = mavenProjectFacade.getProject();
      mavenProject = mavenProjectFacade.getMavenProject();
    }

    ContextChange contextChange = new ContextChange();
    if (mavenProject != null) {
      String directory = mavenProject.getBuild().getDirectory();
      contextChange.buildDirectory(directory);
    }

    IFile source = mavenProjectChangedEvent.getSource();
    if (source != null && source.getName() != null && source.getName().startsWith("pom.xml")) {
      Xpp3Dom goalConfiguration = mavenProject.getGoalConfiguration(
          M2EGoalExecutor.EOSGI_MAVEN_PLUGIN_GROUP_ID,
          M2EGoalExecutor.EOSGI_MAVEN_PLUGIN_ARTIFACT_ID, null,
          M2EGoalExecutor.MavenGoal.DIST.getGoalName());
      contextChange.configuration(new ConfiguratorParser().parse(goalConfiguration));
    }

    if ((project != null) && project.equals(this.project)) {
      refresh(contextChange);
    }
  }

  @Override
  public void refresh(final ContextChange contextChange) {
    Objects.requireNonNull(contextChange, "contextChange must be not null!");

    if (buildDirectory == null) {
      buildDirectory = contextChange.buildDirectory;
      setChanged();
    } else if ((contextChange.buildDirectory != null)
        && !contextChange.buildDirectory.equals(buildDirectory)) {
      buildDirectory = contextChange.buildDirectory;
      setChanged();
    }
    if (contextChange.configuration != null) {
      synchronized (environments) {
        updateEnvironments(contextChange.configuration);
      }
    }

    // TODO replace ModelChangeEvent to a DTO only
    notifyObservers(new ModelChangeEvent().eventType(EventType.ENVIRONMENTS).arg(this));
  }

  @Override
  public void removeObserver(final Observer observer) {
    deleteObserver(observer);
  }

  @Override
  public String toString() {
    return "EOSGiProject [buildDirectory=" + buildDirectory + ", environments=" + environments
        + ", project=" + project + "]";
  }

  private void updateEnvironments(final EnvironmentsDTO environments) {
    Map<String, Environment> newEnvironments = new HashMap<>();
    environments.environments.forEach((newEnvironment) -> {
      Environment environment = null;
      if (this.environments.containsKey(newEnvironment.id)) {
        environment = this.environments.remove(newEnvironment.id);
        environment.update(newEnvironment);
        // TODO update laucher and server (if the dist exists)
      } else {
        environment = new Environment();
        environment.setId(newEnvironment.id);
        environment.setFramework(newEnvironment.framework);
        setChanged();
      }
      newEnvironments.put(newEnvironment.id, environment);
    });
    this.environments.forEach((key, value) -> {
      deleteServer(key);
    });
    if (!this.environments.isEmpty()) {
      setChanged();
    }
    this.environments = newEnvironments;
  }

}
