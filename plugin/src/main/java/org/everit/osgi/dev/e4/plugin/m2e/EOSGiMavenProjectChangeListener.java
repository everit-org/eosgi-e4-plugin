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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.m2e.core.project.IMavenProjectChangedListener;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.everit.osgi.dev.e4.plugin.EOSGiEclipsePlugin;
import org.everit.osgi.dev.e4.plugin.EOSGiNature;

/**
 * Adds or removes EOSGi Nature and refreshes EOSGi information.
 */
public class EOSGiMavenProjectChangeListener implements IMavenProjectChangedListener {

  private void addEosgiNature(final IProgressMonitor monitor, final IProject project)
      throws CoreException {

    if (EOSGiEclipsePlugin.getDefault() == null) {
      return;
    }

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
  public void mavenProjectChanged(final MavenProjectChangedEvent[] events,
      final IProgressMonitor monitor) {

    if (EOSGiEclipsePlugin.getDefault() == null) {
      return;
    }

    for (MavenProjectChangedEvent event : events) {
      IMavenProjectFacade mavenProject = event.getMavenProject();

      if (mavenProject != null) {
        try {
          boolean eosgiProject = M2EUtil.hasEOSGiMavenPlugin(mavenProject.getMavenProject(monitor));
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
          IStatus status = e.getStatus();
          EOSGiEclipsePlugin.getDefault().getLog().log(status);
          Display.getDefault().asyncExec(() -> {
            Shell shell = new Shell();
            ErrorDialog.openError(shell, "Error",
                "Could not refresh project by EOSGi Eclipse plugin: "
                    + mavenProject.getProject().getName(),
                status);
          });
        }
      }
    }

  }

  private void removeProjectNature(final IProject project, final IProgressMonitor monitor)
      throws CoreException {

    if (EOSGiEclipsePlugin.getDefault() == null) {
      return;
    }

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
