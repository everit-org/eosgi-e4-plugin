package org.everit.e4.eosgi.plugin.core.m2e;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ILifecycleMappingConfiguration;
import org.eclipse.m2e.core.project.configurator.MojoExecutionKey;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.everit.e4.eosgi.plugin.ui.nature.EosgiNature;
import org.everit.e4.eosgi.plugin.ui.util.ProjectNatureUtils;

/**
 * Project configurator for EOSGI projects.
 */
public class EosgiDistProjectConfigurator extends AbstractProjectConfigurator
    implements IJavaProjectConfigurator {

  private void addEosgiNature(final IProgressMonitor monitor, final IProject project)
      throws CoreException {
    IProjectDescription projectDescription = project.getDescription();
    if (projectDescription != null) {
      String[] natureIds = projectDescription.getNatureIds();
      String[] newNatureIds = ProjectNatureUtils.addNature(natureIds, EosgiNature.NATURE_ID);
      projectDescription.setNatureIds(newNatureIds);
      project.setDescription(projectDescription, monitor);
    }
  }

  @Override
  public void configure(final ProjectConfigurationRequest request, final IProgressMonitor monitor)
      throws CoreException {
    IProject project = request.getProject();
    if (project == null) {
      return;
    }

    addEosgiNature(monitor, project);

    if (project != null) {
      EosgiManager eosgiManager = Activator.getDefault().getEosgiManager();
      eosgiManager.letDistProject(project);
    }

    Activator.getDefault().info(project.getName() + " configured as EOSGI dist project.");
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
    return new EosgiDistBuildParticipant(execution, true, true);
  }

  @Override
  protected <T> T getParameterValue(MavenProject project, String parameter, Class<T> asType,
      MojoExecution mojoExecution, IProgressMonitor monitor) throws CoreException {
    return super.getParameterValue(project, parameter, asType, mojoExecution, monitor);
  }

  @Override
  public boolean hasConfigurationChanged(final IMavenProjectFacade newFacade,
      final ILifecycleMappingConfiguration oldProjectConfiguration, final MojoExecutionKey key,
      final IProgressMonitor monitor) {
    if (newFacade != null) {
      monitor.subTask("Update configuration");
      try {
        IProject project = newFacade.getProject();
        MojoExecution mojoExecution = newFacade.getMojoExecution(key, monitor);
        Xpp3Dom configuration = mojoExecution.getConfiguration();
        if (project != null && configuration != null) {
          EosgiManager eosgiManager = Activator.getDefault().getEosgiManager();
          eosgiManager.updateEnvironments(project, configuration, monitor);
        }
      } catch (CoreException e) {
        Activator.getDefault()
            .error("Configuration change handling failed for project: " + e.getMessage());
      }
    }
    return super.hasConfigurationChanged(newFacade, oldProjectConfiguration, key, monitor);
  }

  @Override
  public void mavenProjectChanged(final MavenProjectChangedEvent event,
      final IProgressMonitor monitor)
          throws CoreException {
    IMavenProjectFacade mavenProjectFacade = event.getMavenProject();
    MavenProject mavenProject = null;

    IProject project = null;
    if (mavenProjectFacade != null) {
      mavenProject = mavenProjectFacade.getMavenProject();
      project = event.getMavenProject().getProject();
    }

    if (project != null || mavenProject != null) {
      EosgiManager eosgiManager = Activator.getDefault().getEosgiManager();
      eosgiManager.refreshProject(project, mavenProject, monitor);
    }

    super.mavenProjectChanged(event, monitor);
  }

  @Override
  public void unconfigure(final ProjectConfigurationRequest request, final IProgressMonitor monitor)
      throws CoreException {
    super.unconfigure(request, monitor);
  }

}
