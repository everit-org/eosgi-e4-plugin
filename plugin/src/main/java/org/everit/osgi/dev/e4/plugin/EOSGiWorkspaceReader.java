package org.everit.osgi.dev.e4.plugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Generated;

import org.apache.maven.project.MavenProject;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;
import org.eclipse.core.resources.IProject;

public class EOSGiWorkspaceReader implements WorkspaceReader {

  private static class ClassifierAndExtension {

    String classifier;

    String extension;

    public ClassifierAndExtension(final String classifier, final String extension) {
      this.classifier = classifier;
      this.extension = extension;
    }

    @Override
    @Generated("eclipse")
    public boolean equals(final Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ClassifierAndExtension other = (ClassifierAndExtension) obj;
      if (classifier == null) {
        if (other.classifier != null)
          return false;
      } else if (!classifier.equals(other.classifier))
        return false;
      if (extension == null) {
        if (other.extension != null)
          return false;
      } else if (!extension.equals(other.extension))
        return false;
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

  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  private final WorkspaceReader wrapped;

  public EOSGiWorkspaceReader(final WorkspaceReader wrapped) {
    this.wrapped = wrapped;
  }

  @Override
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
      return wrapped.findArtifact(artifact);
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public List<String> findVersions(final Artifact artifact) {
    return wrapped.findVersions(artifact);
  }

  @Override
  public WorkspaceRepository getRepository() {
    return wrapped.getRepository();
  }

  private void putArtifact(final org.apache.maven.artifact.Artifact artifact,
      final Map<ClassifierAndExtension, File> fileByClassifierAndExtensionMap) {

    String classifier = artifact.getClassifier();
    String extension = artifact.getArtifactHandler().getExtension();

    ClassifierAndExtension classifierAndExtension =
        new ClassifierAndExtension(classifier, extension);
    fileByClassifierAndExtensionMap.put(classifierAndExtension, artifact.getFile());

  }

  public void putArtifactsOfMavenProject(final MavenProject mavenProject,
      final IProject eclipseProject) {

    Lock writeLock = readWriteLock.writeLock();
    writeLock.lock();
    try {

      String artifactKey =
          createArtifactKey(mavenProject.getGroupId(), mavenProject.getArtifactId(),
              mavenProject.getVersion());

      artifactKeyByEclipseProject.put(eclipseProject, artifactKey);

      Map<ClassifierAndExtension, File> fileByClassifierAndExtensionMap = new HashMap<>();

      putArtifact(mavenProject.getArtifact(), fileByClassifierAndExtensionMap);

      for (org.apache.maven.artifact.Artifact artifact : mavenProject.getAttachedArtifacts()) {
        putArtifact(artifact, fileByClassifierAndExtensionMap);
      }
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
    } finally {
      writeLock.unlock();
    }
  }

}
