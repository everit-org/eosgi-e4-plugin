package org.everit.e4.eosgi.plugin.dist;

/**
 * Utility method for OS specific staffs.
 */
public final class OSUtils {

  public static String currentOS() {
    return System.getProperty("os.name").toLowerCase();
  }

  private OSUtils() {
    super();
  }
}
