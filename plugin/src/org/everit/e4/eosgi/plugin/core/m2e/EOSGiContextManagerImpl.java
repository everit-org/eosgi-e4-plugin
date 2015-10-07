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
package org.everit.e4.eosgi.plugin.core.m2e;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.everit.e4.eosgi.plugin.core.ContextChange;
import org.everit.e4.eosgi.plugin.core.EOSGiContext;
import org.everit.e4.eosgi.plugin.core.EOSGiContextManager;
import org.everit.e4.eosgi.plugin.ui.EOSGiLog;
import org.everit.e4.eosgi.plugin.ui.nature.EosgiNature;

/**
 * Storing and managing {@link EOSGiContext} instances.
 */
public class EOSGiContextManagerImpl implements EOSGiContextManager {

  private EOSGiLog log;

  private Map<IProject, EOSGiContext> projectContexts = new ConcurrentHashMap<>();

  private IMavenProjectRegistry projectRegistry;

  public EOSGiContextManagerImpl(final EOSGiLog log) {
    this.log = log;
    projectRegistry = MavenPlugin.getMavenProjectRegistry();
  }

  @Override
  public void dispose() {
    projectContexts.forEach((t, u) -> {
      projectRegistry.removeMavenProjectChangedListener(u);
      u.dispose();
    });
    projectContexts.clear();
  }

  @Override
  public EOSGiContext findOrCreate(final IProject project) {
    if (projectContexts.containsKey(project)) {
      return projectContexts.get(project);
    } else {
      if (isNatureOk(project)) {
        EOSGiContext context = new EOSGiProject(project, log);
        projectRegistry.addMavenProjectChangedListener(context);
        projectContexts.put(project, context);
        return context;
      }
    }
    return null;
  }

  private boolean isNatureOk(final IProject project) {
    boolean eosgiNature = false;
    try {
      eosgiNature = project.hasNature(EosgiNature.NATURE_ID);
    } catch (CoreException e) {
      log.error("Couldn't check EOSGI nature", e);
    }
    return eosgiNature;
  }

  @Override
  public boolean refresh(final IProject project, final ContextChange contextChange) {
    Objects.requireNonNull(project, "project must be not null!");
    EOSGiContext eosGiContext = projectContexts.get(project);
    eosGiContext.refresh(contextChange);
    return true;
  }

  @Override
  public void remove(final IProject project) {
    EOSGiContext context = projectContexts.remove(project);
    if (context != null) {
      projectRegistry.removeMavenProjectChangedListener(context);
      context.dispose();
    }
  }

}
