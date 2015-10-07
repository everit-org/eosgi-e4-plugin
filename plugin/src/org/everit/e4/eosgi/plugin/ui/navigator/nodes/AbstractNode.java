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

import java.util.Observer;

import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeListener;

/**
 * Abstract node for EOSGi nodes.
 */
public abstract class AbstractNode implements Observer {
  protected static final AbstractNode[] NO_CHILDREN = new AbstractNode[] {};

  protected AbstractNode[] children = NO_CHILDREN;

  protected String icon;

  protected String label;

  private EosgiNodeChangeListener listener;

  protected String name;

  protected boolean outdated;

  protected String value;

  /**
   * Constructor with name and a change listener.
   * 
   * @param name
   *          name of the node.
   * @param listener
   *          listener for the node.
   * @param label
   *          label for the node.
   */
  public AbstractNode(final String name, final EosgiNodeChangeListener listener,
      final String label) {
    super();
    this.name = name;
    this.listener = listener;
    this.label = label;
  }

  public void dispose() {
    listener = null;
  }

  public abstract AbstractNode[] getChildren();

  public abstract String getIcon();

  public String getLabel() {
    return label;
  }

  public EosgiNodeChangeListener getListener() {
    return listener;
  }

  public String getName() {
    return name;
  }

  public abstract String getText();

  public String getValue() {
    return value;
  }

  public void setIcon(final String icon) {
    this.icon = icon;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public void setListener(final EosgiNodeChangeListener listener) {
    this.listener = listener;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setValue(final String value) {
    this.value = value;
  }

}
