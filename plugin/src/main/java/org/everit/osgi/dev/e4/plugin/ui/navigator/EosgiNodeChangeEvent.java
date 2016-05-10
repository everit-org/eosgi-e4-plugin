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

import org.everit.osgi.dev.e4.plugin.ui.navigator.nodes.AbstractNode;

/**
 * Event class for node changes.
 */
public class EosgiNodeChangeEvent {

  private AbstractNode node;

  public EosgiNodeChangeEvent(final AbstractNode node) {
    super();
    this.node = node;
  }

  public AbstractNode getNode() {
    return node;
  }
}
