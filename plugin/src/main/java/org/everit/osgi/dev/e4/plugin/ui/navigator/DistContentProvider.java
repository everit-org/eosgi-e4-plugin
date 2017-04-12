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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.everit.osgi.dev.e4.plugin.EOSGiEclipsePlugin;
import org.everit.osgi.dev.e4.plugin.EOSGiProject;
import org.everit.osgi.dev.e4.plugin.ExecutableEnvironmentContainer;

/**
 * TreeNodeContentProvider implementation for manage the EOSGI nodes in ProjectExplorer.
 */
public class DistContentProvider extends TreeNodeContentProvider {

  @Override
  public Object[] getChildren(final Object parentElement) {
    if (EOSGiEclipsePlugin.getDefault() == null) {
      return new Object[0];
    }

    if (parentElement instanceof IProject) {
      AtomicReference<EOSGiProject> eosgiProjectReference = new AtomicReference<>();
      IProject project = (IProject) parentElement;
      if (!project.isOpen()) {
        return new Object[0];
      }

      String taskName = "Getting EOSGi information of project: " + project.getName();

      Job job =
          Job.create(taskName, (monitor) -> {
            SubMonitor subMonitor = SubMonitor.convert(monitor, taskName, 1);

            try {
              EOSGiProject eosgiProject =
                  EOSGiEclipsePlugin.getDefault().getEOSGiManager().get(project, subMonitor);
              eosgiProjectReference.set(eosgiProject);
              return Status.OK_STATUS;
            } catch (CoreException e) {
              IStatus status = e.getStatus();
              Display.getDefault().asyncExec(() -> {
                Shell shell = new Shell();
                ErrorDialog.openError(shell, "Error",
                    "Error during refreshing content in Project Explorer for project: "
                        + project.getName(),
                    status);
              });
              return status;
            }

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
