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

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectChangedListener;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.everit.osgi.dev.e4.plugin.EOSGiEclipsePlugin;
import org.everit.osgi.dev.e4.plugin.EOSGiNature;
import org.everit.osgi.dev.e4.plugin.EOSGiProject;
import org.osgi.framework.Version;

/**
 * Adds or removes EOSGi Nature and refreshes EOSGi information.
 */
public class EOSGiMavenProjectChangeListener implements IMavenProjectChangedListener {

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

  private boolean isThereEOSGiExecution(final IMavenProjectFacade mavenProject,
      final IProgressMonitor monitor) {
    try {
      List<MojoExecution> mojoExecutions = mavenProject.getMojoExecutions(
          EOSGiProject.EOSGI_GROUP_ID, EOSGiProject.EOSGI_ARTIFACT_ID,
          monitor, EOSGiProject.EOSGI_SORTED_ACCEPTED_GOAL_ARRAY);

      boolean result = false;
      Iterator<MojoExecution> iterator = mojoExecutions.iterator();
      while (!result && iterator.hasNext()) {
        MojoExecution mojoExecution = iterator.next();
        result = EOSGiProject.EOSGI_VERSION_RANGE
            .includes(new Version(mojoExecution.getVersion().replace('-', '.')));
      }
      return result;
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void mavenProjectChanged(final MavenProjectChangedEvent[] events,
      final IProgressMonitor monitor) {
    for (MavenProjectChangedEvent event : events) {
      IMavenProjectFacade mavenProject = event.getMavenProject();

      try {
        boolean eosgiProject = isThereEOSGiExecution(mavenProject, monitor);
        IProject project = mavenProject.getProject();
        boolean hasNature = project.hasNature(EOSGiNature.NATURE_ID);

        if (eosgiProject && !hasNature) {
          addEosgiNature(monitor, project);
        } else if (!eosgiProject && hasNature) {
          removeProjectNature(project, monitor);
          EOSGiEclipsePlugin.getDefault().getEOSGiManager().remove(project);
        }

        if (eosgiProject) {
          EOSGiEclipsePlugin.getDefault().getEOSGiManager().putOrOverride(mavenProject, monitor);
        }
      } catch (CoreException e) {
        throw new RuntimeException(e);
      }
    }

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

}
