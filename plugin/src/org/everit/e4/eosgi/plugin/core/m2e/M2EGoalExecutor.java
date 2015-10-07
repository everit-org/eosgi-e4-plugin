package org.everit.e4.eosgi.plugin.core.m2e;

import java.util.List;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.everit.e4.eosgi.plugin.ui.EOSGiLog;

public class M2EGoalExecutor {

  private static final String DIST_GOAL = "dist";

  private static final String EOSGI_MAVEN_PLUGIN_ARTIFACT_ID = "eosgi-maven-plugin";

  private static final String EOSGI_MAVEN_PLUGIN_GROUP_ID = "org.everit.osgi.dev";

  private EOSGiLog log;

  private IMaven maven;

  private IMavenProjectRegistry projectRegistry;

  public M2EGoalExecutor(final EOSGiLog log) {
    super();
    this.log = log;
    projectRegistry = MavenPlugin.getMavenProjectRegistry();
    maven = MavenPlugin.getMaven();
  }

  public boolean executeOn(final IProject project, final String environmentName,
      final IProgressMonitor monitor) {
    if (monitor.isCanceled()) {
      return false;
    }
    monitor.setTaskName("fetching project information...");
    IMavenProjectFacade mavenProjectFacade = projectRegistry.getProject(project);
    MavenProject mavenProject = null;

    if (monitor.isCanceled()) {
      return false;
    }
    try {
      mavenProject = mavenProjectFacade.getMavenProject(monitor);
    } catch (CoreException e) {
      log.error("dist generation failed for: " + project.getName() + "/"
          + environmentName, e);
    }

    if (mavenProject != null) {
      try {
        if (monitor.isCanceled()) {
          return false;
        }
        monitor.setTaskName("fetching execution information...");
        MojoExecution execution = fetchDistMojoExecution(mavenProjectFacade, monitor);

        if (monitor.isCanceled()) {
          return false;
        }
        monitor.setTaskName("creating dist...");
        maven.execute(mavenProject, execution, monitor);
        return true;
      } catch (CoreException e) {
        log.error("dist generation failed for: " + project.getName() + "/"
            + environmentName, e);
      }
    }
    return false;
  }

  private MojoExecution fetchDistMojoExecution(final IMavenProjectFacade mavenProjectFacade,
      final IProgressMonitor monitor) throws CoreException {
    List<MojoExecution> mojoExecutions = mavenProjectFacade
        .getMojoExecutions(EOSGI_MAVEN_PLUGIN_GROUP_ID, EOSGI_MAVEN_PLUGIN_ARTIFACT_ID, monitor,
            DIST_GOAL);
    if (!mojoExecutions.isEmpty()) {
      return mojoExecutions.get(0);
    } else {
      return null;
    }
  }
}
