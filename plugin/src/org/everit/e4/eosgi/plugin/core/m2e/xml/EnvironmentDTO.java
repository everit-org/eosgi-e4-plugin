package org.everit.e4.eosgi.plugin.core.m2e.xml;

import java.util.List;
import java.util.Map;

import org.everit.e4.eosgi.plugin.core.m2e.model.BundleSettings;

/**
 * DTO class for an environment.
 */
public class EnvironmentDTO {
  public BundleSettings bundleSettings;

  public String framework;

  public String id;

  public Map<String, String> systemProperties;

  public List<String> vmOptions;

  public EnvironmentDTO bundleSettings(final BundleSettings bundleSettings) {
    this.bundleSettings = bundleSettings;
    return this;
  }

  public EnvironmentDTO framework(final String framework) {
    this.framework = framework;
    return this;
  }

  public EnvironmentDTO id(final String id) {
    this.id = id;
    return this;
  }

  public EnvironmentDTO systemProperties(final Map<String, String> systemProperties) {
    this.systemProperties = systemProperties;
    return this;
  }

  @Override
  public String toString() {
    return "EnvironmentDTO [bundleSettings=" + bundleSettings + ", framework=" + framework + ", id="
        + id + ", systemProperties=" + systemProperties + ", vmOptions=" + vmOptions + "]";
  }

  public EnvironmentDTO vmOptions(final List<String> vmOptions) {
    this.vmOptions = vmOptions;
    return this;
  }

}
