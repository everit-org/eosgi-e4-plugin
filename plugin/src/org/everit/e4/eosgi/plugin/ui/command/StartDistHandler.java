package org.everit.e4.eosgi.plugin.ui.command;

import java.util.logging.Level;
import java.util.logging.Logger;

//import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;
import org.everit.e4.eosgi.plugin.m2e.model.EosgiProject;
import org.everit.e4.eosgi.plugin.ui.Activator;

/**
 * Handler implementation for start the current selected dist project.
 */
public class StartDistHandler extends AbstractDistHandler implements IHandler {

  static final Logger LOGGER = Logger.getLogger(StartDistHandler.class.getName());

  @Override
  public void addHandlerListener(final IHandlerListener handlerListener) {
  }

  private MessageConsoleStream createConsole(final String name) {
    ConsolePlugin consolePlugin = ConsolePlugin.getDefault();
    IConsoleManager conMan = consolePlugin.getConsoleManager();

    MessageConsole messageConsole = new MessageConsole(name,
        Activator.getImageDescriptor("icons/everit.gif"));
    conMan.addConsoles(new IConsole[] { messageConsole });

    LOGGER.log(Level.INFO, "message console created successfully");

    MessageConsoleStream messageStream = messageConsole.newMessageStream();
    return messageStream;
  }

  @Override
  public void dispose() {
    LOGGER.info("dispose");
  }

  @Override
  public Object execute(final ExecutionEvent executionEvent) throws ExecutionException {
    // String console = executionEvent.getParameter("showConsole");

    ISelection currentSelection = HandlerUtil.getCurrentSelection(executionEvent);
    if (currentSelection == null) {
      return null;
    }

    TreeSelection treeSelection = null;
    if (currentSelection instanceof TreeSelection) {
      treeSelection = (TreeSelection) currentSelection;
    }

    if (treeSelection == null) {
      return null;
    }

    processTreeSelection(treeSelection);

    if (project != null && environmentName != null) {
      EosgiProject eosgiProject = Activator.getDefault().getEosgiProjectController()
          .getProject(project);
      if (eosgiProject != null) {
        eosgiProject.startDist(environmentName, createConsole(environmentName));
      } else {
        LOGGER.info("Dont't have dist for selected project: " + project.getName());
      }
    }
    return null;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean isHandled() {
    return true;
  }

  @Override
  public void removeHandlerListener(final IHandlerListener arg0) {
  }

}
