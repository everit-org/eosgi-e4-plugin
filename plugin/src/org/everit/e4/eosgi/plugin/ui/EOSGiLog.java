package org.everit.e4.eosgi.plugin.ui;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class EOSGiLog {

  private ILog log;

  public EOSGiLog(final ILog log) {
    super();
    this.log = log;
  }

  public void error(final String message) {
    log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, message));
  }

  public void error(final String message, final Throwable throwable) {
    log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, throwable));
  }

  public void info(final String message) {
    log.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, message));
  }

  public void warning(final String message) {
    log.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, message));
  }

}
