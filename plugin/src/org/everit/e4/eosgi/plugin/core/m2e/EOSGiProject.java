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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.everit.e4.eosgi.plugin.core.ContextChange;
import org.everit.e4.eosgi.plugin.core.EOSGiContext;
import org.everit.e4.eosgi.plugin.core.EventType;
import org.everit.e4.eosgi.plugin.core.ModelChangeEvent;
import org.everit.e4.eosgi.plugin.core.m2e.model.Environment;
import org.everit.e4.eosgi.plugin.core.m2e.xml.EnvironmentsDTO;
import org.everit.e4.eosgi.plugin.core.server.ServerFactory;
import org.everit.e4.eosgi.plugin.ui.EOSGiLog;
import org.everit.e4.eosgi.plugin.ui.dto.EnvironmentNodeDTO;

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
  public void generate(final String environmentId, final IProgressMonitor monitor) {
    Objects.requireNonNull(environmentId, "environmentName must be not null!");

    ServerFactory serverFactory = new ServerFactory(project.getName(), buildDirectory,
        environmentId);
    serverFactory.deleteServer();

    Environment environment = environments.get(environmentId);
    if (environment == null) {
      log.error("Could not found environment with name '" + environmentId + "'");
      return;
    }

    boolean generated = false;
    try {
      generated = new M2EGoalExecutor(project, environmentId).execute(monitor);
    } catch (CoreException e) {
      log.error(
          MessageFormat.format("Couldn't generate dist for ''{0}'' environment.", environmentId),
          e);
    }

    if (generated) {
      environment.setGenerated(true);
      serverFactory.createServer(monitor);
    }
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

    if ((project != null) && project.equals(this.project)) {
      String directory = mavenProject.getBuild().getDirectory();
      refresh(new ContextChange().buildDirectory(directory));
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
        // TODO create launcher and server
        // new ServerFactory(project.getName(), buildDirectory, newEnvironment.id)
        // .createServer(new NullProgressMonitor());
        setChanged();
      }
      newEnvironments.put(newEnvironment.id, environment);
    });
    this.environments.forEach((key, value) -> {
      new ServerFactory(project.getName(), buildDirectory, key).deleteServer();
    });
    if (!this.environments.isEmpty()) {
      setChanged();
    }
    this.environments = newEnvironments;
  }

}
