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
package org.everit.osgi.dev.e4.plugin;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

/**
 * Listens to workspace resource changes.
 */
public class ChangedProjectTracker implements IResourceChangeListener {

  Set<IProject> changedProjects = new HashSet<>();

  public ChangedProjectTracker() {
    IMavenProjectFacade[] mavenProjects = MavenPlugin.getMavenProjectRegistry().getProjects();
    for (IMavenProjectFacade mavenProjectFacade : mavenProjects) {
      changedProjects.add(mavenProjectFacade.getProject());
    }
  }

  private void processChangeEventRecurse(final IResourceDelta delta) {
    IResource resource = delta.getResource();
    if (resource instanceof IProject) {
      IProject project = (IProject) resource;
      if (project.isOpen()) {
        changedProjects.add(project);
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

  public synchronized boolean removeProject(final IProject project) {
    return changedProjects.remove(project);
  }

  @Override
  public synchronized void resourceChanged(final IResourceChangeEvent event) {
    int eventType = event.getType();

    switch (eventType) {
      case IResourceChangeEvent.PRE_DELETE:
      case IResourceChangeEvent.PRE_CLOSE:
        changedProjects.remove(event.getResource());
        break;
      case IResourceChangeEvent.POST_CHANGE:
        processChangeEventRecurse(event.getDelta());
        break;
      default:
        break;
    }
  }

}
