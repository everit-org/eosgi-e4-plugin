package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

public enum EosgiNodeType {
  BUNDLE("BUNDLE"), BUNDLE_PROJECTS("BUNDLE_PROJECTS"), BUNDLE_SETTINGS("BUNDLE_SETTINGS"), CONFIGURATION("CONFIGURATION"), ENVIRONMENT(
          "ENVIRONMENT"), ENVIRONMENTS("ENVIRONMENTS"), KEY_VALUE("KEY_VALUE"), SYSTEM_PROPS(
              "SYSTEM_PROPS"), VALUE("VALUE");

  private String value;

  EosgiNodeType(final String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
