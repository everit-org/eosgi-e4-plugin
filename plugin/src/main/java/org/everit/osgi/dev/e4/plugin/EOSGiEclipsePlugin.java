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
package org.everit.osgi.dev.e4.plugin;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Activator class for EOSGi plugin.
 */
public class EOSGiEclipsePlugin extends AbstractUIPlugin {

  private static EOSGiEclipsePlugin plugin;

  public static final String PLUGIN_ID = "org.everit.osgi.dev.e4.plugin";

  public static EOSGiEclipsePlugin getDefault() {
    return plugin;
  }

  public static ImageDescriptor getImageDescriptor(final String path) {
    return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
  }

  private ChangedProjectTracker changedProjectTracker;

  private EOSGiProjectManager eosgiProjectManager;

  private EOSGiLog log;

  public ChangedProjectTracker getChangedProjectTracker() {
    return changedProjectTracker;
  }

  public EOSGiLog getEOSGiLog() {
    return log;
  }

  public synchronized EOSGiProjectManager getEOSGiManager() {
    return eosgiProjectManager;
  }

  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);
    EOSGiEclipsePlugin.plugin = this;
    log = new EOSGiLog(getLog());
    changedProjectTracker = new ChangedProjectTracker();
    ResourcesPlugin.getWorkspace().addResourceChangeListener(changedProjectTracker);
    eosgiProjectManager = new EOSGiProjectManager();
  }

  @Override
  public void stop(final BundleContext context) throws Exception {
    EOSGiEclipsePlugin.plugin = null;
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(changedProjectTracker);
    super.stop(context);
  }

}
