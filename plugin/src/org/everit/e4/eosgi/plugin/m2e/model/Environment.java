package org.everit.e4.eosgi.plugin.m2e.model;

import java.util.List;
import java.util.Map;

public class Environment {
    private String id;
    private String framework;
    private Map<String, String> systemProperties;
    private List<String> vmOptions;
    private BundleSettings bundleSettings;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(Map<String, String> systemProperties) {
        this.systemProperties = systemProperties;
    }

    @Override
    public String toString() {
        return "Environment [id=" + id + ", framework=" + framework + ", systemProperties=" + systemProperties
                + ", vmOptions=" + vmOptions + ", bundleSettings=" + bundleSettings + "]";
    }

    public List<String> getVmOptions() {
        return vmOptions;
    }

    public void setVmOptions(List<String> vmOptions) {
        this.vmOptions = vmOptions;
    }

    public BundleSettings getBundleSettings() {
        return bundleSettings;
    }

    public void setBundleSettings(BundleSettings bundleSettings) {
        this.bundleSettings = bundleSettings;
    }

}
