package org.everit.e4.eosgi.plugin.ui.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.everit.e4.eosgi.plugin.core.dist.DistRunner;

public class DistStartJob extends Job {

  private IProgressMonitor monitor;

  private DistRunner runner;

  public DistStartJob(final String name, final DistRunner runner) {
    super("Starting '" + name + "' EOSGI environment...");
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
    runner.start(monitor);
    return Status.OK_STATUS;
  }

}
