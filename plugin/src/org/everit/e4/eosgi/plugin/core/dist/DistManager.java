package org.everit.e4.eosgi.plugin.core.dist;

import org.eclipse.core.resources.IProject;

public interface DistManager {

  void addEnvironmentChangeListener(EnvironmentChangeListener listener);

  void updateDistStatus(IProject project, String environmentId);

  boolean hasDist(IProject project, String environmentId);

  void registerDist(IProject project, String environmentId, String buildDirectory);

  boolean isCreated(IProject project, String environmentId);

  void startDist(IProject project, String environmentId);

  void stopDist(IProject project, String environmentId);
}
