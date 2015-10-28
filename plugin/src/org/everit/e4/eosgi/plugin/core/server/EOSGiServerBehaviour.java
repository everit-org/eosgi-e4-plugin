package org.everit.e4.eosgi.plugin.core.server;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;

public class EOSGiServerBehaviour extends ServerBehaviourDelegate {

  public EOSGiServerBehaviour() {
    super();
  }

  @Override
  public void dispose() {
    super.dispose();
  }

  public EOSGiRuntime getEOSGiRuntime() {
    if (getServer().getRuntime() == null) {
      return null;
    }
    return (EOSGiRuntime) getServer().getRuntime().loadAdapter(EOSGiRuntime.class, null);
  }

  public EOSGiServer getPreviewServer() {
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
  public IStatus publish(final int kind, final IProgressMonitor monitor) {
    return super.publish(kind, monitor);
  }

  @Override
  public void publish(final int kind, final List<IModule[]> modules, final IProgressMonitor monitor,
      final IAdaptable info)
          throws CoreException {
    setServerPublishState(IServer.STATE_STARTED);
    super.publish(kind, modules, monitor, info);
  }

  // @Override
  // public void restart(String launchMode) throws CoreException {
  // ILaunch launch = getServer().getLaunch();
  // IProcess[] processes = launch.getProcesses();
  // IDebugTarget[] debugTargets = launch.getDebugTargets();
  // super.restart(launchMode);
  // }

  public void setStartStatus() {
    setServerState(IServer.STATE_STARTED);
  }

  // @Override
  // public void setupLaunchConfiguration(final ILaunchConfigurationWorkingCopy workingCopy,
  // final IProgressMonitor monitor) throws CoreException {
  // setServerPublishState(IServer.STATE_STARTED);
  // ILaunch launch = getServer().getLaunch();
  // String launchMode = launch.getLaunchMode();
  // IProcess[] processes = launch.getProcesses();
  // IDebugTarget[] debugTargets = launch.getDebugTargets();
  // super.setupLaunchConfiguration(workingCopy, monitor);
  // }

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
