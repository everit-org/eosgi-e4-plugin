package org.everit.e4.eosgi.plugin.m2e;

import java.util.Set;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;
import org.everit.e4.eosgi.plugin.core.m2e.EosgiManager;
import org.everit.e4.eosgi.plugin.ui.Activator;

/**
 * MojoExecutionBuildParticipant implementation for eosgi-maven-plugin.
 */
public class EosgiDistBuildParticipant extends MojoExecutionBuildParticipant {

  public EosgiDistBuildParticipant(final MojoExecution execution,
      final boolean runOnIncremental) {
    super(execution, runOnIncremental);
  }

  public EosgiDistBuildParticipant(final MojoExecution execution,
      final boolean runOnIncremental,
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
    if (project == null) {
      return null;
    }

    MavenProject mavenProject = mavenProjectFacade.getMavenProject(monitor);
    if (mavenProject == null) {
      return null;
    }

    processConfiguration(project, monitor);

    // MojoExecution mojoExecution = getMojoExecution();
    // if (mojoExecution != null) {
    // String goal = mojoExecution.getGoal();
    // if (!"dist".equals(goal)) {
    // return ;
    // }
    // }
    // return super.build(kind, monitor);
    return null;
  }

  @Override
  public void clean(final IProgressMonitor monitor) throws CoreException {
    super.clean(monitor);
  }

  @Override
  protected IResourceDelta getDelta(final IProject project) {
    return super.getDelta(project);
  }

  private void processConfiguration(final IProject project, final IProgressMonitor monitor) {
    if (getMojoExecution() != null && getMojoExecution().getConfiguration() != null) {
      monitor.subTask("Process dist configuration for project (" + project.getName() + ")");
      Xpp3Dom configuration = getMojoExecution().getConfiguration();
      if (configuration != null) {
        EosgiManager eosgiManager = Activator.getDefault().getEosgiManager();
        if (eosgiManager != null) {
          eosgiManager.updateEnvironments(project, configuration, monitor);
        }
      }
    }
  }

}
