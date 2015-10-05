package org.everit.e4.eosgi.plugin.core.m2e;

import java.util.List;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.everit.e4.eosgi.plugin.core.dist.DistRunner;

/**
 * Manage IProject , MavenProject connections and store eosgi dist informations.
 */
public interface EosgiManager {

  List<String> fetchBundlesBy(IProject project);

  List<String> fetchEnvironmentsBy(IProject project);

  void generateDistFor(IProject project, String environmentId,
      IProgressMonitor monitor);

  DistRunner getDistRunner(IProject project, String environmentName);

  void letDistProject(IProject project);

  void refreshProject(IProject project, MavenProject mavenProject, IProgressMonitor monitor);

  void registerProject(IProject project, IProgressMonitor monitor);

  void updateEnvironments(IProject project, Xpp3Dom configuration, IProgressMonitor monitor);

}
