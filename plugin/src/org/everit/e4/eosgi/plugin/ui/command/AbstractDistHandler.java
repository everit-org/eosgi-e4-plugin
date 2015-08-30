package org.everit.e4.eosgi.plugin.ui.command;

import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.everit.e4.eosgi.plugin.ui.navigator.nodes.EnvironmentNode;

public class AbstractDistHandler {

  static final Logger LOGGER = Logger.getLogger(AbstractDistHandler.class.getName());

  protected String environmentName;

  protected IProject project;

  private IProject findParentProject(final TreePath[] treePaths) {
    if (treePaths == null || treePaths.length == 0) {
      return null;
    }

    for (TreePath treePath : treePaths) {
      int segmentCount = treePath.getSegmentCount();
      for (int i = 0; i < segmentCount; i++) {
        Object segment = treePath.getSegment(i);
        if (segment instanceof IProject) {
          return (IProject) segment;

        }
      }
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
    if (firstElement instanceof EnvironmentNode) {
      EnvironmentNode eosgiNode = (EnvironmentNode) firstElement;
      environmentName = eosgiNode.getName();
      project = findParentProject(treeSelection.getPaths());
    }
  }

}
