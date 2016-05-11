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
package org.everit.osgi.dev.e4.plugin.core.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.everit.osgi.dev.e4.plugin.ui.EOSGiPluginActivator;

/**
 * ServerDelegate implementation for EOSGi server.
 */
public class EOSGiServer extends ServerDelegate {

  public static final String SERVER_TYPE_ID = "org.everit.osgi.dev.e4.plugin.server";

  /**
   * Delete the server (stop it if currently running).
   *
   * @param serverId
   *          id of the server.
   * @throws CoreException
   *           throws that if could not delete the server.
   */
  public static void deleteServer(final String serverId) throws CoreException {
    IServer server = ServerCore.findServer(serverId);
    if (server == null) {
      return;
    }

    if (server.getLaunch() != null) {
      EOSGiServerBehaviour eosgiServer = (EOSGiServerBehaviour) server
          .loadAdapter(EOSGiServerBehaviour.class, null);
      if (eosgiServer != null) {
        eosgiServer.stop(true);
      }
    }

    server.delete();
  }

  /**
   * Find or create (if doesn't exist) an IServer instance with given name and runtime.
   *
   * @param serverName
   *          name of the server.
   * @param runtime
   *          runtime instance.
   * @param monitor
   *          optional {@link IProgressMonitor} instance.
   * @return IServer instance.
   * @throws CoreException
   *           throws this if any error occurred.
   */
  public static IServer findServerToEnvironment(final String serverName, final IRuntime runtime,
      final IProgressMonitor monitor) throws CoreException {
    IServerType serverType = ServerCore.findServerType(SERVER_TYPE_ID);
    IServerWorkingCopy serverWorkingCopy = serverType
        .createServer(serverName, null, monitor);
    serverWorkingCopy.setRuntime(runtime);
    serverWorkingCopy.setName(serverName);
    IServer server = serverWorkingCopy.save(true, monitor);
    return server;
  }

  // private EOSGiLog log;

  /**
   * Contrutctor.
   */
  public EOSGiServer() {
    super();
    // ILog iLog = EOSGiPluginActivator.getDefault().getLog();
    // this.log = new EOSGiLog(iLog);
  }

  @Override
  public IStatus canModifyModules(final IModule[] arg0, final IModule[] arg1) {
    return new Status(IStatus.CANCEL, EOSGiPluginActivator.PLUGIN_ID, 0, "", null);
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
