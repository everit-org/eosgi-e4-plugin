package org.everit.e4.eosgi.plugin.core.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.everit.e4.eosgi.plugin.ui.EOSGiPluginActivator;

public class EOSGiServer extends ServerDelegate {

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
