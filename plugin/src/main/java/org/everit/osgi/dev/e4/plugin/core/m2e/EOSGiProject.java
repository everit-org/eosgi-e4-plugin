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
package org.everit.osgi.dev.e4.plugin.core.m2e;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectChangedListener;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.everit.osgi.dev.e4.plugin.core.ContextChange;
import org.everit.osgi.dev.e4.plugin.core.EOSGiContext;
import org.everit.osgi.dev.e4.plugin.core.launcher.LaunchConfigurationBuilder;
import org.everit.osgi.dev.e4.plugin.core.m2e.model.Environment;
import org.everit.osgi.dev.e4.plugin.core.m2e.xml.ConfiguratorParser;
import org.everit.osgi.dev.e4.plugin.core.m2e.xml.EnvironmentDTO;
import org.everit.osgi.dev.e4.plugin.core.m2e.xml.EnvironmentsDTO;
import org.everit.osgi.dev.e4.plugin.ui.EOSGiLog;
import org.everit.osgi.dev.e4.plugin.ui.EOSGiEclipsePlugin;
import org.everit.osgi.dev.e4.plugin.ui.dto.EnvironmentNodeDTO;
import org.everit.osgi.dev.e4.plugin.ui.dto.EnvironmentsNodeDTO;
import org.everit.osgi.dev.e4.plugin.ui.dto.RootNodeDTO;
import org.everit.osgi.dev.eosgi.dist.schema.util.DistributedEnvironmentConfigurationProvider;
import org.everit.osgi.dev.eosgi.dist.schema.util.LaunchConfigurationDTO;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.EnvironmentType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.UseByType;

/**
 * {@link EOSGiContext} base implementation.
 */
public class EOSGiProject extends Observable implements EOSGiContext, IMavenProjectChangedListener {

  private static final int MINIMAL_EOSGI_MAJOR_VERSION = 4;

  private String buildDirectory;

  private boolean enable = true;

  private Map<String, Environment> environments = new HashMap<>();

  private final EOSGiLog log;

  private final IProject project;

  public EOSGiProject(final IProject project, final EOSGiLog log) {
    this.project = project;
    this.log = log;
  }

  private boolean checkEosgiPluginVersion(final IMavenProjectFacade mavenProjectFacade) {
    boolean changed = false;
    boolean old = enable;
    List<MojoExecution> mojoExecutions = null;
    try {
      mojoExecutions = mavenProjectFacade.getMojoExecutions(
          M2EGoalExecutor.EOSGI_MAVEN_PLUGIN_GROUP_ID,
          M2EGoalExecutor.EOSGI_MAVEN_PLUGIN_ARTIFACT_ID, new NullProgressMonitor(),
          M2EGoalExecutor.MavenGoal.DIST.getGoalName());
    } catch (CoreException e) {
      log.error("check EOSGI plugin version", e);
    }

    if (mojoExecutions == null) {
      return changed;
    }

    // if (mojoExecutions.isEmpty()) {
    // enable = false;
    // return false;
    // }

    if (!mojoExecutions.isEmpty()) {
      MojoExecution mojoExecution = mojoExecutions.get(0);
      String version = mojoExecution.getVersion();
      String[] splittedVersion = version.split("\\.");
      Integer majorVersion = Integer.valueOf(splittedVersion[0]);
      if (MINIMAL_EOSGI_MAJOR_VERSION > majorVersion) {
        enable = false;
        EOSGiEclipsePlugin.getDefault().showWarningDialog(
            Messages.dialogTitleIncompatibleMavenPlugin,
            Messages.dialogMessageIncompatibleMavenPlugin);
      } else {
        enable = true;
      }
      if (enable != old) {
        changed = true;
      }
    }
    return changed;
  }

  private void createLauncherForEnvironment(final String environmentId,
      final LaunchConfigurationDTO launchConfigurationDTO,
      final IProgressMonitor monitor) throws CoreException {
    if (monitor != null) {
      monitor.setTaskName(Messages.monitorCreateServer);
    }

    new LaunchConfigurationBuilder(project.getName(), environmentId, buildDirectory)
        .addEnvironmentConfigurationDTO(launchConfigurationDTO)
        .build();
  }

  @Override
  public void delegateObserver(final Observer observer) {
    addObserver(observer);
  }

  @Override
  public void dispose() {
    deleteObservers();
    environments.clear();
  }

  @Override
  public List<EnvironmentNodeDTO> fetchEnvironments() {
    final List<EnvironmentNodeDTO> environmentList = new ArrayList<>();
    for (Environment environment : environments.values()) {
      environmentList.add(
          new EnvironmentNodeDTO()
              .id(environment.getId())
              .outdated(environment.isOutdated())
              .observable(environment));
    }
    return environmentList;
  }

  @Override
  public void generate(final String environmentId, final IProgressMonitor monitor)
      throws CoreException {
    Objects.requireNonNull(environmentId, "environmentName must be not null!");

    Environment environment = environments.get(environmentId);
    if (environment == null) {
      log.error("Could not found environment with name '" + environmentId + "'");
      return;
    }

    M2EGoalExecutor executor = new M2EGoalExecutor(project, environmentId);
    if (!executor.execute(monitor)) {
      return;
    }

    reSyncTargetFolder();

    environment.setGenerated();

    if (monitor != null) {
      monitor.setTaskName(Messages.monitorLoadDistXML);
    }

    LaunchConfigurationDTO launchConfigurationDTO = loadEnvironmentConfiguration(
        environmentId);

    if (launchConfigurationDTO != null) {
      createLauncherForEnvironment(environmentId, launchConfigurationDTO, monitor);
    }
  }

  @Override
  public boolean isEnable() {
    return enable;
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
    if ((source != null) && (source.getName() != null) && source.getName().startsWith("pom.xml")) {
      if (checkEosgiPluginVersion(mavenProjectFacade)) {
        contextChange.enabledDisabled = true;
      }

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

    EnvironmentsNodeDTO environmentsNodeDTO = new EnvironmentsNodeDTO();
    environmentsNodeDTO.context = this;
    notifyObservers(environmentsNodeDTO);

    if (contextChange.enabledDisabled) {
      RootNodeDTO rootNodeDTO = new RootNodeDTO();
      rootNodeDTO.context(this);
      setChanged();
      notifyObservers(rootNodeDTO);
    }
  }

  @Override
  public void removeObserver(final Observer observer) {
    deleteObserver(observer);
  }

  private void reSyncTargetFolder() throws CoreException {
    // TODO replace target folder to maven build directory
    IFolder targetFolder = project.getFolder("target");
    if (!targetFolder.isSynchronized(IResource.DEPTH_INFINITE)) {
      targetFolder.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
    }
  }

  @Override
  public String toString() {
    return "EOSGiProject [buildDirectory=" + buildDirectory + ", environments=" + environments
        + ", project=" + project + "]";
  }

  private void updateEnvironments(final EnvironmentsDTO environments) {
    Map<String, Environment> newEnvironments = new HashMap<>();
    for (EnvironmentDTO newEnvironment : environments.environments) {
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
    }
    if (!this.environments.isEmpty()) {
      setChanged();
    }
    this.environments = newEnvironments;
  }

}
