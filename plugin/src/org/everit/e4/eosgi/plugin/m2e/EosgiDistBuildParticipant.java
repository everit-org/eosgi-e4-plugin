package org.everit.e4.eosgi.plugin.m2e;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;
import org.everit.e4.eosgi.plugin.m2e.model.Environments;
import org.everit.e4.eosgi.plugin.m2e.model.EosgiProject;
import org.everit.e4.eosgi.plugin.m2e.xml.ConfiguratorParser;
import org.everit.e4.eosgi.plugin.ui.Activator;

/**
 * MojoExecutionBuildParticipant implementation for eosgi-maven-plugin.
 */
public class EosgiDistBuildParticipant extends MojoExecutionBuildParticipant {

  private static final Logger LOGGER = Logger.getLogger(EosgiDistBuildParticipant.class.getName());

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

    String projectName = project.getName();
    processEnvironments(getMojoExecution(), project);
    fetchDistDataAndUpdateEosgiProject(mavenProjectFacade, project);

    Set<IProject> buildResult = null;
    try {
      LOGGER.info(projectName + " - building...");
      buildResult = super.build(kind, monitor);
    } catch (Exception e) {
      clearDistDataFromEosgiProject(project);
      LOGGER.warning("Dist creating failed!");
    }

    Activator.getDefault().info(projectName + " - builded.");
    return buildResult;
  }

  private void clearDistDataFromEosgiProject(final IProject project) {
    updateEosgiProject(project, null, null, null);
  }

  private void fetchDistDataAndUpdateEosgiProject(final IMavenProjectFacade mavenProjectFacade,
      final IProject project) {
    File basedir = mavenProjectFacade.getMavenProject().getBasedir();
    Build build = mavenProjectFacade.getMavenProject().getBuild();
    List<Dependency> dependencies = mavenProjectFacade.getMavenProject().getDependencies();
    // mavenProjectFacade.getMavenProject().getProjectReferences() // ez még jó lehet
    updateEosgiProject(project, basedir, build, dependencies);
  }

  private void processEnvironments(final MojoExecution execution, final IProject project) {
    Environments environments = new ConfiguratorParser().parse(execution.getConfiguration());
    Activator.getDefault().info(environments.toString());

    EosgiProject eosgiProject = Activator.getDefault().getEosgiProjectController()
        .getProject(project);
    if (eosgiProject == null) {
      eosgiProject = new EosgiProject(project);
    }
    eosgiProject.setEnvironments(environments.getEnvironments());
    Activator.getDefault().getEosgiProjectController().addProject(project,
        eosgiProject);
  }

  private void updateEosgiProject(final IProject project, final File basedir, final Build build,
      final List<Dependency> dependencies) {
    EosgiProject eosgiProject = Activator.getDefault().getEosgiProjectController()
        .getProject(project);

    if (eosgiProject == null) {
      eosgiProject = new EosgiProject(project);
    }

    eosgiProject.setBaseDir(basedir);
    eosgiProject.setBuild(build);
    eosgiProject.setDependencies(dependencies);

    Activator.getDefault().getEosgiProjectController().addProject(project, eosgiProject);
  }

}
