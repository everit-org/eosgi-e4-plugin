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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.lifecycle.MavenExecutionPlan;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

/**
 * Util functions to help the usage of M2E.
 */
public final class M2EUtil {

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

  public static void executeProjectWithProperties(final MavenProject project,
      final Map<String, Object> properties, final Runnable runnable) {

    Properties projectProperties = project.getProperties();
    Map<String, Object> oldValues = new HashMap<>();
    for (Entry<String, Object> entry : properties.entrySet()) {
      String key = entry.getKey();
      String value = projectProperties.getProperty(key);
      if (value != null) {
        oldValues.put(key, value);
      }
      projectProperties.put(key, entry.getValue());
    }

    try {
      runnable.run();
    } finally {
      for (Entry<String, Object> entry : properties.entrySet()) {
        String key = entry.getKey();
        Object oldValue = oldValues.get(key);
        if (oldValue != null) {
          projectProperties.put(key, oldValue);
        } else {
          projectProperties.remove(key);
        }
      }
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

  public static void packageProject(final IMavenProjectFacade mavenProjectFacade,
      final IProgressMonitor monitor) throws CoreException {
    IMaven maven = MavenPlugin.getMaven();
    MavenExecutionPlan executionPlan =
        maven.calculateExecutionPlan(mavenProjectFacade.getMavenProject(),
            Arrays.asList(new String[] { "package" }), true, monitor);

    List<MojoExecution> mojoExecutions = executionPlan.getMojoExecutions();

    for (MojoExecution mojoExecution : mojoExecutions) {
      if (!SKIPPED_LIFECYCLE_PHASES.contains(mojoExecution.getLifecyclePhase())) {
        maven.execute(mavenProjectFacade.getMavenProject(), mojoExecution,
            monitor);
      }
    }
  }

  private M2EUtil() {
  }
}
