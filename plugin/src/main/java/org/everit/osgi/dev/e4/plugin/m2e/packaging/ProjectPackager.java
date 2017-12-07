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

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.lifecycle.MavenExecutionPlan;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.artifact.ArtifactProperties;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.everit.osgi.dev.e4.plugin.m2e.M2EUtil;
import org.everit.osgi.dev.e4.plugin.m2e.MavenExecutionContextModifiers;

/**
 * Helper class to package a project with m2e maven.
 */
public class ProjectPackager implements Closeable {

  private static final Set<String> SKIPPED_LIFECYCLE_PHASES;

  static {
    SKIPPED_LIFECYCLE_PHASES = new HashSet<>();

    SKIPPED_LIFECYCLE_PHASES.add("test");
  }

  private ChangedProjectTracker changedProjectTracker;

  private final PackagedArtifactContainer packagedArtifactContainer =
      new PackagedArtifactContainer();

  private boolean addArtifactToProps(final Path projectBaseDirPath, final Properties props,
      final Artifact artifact, final String artifactPropKeyPrefix) {

    boolean appendedToProps;
    File artifactFile = artifact.getFile();
    if (artifactFile != null) {
      props.setProperty(artifactPropKeyPrefix + "file",
          projectBaseDirPath.relativize(artifactFile.toPath()).toString());

      String coordinates = convertMavenArtifactToCoordinates(artifact);
      props.setProperty(artifactPropKeyPrefix + "coordinates", coordinates);

      props.setProperty(artifactPropKeyPrefix + "type", artifact.getType());
      appendedToProps = true;
    } else {
      appendedToProps = false;
    }
    return appendedToProps;
  }

  private void checkPackagingResultFile(final IMavenProjectFacade mavenProjectFacade,
      final IProgressMonitor monitor) throws CoreException {
    MavenProject mavenProject = mavenProjectFacade.getMavenProject(monitor);
    File descriptionFile = resolveAttachedFilesDescriptionFile(mavenProject);

    if (!descriptionFile.exists()) {
      return;
    }

    Properties descriptionProperties = readDescriptionFileToPropertiesObj(descriptionFile);

    File baseDir = mavenProject.getBasedir();

    long oldestLastModified = descriptionFile.lastModified();

    org.eclipse.aether.artifact.Artifact projectArtifact =
        createAetherArtifactFromDescriptionArtifact(descriptionProperties, "artifact.", baseDir);

    if (projectArtifact != null) {
      File projectArtifactFile = projectArtifact.getFile();
      if (!projectArtifactFile.exists()) {
        deleteFile(descriptionFile);
        return;
      }

      oldestLastModified = refreshOldestLastModified(oldestLastModified, projectArtifactFile);
    }

    Set<org.eclipse.aether.artifact.Artifact> attachedArtifacts = new HashSet<>();

    int i = 0;
    String artifactPropPrefix = "attachedArtifact." + i + '.';

    org.eclipse.aether.artifact.Artifact attachedArtifact =
        createAetherArtifactFromDescriptionArtifact(descriptionProperties, artifactPropPrefix,
            baseDir);

    while (attachedArtifact != null) {

      File attachedArtifactFile = attachedArtifact.getFile();
      if (!attachedArtifactFile.exists()) {
        deleteFile(descriptionFile);
        return;
      }

      attachedArtifacts.add(attachedArtifact);
      oldestLastModified = refreshOldestLastModified(oldestLastModified, attachedArtifactFile);

      i++;
      artifactPropPrefix = "attachedArtifact." + i + '.';
      attachedArtifact =
          createAetherArtifactFromDescriptionArtifact(descriptionProperties, artifactPropPrefix,
              baseDir);
    }

    // We have an oldest timestamp. Let's see if any of the files is newer.
    if (nonTargetFileExistThatIsChangedLater(baseDir,
        new File(mavenProject.getBuild().getDirectory()),
        oldestLastModified)) {
      deleteFile(descriptionFile);
    } else {
      this.packagedArtifactContainer.putArtifactsOfMavenProject(mavenProjectFacade,
          new ProjectArtifacts(projectArtifact, attachedArtifacts));
    }
  }

  @Override
  public void close() {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(this.changedProjectTracker);

  }

  private String convertMavenArtifactToCoordinates(final Artifact artifact) {
    StringBuilder sb = new StringBuilder();
    sb.append(artifact.getGroupId());
    sb.append(':').append(artifact.getArtifactId());
    sb.append(':').append(artifact.getArtifactHandler().getExtension());
    String classifier = artifact.getClassifier();
    if (classifier != null && !classifier.isEmpty()) {
      sb.append(':').append(classifier);
    }
    sb.append(':').append(artifact.getBaseVersion());
    String coordinates = sb.toString();
    return coordinates;
  }

