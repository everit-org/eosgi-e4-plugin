/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.osgi.dev.e4.plugin.ui;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Eclipse Log wrapper class.
 */
public class EOSGiLog {

  private ILog log;

  public EOSGiLog(final ILog log) {
    super();
    this.log = log;
  }

  public void error(final String message) {
    log.log(new Status(IStatus.ERROR, EOSGiEclipsePlugin.PLUGIN_ID, message));
  }

  public void error(final String message, final Throwable throwable) {
    log.log(new Status(IStatus.ERROR, EOSGiEclipsePlugin.PLUGIN_ID, message, throwable));
  }

  public void info(final String message) {
    log.log(new Status(IStatus.INFO, EOSGiEclipsePlugin.PLUGIN_ID, message));
  }

  public void warning(final String message) {
    log.log(new Status(IStatus.WARNING, EOSGiEclipsePlugin.PLUGIN_ID, message));
  }

}
