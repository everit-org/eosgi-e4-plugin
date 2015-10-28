package org.everit.e4.eosgi.plugin.core.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;

/**
 * Extended {@link JavaLaunchDelegate} class for EOSGi launcher.
 */
public class EOSGILaunchConfigurationDelegate extends JavaLaunchDelegate {

  public static final String LAUNCHER_ATTR_ENVIRONMENT_ID = "environmentId";

  @Override
  public void launch(final ILaunchConfiguration configuration, final String mode,
      final ILaunch launch, final IProgressMonitor monitor) throws CoreException {
    String environmentId = configuration.getAttribute(LAUNCHER_ATTR_ENVIRONMENT_ID, "");
    String projectName = configuration
        .getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
    if ("".equals(environmentId) || "".equals(projectName)) {
      return;
    }
    IServer server = ServerCore.findServer(environmentId + "/" + projectName);
    EOSGiServerBehaviour eosgiServer = (EOSGiServerBehaviour) server
        .loadAdapter(EOSGiServerBehaviour.class, null);
    eosgiServer.setStartStatus();
    super.launch(configuration, mode, launch, monitor);
  }

}
