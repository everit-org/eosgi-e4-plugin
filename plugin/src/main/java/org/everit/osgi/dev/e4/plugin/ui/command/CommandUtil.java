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
package org.everit.osgi.dev.e4.plugin.ui.command;

import java.util.Iterator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.everit.osgi.dev.e4.plugin.EOSGiEclipsePlugin;
import org.everit.osgi.dev.e4.plugin.ExecutableEnvironment;

/**
 * Util methods to help processing commands and menu items.
 *
 */
public final class CommandUtil {

  /**
   * Executes an action on an {@link ExecutableEnvironment} within a job and handling
   * {@link CoreException}s by showing an {@link ErrorDialog}.
   *
   * @param event
   *          The command executionEvent that caused this action.
   * @param jobTitle
   *          The title of the job.
   * @param action
   *          The action that should be executed within the job.
   */
  public static void executeInJobWithErrorHandling(final ExecutionEvent event,
      final String jobTitle, final ExecutableEnvironmentAction action) {

    ExecutableEnvironment executableEnvironment = CommandUtil.resolveExecutableEnvironment(event);

    Job job = Job.create(jobTitle, (monitor) -> {
      try {
        action.run(executableEnvironment, monitor);
      } catch (CoreException e) {
        EOSGiEclipsePlugin.getDefault().getLog().log(e.getStatus());
        Display.getDefault().asyncExec(() -> {
          Shell shell = new Shell();
          ErrorDialog.openError(shell, "Error",
              "Error during executing job '" + jobTitle + "' on environment '"
                  + executableEnvironment.getEnvironmentId() + "' of project '"
                  + executableEnvironment.getEOSGiProject().getMavenProjectFacade().getProject()
                      .getName()
                  + "'. See Error Log for more details!",
              e.getStatus());
        });
      }
    });
    job.schedule();

  }

  /**
   * Gets the single selection from an evaluation context. E.g.: The selected item in case of a
   * popup menu.
   *
   * @param selection
   *          the selection of the current workbench window.
   * @return The single selection or <code>null</code> if no or multiple items were selected.
   */
  public static Object getSingleSelection(final ISelection selection) {

    if (selection == null || !(selection instanceof IStructuredSelection)) {
      return null;
    }

    IStructuredSelection structuredSelection = (IStructuredSelection) selection;

    Iterator<?> iterator = structuredSelection.iterator();

    Object selectionObject = null;
    if (iterator.hasNext()) {
      selectionObject = iterator.next();
    }

    if (iterator.hasNext()) {
      return null;
    }

    return selectionObject;
  }

  /**
   * Resolves the EOSGi executable environment by a context menu click event.
   *
   * @param event
   *          The event that happens if the user selects a context menu.
   * @return The EOSGi executable environment.
   */
  public static ExecutableEnvironment resolveExecutableEnvironment(final ExecutionEvent event) {
    ISelection selection =
        HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();

    Object singleSelection = CommandUtil.getSingleSelection(selection);

    if (singleSelection == null || !(singleSelection instanceof ExecutableEnvironment)) {
      throw new IllegalArgumentException(
          "Selected item should be instance of EOSGiProject: " + singleSelection);
    }

    final ExecutableEnvironment executableEnvironment = (ExecutableEnvironment) singleSelection;
    return executableEnvironment;
  }

  private CommandUtil() {
  }
}
