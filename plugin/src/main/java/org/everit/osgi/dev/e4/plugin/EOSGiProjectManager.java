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
package org.everit.osgi.dev.e4.plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.MojoExecutionKey;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

/**
 * Storing and managing {@link EOSGiProject} instances.
 */
public class EOSGiProjectManager {

  private static final String EOSGI_ARTIFACT_ID = "eosgi-maven-plugin";

  private static final String EOSGI_GROUP_ID = "org.everit.osgi.dev";

  private static final String[] EOSGI_SORTED_ACCEPTED_GOAL_ARRAY =
      new String[] { "dist", "integration-test" };

  private static final VersionRange EOSGI_VERSION_RANGE = new VersionRange("[4.0.0,5.0)");

  private final Map<IProject, EOSGiProject> eosgiProjects = new HashMap<>();

  private final Set<IProject> nonEOSGiProjects = new HashSet<>();

  public synchronized EOSGiProject get(final IProject project) {
    if (nonEOSGiProjects.contains(project)) {
      return null;
    }
    EOSGiProject eosgiProject = eosgiProjects.get(project);
    if (eosgiProject != null) {
      return eosgiProject;
    }
    return resolveProject(project, new NullProgressMonitor());
  }

  private boolean isEOSGiExecution(final MojoExecutionKey mojoExecutionKey) {

    if (EOSGI_GROUP_ID.equals(mojoExecutionKey.getGroupId())
        && EOSGI_ARTIFACT_ID.equals(mojoExecutionKey.getArtifactId())
        && Arrays.binarySearch(EOSGI_SORTED_ACCEPTED_GOAL_ARRAY, mojoExecutionKey.getGoal()) >= 0) {

      String versionString = mojoExecutionKey.getVersion().replace('-', '.');
      Version version = new Version(versionString);
      return EOSGI_VERSION_RANGE.includes(version);
    }
    return false;
  }

  public synchronized void remove(final IProject project) {
    nonEOSGiProjects.remove(project);
    eosgiProjects.remove(project);
  }

  private EOSGiProject resolveProject(final IProject project, final IProgressMonitor monitor) {
    IMavenProjectFacade mavenProjectFacade =
        MavenPlugin.getMavenProjectRegistry().getProject(project);
    if (mavenProjectFacade == null) {
      nonEOSGiProjects.add(project);
      return null;
    }

    Map<MojoExecutionKey, List<IPluginExecutionMetadata>> mojoExecutionMapping =
        mavenProjectFacade.getMojoExecutionMapping();

    Set<MojoExecutionKey> executionKeys = mojoExecutionMapping.keySet();

    Set<MojoExecution> eosgiExecutions = new LinkedHashSet<>();
    for (MojoExecutionKey mojoExecutionKey : executionKeys) {
      if (isEOSGiExecution(mojoExecutionKey)) {
        try {
          MojoExecution mojoExecution =
              mavenProjectFacade.getMojoExecution(mojoExecutionKey, monitor);

          eosgiExecutions.add(mojoExecution);
        } catch (CoreException e) {
          throw new RuntimeException(e);
        }
      }
    }

    if (eosgiExecutions.size() == 0) {
      nonEOSGiProjects.add(project);
      return null;
    }
    // TODO
    EOSGiProject eosgiProject = new EOSGiProject(mavenProjectFacade);
    eosgiProjects.put(project, eosgiProject);

    return eosgiProject;
  }

}
