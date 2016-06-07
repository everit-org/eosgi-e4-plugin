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
package org.everit.osgi.dev.e4.plugin;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.everit.osgi.dev.dist.util.configuration.DistributedEnvironmentConfigurationProvider;
import org.everit.osgi.dev.dist.util.configuration.LaunchConfigurationDTO;
import org.everit.osgi.dev.dist.util.configuration.schema.EnvironmentType;
import org.everit.osgi.dev.dist.util.configuration.schema.UseByType;
import org.everit.osgi.dev.e4.plugin.core.launcher.LaunchConfigurationBuilder;
import org.everit.osgi.dev.e4.plugin.m2e.M2EGoalExecutor;
import org.everit.osgi.dev.e4.plugin.m2e.Messages;
import org.everit.osgi.dev.e4.plugin.m2e.model.Environment;

/**
 * {@link EOSGiProject} base implementation.
 */
public class EOSGiProject {

  private String buildDirectory;

  private final Map<String, Environment> environments = new HashMap<>();

  private final IMavenProjectFacade mavenProjectFacade;

  public EOSGiProject(final IMavenProjectFacade mavenProjectFacade) {
    this.mavenProjectFacade = mavenProjectFacade;
  }

  private void createLauncherForEnvironment(final String environmentId,
      final LaunchConfigurationDTO launchConfigurationDTO,
      final IProgressMonitor monitor) throws CoreException {
    if (monitor != null) {
      monitor.setTaskName(Messages.monitorCreateServer);
    }

    new LaunchConfigurationBuilder(mavenProjectFacade.getProject().getName(), environmentId,
        buildDirectory).addEnvironmentConfigurationDTO(launchConfigurationDTO).build();
  }

  public void generate(final String environmentId, final IProgressMonitor monitor)
      throws CoreException {
    Objects.requireNonNull(environmentId, "environmentName must be not null!");

    Environment environment = environments.get(environmentId);
    if (environment == null) {
      throw new NullPointerException();
    }

    M2EGoalExecutor executor = new M2EGoalExecutor(mavenProjectFacade.getProject(), environmentId);
    if (!executor.execute(monitor)) {
      return;
    }

    environment.setGenerated();

    if (monitor != null) {
      monitor.setTaskName(Messages.monitorLoadDistXML);
    }

    LaunchConfigurationDTO launchConfigurationDTO = loadEnvironmentConfiguration(
        environmentId);

    if (launchConfigurationDTO != null) {
      createLauncherForEnvironment(environmentId, launchConfigurationDTO, monitor);
    }
  }

  public Collection<String> getEnvironmentNames() {
    return null;
  }

  private LaunchConfigurationDTO loadEnvironmentConfiguration(final String environmentId) {
    String distXmlFilePath = buildDirectory + File.separator + "eosgi-dist" //$NON-NLS-1$
        + File.separator + environmentId;
    DistributedEnvironmentConfigurationProvider distSchemaProvider =
        new DistributedEnvironmentConfigurationProvider();

    EnvironmentType distributedEnvironment = distSchemaProvider
        .getOverriddenDistributedEnvironmentConfig(new File(distXmlFilePath), UseByType.IDE);

    // EnvironmentConfigurationDTO environmentConfigurationDTO = distSchemaProvider
    // .getEnvironmentConfiguration(new File(distXmlFilePath), UseByType.IDE);
    return distSchemaProvider.getLaunchConfiguration(distributedEnvironment);
  }

}
