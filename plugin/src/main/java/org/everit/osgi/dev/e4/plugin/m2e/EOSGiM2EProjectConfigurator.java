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
package org.everit.osgi.dev.e4.plugin.m2e;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.everit.osgi.dev.e4.plugin.EOSGiEclipsePlugin;
import org.everit.osgi.dev.e4.plugin.EOSGiNature;

/**
 * Project configurator for EOSGi projects.
 */
public class EOSGiM2EProjectConfigurator extends AbstractProjectConfigurator {

  private void addEosgiNature(final IProgressMonitor monitor, final IProject project)
      throws CoreException {
    IProjectDescription projectDescription = project.getDescription();
    if (projectDescription != null) {
      String[] natureIds = projectDescription.getNatureIds();
      if (!ArrayUtils.contains(natureIds, EOSGiNature.NATURE_ID)) {
        String[] newNatureIds = ArrayUtils.add(natureIds, EOSGiNature.NATURE_ID);
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
    EOSGiEclipsePlugin.getDefault().getEOSGiManager().putOrOverride(project, monitor);
  }

  @Override
  public void mavenProjectChanged(final MavenProjectChangedEvent event,
      final IProgressMonitor monitor)
      throws CoreException {

    EOSGiEclipsePlugin.getDefault().getEOSGiManager()
        .putOrOverride(event.getMavenProject().getProject(), monitor);
  }

  private void removeProjectNature(final IProject project, final IProgressMonitor monitor)
      throws CoreException {
    IProjectDescription projectDescription = project.getDescription();
    if (projectDescription != null) {
      String[] natureIds = projectDescription.getNatureIds();
      int indexOfNature = ArrayUtils.indexOf(natureIds, EOSGiNature.NATURE_ID);
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
    IProject project = request.getProject();
    removeProjectNature(project, monitor);
    EOSGiEclipsePlugin.getDefault().getEOSGiManager().remove(project);
    super.unconfigure(request, monitor);
  }

}
