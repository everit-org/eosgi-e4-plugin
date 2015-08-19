package org.everit.e4.eosgi.plugin.m2e.model;

import java.util.List;

public class Environments {

  private List<Environment> environments;

  public Environment[] getEnvironmentArray() {
    if (environments != null) {
      return environments.toArray(new Environment[] {});
    } else {
      return new Environment[] {};
    }
  }

  public List<Environment> getEnvironments() {
    return environments;
  }

  public void setEnvironments(final List<Environment> environments) {
    this.environments = environments;
  }

  @Override
  public String toString() {
    return "Environments [environments=" + environments + "]";
  }

}
