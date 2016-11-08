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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Generated;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.core.resources.IProject;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

public class PackagedArtifactContainer {

  private static class ClassifierAndExtension {

    String classifier;

    String extension;

    public ClassifierAndExtension(final String classifier, final String extension) {
      this.classifier = ((classifier != null) ? classifier : "").trim();
      this.extension = ((extension != null) ? extension : "").trim();
    }

    @Override
    @Generated("eclipse")
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      ClassifierAndExtension other = (ClassifierAndExtension) obj;
      if (classifier == null) {
        if (other.classifier != null) {
          return false;
        }
      } else if (!classifier.equals(other.classifier)) {
        return false;
      }
      if (extension == null) {
        if (other.extension != null) {
          return false;
        }
      } else if (!extension.equals(other.extension)) {
        return false;
      }
      return true;
    }

    @Override
    @Generated("eclipse")
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((classifier == null) ? 0 : classifier.hashCode());
      result = prime * result + ((extension == null) ? 0 : extension.hashCode());
      return result;
    }
  }

  private static String createArtifactKey(final String groupId, final String artifactId,
      final String version) {
    return groupId + ':' + artifactId + ':' + version;
  }

  private final Map<IProject, String> artifactKeyByEclipseProject = new HashMap<>();

  private final Map<String, Map<ClassifierAndExtension, File>> gavAndFileByClassifierAndExtensionMap =
      new HashMap<>();

  private final Map<IProject, ProjectArtifacts> projectArtifactsByEclipseProject = new HashMap<>();

  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  public File findArtifact(final Artifact artifact) {
    Lock readLock = readWriteLock.readLock();
    readLock.lock();
    try {
      Map<ClassifierAndExtension, File> fileByClassifierAndExtensionMap =
          gavAndFileByClassifierAndExtensionMap.get(createArtifactKey(artifact.getGroupId(),
              artifact.getArtifactId(), artifact.getBaseVersion()));

      if (fileByClassifierAndExtensionMap != null) {
        ClassifierAndExtension classifierAndExtension =
            new ClassifierAndExtension(artifact.getClassifier(), artifact.getExtension());

        File file = fileByClassifierAndExtensionMap.get(classifierAndExtension);
        if (file != null) {
          return file;
        }
      }
      return null;
    } finally {
      readLock.unlock();
    }
  }

  public ProjectArtifacts getProjectArtifacts(final IProject eclipseProject) {
    Lock readLock = readWriteLock.readLock();
    readLock.lock();
    try {
      return projectArtifactsByEclipseProject.get(eclipseProject);
    } finally {
      readLock.unlock();
    }
  }

  private void putArtifact(final Artifact artifact,
      final Map<ClassifierAndExtension, File> fileByClassifierAndExtensionMap) {

    File artifactFile = artifact.getFile();

    if (artifactFile == null) {
      return;
    }

    String classifier = artifact.getClassifier();
    String extension = artifact.getExtension();

    ClassifierAndExtension classifierAndExtension =
        new ClassifierAndExtension(classifier, extension);
    fileByClassifierAndExtensionMap.put(classifierAndExtension, artifactFile);
  }

  public void putArtifactsOfMavenProject(final IMavenProjectFacade mavenProjectFacade,
      final ProjectArtifacts projectArtifacts) {

    Lock writeLock = readWriteLock.writeLock();
    writeLock.lock();
    try {
      ArtifactKey artifactKeyObj = mavenProjectFacade.getArtifactKey();

      String artifactKey =
          createArtifactKey(artifactKeyObj.getGroupId(), artifactKeyObj.getArtifactId(),
              artifactKeyObj.getVersion());

      IProject eclipseProject = mavenProjectFacade.getProject();

      artifactKeyByEclipseProject.put(eclipseProject, artifactKey);

      Map<ClassifierAndExtension, File> fileByClassifierAndExtensionMap = new HashMap<>();

      if (projectArtifacts.artifact != null) {
        putArtifact(projectArtifacts.artifact, fileByClassifierAndExtensionMap);
      }

      for (Artifact artifact : projectArtifacts.attachedArtifacts) {
        putArtifact(artifact, fileByClassifierAndExtensionMap);
      }
      gavAndFileByClassifierAndExtensionMap.put(artifactKey, fileByClassifierAndExtensionMap);
      projectArtifactsByEclipseProject.put(eclipseProject, projectArtifacts);
    } finally {
      writeLock.unlock();
    }
  }

  public void removeArtifactFiles(final IProject eclipseProject) {
    Lock writeLock = readWriteLock.writeLock();
    writeLock.lock();
    try {
      String artifactKey = artifactKeyByEclipseProject.remove(eclipseProject);
      if (artifactKey != null) {
        gavAndFileByClassifierAndExtensionMap.remove(artifactKey);
      }
      projectArtifactsByEclipseProject.remove(eclipseProject);
    } finally {
      writeLock.unlock();
    }
  }

}
