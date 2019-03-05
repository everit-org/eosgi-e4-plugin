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

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.everit.osgi.dev.e4.plugin.GAV;

/**
 * Reads maven artifacts from the eclipse workspace instead of the local maven repository during
 * running a maven command by m2e.
 *
 */
public class EOSGiWorkspaceReader implements WorkspaceReader {

  private final Set<GAV> currentlyBuildingDependencies = new LinkedHashSet<>();

  private final PackagedArtifactContainer packagedArtifactContainer;

  private final ProjectPackager projectPackager;

  private final WorkspaceReader wrapped;

  /**
   * Constructor.
   *
   * @param wrapped
   *          The wrapped workspace reader that is called if the artifact is not a resource of a
   *          workspace project.
   * @param packagedArtifactContainer
   *          The container that holds the packaged artifacts.
   * @param projectPackager
   *          Util class that packages the projects.
   */
  public EOSGiWorkspaceReader(final WorkspaceReader wrapped,
      final PackagedArtifactContainer packagedArtifactContainer,
      final ProjectPackager projectPackager) {

    this.wrapped = wrapped;
    this.packagedArtifactContainer = packagedArtifactContainer;
    this.projectPackager = projectPackager;
  }

  @Override
  public File findArtifact(final Artifact artifact) {
    File result = packagedArtifactContainer.findArtifact(artifact);
    if (result != null) {
      return result;
    }

    // If the project is on the workspace, try packaging it

    GAV gav = new GAV(artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion());

    IMavenProjectFacade mavenProject =
        MavenPlugin.getMavenProjectRegistry().getMavenProject(gav.groupId, gav.artifactId,
            gav.version);

    if (mavenProject == null) {
      return wrapped.findArtifact(artifact);
    }

    try {
      if (!currentlyBuildingDependencies.add(gav)) {
        throw new RuntimeException("Cyclic building of projects. Requested the build of "
            + gav.toString() + " while the currently building project stack contains "
            + currentlyBuildingDependencies.toString());
      }

      if (!projectPackager.isProjectPackagedAndUpToDate(mavenProject,
          new NullProgressMonitor())) {

        projectPackager.packageProject(mavenProject, new NullProgressMonitor());
      }

      result = packagedArtifactContainer.findArtifact(artifact);

      if (result != null) {
        return result;
      }
    } catch (CoreException e) {
      throw new RuntimeException(e);
    } finally {
      currentlyBuildingDependencies.remove(gav);
    }

    // Return original if project is not on workspace
    return wrapped.findArtifact(artifact);
  }

  @Override
  public List<String> findVersions(final Artifact artifact) {
    return wrapped.findVersions(artifact);
  }

  @Override
  public WorkspaceRepository getRepository() {
    return wrapped.getRepository();
  }

}
