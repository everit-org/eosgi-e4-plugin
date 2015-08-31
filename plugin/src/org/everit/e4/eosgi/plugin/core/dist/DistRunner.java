package org.everit.e4.eosgi.plugin.core.dist;

/**
 * Interface for running EOSGI dists.
 */
public interface DistRunner {

  boolean isCreated();

  void start();

  void setCreatedStatus(boolean createdStatus);

  void stop();
}
