package org.everit.e4.eosgi.plugin.ui.command;

import java.util.logging.Logger;

//import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.everit.e4.eosgi.plugin.m2e.model.EosgiProject;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.everit.e4.eosgi.plugin.ui.navigator.nodes.EosgiNode;
import org.everit.e4.eosgi.plugin.ui.navigator.nodes.EosgiNodeType;

/**
 * Handler implementation for start the current selected dist project.
 */
public class StartDistHandler implements IHandler {

  private static final Logger LOGGER = Logger.getLogger(StartDistHandler.class.getName());

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
    return null;
  }

  private IProject findParentProject(final TreePath[] treePaths) {
    if (treePaths == null || treePaths.length == 0) {
      return null;
    }

    TreePath treePath = treePaths[0];
    if (treePath == null) {
      return null;
    }

    Object firstSegment = treePath.getFirstSegment();
    if (firstSegment instanceof IProject) {
      return (IProject) firstSegment;
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

  private void processTreeSelection(final TreeSelection treeSelection) {
    Object firstElement = treeSelection.getFirstElement();
    if (firstElement == null) {
      return;
    }
    IProject project = null;
    if (firstElement instanceof IProject) {
      project = (IProject) firstElement;
    }

    String environmentName = null;
    if (firstElement instanceof EosgiNode) {
      EosgiNode eosgiNode = (EosgiNode) firstElement;
      if (EosgiNodeType.ENVIRONMENT == eosgiNode.getType()) {
        environmentName = eosgiNode.getName();
      }
      project = findParentProject(treeSelection.getPaths());
    }

    if (project != null && environmentName != null) {
      EosgiProject eosgiProject = Activator.getDefault().getEosgiProjectController()
          .getProject(project);
      if (eosgiProject != null) {
        LOGGER.info(eosgiProject.getDistRunnerDescriptor(environmentName).toString());
      } else {
        LOGGER.info("Dont't have dist for selected project: " + project.getName());
      }
    }
  }

  @Override
  public void removeHandlerListener(final IHandlerListener arg0) {
  }

}
