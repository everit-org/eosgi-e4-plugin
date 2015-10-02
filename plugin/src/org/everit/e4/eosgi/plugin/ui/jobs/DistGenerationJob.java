package org.everit.e4.eosgi.plugin.ui.jobs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.everit.e4.eosgi.plugin.ui.Activator;

public final class DistGenerationJob extends Job {

  private String environmentName;
  private IProject project;

  public DistGenerationJob(IProject project, String environmentName) {
    super("Genearate distribution");
    this.project = project;
    this.environmentName = environmentName;
    setPriority(Job.SHORT);
  }

  @Override
  protected IStatus run(final IProgressMonitor monitor) {
    Activator.getDefault().getEosgiManager().generateDistFor(project, environmentName,
        monitor);
    return Status.OK_STATUS;
  }
}