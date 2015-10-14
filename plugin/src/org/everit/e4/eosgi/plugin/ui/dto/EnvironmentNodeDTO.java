package org.everit.e4.eosgi.plugin.ui.dto;

import java.util.Observable;

import org.everit.e4.eosgi.plugin.core.dist.DistStatus;

/**
 * DTO class for update UI about environment.
 */
public class EnvironmentNodeDTO {

  public DistStatus distStatus;

  public String id;

  public Observable observable;

  public Boolean outdated;

  public EnvironmentNodeDTO distStatus(final DistStatus distStatus) {
    this.distStatus = distStatus;
    return this;
  }

  public EnvironmentNodeDTO id(final String id) {
    this.id = id;
    return this;
  }

  public EnvironmentNodeDTO observable(final Observable observable) {
    this.observable = observable;
    return this;
  }

  public EnvironmentNodeDTO outdated(final Boolean outdated) {
    this.outdated = outdated;
    return this;
  }

  @Override
  public String toString() {
    return "EnvironmentNodeDTO [id=" + id + ", observable=" + observable + ", outdated=" + outdated
        + ", distStatus=" + distStatus + "]";
  }

}
