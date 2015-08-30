package org.everit.e4.eosgi.plugin.core.dist;

import org.eclipse.core.resources.IProject;

/**
 * Listener interface for notifing about dist status change.
 */
public interface DistChangeListener {
  void distStatusChanged(DistStatus distStatus, IProject project, String environmentId);
}
