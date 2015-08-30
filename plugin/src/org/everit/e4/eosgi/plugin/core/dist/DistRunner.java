package org.everit.e4.eosgi.plugin.core.dist;

/**
 * Interface for running EOSGI dists.
 */
public interface DistRunner {

  boolean isStartable();

  void start();

  void startable();

  void stop();
}
