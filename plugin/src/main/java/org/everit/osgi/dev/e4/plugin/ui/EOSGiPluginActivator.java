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
package org.everit.osgi.dev.e4.plugin.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobFunction;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.everit.osgi.dev.e4.plugin.core.EOSGiContextManager;
import org.everit.osgi.dev.e4.plugin.core.m2e.EOSGiContextManagerImpl;
import org.osgi.framework.BundleContext;

/**
 * Activator class for EOSGi plugin.
 */
public class EOSGiPluginActivator extends AbstractUIPlugin {

  private static EOSGiPluginActivator plugin;

  public static final String PLUGIN_ID = "org.everit.osgi.dev.e4.plugin";

  public static EOSGiPluginActivator getDefault() {
    return plugin;
  }

  public static ImageDescriptor getImageDescriptor(final String path) {
    return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
  }

  private EOSGiContextManager eosgiManager;

  private EOSGiLog log;

  /**
   * Get or create (if don't exist) an IConsole with the given name and return with an
   * {@link MessageConsoleStream}.
   *
   * @param name
   *          name of the console.
   * @return {@link MessageConsoleStream} instance.
   */
  public MessageConsoleStream getConsoleWithName(final String name) {
    Objects.requireNonNull(name, "name must be not null!");
    ConsolePlugin consolePlugin = ConsolePlugin.getDefault();
    IConsoleManager consoleManager = consolePlugin.getConsoleManager();

    IConsole[] consoles = consoleManager.getConsoles();
    MessageConsole console = null;
    for (IConsole existingConsole : consoles) {
      if (name.equals(existingConsole.getName()) && (existingConsole instanceof MessageConsole)) {
        console = (MessageConsole) existingConsole;
        break;
      }
    }
    if (console == null) {
      console = new MessageConsole(name,
          EOSGiPluginActivator.getImageDescriptor("icons/everit.gif"));
      consoleManager.addConsoles(new IConsole[] { console });
    }
    return console.newMessageStream();
  }

  public synchronized EOSGiContextManager getEOSGiManager() {
    return eosgiManager;
  }

  private void handleProjectChanges(final List<IProject> projects) {
    Job job = Job.create("Handle project changes...", new IJobFunction() {
      @Override
      public IStatus run(final IProgressMonitor monitor) {
        for (IProject project : projects) {
          if (project.isOpen()) {
            eosgiManager.findOrCreate(project);
          } else {
            eosgiManager.remove(project);
          }
        }
        return Status.OK_STATUS;
      }
    });
    job.setPriority(Job.BUILD);
    job.schedule();
  }

  /**
   * Create a warning JFace dialog with given title and message.
   *
   * @param title
   *          dialog title.
   * @param message
   *          dialog message.
   */
  public void showWarningDialog(final String title, final String message) {
    Display.getDefault().syncExec(new Runnable() {
      @Override
      public void run() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        Display display = workbench.getDisplay();
        Shell shell = display.getActiveShell();
        MessageDialog.openWarning(shell, title, message);
      }
    });
  }

  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    log = new EOSGiLog(getLog());
    eosgiManager = new EOSGiContextManagerImpl(log);

    ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {
      @Override
      public void resourceChanged(final IResourceChangeEvent changeEvent) {
        List<IProject> projects = new ArrayList<>();
        if (IResourceChangeEvent.PRE_DELETE == changeEvent.getType()) {
          IResource resource = changeEvent.getResource();
          if (resource instanceof IProject) {
            eosgiManager.remove((IProject) resource);
          }
        } else if (changeEvent.getDelta() != null) {
          IResourceDelta delta = changeEvent.getDelta();
          IResourceDelta[] affectedChildrens = delta.getAffectedChildren();
          if (affectedChildrens == null) {
            return;
          }
          for (IResourceDelta iResourceDelta : affectedChildrens) {
            if (iResourceDelta.getResource() instanceof IProject) {
              IProject project1 = (IProject) iResourceDelta.getResource();
              projects.add(project1);
            }
          }
          if (!projects.isEmpty()) {
            handleProjectChanges(projects);
          }
        }
      }
    });
  }

  @Override
  public void stop(final BundleContext context) throws Exception {
    plugin = null;
    eosgiManager.dispose();
    eosgiManager = null;
    super.stop(context);
  }

}
