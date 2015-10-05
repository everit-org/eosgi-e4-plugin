package org.everit.e4.eosgi.plugin.core.dist;

/**
 * Interface for running EOSGI dists.
 */
public interface DistRunner {

  void forcedStop();

  boolean isRunning();

  void start();

  void stop();
}
