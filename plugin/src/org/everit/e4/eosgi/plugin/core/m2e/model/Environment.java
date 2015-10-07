/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.e4.eosgi.plugin.core.m2e.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.everit.e4.eosgi.plugin.core.dist.DistRunner;

/**
 * Model class for storing environment informations.
 */
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

  public Optional<DistRunner> getDistRunner() {
    return Optional.ofNullable(distRunner);
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

  public void setDistRunner(final DistRunner distRunner) {
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
    return "Environment [bundleSettings=" + bundleSettings + ", distRunner=" + distRunner
        + ", framework=" + framework + ", id=" + id + ", systemProperties=" + systemProperties
        + ", vmOptions=" + vmOptions + "]";
  }

}
