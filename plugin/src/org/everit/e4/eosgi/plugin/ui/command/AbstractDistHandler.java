package org.everit.e4.eosgi.plugin.ui.command;

import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.everit.e4.eosgi.plugin.ui.navigator.nodes.EosgiNode;
import org.everit.e4.eosgi.plugin.ui.navigator.nodes.EosgiNodeType;

public class AbstractDistHandler {

  static final Logger LOGGER = Logger.getLogger(AbstractDistHandler.class.getName());

  protected String environmentName;

  protected IProject project;

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

  protected void processTreeSelection(final TreeSelection treeSelection) {
    Object firstElement = treeSelection.getFirstElement();
    if (firstElement == null) {
      return;
    }
    project = null;
    if (firstElement instanceof IProject) {
      project = (IProject) firstElement;
    }
  
    environmentName = null;
    if (firstElement instanceof EosgiNode) {
      EosgiNode eosgiNode = (EosgiNode) firstElement;
      if (EosgiNodeType.ENVIRONMENT == eosgiNode.getType()) {
        environmentName = eosgiNode.getName();
      }
      project = findParentProject(treeSelection.getPaths());
    }
  }

}
