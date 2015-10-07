package org.everit.e4.eosgi.plugin.core.dist;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface for running EOSGI dists.
 */
public interface DistRunner {

  void forcedStop();

  boolean isRunning();

  void start(IProgressMonitor monitor);

  void stop(IProgressMonitor monitor);
}
