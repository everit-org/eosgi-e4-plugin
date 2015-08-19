package org.everit.e4.eosgi.plugin.m2e.model;

import java.util.Map;

public class Bundle {
  private Map<String, String> bundlePropertiesMap;

  public Map<String, String> getBundlePropertiesMap() {
    return bundlePropertiesMap;
  }

  public void setBundlePropertiesMap(final Map<String, String> bundlePropertiesMap) {
    this.bundlePropertiesMap = bundlePropertiesMap;
  }

  @Override
  public String toString() {
    return "Bundle [bundlePropertiesMap=" + bundlePropertiesMap + "]";
  }

}
