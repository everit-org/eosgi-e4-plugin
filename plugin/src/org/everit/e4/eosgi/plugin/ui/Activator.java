package org.everit.e4.eosgi.plugin.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

  private static Activator plugin;

  public static final String PLUGIN_ID = "org.everit.e4.eosgi.plugin";

  public static Activator getDefault() {
    return plugin;
  }

  public static ImageDescriptor getImageDescriptor(final String path) {
    return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
  }

  private final EosgiProjects eosgiProjectController = new EosgiProjects();

  public Activator() {
  }

  public void error(final String message) {
    Activator.getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message));
  }

  public EosgiProjects getEosgiProjectController() {
    return eosgiProjectController;
  }

  public void info(final String message) {
    Activator.getDefault().getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
  }

  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
  }

  @Override
  public void stop(final BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

}
