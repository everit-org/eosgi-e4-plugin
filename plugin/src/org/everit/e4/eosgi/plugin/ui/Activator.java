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
import org.everit.e4.eosgi.plugin.core.EOSGiManager;
import org.everit.e4.eosgi.plugin.core.m2e.EOSGiManagerImpl;
import org.everit.e4.eosgi.plugin.ui.nature.EosgiNature;
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

  private EOSGiManager eosgiManager;

  private EOSGiLog log;

  public Activator() {
    log = new EOSGiLog(getLog());
  }

  /**
   * Get or create (if don't exist) an IConsole with the given name and return with an
   * {@link MessageConsoleStream}.
   * 
   * @param name
   *          name of the console.
   * @return
   */
  public MessageConsoleStream getConsoleWithName(final String name) {
    Objects.requireNonNull(name, "name must be not null!");
    ConsolePlugin consolePlugin = ConsolePlugin.getDefault();
    IConsoleManager consoleManager = consolePlugin.getConsoleManager();
    
    IConsole[] consoles = consoleManager.getConsoles();
    MessageConsole console = null;
    for (IConsole existingConsole : consoles) {
      if (name.equals(existingConsole.getName()) && existingConsole instanceof MessageConsole) {
        console = (MessageConsole) existingConsole;
        break;
      }
    }
    if (console == null) {
      console = new MessageConsole(name,
          Activator.getImageDescriptor("icons/everit.gif"));
      consoleManager.addConsoles(new IConsole[] { console });
    }
    return console.newMessageStream();
  }

  public synchronized EOSGiManager getEOSGiManager() {
    return eosgiManager;
  }

  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);
    plugin = this;

    eosgiManager = new EOSGiManagerImpl(log);

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
