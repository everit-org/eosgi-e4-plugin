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
package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

import java.util.Observable;
import java.util.Observer;

import org.everit.e4.eosgi.plugin.core.EOSGiContext;
import org.everit.e4.eosgi.plugin.core.EventType;
import org.everit.e4.eosgi.plugin.core.ModelChangeEvent;
import org.everit.e4.eosgi.plugin.core.dist.DistRunner;
import org.everit.e4.eosgi.plugin.core.dist.DistStatus;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeEvent;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeListener;

/**
 * Project explorer node for show environment.
 */
public class EnvironmentNode extends AbstractNode implements Observer {

  private EOSGiContext context;

  private DistStatus distStatus = DistStatus.NONE;

  private String environmentId;

  /**
   * Constructor.
   *
   * @param context
   *          listener for model changes.
   * @param environmentId
   *          id of the environment.
   * @param eosgiNodeChangeListener
   *          node change listener.
   */
  public EnvironmentNode(final EOSGiContext context, final String environmentId,
      final EosgiNodeChangeListener eosgiNodeChangeListener) {
    super(environmentId, eosgiNodeChangeListener, null);
    this.context = context;
    this.environmentId = environmentId;
    context.delegateObserver(this);
  }

  @Override
  public void dispose() {
    context.removeObserver(this);
    super.dispose();
  }

  @Override
  public AbstractNode[] getChildren() {
    return null;
  }

  public DistStatus getDistStatus() {
    return distStatus;
  }

  public String getDistStatusString() {
    return " " + distStatus.name().toLowerCase();
  }

  @Override
  public String getIcon() {
    return "icons/ExecutionEnvironment.gif";
  }

  @Override
  public String getText() {
    if (getLabel() == null) {
      return getName();
    } else {
      return getName() + getLabel();
    }
  }

  public boolean isOutdated() {
    return outdated;
  }

  @Override
  public String toString() {
    return "EnvironmentNode [context=" + context + ", distStatus=" + distStatus + ", environmentId="
        + environmentId + "]";
  }

  @Override
  public void update(final Observable o, final Object arg) {
    ModelChangeEvent event = null;
    if ((arg != null) && (arg instanceof ModelChangeEvent)) {
      event = (ModelChangeEvent) arg;
    }

    if ((event != null) && (event.eventType == EventType.ENVIRONMENT)) {
      String environmentName = (String) event.arg;
      if (environmentName.equals(environmentId)) {
        outdated = true;
        updateDistStatus();
        getListener().nodeChanged(new EosgiNodeChangeEvent(this));
      }
    }
  }

  private void updateDistStatus() {
    DistRunner distRunner = context.runner(environmentId).get();
    if (distRunner == null) {
      distStatus = DistStatus.NONE;
    } else {
      if (distRunner.isRunning()) {
        distStatus = DistStatus.RUNNING;
      } else {
        distStatus = DistStatus.STOPPED;
      }
      if (distRunner instanceof Observable) {
        ((Observable) distRunner).addObserver(this);
      }
    }
  }

}
