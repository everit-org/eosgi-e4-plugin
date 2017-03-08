package org.everit.osgi.dev.e4.plugin;

import javax.annotation.Generated;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyNode;

/**
 * Identifier DTO for maven artifacts with equals and hashcode implementation.
 */
public class GAV {

  public final String artifactId;

  public final String groupId;

  public final String version;

  /**
   * Constructor.
   *
   * @param dependencyNode
   *          The dependency node that is used to generate the identifier of the Maven module.
   */
  public GAV(final DependencyNode dependencyNode) {
    Artifact artifact = dependencyNode.getArtifact();
    if (artifact != null) {
      this.groupId = artifact.getGroupId();
      this.artifactId = artifact.getArtifactId();
      this.version = artifact.getBaseVersion();
    } else {
      this.groupId = null;
      this.artifactId = null;
      this.version = null;
    }
  }

  /**
   * Constructor.
   *
   * @param groupId
   *          GroupId of the artifact.
   * @param artifactId
   *          ArtifactId of the artifact.
   * @param version
   *          The version of the artifact.
   */
  public GAV(final String groupId, final String artifactId, final String version) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
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
    GAV other = (GAV) obj;
    if (artifactId == null) {
      if (other.artifactId != null)
        return false;
    } else if (!artifactId.equals(other.artifactId))
      return false;
    if (groupId == null) {
      if (other.groupId != null)
        return false;
    } else if (!groupId.equals(other.groupId))
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    } else if (!version.equals(other.version))
      return false;
    return true;
  }

  @Override
  @Generated("eclipse")
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
    result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    return result;
  }

}