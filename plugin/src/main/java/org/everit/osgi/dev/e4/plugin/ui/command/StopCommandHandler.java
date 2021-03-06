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
import org.everit.osgi.dev.dist.util.attach.ForcedShutdownParameter;
import org.everit.osgi.dev.e4.plugin.EOSGiEclipsePlugin;
import org.everit.osgi.dev.e4.plugin.ui.navigator.EOSGiProjectPopupMenu;

/**
 * Stops a running EOSGi JVM.
 */
public class StopCommandHandler extends AbstractHandler {

  @Override
  public Object execute(final ExecutionEvent event) throws ExecutionException {
    CommandUtil.executeInJobWithErrorHandling(event,
        "Distributing changes to OSGi Environment",
        (executableEnvironment, monitor) -> {
          String vmId =
              event.getParameter(EOSGiProjectPopupMenu.COMMAND_ID_PREFIX + "stopCommand.vmId");
          EOSGiEclipsePlugin.getDefault().getEOSGiManager().getEosgiVMManager()
              .shutDownVirtualMachine(vmId, 0,
                  new ForcedShutdownParameter(-1, executableEnvironment.getShutdownTimeout()));
        });

    return null;
  }
}
