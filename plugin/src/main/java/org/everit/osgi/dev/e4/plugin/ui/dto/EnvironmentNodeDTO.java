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
package org.everit.osgi.dev.e4.plugin.ui.dto;

import java.util.Observable;

/**
 * DTO class for update UI about environment.
 */
public class EnvironmentNodeDTO {

  public String id;

  public Observable observable;

  public Boolean outdated;

  public EnvironmentNodeDTO id(final String id) {
    this.id = id;
    return this;
  }

  public EnvironmentNodeDTO observable(final Observable observable) {
    this.observable = observable;
    return this;
  }

  public EnvironmentNodeDTO outdated(final Boolean outdated) {
    this.outdated = outdated;
    return this;
  }

  @Override
  public String toString() {
    return "EnvironmentNodeDTO [id=" + id + ", observable=" + observable + ", outdated=" + outdated
        + "]";
  }

}
