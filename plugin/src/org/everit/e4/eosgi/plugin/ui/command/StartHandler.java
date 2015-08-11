package org.everit.e4.eosgi.plugin.ui.command;

import java.util.logging.Logger;

//import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

/**
 * Handler implementation for start the current selected dist project.
 */
public class StartHandler implements IHandler {

  private static final Logger LOGGER = Logger.getLogger(StartHandler.class.getName());

  @Override
  public void addHandlerListener(final IHandlerListener arg0) {
  }

  @Override
  public void dispose() {
    LOGGER.info("dispose");
  }

  @Override
  public Object execute(final ExecutionEvent arg0) throws ExecutionException {
    LOGGER.info("execute");
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
