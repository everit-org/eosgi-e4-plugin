package org.everit.e4.eosgi.plugin.core;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class ContextChange {
  public String buildDirectory;

  public Xpp3Dom configuration;

  public ContextChange buildDirectory(final String buildDirectory) {
    this.buildDirectory = buildDirectory;
    return this;
  }

  public ContextChange configuration(final Xpp3Dom configuration) {
    this.configuration = configuration;
    return this;
  }

  @Override
  public String toString() {
    return "ContextChange [buildDirectory=" + buildDirectory + ", configuration=" + configuration
        + "]";
  }

}
