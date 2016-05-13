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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;

/**
 * Storing and managing {@link EOSGiProject} instances.
 */
public class EOSGiProjectManager {

  private final Map<IProject, EOSGiProject> eosgiProjects = new HashMap<>();

  public EOSGiProject get(final Object key) {
    return eosgiProjects.get(key);
  }

  public EOSGiProject put(final IProject key, final EOSGiProject value) {
    return eosgiProjects.put(key, value);
  }

  public void remove(final IProject project) {
    eosgiProjects.remove(project);
  }

}
