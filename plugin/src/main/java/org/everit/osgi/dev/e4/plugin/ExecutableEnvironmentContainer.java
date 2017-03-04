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
package org.everit.osgi.dev.e4.plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Helper class to handle multiple {@link ExecutableEnvironment}s in a way that they are searchable.
 *
 */
public class ExecutableEnvironmentContainer {

  private final Set<ExecutableEnvironment> executableEnvironments;

  public ExecutableEnvironmentContainer(
      final Collection<ExecutableEnvironment> executableEnvironments) {
    this.executableEnvironments =
        Collections.unmodifiableSet(new TreeSet<>(executableEnvironments));
  }

  public Set<ExecutableEnvironment> getExecutableEnvironments() {
    return executableEnvironments;
  }
}
