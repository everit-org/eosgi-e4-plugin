package org.everit.e4.eosgi.plugin.ui.jobs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.everit.e4.eosgi.plugin.core.EOSGiContext;
import org.everit.e4.eosgi.plugin.core.EOSGiManager;
import org.everit.e4.eosgi.plugin.ui.Activator;

public final class DistGenerationJob extends Job {

  private String environmentName;
  private IProgressMonitor monitor;
  private IProject project;

  public DistGenerationJob(IProject project, String environmentName) {
    super("Generate dist for " + environmentName);
    this.project = project;
    this.environmentName = environmentName;
    setPriority(Job.BUILD);
  }

  @Override
  protected void canceling() {
    this.monitor.setCanceled(true);
    super.canceling();
  }

  @Override
  protected IStatus run(final IProgressMonitor monitor) {
    this.monitor = monitor;
    EOSGiManager eosgiManager2 = Activator.getDefault().getEOSGiManager();
    EOSGiContext eosgiProject = eosgiManager2.findOrCreate(project);
    if (eosgiProject != null) {
      eosgiProject.generate(environmentName, monitor);
    }
    return Status.OK_STATUS;
  }
}