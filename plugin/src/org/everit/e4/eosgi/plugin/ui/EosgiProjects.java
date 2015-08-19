package org.everit.e4.eosgi.plugin.ui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;
import org.everit.e4.eosgi.plugin.m2e.model.EosgiProject;

/**
 * Manage Eosgi projects.
 */
public class EosgiProjects {

  private Map<IProject, EosgiProject> projects = new ConcurrentHashMap<>();

  public void addProject(final IProject project, final EosgiProject eosgiProject) {
    projects.put(project, eosgiProject);
  }

  public EosgiProject getProject(final IProject project) {
    return projects.get(project);
  }

  public void removeProject(final IProject project) {
    projects.remove(project);
  }

}
