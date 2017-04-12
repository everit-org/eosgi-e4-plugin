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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Handler for the clean context menu item.
 */
public class CleanCommandHandler extends AbstractHandler {

  @Override
  public Object execute(final ExecutionEvent event) throws ExecutionException {
    CommandUtil.executeInJobWithErrorHandling(event,
        "Cleaning distributed directory of OSGi environment",
        (executableEnvironment, monitor) -> {
          executableEnvironment.getEOSGiProject().clean(executableEnvironment, monitor);
          Display.getDefault().asyncExec(
              () -> MessageDialog.openInformation(new Shell(), "Cleaning OSGi environment",
                  "Deleting environment distribution directory was successful."));
        });

    return null;
  }

}
