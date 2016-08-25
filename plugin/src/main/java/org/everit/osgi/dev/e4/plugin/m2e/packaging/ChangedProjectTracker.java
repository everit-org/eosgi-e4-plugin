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
package org.everit.osgi.dev.e4.plugin.m2e.packaging;

import java.io.File;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.maven.model.BuildBase;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

/**
 * Tracks changes in projects and fires change event if any of the projects should be re-packaged
 * later. More specifically: If any files changes that are not in {@link BuildBase#getDirectory()}
 * or the file is one of the generated artifacts of the project
 * <ul>
 * <li>{@link MavenProject#getArtifact()}</li>
 * <li>{@link MavenProject#getAttachedArtifacts()}</li>
 * </ul>
 * .
 */
public class ChangedProjectTracker implements IResourceChangeListener {

  private final Function<IProject, Set<File>> projectArtifactFileProvider;

  private final Consumer<IProject> projectChangeHandler;

  public ChangedProjectTracker(final Consumer<IProject> projectChangeHandler,
      final Function<IProject, Set<File>> projectArtifactFileProvider) {
    this.projectChangeHandler = projectChangeHandler;
    this.projectArtifactFileProvider = projectArtifactFileProvider;
  }

  private void processChangeEventOnMavenProject(final IMavenProjectFacade mavenProjectFacade) {
    try {
      String targetDirectory = mavenProjectFacade.getMavenProject(new NullProgressMonitor())
          .getBuild().getDirectory();

      // TODO
      System.out.println(targetDirectory);
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }

  private void processChangeEventRecurse(final IResourceDelta delta) {
    IResource resource = delta.getResource();
    if (resource instanceof IProject) {
      IProject eclipseProject = (IProject) resource;
      if (eclipseProject.isOpen()) {
        IMavenProjectFacade mavenProjectFacade =
            MavenPlugin.getMavenProjectRegistry().getProject(eclipseProject);

        if (mavenProjectFacade == null) {
          projectChangeHandler.accept((IProject) resource);
        } else {
          processChangeEventOnMavenProject(mavenProjectFacade);
        }
      }
    } else {
      IResourceDelta[] affectedChildren = delta.getAffectedChildren();
      if (affectedChildren != null) {
        for (IResourceDelta childDelta : affectedChildren) {
          processChangeEventRecurse(childDelta);
        }
      }
    }
  }

  @Override
  public synchronized void resourceChanged(final IResourceChangeEvent event) {
    int eventType = event.getType();

    switch (eventType) {
      case IResourceChangeEvent.PRE_DELETE:
      case IResourceChangeEvent.PRE_CLOSE:
        IResource resource = event.getResource();
        if (resource instanceof IProject) {
          projectChangeHandler.accept((IProject) resource);
        }
        break;
      case IResourceChangeEvent.POST_CHANGE:
        processChangeEventRecurse(event.getDelta());
        break;
      default:
        break;
    }
  }

}
