package org.everit.e4.eosgi.plugin.core.m2e;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.embedder.IMavenConfigurationChangeListener;
import org.eclipse.m2e.core.embedder.MavenConfigurationChangeEvent;
import org.eclipse.m2e.core.project.IMavenProjectChangedListener;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.everit.e4.eosgi.plugin.core.dist.DistManager;
import org.everit.e4.eosgi.plugin.core.m2e.model.Environment;
import org.everit.e4.eosgi.plugin.core.m2e.model.Environments;
import org.everit.e4.eosgi.plugin.core.m2e.xml.ConfiguratorParser;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.everit.e4.eosgi.plugin.ui.nature.EosgiNature;

/**
 * Default implementation for {@link EosgiManager}.
 */
public class DefaultEosgiManager
    implements EosgiManager, IMavenConfigurationChangeListener,
    IMavenProjectChangedListener {

  private static final String DIST_GOAL = "dist";

  private static final String EOSGI_MAVEN_PLUGIN_ARTIFACT_ID = "eosgi-maven-plugin";

  private static final String EOSGI_MAVEN_PLUGIN_GROUP_ID = "org.everit.osgi.dev";

  private static final Logger LOGGER = Logger.getLogger(DefaultEosgiManager.class.getName());

  private static String createProjectId(final Dependency dependency) {
    return dependency.getGroupId() + dependency.getArtifactId()
        + dependency.getVersion();
  }

  private static String createProjectId(final MavenProject mavenProject) {
    return mavenProject.getGroupId() + mavenProject.getArtifactId()
        + mavenProject.getVersion();
  }

  private IMaven maven;

  private Set<EosgiModelChangeListener> modelChangeListeners = new HashSet<>();

  private Map<String, IProject> projectIdMap = new HashMap<>();

  private Map<IProject, ProjectDescriptor> projectMap = new HashMap<>();

  private IMavenProjectRegistry projectRegistry;

  /**
   * Consturctor.
   */
  public DefaultEosgiManager() {
    super();
    this.projectRegistry = MavenPlugin.getMavenProjectRegistry();
    this.maven = MavenPlugin.getMaven();
    MavenPlugin.getMavenConfiguration().addConfigurationChangeListener(this);
    MavenPlugin.getMavenProjectRegistry().addMavenProjectChangedListener(this);
  }

  @Override
  public void addModelChangeListener(final EosgiModelChangeListener listener) {
    modelChangeListeners.add(listener);
  }

  private void addProjectToIdMap(final String projectId, final IProject relevantProject) {
    if (!projectIdMap.containsKey(projectId)) {
      projectIdMap.put(projectId, relevantProject);
    }
  }

  private void addProjectWithRelevantId(final IProject project, final String relevantId) {
    ProjectDescriptor projectDescriptor = projectMap.get(project);
    if (projectDescriptor != null) {
      projectDescriptor.addProjectId(relevantId);
      projectMap.put(project, projectDescriptor);
    } else {
      Activator.getDefault().error("Update project with relevant project failed");
    }
  }

  @Override
  public List<String> fetchBundlesBy(final IProject project) {
    Objects.requireNonNull(project, "project cannot be null");
    List<String> bundleIdList = new ArrayList<>();
    if (hasProject(project)) {
      ProjectDescriptor projectDescriptor = projectMap.get(project);
      if (projectDescriptor.isDistProject()) {
        Set<String> relevantProjectIds = projectDescriptor.getRelevantProjectIds();
        for (String id : relevantProjectIds) {
          IProject bundleProject = projectIdMap.get(id);
          ProjectDescriptor bundleDescripor = projectMap.get(bundleProject);
          String mavenInfo = bundleDescripor.mavenInfo();
          bundleIdList.add(mavenInfo);
        }
      }
    }
    return bundleIdList;
  }

  @Override
  public List<String> fetchEnvironmentsBy(final IProject project) {
    Objects.requireNonNull(project, "project cannot be null");
    List<String> envList = new ArrayList<>();
    if (!hasProject(project)) {
      return envList;
    }

    ProjectDescriptor projectDescriptor = projectMap.get(project);
    if (projectDescriptor.isDistProject()) {
      return projectDescriptor.getEnvironments();
    }

    return envList;
  }

  private void findAndRegisterDependencies(final ProjectDescriptor projectDescriptor,
      final MavenProject mavenProject, final IProgressMonitor monitor) {
    String distProjectId = createProjectId(mavenProject);
    List<Dependency> dependencies = mavenProject.getDependencies();
    for (Dependency dependency : dependencies) {
      String projectId = createProjectId(dependency);
      IMavenProjectFacade mavenProjectFacade = findMavenProject(dependency);
      if (mavenProjectFacade != null && mavenProjectFacade.getProject() != null) {
        IProject relevantProject = mavenProjectFacade.getProject();
        if (relevantProject != null) {
          addProjectToIdMap(projectId, relevantProject);
          projectDescriptor.addProjectId(projectId);
          registerProject(relevantProject, monitor);
          addProjectWithRelevantId(relevantProject, distProjectId);

          try {
            MavenProject relevantMavenProject = mavenProjectFacade.getMavenProject(monitor);
            findAndRegisterDependencies(projectDescriptor, relevantMavenProject, monitor);
          } catch (CoreException e) {
            Activator.getDefault()
                .error("Can't find maven project for " + relevantProject.getName());
          }
        }
      }
    }
  }

  private IMavenProjectFacade findMavenProject(final Dependency dependency) {
    IMavenProjectFacade depMavenProjectFacade = projectRegistry.getMavenProject(
        dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
    return depMavenProjectFacade;
  }

  private MavenProject findMavenProjectFor(final IProject project, final IProgressMonitor monitor) {
    MavenProject mavenProject = null;

    IMavenProjectFacade mavenProjectFacade = projectRegistry.getProject(project);
    if (mavenProjectFacade == null) {
      Activator.getDefault().error("Not found maven facade for project: " + project.getName());
      return mavenProject;
    }

    try {
      mavenProject = mavenProjectFacade.getMavenProject(monitor);
    } catch (CoreException e) {
      Activator.getDefault()
          .error("find  distribution for " + project.getName() + " - " + e.getMessage());
    }

    if (mavenProject == null) {
      Activator.getDefault().error("Not found MavenProject for project: " + project.getName());
      return mavenProject;
    }
    return mavenProject;
  }

  @Override
  public void generateDistFor(final IProject project, final String environmentId,
      final IProgressMonitor monitor) {
    Objects.requireNonNull(project, "project must be not null");
    Objects.requireNonNull(environmentId, "environmentId must be not null");

    monitor.setTaskName("Fetch maven infomation...");

    IMavenProjectFacade mavenProjectFacade = this.projectRegistry.getProject(project);
    MavenProject mavenProject = null;
    try {
      mavenProject = mavenProjectFacade.getMavenProject(monitor);
    } catch (CoreException e) {
      Activator.getDefault().error("creating distribution for " + project.getName() + "/"
          + environmentId + " - " + e.getMessage());
    }

    if (mavenProject == null) {
      return;
    }

    MojoExecution execution = null;
    try {
      monitor.setTaskName("Fetch execution infomation...");
      List<MojoExecution> mojoExecutions = mavenProjectFacade
          .getMojoExecutions(EOSGI_MAVEN_PLUGIN_GROUP_ID, EOSGI_MAVEN_PLUGIN_ARTIFACT_ID, monitor,
              DIST_GOAL);
      if (mojoExecutions.isEmpty()) {
        return;
      }
      execution = mojoExecutions.get(0);

      monitor.setTaskName("create distribution...");
      maven.execute(mavenProject, execution, monitor);

      DistManager distManager = Activator.getDefault().getDistManager();
      distManager.updateDistStatus(project, environmentId);
    } catch (CoreException e) {
      Activator.getDefault().error("creating distribution for " + project.getName() + "/"
          + environmentId + " - " + e.getMessage());
    }
  }

  private boolean hasProject(final IProject project) {
    return projectMap.containsKey(project);
  }

  @Override
  public void mavenConfigurationChange(final MavenConfigurationChangeEvent event)
      throws CoreException {
    Object newValue = event.getNewValue();
    Object oldValue = event.getOldValue();
    LOGGER.info("");

  }

  @Override
  public void mavenProjectChanged(final MavenProjectChangedEvent[] events,
      final IProgressMonitor monitor) {
    for (MavenProjectChangedEvent mavenProjectChangedEvent : events) {
      processMavenProjectChange(monitor, mavenProjectChangedEvent);
    }
  }

  private void notifyModelChange(final IProject project) {
    for (EosgiModelChangeListener listener : modelChangeListeners) {
      listener.modelChanged(project);
    }
  }

  private void processMavenProjectChange(final IProgressMonitor monitor,
      final MavenProjectChangedEvent mavenProjectChangedEvent) {
    IMavenProjectFacade mavenProjectFacade = mavenProjectChangedEvent.getMavenProject();
    IMavenProjectFacade oldMavenProjectFacade = mavenProjectChangedEvent.getOldMavenProject();

    IProject project = null;
    MavenProject mavenProject = null;
    boolean projectRemoved = false;
    if (mavenProjectFacade != null) {
      project = mavenProjectFacade.getProject();
      mavenProject = mavenProjectFacade.getMavenProject();
    } else {
      projectRemoved = true;
      project = oldMavenProjectFacade.getProject();
      mavenProject = oldMavenProjectFacade.getMavenProject();
    }

    if (project == null || mavenProject == null) {
      Activator.getDefault().error("Maven project update: null project");
      return;
    }

    if (projectRemoved) {
      if (hasProject(project)) {
        removeProject(project, mavenProject);
      }
    } else {
      if (hasProject(project)) {
        updateProject(project, mavenProject, monitor);
      } else {
        registerProject(project, monitor);
      }
    }
  }

  @Override
  public void refreshProject(final IProject project, final MavenProject mavenProject,
      final IProgressMonitor monitor) {
    if (hasProject(project)) {
      updateProject(project, mavenProject, monitor);
    } else {
      registerProject(project, monitor);
    }
  }

  @Override
  public void registerProject(final IProject project, final IProgressMonitor monitor) {
    Objects.requireNonNull(project, "project cannot be null");
    if (hasProject(project)) {
      LOGGER.log(Level.WARNING, project.getName() + " already registered");
      return;
    }

    boolean distProject = false;
    try {
      distProject = project.hasNature(EosgiNature.NATURE_ID);
    } catch (CoreException e) {
      LOGGER.log(Level.WARNING, "check project nature", e);
    }

    MavenProject mavenProject = findMavenProjectFor(project, monitor);
    if (distProject) {
      projectMap.put(project, new ProjectDescriptor(true));
    } else {
      projectMap.put(project, new ProjectDescriptor());
    }

    if (mavenProject == null) {
      LOGGER.log(Level.INFO, "No maven project found for " + project.getName() + ". Skipped.");
    } else {
      updateProject(project, mavenProject, monitor);
    }
  }

  @Override
  public void removeModelChangeListener(final EosgiModelChangeListener listener) {
    modelChangeListeners.remove(listener);
  }

  private void removeProject(final IProject project, final MavenProject mavenProject) {
    Objects.requireNonNull(project, "project cannot be null");
    ProjectDescriptor projectDescriptor = projectMap.get(project);
    if (projectDescriptor.isDistProject()) {
      LOGGER.log(Level.WARNING, "remove dist project?"); // TODO remove dist project?
    } else {
      projectMap.remove(project);
      String projectId = createProjectId(mavenProject);
      Set<String> relevantProjectIds = projectDescriptor.getRelevantProjectIds();
      for (String id : relevantProjectIds) {
        if (projectIdMap.containsKey(id)) {
          ProjectDescriptor relevantDist = projectMap.get(projectIdMap.get(id));
          relevantDist.removeProjectId(id);
        }
      }
      projectIdMap.remove(projectId);
    }
    notifyModelChange(project);
  }

  private void updateBundleProject(final ProjectDescriptor projectDescriptor,
      final MavenProject mavenProject, final IProgressMonitor monitor) {
    Objects.requireNonNull(projectDescriptor, "projectDescriptor cannot be null");
    projectDescriptor.setMavenInfo(mavenProject.getGroupId(), mavenProject.getArtifactId(),
        mavenProject.getVersion());
  }

  private void updateDistProject(final ProjectDescriptor projectDescriptor,
      final MavenProject mavenProject, final IProgressMonitor monitor) {
    Objects.requireNonNull(projectDescriptor, "projectDescriptor cannot be null");

    projectDescriptor.clearProjectIds();
    projectDescriptor.setMavenInfo(mavenProject.getGroupId(), mavenProject.getArtifactId(),
        mavenProject.getVersion());
    projectDescriptor.setBuildDirectory(mavenProject.getBuild().getDirectory());

    findAndRegisterDependencies(projectDescriptor, mavenProject, monitor);
  }

  @Override
  public void updateEnvironments(final IProject project, final Xpp3Dom configuration,
      final IProgressMonitor monitor) {
    Objects.requireNonNull(project, "project cannot be null");

    if (!hasProject(project)) {
      return;
    }

    Environments environments = null;
    try {
      environments = new ConfiguratorParser().parse(configuration);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "process configuration", e);
      return;
    }

    if (environments != null) {
      ProjectDescriptor projectDescriptor = projectMap.get(project);
      if (projectDescriptor.isDistProject()) {
        DistManager distManager = Activator.getDefault().getDistManager();
        projectDescriptor.clearEnvironments();
        for (Environment environment : environments.getEnvironments()) {
          projectDescriptor.addEnvironments(environment);
          if (projectDescriptor.getBuildDirectory() != null) {
            distManager.registerDist(project, environment.getId(),
                projectDescriptor.getBuildDirectory());
          }
        }
        notifyModelChange(project);
      }
    }
  }

  private void updateProject(final IProject project, final MavenProject mavenProject,
      final IProgressMonitor monitor) {
    Objects.requireNonNull(project, "project cannot be null");
    Objects.requireNonNull(mavenProject, "mavenProject cannot be null");

    ProjectDescriptor projectDescriptor = projectMap.get(project);
    if (projectDescriptor.isDistProject()) {
      updateDistProject(projectDescriptor, mavenProject, monitor);

    } else {
      updateBundleProject(projectDescriptor, mavenProject, monitor);
    }

    notifyModelChange(project);
  }
}
