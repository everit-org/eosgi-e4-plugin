package org.everit.e4.eosgi.plugin.util;

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

    if (notFound && natureId != null) {
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
}
