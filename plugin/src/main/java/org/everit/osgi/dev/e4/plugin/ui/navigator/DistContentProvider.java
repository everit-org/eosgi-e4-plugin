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
package org.everit.osgi.dev.e4.plugin.ui.navigator;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.everit.osgi.dev.e4.plugin.EOSGiEclipsePlugin;
import org.everit.osgi.dev.e4.plugin.EOSGiProject;
import org.everit.osgi.dev.e4.plugin.ExecutableEnvironmentContainer;

/**
 * TreeNodeContentProvider implementation for manage the EOSGI nodes in ProjectExplorer.
 */
public class DistContentProvider extends TreeNodeContentProvider {

  @Override
  public Object[] getChildren(final Object parentElement) {
    if (parentElement instanceof IProject) {
      AtomicReference<EOSGiProject> eosgiProjectReference = new AtomicReference<>();
      IProject project = (IProject) parentElement;
      String taskName = "Getting EOSGi information of project: " + project.getName();
      Job job =
          Job.create(taskName, (monitor) -> {
            monitor.beginTask(taskName, 1000);
            EOSGiProject eosgiProject =
                EOSGiEclipsePlugin.getDefault().getEOSGiManager().get(project, monitor);

            eosgiProjectReference.set(eosgiProject);
            return Status.OK_STATUS;
          });
      job.schedule();
      try {
        job.join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      if (eosgiProjectReference.get() == null) {
        return new Object[0];
      }
      return new Object[] { eosgiProjectReference.get() };
    } else if (parentElement instanceof EOSGiProject) {
      ExecutableEnvironmentContainer executableEnvironmentContainer =
          ((EOSGiProject) parentElement).getExecutableEnvironmentContainer();

      return executableEnvironmentContainer.getExecutableEnvironments().toArray();
    } else {
      return super.getChildren(parentElement);
    }
  }

  @Override
  public boolean hasChildren(final Object element) {
    return element instanceof IProject || element instanceof EOSGiProject;
  }
}
