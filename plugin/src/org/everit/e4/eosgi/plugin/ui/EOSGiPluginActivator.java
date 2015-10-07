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
package org.everit.e4.eosgi.plugin.ui;

import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.everit.e4.eosgi.plugin.core.EOSGiContextManager;
import org.everit.e4.eosgi.plugin.core.m2e.EOSGiContextManagerImpl;
import org.everit.e4.eosgi.plugin.ui.nature.EosgiNature;
import org.osgi.framework.BundleContext;

/**
 * Activator class for EOSGi plugin.
 */
public class EOSGiPluginActivator extends AbstractUIPlugin {

  private static EOSGiPluginActivator plugin;

  public static final String PLUGIN_ID = "org.everit.e4.eosgi.plugin";

  public static EOSGiPluginActivator getDefault() {
    return plugin;
  }

  public static ImageDescriptor getImageDescriptor(final String path) {
    return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
  }

  private EOSGiContextManager eosgiManager;

  private EOSGiLog log;

  public EOSGiPluginActivator() {
    log = new EOSGiLog(getLog());
  }

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

  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);
    plugin = this;

    eosgiManager = new EOSGiContextManagerImpl(log);

    Job job = new Job("Initializing EOSGI eosgiManager") {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        monitor.beginTask("Register projects for EOSGI eosgiManager...", projects.length);
        int i = 0;
        for (IProject project : projects) {
          try {
            if (project.isOpen() && project.hasNature(EosgiNature.NATURE_ID)) {
              eosgiManager.findOrCreate(project);
            }
          } catch (CoreException e) {
            log.error("Couldn't register project with name " + project.getName(), e);
          }
          monitor.worked(++i);
        }
        monitor.done();
        return Status.OK_STATUS;
      }
    };
    job.setPriority(Job.BUILD);
    job.schedule();
  }

  @Override
  public void stop(final BundleContext context) throws Exception {
    plugin = null;
    eosgiManager.dispose();
    eosgiManager = null;
    super.stop(context);
  }

}
