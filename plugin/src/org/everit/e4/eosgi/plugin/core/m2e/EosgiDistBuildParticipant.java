/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.e4.eosgi.plugin.core.m2e;

import java.util.Objects;
import java.util.Set;

import org.apache.maven.plugin.MojoExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;
import org.everit.e4.eosgi.plugin.core.ContextChange;
import org.everit.e4.eosgi.plugin.core.EOSGiContext;
import org.everit.e4.eosgi.plugin.core.EOSGiContextManager;
import org.everit.e4.eosgi.plugin.core.m2e.xml.ConfiguratorParser;
import org.everit.e4.eosgi.plugin.core.m2e.xml.EnvironmentsDTO;

/**
 * MojoExecutionBuildParticipant implementation for eosgi-maven-plugin.
 */
public class EosgiDistBuildParticipant extends MojoExecutionBuildParticipant {

  private EOSGiContextManager eosgiManager;

  /**
   * Constructor.<br>
   * The runOnConfiguration and runOnIncremental is <code>true</code>.
   * 
   * @param execution
   *          {@link MojoExecution} instance.
   * @param eosgiManager
   *          EOSGi context manager instance.
   */
  public EosgiDistBuildParticipant(final MojoExecution execution,
      final EOSGiContextManager eosgiManager) {
    super(execution, true, true);
    Objects.requireNonNull(eosgiManager, "eosgiManager must be not null!");
    this.eosgiManager = eosgiManager;
  }

  @Override
  public Set<IProject> build(final int kind, final IProgressMonitor monitor) throws Exception {
    IMavenProjectFacade mavenProjectFacade = getMavenProjectFacade();
    if (mavenProjectFacade == null) {
      return null;
    }

    IProject project = mavenProjectFacade.getProject();
    if (project == null) {
      return null;
    }

    processConfiguration(project, monitor);

    return null;
  }

  @Override
  public void clean(final IProgressMonitor monitor) throws CoreException {
    super.clean(monitor);
  }

  @Override
  protected IResourceDelta getDelta(final IProject project) {
    return super.getDelta(project);
  }

  private void processConfiguration(final IProject project, final IProgressMonitor monitor) {
    if ((getMojoExecution() != null) && (getMojoExecution().getConfiguration() != null)) {
      monitor.subTask(project.getName() + ": processing dist configuration...");
      Xpp3Dom configuration = getMojoExecution().getConfiguration();

      EnvironmentsDTO environments = null;
      if (configuration != null) {
        environments = new ConfiguratorParser().parse(configuration);
      }

      EOSGiContext eosgiProject = eosgiManager.findOrCreate(project);
      if (eosgiProject != null) {
        eosgiProject.refresh(new ContextChange().configuration(environments));
      }
    }
  }

}
