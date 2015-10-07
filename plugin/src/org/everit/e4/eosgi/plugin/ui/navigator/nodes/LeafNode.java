package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

import java.util.Observable;

import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeListener;

public class LeafNode extends AbstractNode {

  public LeafNode(String name, EosgiNodeChangeListener listener, String label) {
    super(name, listener, label);
  }

  @Override
  public AbstractNode[] getChildren() {
    return null;
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
  public void update(Observable o, Object arg) {
  }

}
