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
package org.everit.e4.eosgi.plugin.core;

import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Class for maven changes. Containing the build directory and the configuration at now.
 */
public class ContextChange {
  public String buildDirectory;

  public Xpp3Dom configuration;

  public ContextChange buildDirectory(final String buildDirectory) {
    this.buildDirectory = buildDirectory;
    return this;
  }

  public ContextChange configuration(final Xpp3Dom configuration) {
    this.configuration = configuration;
    return this;
  }

  @Override
  public String toString() {
    return "ContextChange [buildDirectory=" + buildDirectory + ", configuration=" + configuration
        + "]";
  }

}
