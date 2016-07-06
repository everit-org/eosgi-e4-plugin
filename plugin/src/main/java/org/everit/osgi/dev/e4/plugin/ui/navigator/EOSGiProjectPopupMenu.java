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

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.everit.osgi.dev.e4.plugin.EOSGiProject;
import org.everit.osgi.dev.e4.plugin.ui.command.CommandUtil;

public class EOSGiProjectPopupMenu extends ExtensionContributionFactory {

  public EOSGiProjectPopupMenu() {
  }

  private void addMenuItemsForEOSGiProject(final EOSGiProject eosgiProject,
      final IServiceLocator serviceLocator,
      final IContributionRoot additions) {
    CommandContributionItemParameter parameter = new CommandContributionItemParameter(
        serviceLocator, null, "org.everit.osgi.dev.e4.plugin.command.start",
        CommandContributionItem.STYLE_PUSH);
    parameter.label = "Start";
    parameter.visibleEnabled = true;
    additions.addContributionItem(new CommandContributionItem(parameter), Expression.TRUE);

  }

  @Override
  public void createContributionItems(final IServiceLocator serviceLocator,
      final IContributionRoot additions) {

    Object selectionObject = getSingleSelection(serviceLocator);
    if (selectionObject instanceof EOSGiProject) {
      addMenuItemsForEOSGiProject((EOSGiProject) selectionObject, serviceLocator, additions);
    }

  }

  private Object getSingleSelection(final IServiceLocator serviceLocator) {
    IHandlerService handlerService = serviceLocator.getService(IHandlerService.class);

    if (handlerService == null) {
      return null;
    }

    IEvaluationContext evaluationContext = handlerService.getCurrentState();

    return CommandUtil.getSingleSelection(evaluationContext);
  }

  @Override
  public void setInitializationData(final IConfigurationElement config, final String propertyName,
      final Object data)
      throws CoreException {
    super.setInitializationData(config, propertyName, data);
  }

}
