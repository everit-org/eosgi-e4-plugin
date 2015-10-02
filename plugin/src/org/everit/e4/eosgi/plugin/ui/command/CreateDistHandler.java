package org.everit.e4.eosgi.plugin.ui.command;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.everit.e4.eosgi.plugin.ui.EOSGiLog;
import org.everit.e4.eosgi.plugin.ui.jobs.DistGenerationJob;

public class CreateDistHandler extends AbstractDistHandler implements IHandler {

  private EOSGiLog log;

  public CreateDistHandler() {
    super();
    log = new EOSGiLog(Activator.getDefault().getLog());
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

    if (project != null && environmentName != null) {
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
