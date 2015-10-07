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
package org.everit.e4.eosgi.plugin.ui.jobs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.everit.e4.eosgi.plugin.core.EOSGiContext;
import org.everit.e4.eosgi.plugin.core.EOSGiContextManager;
import org.everit.e4.eosgi.plugin.ui.EOSGiPluginActivator;

/**
 * Eclipse Job for generate dist.
 */
public final class DistGenerationJob extends Job {

  private String environmentId;

  private IProgressMonitor monitor;

  private IProject project;

  /**
   * Constructor. Create a job with {@link Job#BUILD} priority.
   * 
   * @param project
   *          project.
   * @param environmentId
   *          environment id.
   */
  public DistGenerationJob(final IProject project, final String environmentId) {
    super("Generate dist for " + environmentId);
    this.project = project;
    this.environmentId = environmentId;
    setPriority(Job.BUILD);
  }

  @Override
  protected void canceling() {
    monitor.setCanceled(true);
    super.canceling();
  }

  @Override
  protected IStatus run(final IProgressMonitor monitor) {
    this.monitor = monitor;
    EOSGiContextManager eosgiManager2 = EOSGiPluginActivator.getDefault().getEOSGiManager();
    EOSGiContext eosgiProject = eosgiManager2.findOrCreate(project);
    if (eosgiProject != null) {
      eosgiProject.generate(environmentId, monitor);
    }
    return Status.OK_STATUS;
  }
}
