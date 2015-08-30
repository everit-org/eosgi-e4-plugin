package org.everit.e4.eosgi.plugin.ui.nature;

import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * Nature implementation for EOSGI dist projects.
 */
public class EosgiNature implements IProjectNature {
  private static final Logger LOGGER = Logger.getLogger(EosgiNature.class.getName());

  public static final String NATURE_ID = "org.everit.e4.eosgi.plugin.ui.natures.eosgi";

  private String name;

  private IProject project;

  @Override
  public void configure() throws CoreException {
    if (project != null) {
      this.name = project.getName();
      LOGGER.info(name + " project configured with EOSGI dist nature.");
    }
  }

  @Override
  public void deconfigure() throws CoreException {
    this.project = null;
    this.name = null;
    LOGGER.info("deconfigure");
  }

  @Override
  public IProject getProject() {
    LOGGER.info("getProject");
    return project;
  }

  @Override
  public void setProject(final IProject project) {
    LOGGER.info("setProject");
    this.project = project;
  }

}
