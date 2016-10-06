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

import org.apache.maven.plugin.MojoExecution;

/**
 * Information about an EOSGi environment.
 */
public class ExecutableEnvironment implements Comparable<ExecutableEnvironment> {

  private final boolean defaultExecution;

  private final String environmentId;

  private final EOSGiProject eosgiProject;

  private final MojoExecution mojoExecution;

  private final File rootFolder;

  private final long shutdownTimeout;

  public ExecutableEnvironment(final String environmentId,
      final MojoExecution mojoExecution, final boolean defaultExecution,
      final EOSGiProject eosgiProject, final File rootFolder,
      final long shutdownTimeout) {
    this.environmentId = environmentId;
    this.mojoExecution = mojoExecution;
    this.defaultExecution = defaultExecution;
    this.eosgiProject = eosgiProject;
    this.rootFolder = rootFolder;
    this.shutdownTimeout = shutdownTimeout;
  }

  @Override
  public int compareTo(final ExecutableEnvironment o) {
    if (o == this) {
      return 0;
    }

    int result = Boolean.compare(o.isDefaultExecution(), defaultExecution);

    if (result != 0) {
      return result;
    }

    result = mojoExecution.getExecutionId().compareTo(o.mojoExecution.getExecutionId());
    if (result != 0) {
      return result;
    }

    result = environmentId.compareTo(o.environmentId);
    if (result != 0) {
      return result;
    }

    return eosgiProject.getMavenProjectFacade().getProject().getName()
        .compareTo(o.getEOSGiProject().getMavenProjectFacade().getProject().getName());
  }

  public String getEnvironmentId() {
    return environmentId;
  }

  public EOSGiProject getEOSGiProject() {
    return eosgiProject;
  }

  public MojoExecution getMojoExecution() {
    return mojoExecution;
  }

  public File getRootFolder() {
    return rootFolder;
  }

  public long getShutdownTimeout() {
    return shutdownTimeout;
  }

  public boolean isDefaultExecution() {
    return defaultExecution;
  }
}
