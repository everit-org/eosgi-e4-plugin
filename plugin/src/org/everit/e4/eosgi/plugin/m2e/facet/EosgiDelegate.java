package org.everit.e4.eosgi.plugin.m2e.facet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;

public class EosgiDelegate implements IDelegate {

  @Override
  public void execute(final IProject project, final IProjectFacetVersion projectFacetVersion,
      final Object obj,
      IProgressMonitor progressMonitor)
          throws CoreException {
    System.out.println("EosgiDelegate works");
  }

}
