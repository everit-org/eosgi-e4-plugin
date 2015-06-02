package org.everit.e4.eosgi.plugin.m2e.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;

/**
 * Class for representing an Eosgi project.
 */
public class EosgiProject {
  public static final String DIST_FOLDER = "/eosgi-dist/";

  public static final String DIST_BIN = "/bin";

  public static final String DIST_LOG = "/log";

  public static final String LINUX_START = "/runConsole.sh";

  private IProject project;

  private Map<String, Environment> environments;

  private boolean dist;

  public EosgiProject(final IProject project) {
    super();
    this.project = project;
    this.environments = new HashMap<>();
  }

  public List<Environment> getEnvironments() {
    return new ArrayList<>(this.environments.values());
  }

  public void setEnvironments(final List<Environment> environments) {
    for (Environment environment : environments) {
      this.environments.put(environment.getId(), environment);
    }
  }

  public IProject getProject() {
    return project;
  }

  public boolean isDist() {
    return dist;
  }

  public void setDist(final boolean dist) {
    this.dist = dist;
  }

  @Override
  public String toString() {
    return "EosgiEclipseProject [project=" + project + ", environments=" + environments + ", dist="
        + dist + "]";
  }

}
