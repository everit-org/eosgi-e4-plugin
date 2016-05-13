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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.everit.osgi.dev.e4.plugin.core.launcher.LaunchConfigurationBuilder;
import org.everit.osgi.dev.e4.plugin.m2e.M2EGoalExecutor;
import org.everit.osgi.dev.e4.plugin.m2e.Messages;
import org.everit.osgi.dev.e4.plugin.m2e.model.Environment;
import org.everit.osgi.dev.e4.plugin.ui.dto.EnvironmentNodeDTO;
import org.everit.osgi.dev.eosgi.dist.schema.util.DistributedEnvironmentConfigurationProvider;
import org.everit.osgi.dev.eosgi.dist.schema.util.LaunchConfigurationDTO;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.EnvironmentType;
import org.everit.osgi.dev.eosgi.dist.schema.xsd.UseByType;

/**
 * {@link EOSGiProject} base implementation.
 */
public class EOSGiProject {

  private String buildDirectory;

  private final boolean enable = true;

  private final Map<String, Environment> environments = new HashMap<>();

  private final IProject project;

  public EOSGiProject(final IProject project) {
    this.project = project;
  }

  private void createLauncherForEnvironment(final String environmentId,
      final LaunchConfigurationDTO launchConfigurationDTO,
      final IProgressMonitor monitor) throws CoreException {
    if (monitor != null) {
      monitor.setTaskName(Messages.monitorCreateServer);
    }

    new LaunchConfigurationBuilder(project.getName(), environmentId, buildDirectory)
        .addEnvironmentConfigurationDTO(launchConfigurationDTO)
        .build();
  }

  public List<EnvironmentNodeDTO> fetchEnvironments() {
    final List<EnvironmentNodeDTO> environmentList = new ArrayList<>();
    for (Environment environment : environments.values()) {
      environmentList.add(
          new EnvironmentNodeDTO()
              .id(environment.getId())
              .outdated(environment.isOutdated())
              .observable(environment));
    }
    return environmentList;
  }

  public void generate(final String environmentId, final IProgressMonitor monitor)
      throws CoreException {
    Objects.requireNonNull(environmentId, "environmentName must be not null!");

    Environment environment = environments.get(environmentId);
    if (environment == null) {
      throw new NullPointerException();
    }

    M2EGoalExecutor executor = new M2EGoalExecutor(project, environmentId);
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

  public boolean isEnable() {
    return enable;
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
