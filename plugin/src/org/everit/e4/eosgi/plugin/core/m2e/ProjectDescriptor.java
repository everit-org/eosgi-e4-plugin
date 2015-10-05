package org.everit.e4.eosgi.plugin.core.m2e;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.everit.e4.eosgi.plugin.core.m2e.model.Environment;

public class ProjectDescriptor {
  private String artifactId;

  private String buildDirectory;

  private boolean distProject;

  private Map<String, Environment> environments = new HashMap<>();

  private String groupId;

  private Set<String> relevantProjectIds = new HashSet<>();

  private String version;

  public ProjectDescriptor() {
    super();
    this.distProject = false;
  }

  public ProjectDescriptor(final boolean dist) {
    distProject = dist;
  }

  public void addEnvironments(final Environment environment) {
    environments.put(environment.getId(), environment);
  }

  public void addProjectId(final String id) {
    relevantProjectIds.add(id);
  }

  public void clearEnvironments() {
    // TODO remove from distmanager too
    environments.clear();
  }

  public void clearProjectIds() {
    relevantProjectIds.clear();
  }

  public String getBuildDirectory() {
    return buildDirectory;
  }

  public Environment getEnvironment(String environmentName) {
    Objects.requireNonNull(environmentName, "environmentName must be not null");
    return environments.get(environmentName);
  }

  public List<String> getEnvironments() {
    List<String> list = new ArrayList<>();
    for (String env : environments.keySet()) {
      list.add(env);
    }
    return list;
  }

  public Set<String> getRelevantProjectIds() {
    return new HashSet<>(relevantProjectIds);
  }

  public boolean isDistProject() {
    return distProject;
  }

  public String mavenInfo() {
    if (groupId == null || artifactId == null || version == null) {
      return null;
    }
    return groupId + ":" + artifactId + ":" + version;
  }

  public void removeProjectId(final String id) {
    relevantProjectIds.remove(id);
  }

  public void setBuildDirectory(final String buildDirectory) {
    this.buildDirectory = buildDirectory;
  }

  public void setDistProject(boolean distProject) {
    this.distProject = distProject;
  }

  public void setMavenInfo(final String groupId, final String artifactId, final String version) {
    Objects.requireNonNull(groupId, "groupId cannot be null");
    Objects.requireNonNull(artifactId, "artifactId cannot be null");
    Objects.requireNonNull(version, "version cannot be null");
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
  }

  @Override
  public String toString() {
    return "ProjectDescriptor [artifactId=" + artifactId + ", buildDirectory=" + buildDirectory
        + ", distProject=" + distProject + ", environments=" + environments + ", groupId=" + groupId
        + ", relevantProjectIds=" + relevantProjectIds + ", version=" + version + "]";
  }

}
