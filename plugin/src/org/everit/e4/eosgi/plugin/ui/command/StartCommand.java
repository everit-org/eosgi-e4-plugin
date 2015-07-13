package org.everit.e4.eosgi.plugin.ui.command;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;

public class StartCommand implements IExecutableExtension {

  @Override
  public void setInitializationData(final IConfigurationElement configurationElement, String arg1,
      Object obj)
          throws CoreException {
    System.out.println("setInitializationData");
  }

}
