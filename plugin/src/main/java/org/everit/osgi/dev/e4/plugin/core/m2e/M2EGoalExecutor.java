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
package org.everit.osgi.dev.e4.plugin.core.m2e;

import java.util.List;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.everit.osgi.dev.e4.plugin.ui.EOSGiLog;
import org.everit.osgi.dev.e4.plugin.ui.EOSGiPluginActivator;

/**
 * Class for executing maven goals.
 */
public class M2EGoalExecutor {

  /**
   * Enum for eosgi-maven-plugin goals.
   */
  public enum MavenGoal {
    DIST("dist"), INTEGRATION_TEST("integration-test");

    private String goalName;

    MavenGoal(final String value) {
      this.goalName = value;
    }

    public String getGoalName() {
      return this.goalName;
    }
  }

  private static final String EOSGI_ANALYTICS_REFERER_PARAMETER = "eosgi.analytics.referer";

  private static final String EOSGI_DIST_ONLY = "eosgi.distOnly";

  private static final String EOSGI_E4_PLUGIN = "eosgi-e4-plugin";

  private static final String EOSGI_ENVIRONMENT_ID = "eosgi.environmentId";

  public static final String EOSGI_MAVEN_PLUGIN_ARTIFACT_ID = "eosgi-maven-plugin";

  public static final String EOSGI_MAVEN_PLUGIN_GROUP_ID = "org.everit.osgi.dev";

  private final String environmentId;

  private final EOSGiLog log;

  private final IMaven maven;

  private final IMavenProjectFacade mavenProjectFacade;

  private final IMavenProjectRegistry projectRegistry;

  /**
   * Constructor.
   *
   * @param project
   *          target {@link IProject} reference.
   * @param environmentId
   *          TODO
   */
  public M2EGoalExecutor(final IProject project, final String environmentId) {
    super();
    ILog iLog = EOSGiPluginActivator.getDefault().getLog();
    log = new EOSGiLog(iLog);
    maven = MavenPlugin.getMaven();
    projectRegistry = MavenPlugin.getMavenProjectRegistry();
    mavenProjectFacade = projectRegistry.getProject(project);
    this.environmentId = environmentId;
  }

  /**
   * Execute a dist generation goal on the given environment of the given project. Polling the
   * monitor for canceling event.
   *
   * @param monitor
   *          {@link IProgressMonitor} instance.
   * @return <code>true</code> is success, <code>false</code> otherwise.
   * @throws CoreException
   *           throws when maven error occurred.
   */
  public boolean execute(final IProgressMonitor monitor)
      throws CoreException {
    MavenProject mavenProject = fetchMavenProject(monitor);
    MojoExecution execution = fetchDistExecutionFor(monitor, mavenProject);

    if ((execution != null) && isNotCancelled(monitor)) {
      if (MavenGoal.INTEGRATION_TEST.getGoalName().equals(execution.getGoal())) {
        mavenProject.getProperties().put(EOSGI_DIST_ONLY, true);
      }
      if (environmentId != null) {
        mavenProject.getProperties().put(EOSGI_ENVIRONMENT_ID, environmentId);
      }
      mavenProject.getProperties().put(EOSGI_ANALYTICS_REFERER_PARAMETER, EOSGI_E4_PLUGIN);

      monitor.setTaskName("Dist generation is running...");
      maven.execute(mavenProject, execution, monitor);

      mavenProject.getProperties().remove(EOSGI_ANALYTICS_REFERER_PARAMETER);
      mavenProject.getProperties().remove(EOSGI_ENVIRONMENT_ID);
      mavenProject.getProperties().remove(EOSGI_DIST_ONLY);

      return true;
    }
    return false;
  }

  private MojoExecution fetchDistExecutionFor(final IProgressMonitor monitor,
      final MavenProject mavenProject) {
    MojoExecution execution = null;
    if ((mavenProject != null) && isNotCancelled(monitor)) {
      monitor.setTaskName("fetching execution information...");
      try {
        execution = fetchMojoExecution(monitor);
      } catch (CoreException e) {
        log.error("Could not fetch dist/integration-test goal.", e);
      }
    }
    return execution;
  }

  private MavenProject fetchMavenProject(final IProgressMonitor monitor) {
    MavenProject mavenProject = null;
    if (isNotCancelled(monitor)) {
      try {
        mavenProject = mavenProjectFacade.getMavenProject(monitor);
      } catch (CoreException e) {
        log.error("Could not found maven project.", e);
      }
    }
    return mavenProject;
  }

  private MojoExecution fetchMojoExecution(final IProgressMonitor monitor) throws CoreException {
    List<MojoExecution> mojoExecutions = mavenProjectFacade
        .getMojoExecutions(EOSGI_MAVEN_PLUGIN_GROUP_ID, EOSGI_MAVEN_PLUGIN_ARTIFACT_ID, monitor,
            MavenGoal.DIST.getGoalName());
    if (!mojoExecutions.isEmpty()) {
      return mojoExecutions.get(0);
    }

    mojoExecutions = mavenProjectFacade
        .getMojoExecutions(EOSGI_MAVEN_PLUGIN_GROUP_ID, EOSGI_MAVEN_PLUGIN_ARTIFACT_ID, monitor,
            MavenGoal.INTEGRATION_TEST.getGoalName());

    if (!mojoExecutions.isEmpty()) {
      return mojoExecutions.get(0);
    } else {
      return null;
    }
  }

  /**
   * Get execution configuration for dist or intgration-test goal. If dist goal doesn't exist then
   * return the configuration of integration-test goal or null if doesn't find either.
   *
   * @param monitor
   *          optional {@link IProgressMonitor} instance.
   * @return dist or integration-test goal configuration or <code>null</code>.
   */
  public Xpp3Dom getConfiguration(final IProgressMonitor monitor) {
    MavenProject mavenProject = fetchMavenProject(monitor);
    MojoExecution execution = fetchDistExecutionFor(monitor, mavenProject);
    return execution.getConfiguration();
  }

  private boolean isNotCancelled(final IProgressMonitor monitor) {
    return (monitor != null) && !monitor.isCanceled();
  }
}
