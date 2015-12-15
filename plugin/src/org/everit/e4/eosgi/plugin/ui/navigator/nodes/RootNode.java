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

import java.util.Objects;
import java.util.Observable;

import org.everit.e4.eosgi.plugin.core.EOSGiContext;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeListener;

/**
 * Node class for EOSGi root.
 */
public class RootNode extends AbstractNode {

  private EOSGiContext context;

  /**
   * Constructor with {@link EOSGiContext} reference.
   *
   * @param context
   *          EOSGi context.
   * @param listener
   *          Node change listener.
   */
  public RootNode(final EOSGiContext context,
      final EosgiNodeChangeListener listener) {
    super(Messages.RootNode_everitEosgiContext, listener, null);
    Objects.requireNonNull(context, "context cannot be null"); //$NON-NLS-1$
    this.context = context;
    setListener(listener);
    outdated = true;
    this.context.delegateObserver(this);
  }

  @Override
  public void dispose() {
    context = null;
    super.dispose();
  }

  @Override
  public AbstractNode[] getChildren() {
    if (outdated) {
      children = new AbstractNode[] { new EnvironmentsNode(context, getListener()) };
      outdated = false;
    }
    return children.clone();
  }

  @Override
  public String getIcon() {
    return "icons/everit.gif"; //$NON-NLS-1$
  }

  @Override
  public String getText() {
    return getName();
  }

  @Override
  public void update(final Observable o, final Object arg) {
  }

}
