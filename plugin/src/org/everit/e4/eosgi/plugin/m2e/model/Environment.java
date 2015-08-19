package org.everit.e4.eosgi.plugin.m2e.model;

import java.util.List;
import java.util.Map;

public class Environment {
  private String id;

  private String framework;

  private Map<String, String> systemProperties;

  private List<String> vmOptions;

  private BundleSettings bundleSettings;

  public String getExecutionString() {
    return EosgiProject.DIST_FOLDER + this.id + EosgiProject.DIST_BIN + EosgiProject.LINUX_START;
  }

  public BundleSettings getBundleSettings() {
    return bundleSettings;
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
    return "Environment [id=" + id + ", framework=" + framework + ", systemProperties="
        + systemProperties
        + ", vmOptions=" + vmOptions + ", bundleSettings=" + bundleSettings + "]";
  }

}
