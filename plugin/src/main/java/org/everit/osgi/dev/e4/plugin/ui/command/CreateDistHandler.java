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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.everit.osgi.dev.e4.plugin.ui.EOSGiLog;
import org.everit.osgi.dev.e4.plugin.ui.EOSGiEclipsePlugin;
import org.everit.osgi.dev.e4.plugin.ui.jobs.DistGenerationJob;

/**
 * IHandler implementation for dist creation.
 */
public class CreateDistHandler extends AbstractDistHandler implements IHandler {

  private EOSGiLog log;

  public CreateDistHandler() {
    super();
    log = new EOSGiLog(EOSGiEclipsePlugin.getDefault().getLog());
  }

  @Override
  public void addHandlerListener(final IHandlerListener handlerListener) {
  }

  @Override
  public void dispose() {
  }

  @Override
  public Object execute(final ExecutionEvent executionEvent) throws ExecutionException {
    ISelection currentSelection = HandlerUtil.getCurrentSelection(executionEvent);
    if (currentSelection == null) {
      return null;
    }

    TreeSelection treeSelection = null;
    if (currentSelection instanceof TreeSelection) {
      treeSelection = (TreeSelection) currentSelection;
    }

    if (treeSelection == null) {
      return null;
    }

    processTreeSelection(treeSelection);

    if ((project != null) && (environmentName != null)) {
      new DistGenerationJob(project, environmentName).schedule();
    } else {
      log.warning("Can't identified the project or name of the environment.");
    }

    return null;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean isHandled() {
    return true;
  }

  @Override
  public void removeHandlerListener(final IHandlerListener handlerListener) {
  }

}
