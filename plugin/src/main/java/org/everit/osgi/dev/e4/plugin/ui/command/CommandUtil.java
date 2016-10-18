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
package org.everit.osgi.dev.e4.plugin.ui.command;

import java.util.Iterator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.everit.osgi.dev.e4.plugin.ExecutableEnvironment;

/**
 * Util methods to help processing commands and menu items.
 *
 */
public final class CommandUtil {

  /**
   * Gets the single selection from an evaluation context. E.g.: The selected item in case of a
   * popup menu.
   *
   * @param evaluationContext
   *          The evaluation context.
   * @return The single selection or <code>null</code> if no or multiple items were selected.
   */
  public static Object getSingleSelection(final IEvaluationContext evaluationContext) {
    if (evaluationContext == null) {
      return null;
    }

    Object activeMenuSelection = evaluationContext.getVariable(ISources.ACTIVE_MENU_SELECTION_NAME);
    if (!(activeMenuSelection instanceof IStructuredSelection)) {
      return null;
    }

    IStructuredSelection structuredSelection = (IStructuredSelection) activeMenuSelection;

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

  public static ExecutableEnvironment resolveExecutableEnvironment(final ExecutionEvent event) {
    Object applicationContext = event.getApplicationContext();
    if (!(applicationContext instanceof IEvaluationContext)) {
      throw new IllegalArgumentException(
          "Parameter should be instance of IEvaluationContext: " + applicationContext);
    }
    Object singleSelection =
        CommandUtil.getSingleSelection((IEvaluationContext) applicationContext);

    if (!(singleSelection instanceof ExecutableEnvironment)) {
      throw new IllegalArgumentException(
          "Selected item should be instance of EOSGiProject: " + singleSelection);
    }

    final ExecutableEnvironment executableEnvironment = (ExecutableEnvironment) singleSelection;
    return executableEnvironment;
  }

  private CommandUtil() {
  }
}
