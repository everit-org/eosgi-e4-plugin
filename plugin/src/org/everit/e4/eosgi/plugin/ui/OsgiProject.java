package org.everit.e4.eosgi.plugin.ui;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;

public class OsgiProject {

  private MavenProject mavenProject;

  private IProject project;

  public OsgiProject(final IProject project, final MavenProject mavenProject) {
    super();
    this.project = project;
    this.mavenProject = mavenProject;
  }

  public MavenProject getMavenProject() {
    return mavenProject;
  }

  public IProject getProject() {
    return project;
  }

}
