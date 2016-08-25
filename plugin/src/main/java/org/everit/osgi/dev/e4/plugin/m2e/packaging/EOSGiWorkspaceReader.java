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
import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;

public class EOSGiWorkspaceReader implements WorkspaceReader {

  private final PackagedArtifactContainer packagedArtifactContainer;

  private final WorkspaceReader wrapped;

  public EOSGiWorkspaceReader(final WorkspaceReader wrapped,
      final PackagedArtifactContainer packagedArtifactContainer) {
    this.wrapped = wrapped;
    this.packagedArtifactContainer = packagedArtifactContainer;
  }

  @Override
  public File findArtifact(final Artifact artifact) {
    File result = packagedArtifactContainer.findArtifact(artifact);
    if (result != null) {
      return result;
    }
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
