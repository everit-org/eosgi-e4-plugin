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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.embedder.IMavenExecutionContext;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.everit.osgi.dev.e4.plugin.m2e.M2EUtil;
import org.everit.osgi.dev.e4.plugin.m2e.MavenExecutionContextModifiers;

public class ProjectPackager {

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

  private void checkExecutionResultExceptions(final IMavenExecutionContext context) {
    List<Throwable> exceptions = context.getSession().getResult().getExceptions();
    if (exceptions.size() > 0) {
      Throwable throwable = exceptions.get(0);
      if (exceptions instanceof RuntimeException) {
        throw (RuntimeException) throwable;
      } else if (exceptions instanceof Error) {
        throw (Error) throwable;
      } else {
        throw new RuntimeException(throwable);
      }
    }
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

    long oldestLastModified = Long.MAX_VALUE;

    org.eclipse.aether.artifact.Artifact projectArtifact =
        createAetherArtifactFromDescriptionArtifact(descriptionProperties, "artifact.", baseDir);

    if (projectArtifact != null) {
      File projectArtifactFile = projectArtifact.getFile();
      if (!projectArtifactFile.exists()) {
        descriptionFile.delete();
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
        descriptionFile.delete();
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
      descriptionFile.delete();
    } else {
      packagedArtifactContainer.putArtifactsOfMavenProject(mavenProjectFacade,
          new ProjectArtifacts(projectArtifact, attachedArtifacts));
    }
  }

  public void close() {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(changedProjectTracker);

  }

  private String convertMavenArtifactToCoordinates(final Artifact artifact) {
    StringBuilder sb = new StringBuilder(128);
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

  public WorkspaceReader createWorkspaceReader(final WorkspaceReader original) {
    return new EOSGiWorkspaceReader(original, packagedArtifactContainer);
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

  public void open() {
    changedProjectTracker = new ChangedProjectTracker(
        (eclipseProject) -> {
          packagedArtifactContainer.removeArtifactFiles(eclipseProject);
        },
        (eclipseProject) -> packagedArtifactContainer.getProjectArtifacts(eclipseProject));

    ResourcesPlugin.getWorkspace().addResourceChangeListener(changedProjectTracker);
  }

  public void packageProject(final IMavenProjectFacade mavenProjectFacade,
      final IProgressMonitor monitor) throws CoreException {

    IProject eclipseProject = mavenProjectFacade.getProject();

    if (packagedArtifactContainer.getProjectArtifacts(eclipseProject) != null) {
      return;
    }

    checkPackagingResultFile(mavenProjectFacade, monitor);

    if (packagedArtifactContainer.getProjectArtifacts(eclipseProject) != null) {
      return;
    }

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

            checkExecutionResultExceptions(context);
          }
        }
      }, monitor);

      saveOrReplaceAttachedFilesDescription(mavenProject);

      eclipseProject.refreshLocal(IProject.DEPTH_INFINITE, monitor1);

      packagedArtifactContainer.putArtifactsOfMavenProject(mavenProjectFacade,
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

  private long refreshOldestLastModified(long oldestLastModified, final File artifactFile) {
    long lastModified = artifactFile.lastModified();
    if (lastModified < oldestLastModified) {
      oldestLastModified = lastModified;
    }
    return oldestLastModified;
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

  public void setArtifactsOnMavenProject(final MavenProject mavenProject,
      final IProject eclipseProject) {
    ProjectArtifacts projectArtifacts =
        packagedArtifactContainer.getProjectArtifacts(eclipseProject);
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
