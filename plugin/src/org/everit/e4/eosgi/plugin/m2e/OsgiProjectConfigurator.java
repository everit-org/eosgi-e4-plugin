package org.everit.e4.eosgi.plugin.m2e;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;
import org.everit.e4.eosgi.plugin.ui.nature.BundleNature;

/**
 * Project configurator for OSGI projects.
 */
public class OsgiProjectConfigurator extends AbstractProjectConfigurator
    implements IJavaProjectConfigurator {

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger.getLogger(OsgiProjectConfigurator.class.getName());

  private String[] addNature(final String[] natureIds) {
    boolean notFound = true;
    for (String nature : natureIds) {
      if (BundleNature.NATURE_ID.equals(nature)) {
        notFound = false;
        break;
      }
    }

    if (notFound) {
      String[] newNatureIds = new String[natureIds.length + 1];
      System.arraycopy(natureIds, 0, newNatureIds, 1, natureIds.length);
      newNatureIds[0] = BundleNature.NATURE_ID;
      return newNatureIds;
    } else {
      return natureIds;
    }
  }

  @Override
  public void configure(final ProjectConfigurationRequest request, final IProgressMonitor monitor)
      throws CoreException {
    IProject project = request.getProject();
    if (project == null) {
      return;
    }

    IProjectDescription projectDescription = project.getDescription();
    if (projectDescription != null) {
      String[] natureIds = projectDescription.getNatureIds();
      String[] newNatureIds = addNature(natureIds);
      projectDescription.setNatureIds(newNatureIds);
      project.setDescription(projectDescription, monitor);
    }

    // Map<QualifiedName, String> persistentProperties = project.getPersistentProperties();
    // Map<QualifiedName, Object> sessionProperties = project.getSessionProperties();
    // project.setPersistentProperty(QualifiedName, arg1);
    // IAdapterManager adapterManager = Platform.getAdapterManager();

    LOGGER.log(Level.INFO, project.getName() + " configured as bundle project.");
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
