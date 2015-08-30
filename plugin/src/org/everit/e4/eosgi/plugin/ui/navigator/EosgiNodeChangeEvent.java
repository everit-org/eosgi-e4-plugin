package org.everit.e4.eosgi.plugin.ui.navigator;

import org.everit.e4.eosgi.plugin.ui.navigator.nodes.AbstractEosgiNode;

public class EosgiNodeChangeEvent {

  private AbstractEosgiNode node;

  public EosgiNodeChangeEvent(final AbstractEosgiNode node) {
    super();
    this.node = node;
  }

  public AbstractEosgiNode getNode() {
    return node;
  }
}
