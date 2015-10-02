package org.everit.e4.eosgi.plugin.core.dist;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.everit.e4.eosgi.plugin.ui.EOSGiLog;

/**
 * Default implementation for {@link DistManager} interface.
 */
public class DefaultDistManager implements DistManager, DistChangeListener {

  private Map<IProject, Map<String, DistRunner>> distMap;

  private Set<EnvironmentChangeListener> environmentChangeListeners;

  private EOSGiLog log;

  public DefaultDistManager(EOSGiLog log) {
    super();
    distMap = new HashMap<>();
    environmentChangeListeners = new HashSet<>();
    this.log = log;
  }

  @Override
  public void addEnvironmentChangeListener(final EnvironmentChangeListener listener) {
    this.environmentChangeListeners.add(listener);
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

  @Override
  public boolean isCreated(final IProject project, final String environmentId) {
    if (!hasDist(project, environmentId)) {
      return false;
    }
    DistRunner distRunner = getDistRunner(project, environmentId);
    return distRunner != null && distRunner.isCreated();
  }

  private void notifyEnvironmentChange(final String environmentId, final DistStatus distStatus) {
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
      distRunner = new EOSGiDistRunner(buildDirectory, environmentId, this, project);
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
  public void startDist(final IProject project, final String environmentId) {
    Objects.requireNonNull(project, "project cannot be null");
    Objects.requireNonNull(environmentId, "environment cannot be null");

    DistRunner distRunner = getDistRunner(project, environmentId);
    if (distRunner != null && distRunner.isCreated()) {
      distRunner.start();
    }
  }

  @Override
  public void statusChangeEvent(DistStatusEvent event) {
    notifyEnvironmentChange(event.environmentName, event.distStatus);
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

  @Override
  public void updateDistStatus(final IProject project, final String environmentId) {
    Objects.requireNonNull(project, "project cannot be null");
    Objects.requireNonNull(environmentId, "environment cannot be null");

    DistRunner distRunner = getDistRunner(project, environmentId);
    if (distRunner != null) {
      distRunner.setCreatedStatus(true);
      notifyEnvironmentChange(environmentId, DistStatus.STOPPED);
    }
  }

}
