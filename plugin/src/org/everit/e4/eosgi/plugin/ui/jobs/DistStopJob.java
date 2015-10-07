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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.everit.e4.eosgi.plugin.core.dist.DistRunner;

/**
 * Eclipse {@link Job} for stop a dist.
 */
public class DistStopJob extends Job {

  private IProgressMonitor monitor;

  private DistRunner runner;

  /**
   * Constructor. Create a job with {@link Job#SHORT} priority.
   * 
   * @param name
   *          name of the envronment.
   * @param runner
   *          {@link DistRunner} instance.
   */
  public DistStopJob(final String name, final DistRunner runner) {
    super("Stopping '" + name + "' EOSGI environment...");
    this.runner = runner;
    setPriority(Job.SHORT);
  }

  @Override
  protected void canceling() {
    monitor.setCanceled(true);
    super.canceling();
  }

  @Override
  protected IStatus run(final IProgressMonitor monitor) {
    this.monitor = monitor;
    runner.stop(monitor);
    return Status.OK_STATUS;
  }

}