  private org.eclipse.aether.artifact.Artifact createAetherArtifactFromDescriptionArtifact(
      final Properties descriptionProperties, final String artifactPropPrefix,
      final File baseDirectory) {

    String artifactFileName = descriptionProperties.getProperty(artifactPropPrefix + "file");

    if (artifactFileName == null) {
      return null;
    }

    File artifactFile = new File(baseDirectory, artifactFileName);

    String coordinates = descriptionProperties.getProperty(artifactPropPrefix + "coordinates");

    Map<String, String> properties = new HashMap<>();
    properties.put(ArtifactProperties.TYPE,
        descriptionProperties.getProperty(artifactPropPrefix + "type"));

    org.eclipse.aether.artifact.Artifact artifact = new DefaultArtifact(coordinates, properties);
    artifact = artifact.setFile(artifactFile);
    return artifact;
  }

  private Properties createEOSGiPackagingProps(final File projectBaseDir,
      final Artifact artifact, final Collection<Artifact> attachedArtifacts) {

    Path projectBaseDirPath = projectBaseDir.toPath();

    Properties props = new Properties();
    props.setProperty("eosgi.version", "4.0.0");

    if (artifact != null) {
      addArtifactToProps(projectBaseDirPath, props, artifact, "artifact.");
    }

    int i = 0;
    for (Artifact attachedArtifact : attachedArtifacts) {
      String artifactPropKeyPrefix = "attachedArtifact." + i + ".";
      if (addArtifactToProps(projectBaseDirPath, props, attachedArtifact, artifactPropKeyPrefix)) {
        i++;
      }
    }
    return props;
  }

  /**
   * Creates a workspace reader for m2e that resolves maven modules that are eclipse projects on the
   * current workspace and resolves artifact files from the workspace instead of local maven
   * repository.
   *
   * @param original
   *          The original workspace reader of the maven execution context.
   * @return A workspace reader that searches artifact files first in the eclipse workspace.
   */
  public WorkspaceReader createWorkspaceReader(final WorkspaceReader original) {
    return new EOSGiWorkspaceReader(original, this.packagedArtifactContainer);
  }

  private void deleteFile(final File file) {
    if (file.exists() && !file.delete()) {
      throw new RuntimeException("Cannot delete file: " + file);
    }
  }

  /**
   * Checks whether the project has any modifications since last successful m2e package.
   *
   * @param mavenProjectFacade
   *          The m2e project facade.
   * @param monitor
   *          The monitor to show progress.
   * @return true if there was no file changed in the project since last build.
   * @throws CoreException
   *           if something happens.
   */
  public boolean isProjectPackagedAndUpToDate(final IMavenProjectFacade mavenProjectFacade,
      final IProgressMonitor monitor) throws CoreException {

    IProject eclipseProject = mavenProjectFacade.getProject();

    if (this.packagedArtifactContainer.getProjectArtifacts(eclipseProject) != null) {
      return true;
    }

    checkPackagingResultFile(mavenProjectFacade, monitor);

    return this.packagedArtifactContainer.getProjectArtifacts(eclipseProject) != null;
  }

