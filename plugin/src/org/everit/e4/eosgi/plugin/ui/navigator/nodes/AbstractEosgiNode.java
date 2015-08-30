package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeListener;

public abstract class AbstractEosgiNode {

  protected AbstractEosgiNode[] children;

  protected String icon;

  protected String label;

  private EosgiNodeChangeListener listener;

  protected String name;

  protected boolean outdated;

  protected String value;

  public abstract AbstractEosgiNode[] getChildren();

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
