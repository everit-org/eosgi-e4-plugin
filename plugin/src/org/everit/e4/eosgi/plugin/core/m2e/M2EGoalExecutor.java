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
package org.everit.e4.eosgi.plugin.core.m2e;

import java.util.List;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.everit.e4.eosgi.plugin.ui.EOSGiLog;

/**
 * Class for executing maven goals.
 */
public class M2EGoalExecutor {

  private static final String DIST_GOAL = "dist";

  private static final String EOSGI_MAVEN_PLUGIN_ARTIFACT_ID = "eosgi-maven-plugin";

  private static final String EOSGI_MAVEN_PLUGIN_GROUP_ID = "org.everit.osgi.dev";

  private EOSGiLog log;

  private IMaven maven;

  private IMavenProjectRegistry projectRegistry;

  /**
   * Constructor with log wrapper.
   * 
   * @param log
   *          EOSGiLog instance.
   */
  public M2EGoalExecutor(final EOSGiLog log) {
    super();
    this.log = log;
    projectRegistry = MavenPlugin.getMavenProjectRegistry();
    maven = MavenPlugin.getMaven();
  }

  /**
   * Execute a dist generation goal on the given environment of the given project. Polling the
   * monitor for canceling event.
   * 
   * @param project
   *          target {@link IProject} reference.
   * @param environmentId
   *          target environment id.
   * @param monitor
   *          {@link IProgressMonitor} instance.
   * @return <code>true</code> is success, <code>false</code> otherwise.
   */
  public boolean executeOn(final IProject project, final String environmentId,
      final IProgressMonitor monitor) {
    if (monitor.isCanceled()) {
      return false;
    }
    monitor.setTaskName("fetching project information...");
    IMavenProjectFacade mavenProjectFacade = projectRegistry.getProject(project);
    MavenProject mavenProject = null;

    if (monitor.isCanceled()) {
      return false;
    }
    try {
      mavenProject = mavenProjectFacade.getMavenProject(monitor);
    } catch (CoreException e) {
      log.error("dist generation failed for: " + project.getName() + "/"
          + environmentId, e);
    }

    if (mavenProject != null) {
      try {
        if (monitor.isCanceled()) {
          return false;
        }
        monitor.setTaskName("fetching execution information...");
        MojoExecution execution = fetchDistMojoExecution(mavenProjectFacade, monitor);

        if (monitor.isCanceled()) {
          return false;
        }
        monitor.setTaskName("creating dist...");
        maven.execute(mavenProject, execution, monitor);
        return true;
      } catch (CoreException e) {
        log.error("dist generation failed for: " + project.getName() + "/"
            + environmentId, e);
      }
    }
    return false;
  }

  private MojoExecution fetchDistMojoExecution(final IMavenProjectFacade mavenProjectFacade,
      final IProgressMonitor monitor) throws CoreException {
    List<MojoExecution> mojoExecutions = mavenProjectFacade
        .getMojoExecutions(EOSGI_MAVEN_PLUGIN_GROUP_ID, EOSGI_MAVEN_PLUGIN_ARTIFACT_ID, monitor,
            DIST_GOAL);
    if (!mojoExecutions.isEmpty()) {
      return mojoExecutions.get(0);
    } else {
      return null;
    }
  }
}
