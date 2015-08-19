package org.everit.e4.eosgi.plugin.dist;

/**
 * Interface for running EOSGI dists.
 */
public interface DistRunner {

  void onStatusChanged(DistStatus distStatus);

  void start();

  void stop();
}
