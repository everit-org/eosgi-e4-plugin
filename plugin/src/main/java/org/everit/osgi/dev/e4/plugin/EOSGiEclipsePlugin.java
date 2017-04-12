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

import java.util.UUID;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.everit.osgi.dev.dist.util.attach.EOSGiVMManager;
import org.everit.osgi.dev.e4.plugin.m2e.packaging.ProjectPackager;
import org.osgi.framework.BundleContext;

/**
 * Activator class for EOSGi plugin.
 */
public class EOSGiEclipsePlugin extends AbstractUIPlugin {

  public static final String ECLIPSE_INSTANCE = UUID.randomUUID().toString();

  private static EOSGiEclipsePlugin plugin;

  public static final String PLUGIN_ID = "org.everit.osgi.dev.e4.plugin";

  public static final String SYSPROP_ECLIPSE_INSTANCE = "eosgi.eclipseInstance";

  public static final String SYSPROP_ECLIPSE_PROJECT_NAME = "eosgi.projectName";

  public static final String SYSPROP_START_TIMESTAMP = "eosgi.startTimestamp";

  public static final String SYSPROP_TEST_RESULT_FOLDER = "eosgi.testResultFolder";

  public static EOSGiEclipsePlugin getDefault() {
    return plugin;
  }

  public static ImageDescriptor getImageDescriptor(final String path) {
    return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
  }

  private EOSGiProjectManager eosgiProjectManager;

  private EOSGiLog log;

  private ProjectPackager projectPackageUtil;

  private boolean checkAttachAPIAvailable() {
    try {
      EOSGiVMManager.class.getClassLoader().loadClass("com.sun.tools.attach.VirtualMachine");
    } catch (ClassNotFoundException e) {
      String message = "Can't start Everit OSGi Eclipse plugin due to the following reason: "
          + "Sun Attach API is not available. This can happen if Eclipse is started with "
          + "a JRE instead of a JDK. Make sure you start Eclipse with a JDK that contains the "
          + "Sun Attach API! E.g.: OpenJDK or Oracle JDK";

      IStatus status = new Status(IStatus.ERROR, EOSGiEclipsePlugin.PLUGIN_ID, message);
      getLog().log(status);
      Display.getDefault().asyncExec(() -> {
        Shell shell = new Shell();
        ErrorDialog.openError(shell, "Error", "Could not start Everit OSGi Eclipse plugin", status);
      });
      return false;
    }
    return true;
  }

  public EOSGiLog getEOSGiLog() {
    return this.log;
  }

  public synchronized EOSGiProjectManager getEOSGiManager() {
    return this.eosgiProjectManager;
  }

  public ProjectPackager getProjectPackageUtil() {
    return this.projectPackageUtil;
  }

  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);

    if (!checkAttachAPIAvailable()) {
      return;
    }

    EOSGiEclipsePlugin.plugin = this;
    this.log = new EOSGiLog(getLog());
    this.projectPackageUtil = new ProjectPackager();
    this.projectPackageUtil.open();
    this.eosgiProjectManager = new EOSGiProjectManager();
  }

  @Override
  public void stop(final BundleContext context) throws Exception {
    EOSGiEclipsePlugin.plugin = null;
    if (this.projectPackageUtil != null) {
      this.projectPackageUtil.close();
    }
    try {
      if (this.eosgiProjectManager != null) {
        this.eosgiProjectManager.close();
      }
    } finally {
      super.stop(context);
    }
  }

}
