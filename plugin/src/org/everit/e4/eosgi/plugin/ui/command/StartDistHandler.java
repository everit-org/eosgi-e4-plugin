package org.everit.e4.eosgi.plugin.ui.command;

//import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.everit.e4.eosgi.plugin.core.dist.DistRunner;
import org.everit.e4.eosgi.plugin.core.m2e.EosgiManager;
import org.everit.e4.eosgi.plugin.ui.Activator;

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
      EosgiManager eosgiManager = Activator.getDefault().getEosgiManager();
      DistRunner distRunner = eosgiManager.getDistRunner(project, environmentName);
      if (distRunner != null) {
        Job job = new Job("Starting '" + environmentName + "' EOSGI environment...") {
          @Override
          protected void canceling() {
            distRunner.stop();
            super.canceling();
          }

          @Override
          protected IStatus run(IProgressMonitor monitor) {
            distRunner.start();
            return Status.OK_STATUS;
          }
        };
        job.setPriority(Job.SHORT);
        job.schedule();
      }
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

}
