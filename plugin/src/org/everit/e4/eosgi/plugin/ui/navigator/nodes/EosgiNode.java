package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

import java.util.Arrays;

public class EosgiNode {
  private String name;
  private String label;
  private String value;

  private EosgiNodeType type;

  private EosgiNode[] childs;

  public EosgiNode[] getChilds() {
    return childs;
  }

  public String getLabel() {
    return label;
  }

  public String getName() {
    return name;
  }

  public EosgiNodeType getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  public boolean hasLabel() {
    return (label != null) && !label.isEmpty();
  }

  public boolean hasValue() {
    return (value != null) && !value.isEmpty();
  }

  public void setChilds(final EosgiNode[] childs) {
    this.childs = childs;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public void setName(final String label) {
    name = label;
  }

  public void setType(final EosgiNodeType type) {
    this.type = type;
  }

  public void setValue(final String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "EosgiNode [name=" + name + ", label=" + label + ", value=" + value + ", type=" + type
        + ", childs="
        + Arrays.toString(childs) + "]";
  }

}
