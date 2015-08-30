package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

public class LeafNode extends AbstractEosgiNode {

  @Override
  public AbstractEosgiNode[] getChildren() {
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

}
