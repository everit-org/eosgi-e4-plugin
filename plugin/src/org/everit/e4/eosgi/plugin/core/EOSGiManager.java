package org.everit.e4.eosgi.plugin.core;

import org.eclipse.core.resources.IProject;

public interface EOSGiManager {

  void dispose();

  EOSGiContext findOrCreate(IProject project);

  boolean refresh(IProject project, ContextChange contextParam);

  void remove(IProject project);
}
