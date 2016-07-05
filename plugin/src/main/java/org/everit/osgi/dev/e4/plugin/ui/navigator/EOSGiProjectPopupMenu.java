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

import java.util.Iterator;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.everit.osgi.dev.e4.plugin.EOSGiProject;

public class EOSGiProjectPopupMenu extends ExtensionContributionFactory {

  public EOSGiProjectPopupMenu() {
  }

  private void addMenuItemsForEOSGiProject(final EOSGiProject selectionObject,
      final IContributionRoot additions) {
    // TODO Auto-generated method stub
    additions.addContributionItem(new CommandContributionItem(), Expression.TRUE);
  }

  @Override
  public void createContributionItems(final IServiceLocator serviceLocator,
      final IContributionRoot additions) {

    Object selectionObject = getSingleSelection(serviceLocator);
    if (selectionObject instanceof EOSGiProject) {
      addMenuItemsForEOSGiProject((EOSGiProject) selectionObject, additions);
    }

  }

  private Object getSingleSelection(final IServiceLocator serviceLocator) {
    IStructuredSelection structuredSelection = getStructuredSelection(serviceLocator);
    if (structuredSelection == null) {
      return null;
    }

    Iterator<?> iterator = structuredSelection.iterator();

    Object selectionObject = null;
    if (iterator.hasNext()) {
      selectionObject = iterator.next();
    }

    if (iterator.hasNext()) {
      return null;
    }

    return selectionObject;
  }

  private IStructuredSelection getStructuredSelection(final IServiceLocator serviceLocator) {
    IHandlerService handlerService = serviceLocator.getService(IHandlerService.class);

    if (handlerService == null) {
      return null;
    }

    IEvaluationContext currentState = handlerService.getCurrentState();

    if (currentState == null) {
      return null;
    }

    Object activeMenuSelection = currentState.getVariable(ISources.ACTIVE_MENU_SELECTION_NAME);
    if (!(activeMenuSelection instanceof IStructuredSelection)) {
      return null;
    }

    return (IStructuredSelection) activeMenuSelection;
  }

  @Override
  public void setInitializationData(final IConfigurationElement config, final String propertyName,
      final Object data)
      throws CoreException {
    super.setInitializationData(config, propertyName, data);
  }

}
