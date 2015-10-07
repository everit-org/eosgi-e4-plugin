package org.everit.e4.eosgi.plugin.ui.command;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.everit.e4.eosgi.plugin.core.EOSGiContext;
import org.everit.e4.eosgi.plugin.core.EOSGiManager;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.everit.e4.eosgi.plugin.ui.jobs.DistStartJob;

/**
 * Handler implementation for start the current selected dist project.
 */
public class StartDistHandler extends AbstractDistHandler implements IHandler {

  @Override
  public void addHandlerListener(final IHandlerListener handlerListener) {
  }

  @Override
  public void dispose() {
    LOGGER.info("dispose");
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
      tryToStart();
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
  public void removeHandlerListener(final IHandlerListener arg0) {
  }

  private void tryToStart() {
    EOSGiManager eosgiManager = Activator.getDefault().getEOSGiManager();
    EOSGiContext eosgiProject = eosgiManager.findOrCreate(project);
    if (eosgiProject != null) {
      eosgiProject.runner(environmentName).ifPresent((runner) -> {
        new DistStartJob(environmentName, runner).schedule();
      });
    }
  }

}
