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
package org.everit.e4.eosgi.plugin.ui.command;

import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.everit.e4.eosgi.plugin.ui.navigator.nodes.EnvironmentNode;

/**
 * Abstract handler for EOSGi project explorer context menus.
 */
public class AbstractDistHandler {

  static final Logger LOGGER = Logger.getLogger(AbstractDistHandler.class.getName());

  protected String environmentName;

  protected IProject project;

  private IProject findParentProject(final TreePath[] treePaths) {
    if ((treePaths == null) || (treePaths.length == 0)) {
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

  /**
   * Process the TreeSelection element.
   *
   * @param treeSelection
   *          processed element.
   */
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
