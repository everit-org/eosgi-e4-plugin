package org.everit.e4.eosgi.plugin.core.dist;

/**
 * Listener interface for notifing about dist status change.
 */
public interface DistChangeListener {
  void statusChangeEvent(DistStatusEvent event);
}
