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
package org.everit.e4.eosgi.plugin.core.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.everit.e4.eosgi.plugin.ui.EOSGiLog;
import org.everit.e4.eosgi.plugin.ui.EOSGiPluginActivator;

/**
 * ServerDelegate implementation for EOSGi server.
 */
public class EOSGiServer extends ServerDelegate {

  private EOSGiLog log;

  /**
   * Contrutctor.
   */
  public EOSGiServer() {
    super();
    ILog iLog = EOSGiPluginActivator.getDefault().getLog();
    this.log = new EOSGiLog(iLog);
  }

  @Override
  public IStatus canModifyModules(final IModule[] arg0, final IModule[] arg1) {
    return new Status(IStatus.CANCEL, EOSGiPluginActivator.PLUGIN_ID, 0, "", null);
  }

  @Override
  protected void finalize() throws Throwable {
    log.info("server finalized");
    super.finalize();
  }

  @Override
  public IModule[] getChildModules(final IModule[] arg0) {
    return new IModule[0];
  }

  @Override
  public IModule[] getRootModules(final IModule module) throws CoreException {
    return new IModule[] { module };
  }

  @Override
  public void modifyModules(final IModule[] arg0, final IModule[] arg1, final IProgressMonitor arg2)
      throws CoreException {
  }

}
