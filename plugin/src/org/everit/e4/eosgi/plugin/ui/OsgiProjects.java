package org.everit.e4.eosgi.plugin.ui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;

/**
 * Store OSGi projects.
 */
public class OsgiProjects {

  private Map<IProject, OsgiProject> osgiProjects = new ConcurrentHashMap<>();

  public void addProject(final IProject iProject, final OsgiProject osgiProject) {
    this.osgiProjects.put(iProject, osgiProject);
  }

  public OsgiProject getProject(final IProject iProject) {
    return this.osgiProjects.get(iProject);
  }

  public OsgiProject getProjectBy(String artifactId) {
    for (OsgiProject osgiProject : osgiProjects.values()) {
      if (osgiProject.getMavenProject().getArtifactId().equals(artifactId)) {
        return osgiProject;
      }
    }
    return null;
  }

  public void removeProject(final IProject iProject) {
    this.osgiProjects.remove(iProject);
  }

}
