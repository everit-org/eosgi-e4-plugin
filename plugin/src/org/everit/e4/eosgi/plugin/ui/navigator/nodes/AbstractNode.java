package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

import java.util.Observer;

import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeListener;

public abstract class AbstractNode implements Observer {

  protected AbstractNode[] children;

  protected String icon;

  protected String label;

  private EosgiNodeChangeListener listener;

  protected String name;

  protected boolean outdated;

  protected String value;

  public AbstractNode(String name, EosgiNodeChangeListener listener, String label) {
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
