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

import javax.management.InstanceNotFoundException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.everit.osgi.dev.e4.plugin.EOSGiEclipsePlugin;
import org.everit.osgi.dev.e4.plugin.ExecutableEnvironment;

/**
 * IHandler implementation for dist creation.
 */
public class DistCommandHandler extends AbstractHandler {

  @Override
  public Object execute(final ExecutionEvent event) throws ExecutionException {
    CommandUtil.executeInJobWithErrorHandling(event,
        "Distributing changes to OSGi Environment",
        (executableEnvironment, monitor) -> {
          try {
            executableEnvironment.getEOSGiProject().dist(executableEnvironment, monitor);
          } catch (CoreException e) {
            throw replaceCoreExceptionIfItComesFromInstanceNotFound(executableEnvironment, e);
          }
        });

    return null;
  }

  private CoreException replaceCoreExceptionIfItComesFromInstanceNotFound(
      final ExecutableEnvironment executableEnvironment, final CoreException e) {
    IStatus status = e.getStatus();
    Throwable cause = status.getException();
    while (cause != null) {
      if (cause instanceof InstanceNotFoundException) {
        return new CoreException(
            new Status(IStatus.ERROR, EOSGiEclipsePlugin.PLUGIN_ID,
                "Cannot distrubute changes to environment '"
                    + executableEnvironment.getExecutionId() + "' of project '"
                    + executableEnvironment.getEOSGiProject().getMavenProjectFacade().getProject()
                        .getName()
                    + "': " + cause.getMessage(),
                cause));
      }
      cause = cause.getCause();
    }
    return e;
  }

}
