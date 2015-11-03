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

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ILifecycleMappingConfiguration;
import org.eclipse.m2e.core.project.configurator.MojoExecutionKey;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;
import org.everit.e4.eosgi.plugin.core.ContextChange;
import org.everit.e4.eosgi.plugin.core.EOSGiContext;
import org.everit.e4.eosgi.plugin.core.EOSGiContextManager;
import org.everit.e4.eosgi.plugin.core.m2e.xml.ConfiguratorParser;
import org.everit.e4.eosgi.plugin.core.m2e.xml.EnvironmentsDTO;
import org.everit.e4.eosgi.plugin.ui.EOSGiLog;
import org.everit.e4.eosgi.plugin.ui.EOSGiPluginActivator;
import org.everit.e4.eosgi.plugin.ui.nature.EosgiNature;
import org.everit.e4.eosgi.plugin.ui.util.ProjectNatureUtils;

/**
 * Project configurator for EOSGI projects.
 */
public class DistProjectConfigurator extends AbstractProjectConfigurator
    implements IJavaProjectConfigurator {

  private EOSGiContextManager eosgiManager;

  private EOSGiLog log;

  /**
   * Constructor.
   */
  public DistProjectConfigurator() {
    super();
    EOSGiPluginActivator plugin = EOSGiPluginActivator.getDefault();
    log = new EOSGiLog(plugin.getLog());
    eosgiManager = plugin.getEOSGiManager();
  }

  private void addEosgiNature(final IProgressMonitor monitor, final IProject project)
      throws CoreException {
    IProjectDescription projectDescription = project.getDescription();
    if (projectDescription != null) {
      String[] natureIds = projectDescription.getNatureIds();
      String[] newNatureIds = ProjectNatureUtils.addNature(natureIds, EosgiNature.NATURE_ID);
      projectDescription.setNatureIds(newNatureIds);
      project.setDescription(projectDescription, monitor);
    }
  }

  @Override
  public void configure(final ProjectConfigurationRequest request, final IProgressMonitor monitor)
      throws CoreException {
    IProject project = request.getProject();
    if (project == null) {
      return;
    }
    addEosgiNature(monitor, project);
    eosgiManager.findOrCreate(project);
  }

  @Override
  public void configureClasspath(final IMavenProjectFacade mavenProjectFacade,
      final IClasspathDescriptor classpathDescriptor,
      final IProgressMonitor progressMonitor) throws CoreException {
  }

  @Override
  public void configureRawClasspath(final ProjectConfigurationRequest projectConfigurationRequest,
      final IClasspathDescriptor classpathDescriptor,
      final IProgressMonitor progressMonitor) throws CoreException {
  }

  @Override
  public AbstractBuildParticipant getBuildParticipant(final IMavenProjectFacade projectFacade,
      final MojoExecution execution,
      final IPluginExecutionMetadata executionMetadata) {
    return new DistBuildParticipant(execution, eosgiManager);
  }

  @Override
  protected <T> T getParameterValue(final MavenProject project, final String parameter,
      final Class<T> asType,
      final MojoExecution mojoExecution, final IProgressMonitor monitor) throws CoreException {
    return super.getParameterValue(project, parameter, asType, mojoExecution, monitor);
  }

  @Override
  public boolean hasConfigurationChanged(final IMavenProjectFacade newFacade,
      final ILifecycleMappingConfiguration oldProjectConfiguration, final MojoExecutionKey key,
      final IProgressMonitor monitor) {
    if (newFacade != null) {
      monitor.subTask("Update configuration");
      // IProject project = newFacade.getProject();
      // if (project != null) {
      // M2EGoalExecutor m2eGoalExecutor = new M2EGoalExecutor(project, null);
      //
      // EnvironmentsDTO environments = null;
      // Optional<Xpp3Dom> configuration = m2eGoalExecutor.getConfiguration(monitor);
      // if (configuration.isPresent()) {
      // try {
      // environments = new ConfiguratorParser().parse(configuration.get());
      // } catch (Exception e) {
      // log.error("can't parse configuration", e);
      // }
      // }
      //
      // ContextChange contextChange = new ContextChange();
      // if (environments != null) {
      // contextChange.configuration(environments);
      // }
      // m2eGoalExecutor.isLegacy(monitor)
      // .ifPresent((isLegacy) -> contextChange.legacyMavenPlugin(true));
      //
      // EOSGiContext eosgiProject = eosgiManager.findOrCreate(project);
      // if (eosgiProject != null) {
      // eosgiProject.refresh(contextChange);
      // }
      // }
      try {
        IProject project = newFacade.getProject();
        MojoExecution mojoExecution = newFacade.getMojoExecution(key, monitor);
        Xpp3Dom configuration = mojoExecution.getConfiguration();
        if ((project != null) && (configuration != null)) {
          try {
            EnvironmentsDTO environments = new ConfiguratorParser().parse(configuration);
            EOSGiContext eosgiProject = eosgiManager.findOrCreate(project);
            if (eosgiProject != null) {
              eosgiProject.refresh(new ContextChange().configuration(environments));
            }
          } catch (Exception e) {
            log.error("can't parse configuration", e);
          }
        }
      } catch (CoreException e) {
        log.error("Configuration change handling failed for project: " + e.getMessage());
      }
    }
    return super.hasConfigurationChanged(newFacade, oldProjectConfiguration, key, monitor);
  }

  @Override
  public void mavenProjectChanged(final MavenProjectChangedEvent event,
      final IProgressMonitor monitor)
          throws CoreException {
    IMavenProjectFacade mavenProjectFacade = event.getMavenProject();
    MavenProject mavenProject = null;

    IProject project = null;
    if (mavenProjectFacade != null) {
      mavenProject = mavenProjectFacade.getMavenProject();
      project = event.getMavenProject().getProject();
    }

    if ((project != null) || (mavenProject != null)) {
      EOSGiContext eosgiProject = eosgiManager.findOrCreate(project);
      if (eosgiProject != null) {
        eosgiProject.refresh(
            new ContextChange().buildDirectory(mavenProject.getBuild().getDirectory()));
      }
    }

    super.mavenProjectChanged(event, monitor);
  }

  @Override
  public void unconfigure(final ProjectConfigurationRequest request, final IProgressMonitor monitor)
      throws CoreException {
    super.unconfigure(request, monitor);
  }

}
