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
package org.everit.osgi.dev.e4.plugin.m2e.packaging;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.lifecycle.MavenExecutionPlan;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.embedder.IMavenExecutionContext;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.everit.osgi.dev.e4.plugin.m2e.M2EUtil;
import org.everit.osgi.dev.e4.plugin.m2e.MavenExecutionContextModifiers;

public class ProjectPackager {

  private static final Set<String> SKIPPED_LIFECYCLE_PHASES;
  static {
    SKIPPED_LIFECYCLE_PHASES = new HashSet<>();
    SKIPPED_LIFECYCLE_PHASES.add("test");
  }

  private ChangedProjectTracker changedProjectTracker;

  private final PackagedArtifactContainer packagedArtifactContainer =
      new PackagedArtifactContainer();

  private void checkExecutionResultExceptions(final IMavenExecutionContext context) {
    List<Throwable> exceptions = context.getSession().getResult().getExceptions();
    if (exceptions.size() > 0) {
      Throwable throwable = exceptions.get(0);
      if (exceptions instanceof RuntimeException) {
        throw (RuntimeException) throwable;
      } else if (exceptions instanceof Error) {
        throw (Error) throwable;
      } else {
        throw new RuntimeException(throwable);
      }
    }
  }

  public void close() {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(changedProjectTracker);

  }

  public WorkspaceReader createWorkspaceReader(final WorkspaceReader original) {
    return new EOSGiWorkspaceReader(original, packagedArtifactContainer);
  }

  public void open() {
    changedProjectTracker = new ChangedProjectTracker(
        (eclipseProject) -> packagedArtifactContainer.removeArtifactFiles(eclipseProject),
        (eclipseProject) -> packagedArtifactContainer.getProjectArtifactFiles(eclipseProject));

    ResourcesPlugin.getWorkspace().addResourceChangeListener(changedProjectTracker);
  }

  public void packageProject(final IMavenProjectFacade mavenProjectFacade,
      final IProgressMonitor monitor) throws CoreException {

    IProject eclipseProject = mavenProjectFacade.getProject();

    if (packagedArtifactContainer.getProjectArtifactFiles(eclipseProject) != null) {
      return;
    }

    MavenExecutionContextModifiers modifiers = new MavenExecutionContextModifiers();
    modifiers.workspaceReaderReplacer = (original) -> createWorkspaceReader(original);

    M2EUtil.executeInContext(mavenProjectFacade, modifiers, (context, monitor1) -> {
      IMaven maven = MavenPlugin.getMaven();
      MavenProject mavenProject = mavenProjectFacade.getMavenProject(monitor);

      MavenExecutionPlan executionPlan =
          maven.calculateExecutionPlan(mavenProject,
              Arrays.asList(new String[] { "package" }), true, monitor);

      List<MojoExecution> mojoExecutions = executionPlan.getMojoExecutions();

      M2EUtil.executeWithMutableProjectState(mavenProject, (monitor2) -> {
        for (MojoExecution mojoExecution : mojoExecutions) {
          if (!SKIPPED_LIFECYCLE_PHASES.contains(mojoExecution.getLifecyclePhase())) {
            maven.execute(mavenProject, mojoExecution, monitor);

            checkExecutionResultExceptions(context);
          }
        }
      }, monitor);

      eclipseProject.refreshLocal(IProject.DEPTH_INFINITE, monitor1);
      packagedArtifactContainer.putArtifactsOfMavenProject(mavenProject, eclipseProject);
      return null;
    }, monitor);
  }
}
