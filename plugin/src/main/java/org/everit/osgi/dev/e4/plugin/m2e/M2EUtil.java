/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.osgi.dev.e4.plugin.m2e;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ICallable;
import org.eclipse.m2e.core.embedder.IMavenExecutionContext;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.m2e.core.internal.embedder.MavenProjectMutableState;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.everit.osgi.dev.e4.plugin.EOSGiEclipsePlugin;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

/**
 * Util functions to help the usage of M2E.
 */
public final class M2EUtil {

  public static final String EOSGI_ARTIFACT_ID = "eosgi-maven-plugin";

  public static final String EOSGI_GROUP_ID = "org.everit.osgi.dev";

  public static final VersionRange EOSGI_VERSION_RANGE = new VersionRange("[4.0.0,5.0)");

  private static final Set<String> SKIPPED_LIFECYCLE_PHASES;

  static {
    SKIPPED_LIFECYCLE_PHASES = new HashSet<>();
    SKIPPED_LIFECYCLE_PHASES.add("generate-test-sources");
    SKIPPED_LIFECYCLE_PHASES.add("process-test-sources");
    SKIPPED_LIFECYCLE_PHASES.add("generate-test-resources");
    SKIPPED_LIFECYCLE_PHASES.add("process-test-resources");
    SKIPPED_LIFECYCLE_PHASES.add("test-compile");
    SKIPPED_LIFECYCLE_PHASES.add("process-test-classes");
    SKIPPED_LIFECYCLE_PHASES.add("test");
  }

  /**
   * Throws a {@link CoreException} if there is any exception in the passed
   * {@link IMavenExecutionContext} with all exceptions as statuses inside.
   *
   * @param context
   *          The maven execution context.
   * @param errorMessage
   *          The error message that should be added to the statuses.
   * @throws CoreException
   *           if the {@link IMavenExecutionContext} contains any exception.
   */
  public static void checkExecutionResultExceptions(final IMavenExecutionContext context,
      final String errorMessage) throws CoreException {

    List<Throwable> exceptions = context.getSession().getResult().getExceptions();
    if (exceptions.size() == 0) {
      return;
    }

    if (exceptions.size() == 1) {
      throw new CoreException(new Status(IStatus.ERROR, EOSGiEclipsePlugin.PLUGIN_ID,
          errorMessage, exceptions.get(0)));
    }

    MultiStatus multiStatus =
        new MultiStatus(EOSGiEclipsePlugin.PLUGIN_ID, IStatus.ERROR, errorMessage, null);

    for (Throwable exception : exceptions) {
      multiStatus
          .add(new Status(IStatus.ERROR, EOSGiEclipsePlugin.PLUGIN_ID, exception.getMessage(),
              exception));
    }

    throw new CoreException(multiStatus);
  }

  @SuppressWarnings("restriction")
  public static IMavenExecutionContext createExecutionContext(final IMavenProjectFacade facade,
      final IProgressMonitor monitor) throws CoreException {
    return MavenPluginActivator.getDefault().getMavenProjectManagerImpl()
        .createExecutionContext(facade.getPom(), facade.getResolverConfiguration());
  }

  public static <V> V executeInContext(final IMavenProjectFacade facade,
      final MavenExecutionContextModifiers modifications,
      final ICallable<V> callable, final IProgressMonitor monitor) throws CoreException {

    IMavenExecutionContext executionContext = createExecutionContext(facade, monitor);
    MavenExecutionRequest executionRequest = executionContext.getExecutionRequest();
    executionRequest.setStartTime(new Date());
    if (modifications != null && modifications.workspaceReaderReplacer != null) {
      executionRequest.setWorkspaceReader(modifications.workspaceReaderReplacer
          .apply(executionRequest.getWorkspaceReader()));
    }

    return executionContext.execute(facade.getMavenProject(monitor),
        (context, monitor1) -> {
          if (modifications != null) {
            if (modifications.systemPropertiesReplacer != null) {
              executionRequest.setSystemProperties(modifications.systemPropertiesReplacer
                  .apply(executionRequest.getSystemProperties()));
            }
            if (modifications.executionRequestDataModifier != null) {
              modifications.executionRequestDataModifier.accept(executionRequest.getData());
            }
          }

          return callable.call(context, monitor1);
        }, monitor);
  }

  /**
   * Runs an action within the scope of mutable state of a project. This is useful for example if
   * multiple mojo goals should be executed in the way that they share the same project state.
   *
   * @param project
   *          The project that should have the mutable state.
   * @param action
   *          The action that should be executed.
   */
  @SuppressWarnings("restriction")
  public static void executeWithMutableProjectState(final MavenProject project,
      final ICoreRunnable action, final IProgressMonitor monitor) throws CoreException {

    LinkedHashSet<Artifact> artifacts = new LinkedHashSet<>(project.getArtifacts());
    MavenProjectMutableState snapshot = MavenProjectMutableState.takeSnapshot(project);

    try {
      action.run(monitor);
    } finally {
      project.setArtifactFilter(null);
      project.setResolvedArtifacts(null);
      project.setArtifacts(artifacts);
      snapshot.restore(project);
    }
  }

  /**
   * Resolves the value of a parameter of a plugin configuration.
   *
   * @param project
   *          The maven project that contains the configuration.
   * @param parameter
   *          The name of the parameter.
   * @param asType
   *          The type that the configuration will be converted to.
   * @param mojoExecution
   *          The mojo execution.
   * @param monitor
   *          The monitor that is used in Eclipse.
   * @return The resolved parameter value.
   * @throws CoreException
   *           If anything wrong happens.
   */
  public static <T> T getParameterValue(final MavenProject project, final String parameter,
      final Class<T> asType,
      final MojoExecution mojoExecution, final IProgressMonitor monitor) throws CoreException {
    PluginExecution execution = new PluginExecution();
    execution.setConfiguration(mojoExecution.getConfiguration());
    return MavenPlugin.getMaven().getMojoParameterValue(project, parameter, asType,
        mojoExecution.getPlugin(), execution, mojoExecution.getGoal(), monitor);
  }

  public static boolean hasEOSGiMavenPlugin(final MavenProject mavenProject) {
    List<Plugin> plugins = mavenProject.getBuild().getPlugins();

    for (Plugin plugin : plugins) {
      if (EOSGI_GROUP_ID.equals(plugin.getGroupId())
          && EOSGI_ARTIFACT_ID.equals(plugin.getArtifactId())
          && isEOSGiMojoVersionSupported(plugin.getVersion())) {

        return true;
      }
    }

    return false;
  }

  public static boolean isEOSGiMojoVersionSupported(final String mojoVersion) {
    String semanticVersion = mojoVersion.replace('-', '.');
    Version version = new Version(semanticVersion);
    return EOSGI_VERSION_RANGE.includes(version);
  }

  private M2EUtil() {
  }
}
