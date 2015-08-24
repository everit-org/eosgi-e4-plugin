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

  @Override
  public void configure() throws CoreException {
    LOGGER.info("configure");
  }

  @Override
  public void deconfigure() throws CoreException {
    LOGGER.info("deconfigure");
  }

  @Override
  public IProject getProject() {
    LOGGER.info("getProject");
    return null;
  }

  @Override
  public void setProject(final IProject project) {
    LOGGER.info("setProject");
  }

}
