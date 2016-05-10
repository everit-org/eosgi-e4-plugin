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
package org.everit.osgi.dev.e4.plugin.core;

import org.everit.osgi.dev.e4.plugin.core.m2e.xml.EnvironmentsDTO;

/**
 * Class for maven changes. Containing the build directory and the configuration at now.
 */
public class ContextChange {
  public String buildDirectory;

  public EnvironmentsDTO configuration;

  public boolean enabledDisabled;

  public ContextChange buildDirectory(final String buildDirectory) {
    this.buildDirectory = buildDirectory;
    return this;
  }

  public ContextChange enabledDisabled(final boolean enabledDisabled) {
    this.enabledDisabled = enabledDisabled;
    return this;
  }

  public ContextChange configuration(final EnvironmentsDTO configuration) {
    this.configuration = configuration;
    return this;
  }

  @Override
  public String toString() {
    return "ContextChange [buildDirectory=" + buildDirectory + ", configuration=" + configuration
        + "]";
  }

}
