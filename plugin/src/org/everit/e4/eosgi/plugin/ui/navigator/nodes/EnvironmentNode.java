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
import java.util.Optional;

import org.everit.e4.eosgi.plugin.core.EOSGiContext;
import org.everit.e4.eosgi.plugin.core.EventType;
import org.everit.e4.eosgi.plugin.core.ModelChangeEvent;
import org.everit.e4.eosgi.plugin.ui.dto.EnvironmentNodeDTO;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeEvent;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeListener;

/**
 * Project explorer node for show environment.
 */
public class EnvironmentNode extends AbstractNode {

  private EOSGiContext context;

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
    // context.delegateObserver(this); // doesn't the context the observable
  }

  private void applyPresentedArgs(final EnvironmentNodeDTO args) {
    Optional.ofNullable(args.outdated).ifPresent(outdated -> {
      if (outdated) {
        setLabel(" (outdated)");
      } else {
        setLabel(null);
      }
    });

    Optional.ofNullable(args.observable).ifPresent(observable -> {
      observable.addObserver(this);
    });
  }

  @Override
  public void dispose() {
    context = null;
    super.dispose();
  }

  @Override
  public AbstractNode[] getChildren() {
    return NO_CHILDREN;
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
    return "EnvironmentNode [context=" + context + ", environmentId=" + environmentId + "]";
  }

  @Override
  public void update(final Observable o, final Object arg) {
    if (context == null) {
      return;
    }

    ModelChangeEvent event = null;
    if ((arg != null) && (arg instanceof ModelChangeEvent)) {
      event = (ModelChangeEvent) arg;
    }

    if ((event != null) && (event.eventType == EventType.ENVIRONMENT)
        && (event.arg instanceof EnvironmentNodeDTO)) {
      EnvironmentNodeDTO args = (EnvironmentNodeDTO) event.arg;
      if (environmentId.equals(args.id)) {
        applyPresentedArgs(args);
        changed(new EosgiNodeChangeEvent(this));
      }
    }
  }

}
