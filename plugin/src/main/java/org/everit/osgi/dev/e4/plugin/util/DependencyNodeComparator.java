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
package org.everit.osgi.dev.e4.plugin.util;

import java.util.Comparator;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyNode;

public class DependencyNodeComparator implements Comparator<DependencyNode> {

  @Override
  public int compare(final DependencyNode o1, final DependencyNode o2) {
    Artifact a1 = o1.getArtifact();
    Artifact a2 = o2.getArtifact();

    if (a1 == null && a2 != null) {
      return -1;
    }

    if (a1 != null && a2 == null) {
      return 1;
    }

    if (a1 == null) {
      return 0;
    }

    int result = a1.getGroupId().compareTo(a2.getGroupId());
    if (result != 0) {
      return result;
    }

    result = a1.getArtifactId().compareTo(a2.getArtifactId());
    if (result != 0) {
      return result;
    }

    return a1.getVersion().compareTo(a2.getVersion());

  }

}
