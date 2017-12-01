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
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import javax.annotation.Generated;

/**
 * Information about an EOSGi environment.
 */
public final class ExecutableEnvironment implements Comparable<ExecutableEnvironment> {

  /**
   * Builder class for {@link ExecutableEnvironment}.
   */
  public static class Builder {

    public Collection<GAV> additionalArtifactGAVs = Collections.emptyList();

    public Boolean defaultExecution;

    public String environmentId;

    public EOSGiProject eosgiProject;

    public String executionId;

    public File rootFolder;

    public long shutdownTimeout;

    public File testResultFolder;

    public ExecutableEnvironment build() {
      return new ExecutableEnvironment(this);
    }

    public Builder withAdditionalArtifactGAVs(
        final Collection<GAV> additionalArtifactGAVs) {
      this.additionalArtifactGAVs = additionalArtifactGAVs;
      return this;
    }

    public Builder withDefaultExecution(final boolean defaultExecution) {
      this.defaultExecution = defaultExecution;
      return this;
    }

    public Builder withEnvironmentId(final String environmentId) {
      this.environmentId = environmentId;
      return this;
    }

    public Builder withEosgiProject(final EOSGiProject eosgiProject) {
      this.eosgiProject = eosgiProject;
      return this;
    }

    public Builder withExecutionId(final String executionId) {
      this.executionId = executionId;
      return this;
    }

    public Builder withRootFolder(final File rootFolder) {
      this.rootFolder = rootFolder;
      return this;
    }

    public Builder withShutdownTimeout(final long shutdownTimeout) {
      this.shutdownTimeout = shutdownTimeout;
      return this;
    }

    public Builder withTestResultFolder(final File testResultFolder) {
      this.testResultFolder = testResultFolder;
      return this;
    }
  }

  private final Collection<GAV> additionalArtifactGAVs;

  private final boolean defaultExecution;

  private final String environmentId;

  private final EOSGiProject eosgiProject;

  private final String executionId;

  private final File rootFolder;

  private final long shutdownTimeout;

  private final File testResultFolder;

  private ExecutableEnvironment(final Builder builder) {
    this.environmentId = Objects.requireNonNull(builder.environmentId);
    this.executionId = Objects.requireNonNull(builder.executionId);
    this.defaultExecution = Objects.requireNonNull(builder.defaultExecution);
    this.eosgiProject = Objects.requireNonNull(builder.eosgiProject);
    this.rootFolder = Objects.requireNonNull(builder.rootFolder);
    this.testResultFolder = Objects.requireNonNull(builder.testResultFolder);
    this.shutdownTimeout = Objects.requireNonNull(builder.shutdownTimeout);
    this.additionalArtifactGAVs =
        Collections.unmodifiableList(new ArrayList<>(builder.additionalArtifactGAVs));
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

    result = executionId.compareTo(o.executionId);
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

  @Override
  @Generated("eclipse")
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ExecutableEnvironment other = (ExecutableEnvironment) obj;
    if (defaultExecution != other.defaultExecution)
      return false;
    if (environmentId == null) {
      if (other.environmentId != null)
        return false;
    } else if (!environmentId.equals(other.environmentId))
      return false;
    if (eosgiProject == null) {
      if (other.eosgiProject != null)
        return false;
    } else if (!eosgiProject.equals(other.eosgiProject))
      return false;
    if (executionId == null) {
      if (other.executionId != null)
        return false;
    } else if (!executionId.equals(other.executionId))
      return false;
    return true;
  }

  public Collection<GAV> getAdditionalArtifactGAVs() {
    return additionalArtifactGAVs;
  }

  public String getEnvironmentId() {
    return environmentId;
  }

  public EOSGiProject getEOSGiProject() {
    return eosgiProject;
  }

  public String getExecutionId() {
    return executionId;
  }

  public File getRootFolder() {
    return rootFolder;
  }

  public long getShutdownTimeout() {
    return shutdownTimeout;
  }

  public File getTestResultFolder() {
    return testResultFolder;
  }

  @Override
  @Generated("eclipse")
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (defaultExecution ? 1231 : 1237);
    result = prime * result + ((environmentId == null) ? 0 : environmentId.hashCode());
    result = prime * result + ((eosgiProject == null) ? 0 : eosgiProject.hashCode());
    result = prime * result + ((executionId == null) ? 0 : executionId.hashCode());
    return result;
  }

  public boolean isDefaultExecution() {
    return defaultExecution;
  }

}
