package org.everit.e4.eosgi.plugin.m2e;

import java.util.logging.Logger;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;

/**
 * Project configurator for EOSGI projects.
 */
public class EosgiDistProjectConfigurator extends AbstractProjectConfigurator
    implements IJavaProjectConfigurator {

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger
      .getLogger(EosgiDistProjectConfigurator.class.getName());

  @Override
  public void configure(final ProjectConfigurationRequest request, final IProgressMonitor monitor)
      throws CoreException {
    // IProject project = request.getProject();
    //
    // EosgiProject eosgiEclipseProject = new EosgiProject(project);
    // Activator.getDefault().getEosgiProjectController().addProject(project, eosgiEclipseProject);
    //
    // String projectName = project.getName();
    // Activator.getDefault().info(projectName + " configure");
  }

  @Override
  public void configureClasspath(final IMavenProjectFacade mavenProjectFacade,
      final IClasspathDescriptor classpathDescriptor,
      final IProgressMonitor progressMonitor) throws CoreException {
  }

  @Override
  public void configureRawClasspath(final ProjectConfigurationRequest projectConfigurationRequest,
      final IClasspathDescriptor classpathDescriptor,
      final IProgressMonitor progressMonitor) throws CoreException {
  }

  @Override
  public AbstractBuildParticipant getBuildParticipant(final IMavenProjectFacade projectFacade,
      final MojoExecution execution,
      final IPluginExecutionMetadata executionMetadata) {
    // IProject project = projectFacade.getProject();
    // processEnvironments(execution, project);
    // String executionId = execution.getExecutionId();

    if ("dist".equals(execution.getGoal())) {
      return new EosgiDistBuildParticipant(execution, true, true);
    } else {
      return null;
    }
  }

  // private void processEnvironments(final MojoExecution execution, final IProject project) {
  // Environments environments = new ConfiguratorParser().parse(execution.getConfiguration());
  // Activator.getDefault().info(environments.toString());
  //
  // EosgiProject eosgiProject = Activator.getDefault().getEosgiProjectController()
  // .getProject(project);
  // if (eosgiProject == null) {
  // // eosgiProject = new EosgiEclipseProject(project);
  // // Activator.getDefault().info("Create EosgiProject: " + project.getName());
  // } else {
  // LOGGER.log(Level.INFO, "Update EosgiProject: " + project.getName());
  // eosgiProject.setEnvironments(environments.getEnvironments());
  // Activator.getDefault().getEosgiProjectController().addProject(project,
  // eosgiProject);
  // }
  // }

}
