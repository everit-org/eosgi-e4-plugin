package org.everit.e4.eosgi.plugin.ui.command;

import java.util.logging.Logger;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

/**
 * Handler implementation for stop the current selected dist project.
 */
public class StopDistHandler implements IHandler {

  private static final Logger LOGGER = Logger.getLogger(StopDistHandler.class.getName());

  @Override
  public void addHandlerListener(final IHandlerListener handlerListener) {
  }

  @Override
  public void dispose() {
    LOGGER.info("dispose");
  }

  @Override
  public Object execute(final ExecutionEvent executionEvent) throws ExecutionException {
    LOGGER.info("exectuted stop dist handler");
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
  public void removeHandlerListener(final IHandlerListener handlerListener) {
  }

}
