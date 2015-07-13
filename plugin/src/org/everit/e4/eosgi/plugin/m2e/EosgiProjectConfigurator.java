package org.everit.e4.eosgi.plugin.m2e;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;
import org.everit.e4.eosgi.plugin.m2e.model.Environments;
import org.everit.e4.eosgi.plugin.m2e.model.EosgiProject;
import org.everit.e4.eosgi.plugin.m2e.xml.ConfiguratorParser;
import org.everit.e4.eosgi.plugin.ui.Activator;

/**
 * Project configurator for EOSGI projects.
 */
public class EosgiProjectConfigurator extends AbstractProjectConfigurator
    implements IJavaProjectConfigurator {

  @Override
  public void configure(final ProjectConfigurationRequest request, final IProgressMonitor monitor)
      throws CoreException {
    IProject project = request.getProject();

    EosgiProject eosgiEclipseProject = new EosgiProject(project);
    Activator.getDefault().getEosgiProjectController().addProject(project, eosgiEclipseProject);

    String projectName = project.getName();
    Activator.getDefault().info(projectName + " configure");
  }

  @Override
  public AbstractBuildParticipant getBuildParticipant(final IMavenProjectFacade projectFacade,
      final MojoExecution execution,
      final IPluginExecutionMetadata executionMetadata) {
    IProject project = projectFacade.getProject();

    processEnvironments(execution, project);

    String executionId = execution.getExecutionId();
    String projectName = project.getName();

    if ("dist".equals(execution.getGoal())) {
      Activator.getDefault().info(projectName + " - " + execution.getGoal());
      return new EosgiDistBuildParticipant(execution, true, true);
    } else {
      return null;
    }
  }

  private void processEnvironments(final MojoExecution execution, final IProject project) {
    Environments environments = new ConfiguratorParser().parse(execution.getConfiguration());
    Activator.getDefault().info(environments.toString());

    EosgiProject eosgiProject = Activator.getDefault().getEosgiProjectController()
        .getProject(project);
    if (eosgiProject == null) {
      // eosgiProject = new EosgiEclipseProject(project);
      // Activator.getDefault().info("Create EosgiProject: " + project.getName());
    } else {
      Activator.getDefault().info("Update EosgiProject: " + project.getName());
      eosgiProject.setEnvironments(environments.getEnvironments());
      Activator.getDefault().getEosgiProjectController().addProject(project,
          eosgiProject);
    }
  }

  @Override
  public void configureClasspath(IMavenProjectFacade arg0, IClasspathDescriptor arg1,
      IProgressMonitor arg2) throws CoreException {
    // TODO Auto-generated method stub

  }

  @Override
  public void configureRawClasspath(ProjectConfigurationRequest arg0, IClasspathDescriptor arg1,
      IProgressMonitor arg2) throws CoreException {
    // TODO Auto-generated method stub

  }

}
