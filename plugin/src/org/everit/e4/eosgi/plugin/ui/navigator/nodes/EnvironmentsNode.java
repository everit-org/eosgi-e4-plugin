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
package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.everit.e4.eosgi.plugin.core.EOSGiContext;
import org.everit.e4.eosgi.plugin.core.EventType;
import org.everit.e4.eosgi.plugin.core.ModelChangeEvent;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeEvent;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeListener;

/**
 * Project explorer node for show environments.
 */
public class EnvironmentsNode extends AbstractNode implements Observer {

  private EOSGiContext context;

  /**
   * Constructor.
   *
   * @param context
   *          relevant {@link EOSGiContext} reference.
   * @param listener
   *          listener for changes.
   */
  public EnvironmentsNode(final EOSGiContext context, final EosgiNodeChangeListener listener) {
    super("Environments", listener, null);
    this.context = context;
    outdated = true;
    context.delegateObserver(this);
  }

  private void disposeChildren() {
    if (children != null) {
      for (AbstractNode child : children) {
        child.dispose();
      }
    }
  }

  @Override
  public AbstractNode[] getChildren() {
    if (outdated) {
      List<String> environments = context.environmentNames();
      disposeChildren();
      List<EnvironmentNode> nodes = new ArrayList<>();
      for (String environment : environments) {
        EnvironmentNode node = new EnvironmentNode(context, environment, getListener());
        nodes.add(node);
      }
      children = nodes.toArray(new EnvironmentNode[] {});
      outdated = false;
    }
    return children;
  }

  @Override
  public String getIcon() {
    return "icons/console_view.gif";
  }

  @Override
  public String getText() {
    if (getLabel() == null) {
      return getName();
    } else {
      return getName() + getLabel();
    }
  }

  @Override
  public String toString() {
    return "EnvironmentsNode [context=" + context + "]";
  }

  @Override
  public void update(final Observable o, final Object arg) {
    ModelChangeEvent event = null;
    if ((arg != null) && (arg instanceof ModelChangeEvent)) {
      event = (ModelChangeEvent) arg;
    }
    if ((event != null) && (event.eventType == EventType.ENVIRONMENTS) && (event.arg != null)) {
      EOSGiContext context = (EOSGiContext) event.arg;
      if (this.context.equals(context)) {
        outdated = true;
        getListener().nodeChanged(new EosgiNodeChangeEvent(this));
      }
    }
  }

}
