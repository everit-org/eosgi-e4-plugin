package org.everit.e4.eosgi.plugin.m2e.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.eclipse.core.resources.IProject;

/**
 * Class for representing an Eosgi project.
 */
public class EosgiProject {
  public static final String DIST_BIN = "/bin";

  public static final String DIST_FOLDER = "/eosgi-dist/";

  public static final String DIST_LOG = "/log";

  public static final String LINUX_START = "/runConsole.sh";

  private File basedir;

  private Build build;

  private List<Dependency> dependencies;

  private boolean dist;

  private Map<String, Environment> environments;

  private IProject project;

  public EosgiProject(final IProject project) {
    super();
    this.project = project;
    this.environments = new HashMap<>();
  }

  public List<Environment> getEnvironments() {
    return new ArrayList<>(this.environments.values());
  }

  public IProject getProject() {
    return project;
  }

  public boolean isDist() {
    return dist;
  }

  public void setBaseDir(final File basedir) {
    this.basedir = basedir;
  }

  public void setBuild(final Build build) {
    this.build = build;
  }

  public void setDependencies(final List<Dependency> dependencies) {
    this.dependencies = dependencies;
  }

  public void setDist(final boolean dist) {
    this.dist = dist;
  }

  public void setEnvironments(final List<Environment> environments) {
    for (Environment environment : environments) {
      this.environments.put(environment.getId(), environment);
    }
  }

  @Override
  public String toString() {
    return "EosgiEclipseProject [project=" + project + ", environments=" + environments + ", dist="
        + dist + "]";
  }

}
