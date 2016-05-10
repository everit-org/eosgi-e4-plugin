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
package org.everit.osgi.dev.e4.plugin.ui.util;

/**
 * Util functions for manage project natures.
 */
public final class ProjectNatureUtils {

  /**
   * Add nature id to given array and return with them.
   *
   * @param natureIds
   *          original nature id array.
   * @param natureId
   *          new nature id.
   * @return nature id array with given natureId.
   */
  public static String[] addNature(final String[] natureIds, final String natureId) {
    boolean notFound = true;
    for (String nature : natureIds) {
      if (natureId.equals(nature)) {
        notFound = false;
        break;
      }
    }

    if (notFound && (natureId != null)) {
      String[] newNatureIds = new String[natureIds.length + 1];
      System.arraycopy(natureIds, 0, newNatureIds, 1, natureIds.length);
      newNatureIds[0] = natureId;
      return newNatureIds;
    } else {
      return natureIds;
    }
  }

  private ProjectNatureUtils() {
  }

  public static String[] removeNature(final String[] natureIds, final String natureId) {
    // TODO implement this
    return natureIds;
  }
}