  private boolean nonTargetFileExistThatIsChangedLater(final File basedir,
      final File buildDirectoryFile,
      final long oldestLastModified) {

    File[] files = basedir.listFiles();
    if (files == null) {
      return false;
    }

    for (File file : files) {
      if (!file.equals(buildDirectoryFile)) {
        if (file.lastModified() > oldestLastModified) {
          return true;
        }

        if (file.isDirectory()
            && nonTargetFileExistThatIsChangedLater(file, buildDirectoryFile, oldestLastModified)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Starts tracking of the eclipse projects.
   */
  public void open() {
    this.changedProjectTracker = new ChangedProjectTracker(
        (eclipseProject) -> {
          this.packagedArtifactContainer.removeArtifactFiles(eclipseProject);
        },
        (eclipseProject) -> this.packagedArtifactContainer.getProjectArtifacts(eclipseProject));

    ResourcesPlugin.getWorkspace().addResourceChangeListener(this.changedProjectTracker);
  }

  /**
   * Packages a maven project that is on the eclipse workspace and adds its artifact files to the
   * workspace reader.
   *
   * @param mavenProjectFacade
   *          The m2e project.
   * @param monitor
   *          The monitor to show progress.
   * @throws CoreException
   *           if anything happens.
   */
  public void packageProject(final IMavenProjectFacade mavenProjectFacade,
      final IProgressMonitor monitor) throws CoreException {

    mavenProjectFacade.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);

    MavenExecutionContextModifiers modifiers = new MavenExecutionContextModifiers();
    modifiers.workspaceReaderReplacer = (original) -> createWorkspaceReader(original);

    M2EUtil.executeInContext(mavenProjectFacade, modifiers, (context, monitor1) -> {
      IMaven maven = MavenPlugin.getMaven();

      MavenProject mavenProject = mavenProjectFacade.getMavenProject(monitor);

      MavenExecutionPlan executionPlan =
          maven.calculateExecutionPlan(mavenProject,
              Arrays.asList(new String[] { "package" }), true, monitor);

      List<MojoExecution> mojoExecutions = executionPlan.getMojoExecutions();

      M2EUtil.executeWithMutableProjectState(mavenProject, (monitor2) -> {
        for (MojoExecution mojoExecution : mojoExecutions) {
          if (!SKIPPED_LIFECYCLE_PHASES.contains(mojoExecution.getLifecyclePhase())) {
            maven.execute(mavenProject, mojoExecution, monitor);

            M2EUtil.checkExecutionResultExceptions(context,
                "Error during packaging project: " + mavenProjectFacade.getProject().getName());
          }
        }
      }, monitor);

      saveOrReplaceAttachedFilesDescription(mavenProject);

      mavenProjectFacade.getProject().refreshLocal(IProject.DEPTH_INFINITE, monitor1);

      this.packagedArtifactContainer.putArtifactsOfMavenProject(mavenProjectFacade,
          new ProjectArtifacts(toAetherArtifact(mavenProject.getArtifact()),
              RepositoryUtils.toArtifacts(mavenProject.getAttachedArtifacts())));
      return null;
    }, monitor);
  }

  private Properties readDescriptionFileToPropertiesObj(final File descriptionFile) {
    Properties props = new Properties();
    try (FileInputStream fin = new FileInputStream(descriptionFile)) {
      props.load(fin);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return props;
  }

  private long refreshOldestLastModified(final long oldestLastModified, final File artifactFile) {
    long lastModified = artifactFile.lastModified();
    return (lastModified < oldestLastModified) ? lastModified : oldestLastModified;
  }

  private File resolveAttachedFilesDescriptionFile(final MavenProject mavenProject) {
    String buildDirectory = mavenProject.getBuild().getDirectory();
    File buildDirectoryFile = new File(buildDirectory);
    File attachedFilesDescriptionFile =
        new File(buildDirectoryFile, ".eosgi-e4-packaging-result.properties");
    return attachedFilesDescriptionFile;
  }

  private void saveOrReplaceAttachedFilesDescription(final MavenProject mavenProject) {

    File attachedFilesDescriptionFile = resolveAttachedFilesDescriptionFile(mavenProject);

    File descriptorFileFolder = attachedFilesDescriptionFile.getParentFile();

    if (!descriptorFileFolder.exists() && !descriptorFileFolder.mkdirs()) {
      throw new UncheckedIOException(new IOException(
          "Cannot create directory of attachedDescriptorFile: " + descriptorFileFolder.toString()));
    }

    Properties props =
        createEOSGiPackagingProps(mavenProject.getBasedir(), mavenProject.getArtifact(),
            mavenProject.getAttachedArtifacts());

    try (FileOutputStream fout = new FileOutputStream(attachedFilesDescriptionFile)) {
      props.store(fout, null);
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Cannot create description file after calling package: " + attachedFilesDescriptionFile,
          e);
    }

  }

  /**
   * Sets artifact files to a m2e project. So all created workspace readers will now about them.
   *
   * @param mavenProject
   *          The m2e project.
   * @param eclipseProject
   *          The eclipse project.
   */
  public void setArtifactsOnMavenProject(final MavenProject mavenProject,
      final IProject eclipseProject) {
    ProjectArtifacts projectArtifacts =
        this.packagedArtifactContainer.getProjectArtifacts(eclipseProject);
    if (projectArtifacts == null) {
      return;
    }

    if (projectArtifacts.artifact != null) {
      mavenProject.setArtifact(RepositoryUtils.toArtifact(projectArtifacts.artifact));
    }

    List<Artifact> attachedArtifacts = mavenProject.getAttachedArtifacts();
    attachedArtifacts.clear();
    for (org.eclipse.aether.artifact.Artifact artifact : projectArtifacts.attachedArtifacts) {
      mavenProject.addAttachedArtifact(RepositoryUtils.toArtifact(artifact));
    }
  }

  private org.eclipse.aether.artifact.Artifact toAetherArtifact(final Artifact artifact) {
    if (artifact == null) {
      return null;
    }

    return RepositoryUtils.toArtifact(artifact);
  }
}
