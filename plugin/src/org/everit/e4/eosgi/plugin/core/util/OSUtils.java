package org.everit.e4.eosgi.plugin.core.util;

/**
 * Utility method for OS specific staffs.
 */
public final class OSUtils {

  /**
   * OS type.
   */
  public static enum OSType {
    LINUX, WINDOWS;
  }

  private static final String LINUX = "linux";

  private static final String WIN = "win";

  /**
   * Detect the type of the current OS.
   * 
   * @return {@link OSType} instance by current OS.
   */
  public static OSType currentOS() {
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.contains(LINUX)) {
      return OSType.LINUX;
    } else if (osName.contains(WIN)) {
      return OSType.WINDOWS;
    } else {
      throw new RuntimeException("Unknown OS type");
    }
  }

  private OSUtils() {
    super();
  }
}
