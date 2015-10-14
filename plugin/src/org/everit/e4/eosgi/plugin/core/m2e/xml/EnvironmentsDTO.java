package org.everit.e4.eosgi.plugin.core.m2e.xml;

import java.util.List;

/**
 * DTO class for environments.
 */
public class EnvironmentsDTO {
  public List<EnvironmentDTO> environments;

  public EnvironmentsDTO environments(final List<EnvironmentDTO> environments) {
    this.environments = environments;
    return this;
  }

  @Override
  public String toString() {
    return "EnvironmentsDTO [environments=" + environments + "]";
  }

}
