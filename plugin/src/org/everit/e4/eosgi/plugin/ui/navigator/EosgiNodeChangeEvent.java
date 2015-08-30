package org.everit.e4.eosgi.plugin.ui.navigator;

import org.everit.e4.eosgi.plugin.ui.navigator.nodes.AbstractNode;

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
