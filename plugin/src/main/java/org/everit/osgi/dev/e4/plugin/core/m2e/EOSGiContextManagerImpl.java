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

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobFunction;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.everit.osgi.dev.e4.plugin.core.ContextChange;
import org.everit.osgi.dev.e4.plugin.core.EOSGiContext;
import org.everit.osgi.dev.e4.plugin.core.EOSGiContextManager;
import org.everit.osgi.dev.e4.plugin.core.m2e.xml.ConfiguratorParser;
import org.everit.osgi.dev.e4.plugin.core.m2e.xml.EnvironmentsDTO;
import org.everit.osgi.dev.e4.plugin.ui.EOSGiLog;
import org.everit.osgi.dev.e4.plugin.ui.nature.EosgiNature;

/**
 * Storing and managing {@link EOSGiContext} instances.
 */
public class EOSGiContextManagerImpl implements EOSGiContextManager {

  private final EOSGiLog log;

  private final Map<IProject, EOSGiContext> projectContexts = new ConcurrentHashMap<>();

  private final IMavenProjectRegistry projectRegistry;

  public EOSGiContextManagerImpl(final EOSGiLog log) {
    this.log = log;
    projectRegistry = MavenPlugin.getMavenProjectRegistry();
  }

  private void createAndStartMavenRefresh(final IProject project, final EOSGiContext eosgiContext) {
    Job job = Job.create("Fetch maven informations for EOSGi project...", new IJobFunction() {
      @Override
      public IStatus run(final IProgressMonitor monitor) {
        IMavenProjectFacade mavenProjectFacade = projectRegistry.getProject(project);
        if (mavenProjectFacade == null) {
          // Maven facade not found, yet.
          return Status.CANCEL_STATUS;
        }

        ContextChange contextChange = new ContextChange();
        try {
          String buildDirectory = mavenProjectFacade.getMavenProject(monitor).getBuild()
              .getDirectory();
          contextChange.buildDirectory = buildDirectory;
        } catch (Exception e) {
          log.error(MessageFormat.format("Couldn''t satisfied build directory for ''{0}''",
              project.getName()), e);
        }

        M2EGoalExecutor executor = new M2EGoalExecutor(project, null);
        Xpp3Dom configuration = executor.getConfiguration(monitor);
        if (configuration != null) {
          EnvironmentsDTO environments = null;
          if (configuration != null) {
            environments = new ConfiguratorParser().parse(configuration);
            eosgiContext.refresh(contextChange.configuration(environments));
          }
        }
        return Status.OK_STATUS;
      }
    });
    job.setPriority(Job.SHORT);
    job.schedule();
  }

  @Override
  public synchronized void dispose() {
    for (EOSGiContext eosgiContext : projectContexts.values()) {
      projectRegistry.removeMavenProjectChangedListener(eosgiContext);
      eosgiContext.dispose();
    }
    projectContexts.clear();
  }

  @Override
  public synchronized EOSGiContext findOrCreate(final IProject project) {
    if (projectContexts.containsKey(project)) {
      return projectContexts.get(project);
    } else {
      if (isNatureOk(project)) {
        EOSGiContext context = new EOSGiProject(project, log);
        projectRegistry.addMavenProjectChangedListener(context);
        createAndStartMavenRefresh(project, context);
        projectContexts.put(project, context);
        return context;
      }
    }
    return null;
  }

  private boolean isNatureOk(final IProject project) {
    boolean eosgiNature = false;
    try {
      eosgiNature = project.hasNature(EosgiNature.NATURE_ID);
    } catch (CoreException e) {
      log.error("Couldn't check EOSGI nature", e);
    }
    return eosgiNature;
  }

  @Override
  public synchronized void remove(final IProject project) {
    EOSGiContext context = projectContexts.remove(project);
    if (context != null) {
      projectRegistry.removeMavenProjectChangedListener(context);
      context.dispose();
    }
  }

}
