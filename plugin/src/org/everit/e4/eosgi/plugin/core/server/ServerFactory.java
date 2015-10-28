package org.everit.e4.eosgi.plugin.core.server;

import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.everit.e4.eosgi.plugin.core.launcher.LauncherConfigurationFactory;
import org.everit.e4.eosgi.plugin.ui.EOSGiLog;
import org.everit.e4.eosgi.plugin.ui.EOSGiPluginActivator;

public class ServerFactory {
  public static final String RUNTIME_ID = "org.everit.e4.eosgi.plugin.runtime";

  public static final String SERVER_TYPE_ID = "org.everit.e4.eosgi.plugin.server";

  private String buildDirectory;

  private String environmentId;

  private EOSGiLog log;

  private String projectName;

  private boolean withLauncher;

  /**
   * Constructor.
   *
   * @param projectName
   *          name of the project.
   * @param environmentId
   *          id of environment.
   */
  public ServerFactory(final String projectName, final String buildDirectory,
      final String environmentId) {
    super();
    ILog iLog = EOSGiPluginActivator.getDefault().getLog();
    log = new EOSGiLog(iLog);
    Objects.requireNonNull(projectName, "projectName must be not null!");
    Objects.requireNonNull(buildDirectory, "buildDirectory must be not null!");
    Objects.requireNonNull(environmentId, "environmentId must be not null!");
    this.projectName = projectName;
    this.buildDirectory = buildDirectory;
    this.environmentId = environmentId;
  }

  // public void create(final IProject project, final String environmentId) {
  // NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();
  //
  // IRuntimeType runtime = ServerCore.findRuntimeType(RUNTIME_ID);
  // try {
  // IRuntimeWorkingCopy runtimeWorkingCopy = runtime.createRuntime("eosgi-runtime",
  // nullProgressMonitor);
  // IRuntime eosgiRuntime = runtimeWorkingCopy.save(true, nullProgressMonitor);
  //
  // IServerType serverType = ServerCore.findServerType(SERVER_TYPE_ID);
  // IServerWorkingCopy serverWorkingCopy = serverType.createServer(environmentId, null,
  // nullProgressMonitor);
  // serverWorkingCopy.setRuntime(eosgiRuntime);
  // serverWorkingCopy.setName(environmentId);
  //
  // IServer server = serverWorkingCopy.save(true, nullProgressMonitor);
  // ILaunchConfiguration launchConfiguration2 = server.getLaunchConfiguration(true,
  // nullProgressMonitor);
  //
  // new LauncherConfigurationFactory(launchConfiguration2.getWorkingCopy())
  // .create(project, environmentId);
  // } catch (CoreException e) {
  // e.printStackTrace();
  // }
  // }

  // public void create2(final IProject project, final String environmentId) {
  // NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();
  //
  // ILaunchConfiguration launchConfiguration = new LauncherConfigurationFactory().create(project,
  // environmentId);
  //
  // try {
  // ILaunchConfigurationWorkingCopy workingCopy = launchConfiguration.getWorkingCopy();
  //
  // IServer server = ServerUtil.getServer(launchConfiguration);
  // EOSGiServerBehaviour previewServer = (EOSGiServerBehaviour) server
  // .loadAdapter(EOSGiServerBehaviour.class, null);
  // previewServer.setupLaunchConfiguration(workingCopy, nullProgressMonitor);
  // } catch (CoreException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  // }

  private IRuntime createRuntime(final IProgressMonitor monitor) {
    IRuntimeType runtime = ServerCore.findRuntimeType(RUNTIME_ID);
    IRuntime eosgiRuntime = null;
    try {
      IRuntimeWorkingCopy runtimeWorkingCopy = runtime.createRuntime(environmentId, monitor);
      eosgiRuntime = runtimeWorkingCopy.save(true, monitor);
    } catch (CoreException e) {
      log.error("Could not create runtime with name " + environmentId, e);
    }
    return eosgiRuntime;
  }

  /**
   * Create an IServer instance for project/environmentId pair.
   *
   * @param monitor
   *          optional {@link IProgressMonitor} instance.
   */
  public void createServer(final IProgressMonitor monitor) {
    if (monitor != null) {
      monitor.setTaskName("Creating Server...");
    }

    IRuntime runtime = createRuntime(monitor);
    if (runtime == null) {
      return;
    }

    IServer server = findServerToEnvironment(runtime, monitor);
    if (server == null) {
      return;
    }

    if (withLauncher) {
      ILaunchConfigurationWorkingCopy workingCopy = null;
      try {
        ILaunchConfiguration serverLaunchConfiguration = server.getLaunchConfiguration(true,
            monitor);
        workingCopy = serverLaunchConfiguration.getWorkingCopy();
      } catch (CoreException e) {
        log.error("Could not find launcher configuration for this server: " + server.getName(), e);
      }
      if (workingCopy == null) {
        return;
      }
      new LauncherConfigurationFactory(workingCopy)
          .create(projectName, environmentId, buildDirectory);
    }
  }

  public void createServerWithLauncher(final IProgressMonitor monitor) {
    withLauncher = true;
    createServer(monitor);
  }

  private IServer findServerToEnvironment(final IRuntime runtime, final IProgressMonitor monitor) {
    String serverName = environmentId + "/" + projectName;
    IServer server = null;
    try {
      IServerType serverType = ServerCore.findServerType(SERVER_TYPE_ID);
      IServerWorkingCopy serverWorkingCopy = serverType
          .createServer(serverName, null, monitor);
      serverWorkingCopy.setRuntime(runtime);
      serverWorkingCopy.setName(serverName);
      server = serverWorkingCopy.save(true, monitor);
    } catch (CoreException e) {
      log.error("Could not find/create server for " + environmentId, e);
    }

    return server;
  }

}
