package org.everit.e4.eosgi.plugin.util;

import java.io.File;
import java.util.Objects;

import org.everit.e4.eosgi.plugin.util.OSUtils.OSType;

public final class DistUtils {
  public static final String DIST_BIN = "bin";

  public static final String DIST_FOLDER = "eosgi-dist";

  public static final String DIST_LOG = "log";

  public static final String LINUX_START = "runConsole.sh";

  public static final String WIN_START = "runConsole.bat";

  public static String getDistStartCommand(final String buildDirectory,
      final String environmentId) {
    Objects.requireNonNull(environmentId, "environmentId cannot be null");
    Objects.requireNonNull(buildDirectory, "buildDirectory cannot be null");

    String binPath = buildDirectory + File.separator + DIST_FOLDER + File.separator
        + environmentId + File.separator + DIST_BIN + File.separator;
    if (OSType.WINDOWS == OSUtils.currentOS()) {
      binPath = binPath + WIN_START;
    } else {
      binPath = binPath + LINUX_START;
    }
    return binPath;
  }

  private DistUtils() {
  }
}
