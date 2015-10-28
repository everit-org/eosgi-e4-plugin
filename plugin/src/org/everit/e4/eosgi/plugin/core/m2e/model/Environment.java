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

import java.util.Observable;

import org.everit.e4.eosgi.plugin.core.EventType;
import org.everit.e4.eosgi.plugin.core.ModelChangeEvent;
import org.everit.e4.eosgi.plugin.core.m2e.xml.EnvironmentDTO;
import org.everit.e4.eosgi.plugin.ui.dto.EnvironmentNodeDTO;

/**
 * Model class for storing environment informations.
 */
public class Environment extends Observable {
  private String framework;

  private String id;

  private boolean outdated;

  public String getFramework() {
    return framework;
  }

  public String getId() {
    return id;
  }

  public boolean isOutdated() {
    return outdated;
  }

  // /**
  // * Set the current {@link DistRunner} instance.<br>
  // * Notifing the observers.
  // *
  // * @param distRunner
  // * dist runner instance.
  // */
  // public void setDistRunner(final DistRunner distRunner) {
  // this.distRunner = distRunner;
  // setChanged();
  // notifyObservers(new ModelChangeEvent().eventType(EventType.ENVIRONMENT)
  // .arg(new EnvironmentNodeDTO().id(id).distStatus(DistStatus.STOPPED).outdated(false)
  // .observable((Observable) distRunner)));
  // }

  public void setFramework(final String framework) {
    this.framework = framework;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setOutdated(final boolean outdated) {
    this.outdated = outdated;
  }

  @Override
  public String toString() {
    return "Environment [framework=" + framework + ", id=" + id + ", outdated=" + outdated + "]";
  }

  /**
   * Update the environemnt state by {@link EnvironmentDTO}.
   *
   * @param environmentDTO
   *          environemnt DTO instance.
   */
  public void update(final EnvironmentDTO environmentDTO) {
    if (!id.equals(environmentDTO.id)) {
      id = environmentDTO.id;
      outdated = true;
      setChanged();
    }
    if (!framework.equals(environmentDTO.framework)) {
      framework = environmentDTO.framework;
      outdated = true;
      setChanged();
    }
    EnvironmentNodeDTO environmentNodeDTO = new EnvironmentNodeDTO().id(id).outdated(outdated);
    notifyObservers(
        new ModelChangeEvent()
            .eventType(EventType.ENVIRONMENT)
            .arg(environmentNodeDTO));
  }

}
