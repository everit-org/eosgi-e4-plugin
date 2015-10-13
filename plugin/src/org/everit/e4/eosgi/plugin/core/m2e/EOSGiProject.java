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
import java.util.Optional;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.everit.e4.eosgi.plugin.core.ContextChange;
import org.everit.e4.eosgi.plugin.core.EOSGiContext;
import org.everit.e4.eosgi.plugin.core.EventType;
import org.everit.e4.eosgi.plugin.core.ModelChangeEvent;
import org.everit.e4.eosgi.plugin.core.dist.DistRunner;
import org.everit.e4.eosgi.plugin.core.dist.DistStatus;
import org.everit.e4.eosgi.plugin.core.dist.EOSGiDistRunner;
import org.everit.e4.eosgi.plugin.core.m2e.model.Environment;
import org.everit.e4.eosgi.plugin.core.m2e.model.Environments;
import org.everit.e4.eosgi.plugin.core.m2e.xml.ConfiguratorParser;
import org.everit.e4.eosgi.plugin.ui.EOSGiLog;

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
    environments.forEach((key, value) -> {
      value.getDistRunner().ifPresent(runner -> {
        runner.stop();
      });
    });
    environments.clear();
  }

  @Override
  public List<String> environmentIds() {
    final List<String> environmentList = new ArrayList<>();
    environments.keySet().forEach((name) -> {
      environmentList.add(name);
    });
    return environmentList;
  }

  @Override
  public void forcedStop(final String environmentName) {
    throw new UnsupportedOperationException("Not implemented, yet");
  }

  @Override
  public void generate(final String environmentId, final IProgressMonitor monitor) {
    Objects.requireNonNull(environmentId, "environmentName must be not null!");

    Environment environment = environments.get(environmentId);
    if (environment == null) {
      log.error("Could not found environment with name '" + environmentId + "'");
      return;
    }

    boolean generated = false;
    try {
      generated = new M2EGoalExecutor(project).execute(monitor);
    } catch (CoreException e) {
      log.error(
          MessageFormat.format("Couldn''t generate dist for ''{0}'' environment.", environmentId),
          e);
    }

    DistStatus newDistStatus = DistStatus.NONE;
    if (generated) {
      DistRunner distRunner = new EOSGiDistRunner(buildDirectory, environmentId);
      environment.setDistRunner(distRunner);
      newDistStatus = DistStatus.STOPPED;
      setChanged();
    }

    notifyObservers(new ModelChangeEvent()
        .eventType(EventType.ENVIRONMENT)
        .arg(new Object[] { environmentId, newDistStatus }));
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
    } else if (contextChange.buildDirectory != null
        && !contextChange.buildDirectory.equals(buildDirectory)) {
      buildDirectory = contextChange.buildDirectory;
      setChanged();
    }
    if (contextChange.configuration != null) {
      updateEnvironments(contextChange.configuration);
      setChanged();
    }

    notifyObservers(
        new ModelChangeEvent()
            .eventType(EventType.ENVIRONMENTS)
            .arg(this));

  }

  @Override
  public void removeObserver(final Observer observer) {
    deleteObserver(observer);
  }

  @Override
  public Optional<DistRunner> runner(final String environmentName) {
    Objects.requireNonNull(environmentName, "environmentName must be not null!");
    if (environments.containsKey(environmentName)) {
      return environments.get(environmentName).getDistRunner();
    } else {
      log.error("Couldn't find environment with name " + environmentName);
      return Optional.empty();
    }
  }

  @Override
  public String toString() {
    return "EOSGiProject [buildDirectory=" + buildDirectory + ", environments=" + environments
        + ", project=" + project + "]";
  }

  private void updateEnvironments(final Xpp3Dom configuration) {
    Environments environments = null;
    try {
      environments = new ConfiguratorParser().parse(configuration);
    } catch (Exception e) {
      log.error("can't parse configuration", e);
      return;
    }

    Map<String, Environment> newEnvironments = new HashMap<>();
    environments.getEnvironments().forEach((newEnvironment) -> {
      Environment environment = null;
      if (this.environments.containsKey(newEnvironment.getId())) {
        environment = this.environments.remove(newEnvironment.getId());
        environment.setBundleSettings(newEnvironment.getBundleSettings());
        environment.setFramework(newEnvironment.getFramework());
        environment.setSystemProperties(newEnvironment.getSystemProperties());
        environment.setVmOptions(newEnvironment.getVmOptions());
        environment.setOutdated(false);
        newEnvironments.put(environment.getId(), environment);
      } else {
        // setChanged();
        newEnvironments.put(newEnvironment.getId(), newEnvironment);
      }
    });
    // if (!this.environments.isEmpty()) {
    // setChanged(); // environments changes if some env deleted.
    // }
    this.environments.forEach((key, environment) -> {
      Optional<DistRunner> distRunner = environment.getDistRunner();
      distRunner.ifPresent((runner) -> {
        runner.stop();
      });
    });
    this.environments = newEnvironments;
  }

}
