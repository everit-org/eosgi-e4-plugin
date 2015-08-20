package org.everit.e4.eosgi.plugin.m2e;

import java.util.logging.Level;
import java.util.logging.Logger;

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

/**
 * Project configurator for OSGI projects.
 */
public class OsgiProjectConfigurator extends AbstractProjectConfigurator
    implements IJavaProjectConfigurator {

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger.getLogger(OsgiProjectConfigurator.class.getName());

  @Override
  public void configure(final ProjectConfigurationRequest request, final IProgressMonitor monitor)
      throws CoreException {
    IProject project = request.getProject();
    if (project != null) {
      LOGGER.log(Level.INFO, "configure for " + project.getName());
    }
  }

  @Override
  public void configureClasspath(final IMavenProjectFacade mavenProjectFacade,
      final IClasspathDescriptor classpathDescriptor,
      final IProgressMonitor progressMonitor) throws CoreException {
    LOGGER.log(Level.INFO, "configure classpath");
  }

  @Override
  public void configureRawClasspath(final ProjectConfigurationRequest projectConfigurationRequest,
      final IClasspathDescriptor classpathDescriptor,
      final IProgressMonitor progressMonitor) throws CoreException {
    LOGGER.log(Level.INFO, "configure raw classpath");
  }

  @Override
  public AbstractBuildParticipant getBuildParticipant(final IMavenProjectFacade projectFacade,
      final MojoExecution execution,
      final IPluginExecutionMetadata executionMetadata) {
    return new BundleBuildParticipant(execution, true);
  }

}
