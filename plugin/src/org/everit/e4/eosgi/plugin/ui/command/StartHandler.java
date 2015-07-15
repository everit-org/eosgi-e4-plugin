package org.everit.e4.eosgi.plugin.ui.command;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

public class StartHandler implements IHandler {

  @Override
  public void addHandlerListener(IHandlerListener arg0) {
    System.out.println("add handler listener");
  }

  @Override
  public void dispose() {
    // TODO Auto-generated method stub

  }

  @Override
  public Object execute(final ExecutionEvent arg0) throws ExecutionException {
    System.out.println("execute command");
    return null;
  }

  @Override
  public boolean isEnabled() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isHandled() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void removeHandlerListener(IHandlerListener arg0) {
    // TODO Auto-generated method stub

  }

}
