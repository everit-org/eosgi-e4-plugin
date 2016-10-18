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
package org.everit.osgi.dev.e4.plugin.m2e;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;

/**
 * An empty project configurator. The main logic is implemented in
 * {@link EOSGiMavenProjectChangeListener} as configurators are not always called (e.g.: when plugin
 * is removed from pom file).
 */
public class EOSGiM2EProjectConfigurator extends AbstractProjectConfigurator {

  @Override
  public void configure(final ProjectConfigurationRequest request, final IProgressMonitor monitor)
      throws CoreException {

    // Do nothing
  }

  @Override
  public void mavenProjectChanged(final MavenProjectChangedEvent event,
      final IProgressMonitor monitor)
      throws CoreException {

    // Do nothing
  }

  @Override
  public void unconfigure(final ProjectConfigurationRequest request, final IProgressMonitor monitor)
      throws CoreException {
    // Do nothing
  }

}
