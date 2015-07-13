package org.everit.e4.eosgi.plugin.m2e;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;
import org.everit.e4.eosgi.plugin.m2e.model.EosgiProject;
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
    IProject project = mavenProjectFacade.getProject();
    String projectName = project.getName();
    Activator.getDefault().info(projectName + " - building...");

    Set<IProject> buildResult = null;
    try {
      buildResult = super.build(kind, monitor);
      linkProjectAndBuildData(mavenProjectFacade, project);
    } catch (Exception e) {
      Activator.getDefault().error("Dist creating failed!");
    }

    Activator.getDefault().info(projectName + " - builded.");
    return buildResult;
  }

  private void linkProjectAndBuildData(final IMavenProjectFacade mavenProjectFacade,
      final IProject project) {
    File basedir = mavenProjectFacade.getMavenProject().getBasedir();
    Build build = mavenProjectFacade.getMavenProject().getBuild();
    String directory = build.getDirectory();
    String distDir = "/eosgi-dist/equinoxtest";
    String runConsoleSh = "/bin/runConsole.sh";
    List<Dependency> dependencies = mavenProjectFacade.getMavenProject().getDependencies();
    // mavenProjectFacade.getMavenProject().getProjectReferences() // ez még jó lehet
    EosgiProject eosgiProject = Activator.getDefault().getEosgiProjectController()
        .getProject(project);
    eosgiProject.setBaseDir(basedir);
    eosgiProject.setBuild(build);
    eosgiProject.setDependencies(dependencies);
    Activator.getDefault().getEosgiProjectController().addProject(project, eosgiProject);
  }

}
