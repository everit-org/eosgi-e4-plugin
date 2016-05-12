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

import org.apache.commons.lang3.ArrayUtils;
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
import org.everit.osgi.dev.e4.plugin.core.ContextChange;
import org.everit.osgi.dev.e4.plugin.core.EOSGiContext;
import org.everit.osgi.dev.e4.plugin.core.EOSGiContextManager;
import org.everit.osgi.dev.e4.plugin.core.m2e.xml.ConfiguratorParser;
import org.everit.osgi.dev.e4.plugin.core.m2e.xml.EnvironmentsDTO;
import org.everit.osgi.dev.e4.plugin.ui.EOSGiEclipsePlugin;
import org.everit.osgi.dev.e4.plugin.ui.EOSGiLog;
import org.everit.osgi.dev.e4.plugin.ui.nature.EosgiNature;

/**
 * Project configurator for EOSGi projects.
 */
public class EOSGiM2EProjectConfigurator extends AbstractProjectConfigurator {

  private final EOSGiContextManager eosgiManager;

  private final EOSGiLog log;

  /**
   * Constructor.
   */
  public EOSGiM2EProjectConfigurator() {
    super();
    EOSGiEclipsePlugin plugin = EOSGiEclipsePlugin.getDefault();
    log = new EOSGiLog(plugin.getLog());
    eosgiManager = plugin.getEOSGiManager();
  }

  private void addEosgiNature(final IProgressMonitor monitor, final IProject project)
      throws CoreException {
    IProjectDescription projectDescription = project.getDescription();
    if (projectDescription != null) {
      String[] natureIds = projectDescription.getNatureIds();
      if (!ArrayUtils.contains(natureIds, EosgiNature.NATURE_ID)) {
        String[] newNatureIds = ArrayUtils.add(natureIds, EosgiNature.NATURE_ID);
        projectDescription.setNatureIds(newNatureIds);
        project.setDescription(projectDescription, monitor);
      }
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

    boolean changed =
        super.hasConfigurationChanged(newFacade, oldProjectConfiguration, key, monitor);

    if (changed && newFacade != null) {
      monitor.subTask("Update configuration");
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
    return changed;
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

  private void removeProjectNature(final IProject project, final IProgressMonitor monitor)
      throws CoreException {
    IProjectDescription projectDescription = project.getDescription();
    if (projectDescription != null) {
      String[] natureIds = projectDescription.getNatureIds();
      int indexOfNature = ArrayUtils.indexOf(natureIds, EosgiNature.NATURE_ID);
      if (indexOfNature >= 0) {
        String[] newNatureIds = ArrayUtils.remove(natureIds, indexOfNature);
        projectDescription.setNatureIds(newNatureIds);
        project.setDescription(projectDescription, monitor);
      }
    }
  }

  @Override
  public void unconfigure(final ProjectConfigurationRequest request, final IProgressMonitor monitor)
      throws CoreException {
    removeProjectNature(request.getProject(), monitor);
    super.unconfigure(request, monitor);
  }

}
