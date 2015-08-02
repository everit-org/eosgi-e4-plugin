package org.everit.e4.eosgi.plugin.dist;

/**
 * Listener interface for notifing about dist status change.
 */
public interface DistStatusListener {
  void distStatusChanged(DistStatus distStatus);
}
