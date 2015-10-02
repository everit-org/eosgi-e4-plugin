package org.everit.e4.eosgi.plugin.core.dist;

import org.eclipse.core.resources.IProject;

public class DistStatusEvent {

  public DistStatus distStatus;

  public String environmentName;

  public IProject project;

  public DistStatusEvent distStatus(final DistStatus distStatus) {
    this.distStatus = distStatus;
    return this;
  }

  public DistStatusEvent environmentName(final String environmentName) {
    this.environmentName = environmentName;
    return this;
  }

  public DistStatusEvent project(final IProject project) {
    this.project = project;
    return this;
  }

  @Override
  public String toString() {
    return "DistStatusEvent [environmentName=" + environmentName + ", project=" + project
        + ", distStatus=" + distStatus + "]";
  }

}
