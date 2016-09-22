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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.everit.osgi.dev.dist.util.attach.EOSGiVMManager;
import org.everit.osgi.dev.dist.util.attach.EnvironmentRuntimeInfo;
import org.everit.osgi.dev.e4.plugin.EOSGiEclipsePlugin;
import org.everit.osgi.dev.e4.plugin.ExecutableEnvironment;
import org.everit.osgi.dev.e4.plugin.ui.command.CommandUtil;

public class EOSGiProjectPopupMenu extends ExtensionContributionFactory {

  public static final String COMMAND_ID_PREFIX = "org.everit.osgi.dev.e4.plugin.command.";

  public EOSGiProjectPopupMenu() {
  }

  private void addMenuItem(final String label, final String commandId,
      final IContributionRoot additions, final IServiceLocator serviceLocator,
      final Map<Object, Object> parameters) {
    CommandContributionItem menuItem =
        createMenuItem(label, commandId, serviceLocator, parameters);
    additions.addContributionItem(menuItem, Expression.TRUE);
  }

  private void addMenuItemsForEnvironment(final ExecutableEnvironment executableEnvironment,
      final IContributionRoot additions, final IServiceLocator serviceLocator) {
    EOSGiVMManager eosgiVMManager =
        EOSGiEclipsePlugin.getDefault().getEOSGiManager().getEosgiVMManager();
    eosgiVMManager.refresh();

    final Set<EnvironmentRuntimeInfo> runtimeInformations =
        eosgiVMManager.getRuntimeInformations(executableEnvironment.getEnvironmentId(),
            executableEnvironment.getRootFolder());

    if (runtimeInformations.isEmpty()) {
      addMenuItem("Start", COMMAND_ID_PREFIX + "start", additions, serviceLocator, null);
      addMenuItem("Debug", COMMAND_ID_PREFIX + "debug", additions, serviceLocator, null);
    } else if (runtimeInformations.size() == 1) {
      addMenuItem("Dist", COMMAND_ID_PREFIX + "dist", additions, serviceLocator, null);

      String vmId = runtimeInformations.iterator().next().virtualMachineId;
      Map<Object, Object> parameters = new HashMap<>();
      parameters.put(COMMAND_ID_PREFIX + "stopCommand.vmId", vmId);

      addMenuItem("Stop", COMMAND_ID_PREFIX + "stop", additions, serviceLocator,
          parameters);
    } else {
      MenuManager stopMainMenu = new MenuManager();
      stopMainMenu.setMenuText("Stop");

      for (EnvironmentRuntimeInfo runtimeInfo : runtimeInformations) {
        String vmId = runtimeInfo.virtualMachineId;
        Map<Object, Object> parameters = new HashMap<>();
        parameters.put(COMMAND_ID_PREFIX + "stopCommand.vmId", vmId);

        CommandContributionItem stopMenuItem =
            createMenuItem(vmId, COMMAND_ID_PREFIX + "stop", serviceLocator, parameters);

        stopMainMenu.add(stopMenuItem);

      }

      additions.addContributionItem(stopMainMenu, Expression.TRUE);
    }
  }

  @Override
  public void createContributionItems(final IServiceLocator serviceLocator,
      final IContributionRoot additions) {

    Object selectionObject = getSingleSelection(serviceLocator);
    if (selectionObject instanceof ExecutableEnvironment) {
      addMenuItemsForEnvironment((ExecutableEnvironment) selectionObject, additions,
          serviceLocator);
    }

  }

  private CommandContributionItem createMenuItem(final String label, final String commandId,
      final IServiceLocator serviceLocator, final Map<?, ?> parameters) {
    CommandContributionItemParameter parameter = new CommandContributionItemParameter(
        serviceLocator, null, commandId, CommandContributionItem.STYLE_PUSH);

    parameter.label = label;
    parameter.visibleEnabled = true;
    if (parameters != null) {
      parameter.parameters = parameters;
    }
    return new CommandContributionItem(parameter);
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
