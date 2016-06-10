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
package org.everit.osgi.dev.e4.plugin.ui.navigator;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.everit.osgi.dev.e4.plugin.EOSGiEclipsePlugin;
import org.everit.osgi.dev.e4.plugin.EOSGiProject;

/**
 * TreeNodeContentProvider implementation for manage the EOSGI nodes in ProjectExplorer.
 */
public class DistContentProvider extends TreeNodeContentProvider {

  @Override
  public Object[] getChildren(final Object parentElement) {
    if (parentElement instanceof IProject) {
      EOSGiProject eosgiProject =
          EOSGiEclipsePlugin.getDefault().getEOSGiManager().get((IProject) parentElement);

      if (eosgiProject == null) {
        return new Object[0];
      }
      return new Object[] { eosgiProject };
    } else if (parentElement instanceof EOSGiProject) {
      return ((EOSGiProject) parentElement).getEnvironmentIds().toArray(new String[0]);
    } else {
      return super.getChildren(parentElement);
    }
  }

  @Override
  public boolean hasChildren(final Object element) {
    return element instanceof IProject || element instanceof EOSGiProject;
  }
}
