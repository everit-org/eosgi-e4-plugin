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

/**
 * Class for executing maven goals.
 */
public class M2EGoalExecutor {

  public static final String DIST_GOAL = "dist";

  public static final String EOSGI_MAVEN_PLUGIN_ARTIFACT_ID = "eosgi-maven-plugin";

  public static final String EOSGI_MAVEN_PLUGIN_GROUP_ID = "org.everit.osgi.dev";

  private IMaven maven;

  private IMavenProjectFacade mavenProjectFacade;

  private IMavenProjectRegistry projectRegistry;

  /**
   * Constructor.
   *
   * @param project
   *          target {@link IProject} reference.
   */
  public M2EGoalExecutor(final IProject project) {
    super();
    maven = MavenPlugin.getMaven();
    projectRegistry = MavenPlugin.getMavenProjectRegistry();
    mavenProjectFacade = projectRegistry.getProject(project);
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
  public boolean execute(final IProgressMonitor monitor) throws CoreException {
    MavenProject mavenProject = null;
    if (isNotCancelled(monitor)) {
      mavenProject = mavenProjectFacade.getMavenProject(monitor);
    }

    MojoExecution execution = null;
    if ((mavenProject != null) && isNotCancelled(monitor)) {
      monitor.setTaskName("fetching execution information...");
      execution = fetchDistMojoExecution(mavenProjectFacade, monitor);
    }

    if ((execution != null) && isNotCancelled(monitor)) {
      // MojoDescriptor mojoDescriptor = execution.getMojoDescriptor();
      // Parameter parameter = new Parameter();
      // "eosgi.eclipse=true"
      // parameter.
      // try {
      // mojoDescriptor.addParameter(parameter);
      // } catch (DuplicateParameterException e) {
      // // TODO Auto-generated catch block
      // e.printStackTrace();
      // }
      monitor.setTaskName("creating dist...");
      maven.execute(mavenProject, execution, monitor);
      return true;
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

  private boolean isNotCancelled(final IProgressMonitor monitor) {
    return (monitor != null) && !monitor.isCanceled();
  }
}
