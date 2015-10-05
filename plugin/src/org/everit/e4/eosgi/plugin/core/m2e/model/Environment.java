package org.everit.e4.eosgi.plugin.core.m2e.model;

import java.util.List;
import java.util.Map;

import org.everit.e4.eosgi.plugin.core.dist.DistRunner;

public class Environment {
  private BundleSettings bundleSettings;

  private DistRunner distRunner;

  private String framework;

  private String id;

  private Map<String, String> systemProperties;

  private List<String> vmOptions;

  public BundleSettings getBundleSettings() {
    return bundleSettings;
  }

  public DistRunner getDistRunner() {
    return distRunner;
  }

  public String getFramework() {
    return framework;
  }

  public String getId() {
    return id;
  }

  public Map<String, String> getSystemProperties() {
    return systemProperties;
  }

  public List<String> getVmOptions() {
    return vmOptions;
  }

  public void setBundleSettings(final BundleSettings bundleSettings) {
    this.bundleSettings = bundleSettings;
  }

  public void setDistRunner(DistRunner distRunner) {
    this.distRunner = distRunner;
  }

  public void setFramework(final String framework) {
    this.framework = framework;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setSystemProperties(final Map<String, String> systemProperties) {
    this.systemProperties = systemProperties;
  }

  public void setVmOptions(final List<String> vmOptions) {
    this.vmOptions = vmOptions;
  }

  @Override
  public String toString() {
    return "Environment [bundleSettings=" + bundleSettings + ", framework=" + framework + ", id="
        + id + ", systemProperties=" + systemProperties + ", vmOptions=" + vmOptions
        + ", distRunner=" + distRunner + "]";
  }

}
