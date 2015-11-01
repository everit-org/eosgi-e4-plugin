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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.everit.e4.eosgi.plugin.ui.EOSGiLog;
import org.everit.e4.eosgi.plugin.ui.EOSGiPluginActivator;

/**
 * Behavoiur implementation for EOSGi server.
 */
public class EOSGiServerBehaviour extends ServerBehaviourDelegate {

  /**
   * Thread for tracking server status.
   */
  private static final class ServerStatusThread extends Thread {

    private static final int CHECK_INTERVAL = 2000;

    private EOSGiServerBehaviour serverBehaviour;

    private final AtomicBoolean stop = new AtomicBoolean(false);

    /**
     * Contructor.
     *
     * @param serverBehaviour
     *          server behaviour implementation.
     */
    public ServerStatusThread(final EOSGiServerBehaviour serverBehaviour) {
      this.serverBehaviour = serverBehaviour;
    }

    @Override
    public void run() {
      IServer server = serverBehaviour.getServer();
      if (server == null) {
        return;
      }

      while (!stop.get() && server.getLaunch() == null) {
        waitASec();
      }

      serverBehaviour.serverStarted();

      while (!stop.get()) {
        if (server.getLaunch() == null) {
          stopThisThread();
          break;
        }
        waitASec();
      }
    }

    private void stopThisThread() {
      serverBehaviour.setStopState();
      stop.set(true);
    }

    private void waitASec() {
      try {
        Thread.sleep(CHECK_INTERVAL);
      } catch (InterruptedException e) {
        stop.set(true);
        Thread.currentThread().interrupt();
      }
    }
  }

  private EOSGiLog log;

  private ServerStatusThread serverStatusThread;

  /**
   * Contructor.
   */
  public EOSGiServerBehaviour() {
    super();
    ILog iLog = EOSGiPluginActivator.getDefault().getLog();
    log = new EOSGiLog(iLog);
  }

  private void createAndStartPingThread() {
    serverStatusThread = new ServerStatusThread(this);
    serverStatusThread.start();
  }

  // public EOSGiRuntime getEOSGiRuntime() {
  // if (getServer().getRuntime() == null) {
  // return null;
  // }
  // return (EOSGiRuntime) getServer().getRuntime().loadAdapter(EOSGiRuntime.class, null);
  // }

  public EOSGiServer getEOSGiServer() {
    return getServer().getAdapter(EOSGiServer.class);
  }

  /**
   * Returns the runtime base path for relative paths in the server configuration.
   *
   * @return the base path
   */
  public IPath getRuntimeBaseDirectory() {
    return getServer().getRuntime().getLocation();
  }

  @Override
  protected void initialize(final IProgressMonitor monitor) {
    super.initialize(monitor);

  }

  @Override
  public void publish(final int kind, final List<IModule[]> modules, final IProgressMonitor monitor,
      final IAdaptable info)
          throws CoreException {
    setServerPublishState(IServer.STATE_STARTED);
    super.publish(kind, modules, monitor, info);
  }

  public void serverStarted() {
    setServerState(IServer.STATE_STARTED);
  }

  /**
   * Notify about the server starting.
   */
  public void serverStarting() {
    setServerState(IServer.STATE_STARTING);

    IServer server = getServer();
    if (server == null) {
      return;
    }

    createAndStartPingThread();
  }

  // @Override
  // public void setupLaunchConfiguration(final ILaunchConfigurationWorkingCopy workingCopy,
  // final IProgressMonitor monitor) throws CoreException {
  // super.setupLaunchConfiguration(workingCopy, monitor);
  // }

  public void setStopState() {
    setServerState(IServer.STATE_STOPPED);
    serverStatusThread = null;
  }

  @Override
  protected boolean shouldIgnorePublishRequest(final IModule m) {
    return true;
  }

  @Override
  public void stop(final boolean force) {
    setServerState(IServer.STATE_STOPPING);
    IServer server = getServer();
    if (server != null) {
      ILaunch launch = server.getLaunch();
      if (launch != null) {
        try {
          launch.terminate();
          setServerState(IServer.STATE_STOPPED);
        } catch (DebugException e) {
          e.printStackTrace();
        }
      }
    }
    setServerState(IServer.STATE_STOPPED);
  }

}
