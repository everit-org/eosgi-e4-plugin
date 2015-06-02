package org.everit.e4.eosgi.plugin.m2e;

import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;
import org.everit.e4.eosgi.plugin.m2e.model.EosgiProject;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * MojoExecutionBuildParticipant implementation for eosgi-maven-plugin.
 */
public class EOSGIDistBuildParticipant extends MojoExecutionBuildParticipant {

  public EOSGIDistBuildParticipant(final MojoExecution execution,
      final boolean runOnIncremental) {
    super(execution, runOnIncremental);
  }

  public EOSGIDistBuildParticipant(final MojoExecution execution,
      final boolean runOnIncremental,
      final boolean runOnConfiguration) {
    super(execution, runOnIncremental, runOnConfiguration);
  }

  @Override
  public Set<IProject> build(final int kind, final IProgressMonitor monitor) throws Exception {
    IProject project = getMavenProjectFacade().getProject();
    String projectName = project.getName();
    Activator.getDefault().info(projectName + " - build");
    monitor.setTaskName("Creating eosgi dist");

    Set<IProject> buildResult = null;
    try {
      buildResult = super.build(kind, monitor);
      setDistCompleted(project, true);
    } catch (Exception e) {
      setDistCompleted(project, false);
      Activator.getDefault().error("Dist creating failed!");
    }

    if (buildResult != null) {
      for (IProject iProject : buildResult) {
        Activator.getDefault().info(iProject.getName() + " - builded");
      }
    } else {
      Activator.getDefault().info("No build result");
    }

    BuildContext buildContext = getBuildContext();
    MavenSession mavenSession = getSession();
    IMavenProjectFacade mavenProjectFacade = getMavenProjectFacade();
    IResourceDelta delta = getDelta(mavenProjectFacade.getProject());
    return buildResult;
  }

  private void setDistCompleted(final IProject project, final boolean completed) {
    EosgiProject eosgProject = Activator.getDefault().getEosgiProjectController()
        .getProject(project);
    eosgProject.setDist(completed);
    Activator.getDefault().getEosgiProjectController().addProject(project, eosgProject);
  }

}
