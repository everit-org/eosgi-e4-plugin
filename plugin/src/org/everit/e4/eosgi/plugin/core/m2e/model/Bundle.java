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

import java.util.Map;

/**
 * Model class for storing bundle information about an environment.
 */
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
