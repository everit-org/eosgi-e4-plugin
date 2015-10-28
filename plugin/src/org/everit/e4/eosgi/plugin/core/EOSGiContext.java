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

import java.util.List;
import java.util.Observer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectChangedListener;
import org.everit.e4.eosgi.plugin.ui.dto.EnvironmentNodeDTO;

/**
 * Interface for EOSGI context functionality.
 */
public interface EOSGiContext extends IMavenProjectChangedListener {

  /**
   * Capability for delegating an observer instance to the manager class.
   *
   * @param observer
   *          {@link Observer} instance.
   */
  void delegateObserver(Observer observer);

  void dispose();

  /**
   * Fetch the current environment informations.
   *
   * @return List of the environment change DTO.
   */
  List<EnvironmentNodeDTO> fetchEnvironments();

  void forcedStop(String environmentId);

  /**
   * Generate a dist for this project by the given environment name. All argumentum must be not
   * null!
   *
   * @param environmentId
   *          id of the environment.
   * @param monitor
   *          monitor for prigress bar (need for m2e).
   */
  void generate(String environmentId, IProgressMonitor monitor);

  /**
   * Refreshing the project state by the {@link ContextChange} instance.
   *
   * @param contextChange
   *          DTO for changes.
   */
  void refresh(ContextChange contextChange);

  /**
   * Delegate the call for the manager class. Remove the given observer instance from the observable
   * instance.
   *
   * @param observer
   *          {@link Observer} instance.
   */
  void removeObserver(Observer observer);

  // /**
  // * Get an {@link DistRunner} instance by the environment id. The dist runner exists is not sure,
  // * so return an {@link Optional} instance.
  // *
  // * @param environmentId
  // * name of the envronment.
  // * @return Optional DistRunner.
  // */
  // Optional<DistRunner> runner(String environmentId);
}
