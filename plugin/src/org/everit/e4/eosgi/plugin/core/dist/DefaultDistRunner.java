package org.everit.e4.eosgi.plugin.core.dist;

import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.everit.e4.eosgi.plugin.core.dist.DistTask.DistStoppedCallback;
import org.everit.e4.eosgi.plugin.util.DistUtils;

public class DefaultDistRunner implements DistRunner {

  private DistTask distTask;

  private String environmentId;

  private IProject project;

  private boolean startable;

  private DistChangeListener statusListener;

  public DefaultDistRunner(IProject project, final String environmentId,
      final String buildDirectory, final DistChangeListener statusListener) {
    super();
    this.project = project;
    this.environmentId = environmentId;
    Objects.requireNonNull(project, "project cannot be null");
    Objects.requireNonNull(buildDirectory, "buildDirectory cannot be null");
    Objects.requireNonNull(environmentId, "environmentId cannot be null");
    Objects.requireNonNull(statusListener, "statusListener cannot be null");

    this.statusListener = statusListener;
    String startCommand = DistUtils.getDistStartCommand(buildDirectory, environmentId);
    this.distTask = new DistTask(startCommand, environmentId, new DistStoppedCallback() {

      @Override
      public void distStopped() {
        statusListener.distStatusChanged(DistStatus.STOPPED, project, environmentId);
      }
    }, null);
  }

  @Override
  public boolean isStartable() {
    return startable;
  }

  @Override
  public void start() {
    statusListener.distStatusChanged(DistStatus.STARTING, project, environmentId);
    new Thread(distTask).start();
    statusListener.distStatusChanged(DistStatus.RUNNING, project, environmentId);
  }

  @Override
  public void startable() {
    this.startable = true;
  }

  @Override
  public void stop() {
    statusListener.distStatusChanged(DistStatus.STOPPING, project, environmentId);
    this.distTask.stop();
  }

}
