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

/**
 * Helper class to track project artifacts that are on the workspace and compiled by this plugin.
 */
public class PackagedArtifactContainer {

  /**
   * DTO.
   */
  private static class ClassifierAndExtension {

    String classifier;

    String extension;

    ClassifierAndExtension(final String classifier, final String extension) {
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

  /**
   * Creates a unique key for an artifact.
   *
   * @param groupId
   *          The group id of the maven artifact.
   * @param artifactId
   *          The artifact id of the maven artifact.
   * @param version
   *          The version of the maven artifact.
   * @return A unique key of the maven artifact.
   */
  private static String createArtifactKey(final String groupId, final String artifactId,
      final String version) {
    return groupId + ':' + artifactId + ':' + version;
  }

  private final Map<IProject, String> artifactKeyByEclipseProject = new HashMap<>();

  private final Map<String, Map<ClassifierAndExtension, File>> gavAndFileByClassifierAndExtMap =
      new HashMap<>();

  private final Map<IProject, ProjectArtifacts> projectArtifactsByEclipseProject = new HashMap<>();

  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  /**
   * Finds an artifact file if exists.
   *
   * @param artifact
   *          The maven artifact.
   * @return The file of the maven artifact.
   */
  public File findArtifact(final Artifact artifact) {
    Lock readLock = readWriteLock.readLock();
    readLock.lock();
    try {
      Map<ClassifierAndExtension, File> fileByClassifierAndExtensionMap =
          gavAndFileByClassifierAndExtMap.get(createArtifactKey(artifact.getGroupId(),
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

  /**
   * Provides all of the maven artifacts that belong to an eclipse project.
   *
   * @param eclipseProject
   *          The maven-eclipse project.
   * @return The artifacts that are created with maven after running mvn package.
   */
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

  /**
   * Add artifact files of a maven project.
   *
   * @param mavenProjectFacade
   *          The eclipse-maven project.
   * @param projectArtifacts
   *          The artifacts of the eclipse-maven-project.
   */
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
      gavAndFileByClassifierAndExtMap.put(artifactKey, fileByClassifierAndExtensionMap);
      projectArtifactsByEclipseProject.put(eclipseProject, projectArtifacts);
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Remove maven artifact files of an eclipse project.
   *
   * @param eclipseProject
   *          The eclipse project that the artifact files belonged to.
   */
  public void removeArtifactFiles(final IProject eclipseProject) {
    Lock writeLock = readWriteLock.writeLock();
    writeLock.lock();
    try {
      String artifactKey = artifactKeyByEclipseProject.remove(eclipseProject);
      if (artifactKey != null) {
        gavAndFileByClassifierAndExtMap.remove(artifactKey);
      }
      projectArtifactsByEclipseProject.remove(eclipseProject);
    } finally {
      writeLock.unlock();
    }
  }

}
