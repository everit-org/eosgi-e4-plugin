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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.lifecycle.MavenExecutionPlan;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

public class ProjectPackageUtil {

  private static final Set<String> SKIPPED_LIFECYCLE_PHASES;
  static {
    SKIPPED_LIFECYCLE_PHASES = new HashSet<>();
    // SKIPPED_LIFECYCLE_PHASES.add("generate-test-sources");
    // SKIPPED_LIFECYCLE_PHASES.add("process-test-sources");
    // SKIPPED_LIFECYCLE_PHASES.add("generate-test-resources");
    // SKIPPED_LIFECYCLE_PHASES.add("process-test-resources");
    // SKIPPED_LIFECYCLE_PHASES.add("test-compile");
    // SKIPPED_LIFECYCLE_PHASES.add("process-test-classes");
    SKIPPED_LIFECYCLE_PHASES.add("test");
  }

  private ChangedProjectTracker changedProjectTracker;

  public void close() {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(changedProjectTracker);

  }

  public void open() {
    changedProjectTracker = new ChangedProjectTracker();
    ResourcesPlugin.getWorkspace().addResourceChangeListener(changedProjectTracker);
  }

  public void packageProject(final IMavenProjectFacade mavenProjectFacade,
      final IProgressMonitor monitor) throws CoreException {
    MavenPlugin.getMavenProjectRegistry().execute(mavenProjectFacade, (context, monitor1) -> {
      IMaven maven = MavenPlugin.getMaven();
      MavenProject mavenProject = mavenProjectFacade.getMavenProject(monitor);

      maven.createExecutionContext().execute(mavenProject, (context1, monitor2) -> {
        MavenExecutionPlan executionPlan =
            maven.calculateExecutionPlan(mavenProject,
                Arrays.asList(new String[] { "package" }), true, monitor);

        List<MojoExecution> mojoExecutions = executionPlan.getMojoExecutions();

        for (MojoExecution mojoExecution : mojoExecutions) {
          if (!SKIPPED_LIFECYCLE_PHASES.contains(mojoExecution.getLifecyclePhase())) {
            maven.execute(mavenProject, mojoExecution, monitor);
          }
        }
        return null;
      }, monitor);

      return null;
    }, monitor);

  }
}