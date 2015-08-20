package org.everit.e4.eosgi.plugin.m2e;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.everit.e4.eosgi.plugin.ui.OsgiProject;
import org.everit.e4.eosgi.plugin.ui.OsgiProjects;

/**
 * Build participant for OSGI projects (for maven-bundle-plugin).
 */
public class BundleBuildParticipant extends MojoExecutionBuildParticipant {

  private static final Logger LOGGER = Logger.getLogger(BundleBuildParticipant.class.getName());

  public BundleBuildParticipant(final MojoExecution execution, final boolean runOnIncremental) {
    super(execution, runOnIncremental);
  }

  public BundleBuildParticipant(final MojoExecution execution, final boolean runOnIncremental,
      final boolean runOnConfiguration) {
    super(execution, runOnIncremental, runOnConfiguration);
  }

  @Override
  public Set<IProject> build(final int kind, final IProgressMonitor monitor) throws Exception {
    IMavenProjectFacade mavenProjectFacade = getMavenProjectFacade();
    if (mavenProjectFacade == null) {
      return null;
    }

    IProject project = mavenProjectFacade.getProject();
    if (project != null) {
      MavenProject mavenProject = mavenProjectFacade.getMavenProject();
      handleOsgiProject(project, mavenProject);
    }

    return super.build(kind, monitor);
  }

  private void handleOsgiProject(final IProject project, final MavenProject mavenProject) {
    OsgiProjects osgiProjects = Activator.getDefault().getOsgiProjects();
    if (osgiProjects == null) {
      LOGGER.log(Level.WARNING, "OsgiProjects instance not available!");
      return;
    }

    OsgiProject osgiProject = osgiProjects.getProject(project);
    if (osgiProject == null && mavenProject != null) {
      osgiProject = new OsgiProject(project, mavenProject);
      osgiProjects.addProject(project, osgiProject);
    } else {
      LOGGER.log(Level.INFO, project.getName() + " already added as OsgiProject.");
    }
  }

}
