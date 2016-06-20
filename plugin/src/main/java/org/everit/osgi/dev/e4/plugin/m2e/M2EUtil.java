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

import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;

/**
 * Util functions to help the usage of M2E.
 */
public final class M2EUtil {

  /**
   * Resolves the value of a parameter of a plugin configuration.
   *
   * @param project
   *          The maven project that contains the configuration.
   * @param parameter
   *          The name of the parameter.
   * @param asType
   *          The type that the configuration will be converted to.
   * @param mojoExecution
   *          The mojo execution.
   * @param monitor
   *          The monitor that is used in Eclipse.
   * @return The resolved parameter value.
   * @throws CoreException
   *           If anything wrong happens.
   */
  public static <T> T getParameterValue(final MavenProject project, final String parameter,
      final Class<T> asType,
      final MojoExecution mojoExecution, final IProgressMonitor monitor) throws CoreException {
    PluginExecution execution = new PluginExecution();
    execution.setConfiguration(mojoExecution.getConfiguration());
    return MavenPlugin.getMaven().getMojoParameterValue(project, parameter, asType,
        mojoExecution.getPlugin(), execution, mojoExecution.getGoal(), monitor);
  }

  private M2EUtil() {
  }
}
