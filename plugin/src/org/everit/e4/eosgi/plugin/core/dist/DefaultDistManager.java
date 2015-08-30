package org.everit.e4.eosgi.plugin.core.dist;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.resources.IProject;

public class DefaultDistManager implements DistManager, DistChangeListener {

  private Map<IProject, Map<String, DistRunner>> distMap;

  private Set<EnvironmentChangeListener> environmentChangeListeners;

  public DefaultDistManager() {
    super();
    distMap = new HashMap<>();
    environmentChangeListeners = new HashSet<>();
  }

  @Override
  public void addEnvironmentChangeListener(final EnvironmentChangeListener listener) {
    this.environmentChangeListeners.add(listener);
  }

  @Override
  public void distStartable(IProject project, String environmentId) {
    Objects.requireNonNull(project, "project cannot be null");
    Objects.requireNonNull(environmentId, "environment cannot be null");

    DistRunner distRunner = getDistRunner(project, environmentId);
    if (distRunner != null) {
      distRunner.startable();
    }
  }

  @Override
  public void distStatusChanged(final DistStatus distStatus, final IProject project,
      final String environmentId) {
    notifyEnvironmentChange(environmentId, distStatus);
  }

  private DistRunner getDistRunner(final IProject project, final String environmentId) {
    DistRunner distRunner = null;
    if (distMap.containsKey(project) && distMap.get(project).containsKey(environmentId)) {
      distRunner = distMap.get(project).get(environmentId);
    }
    return distRunner;
  }

  @Override
  public boolean hasDist(final IProject project, final String environmentId) {
    return getDistRunner(project, environmentId) != null;
  }

  private void notifyEnvironmentChange(String environmentId, DistStatus distStatus) {
    for (EnvironmentChangeListener listener : environmentChangeListeners) {
      listener.environmentChanged(environmentId, distStatus);
    }
  }

  @Override
  public void registerDist(final IProject project, final String environmentId,
      final String buildDirectory) {
    Objects.requireNonNull(project, "project cannot be null");
    Objects.requireNonNull(environmentId, "environmentId cannot be null");

    DistRunner distRunner = getDistRunner(project, environmentId);
    if (distRunner == null) {
      distRunner = new DefaultDistRunner(project, environmentId, buildDirectory, this);
      Map<String, DistRunner> environments = null;
      if (distMap.containsKey(project)) {
        environments = distMap.get(project);
      } else {
        environments = new HashMap<>();
      }
      environments.put(environmentId, distRunner);
      distMap.put(project, environments);
    }
  }

  @Override
  public boolean startable(final IProject project, final String environmentId) {
    if (!hasDist(project, environmentId)) {
      return false;
    }
    DistRunner distRunner = getDistRunner(project, environmentId);
    return distRunner != null && distRunner.isStartable();
  }

  @Override
  public void startDist(final IProject project, final String environmentId) {
    Objects.requireNonNull(project, "project cannot be null");
    Objects.requireNonNull(environmentId, "environment cannot be null");

    DistRunner distRunner = getDistRunner(project, environmentId);
    if (distRunner != null && distRunner.isStartable()) {
      distRunner.start();
    }
  }

  @Override
  public void stopDist(final IProject project, final String environmentId) {
    Objects.requireNonNull(project, "project cannot be null");
    Objects.requireNonNull(environmentId, "environment cannot be null");

    DistRunner distRunner = getDistRunner(project, environmentId);
    if (distRunner != null) {
      distRunner.stop();
    }
  }

}
