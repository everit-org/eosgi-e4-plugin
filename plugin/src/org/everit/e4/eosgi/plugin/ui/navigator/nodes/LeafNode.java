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

import java.util.Observable;

import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeListener;

/**
 * Leaf node.
 */
public class LeafNode extends AbstractNode {

  public LeafNode(final String name, final EosgiNodeChangeListener listener, final String label) {
    super(name, listener, label);
  }

  @Override
  public AbstractNode[] getChildren() {
    return NO_CHILDREN;
  }

  @Override
  public String getIcon() {
    return icon;
  }

  @Override
  public String getText() {
    String text = getName();
    if (getLabel() != null) {
      text += " (" + getLabel() + ")";
    }
    return text;
  }

  @Override
  public void update(final Observable o, final Object arg) {
  }

}
