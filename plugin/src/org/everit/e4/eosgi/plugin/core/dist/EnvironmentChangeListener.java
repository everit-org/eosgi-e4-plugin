package org.everit.e4.eosgi.plugin.core.dist;

public interface EnvironmentChangeListener {
  void environmentChanged(String environmentId, DistStatus distStatus);
}
