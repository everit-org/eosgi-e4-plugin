package org.everit.e4.eosgi.plugin.core;

public class ModelChangeEvent {
  public Object arg;

  public EventType eventType;

  public ModelChangeEvent arg(final Object arg) {
    this.arg = arg;
    return this;
  }

  public ModelChangeEvent eventType(final EventType eventType) {
    this.eventType = eventType;
    return this;
  }

  @Override
  public String toString() {
    return "ModelChangeEvent [arg=" + arg + ", eventType=" + eventType + "]";
  }

}
