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
package org.everit.e4.eosgi.plugin.core.util;

import java.io.File;
import java.util.Objects;

import org.everit.e4.eosgi.plugin.core.util.OSUtils.OSType;

/**
 * Utility class for dist managing.
 */
public final class DistUtils {
  public static final String DIST_BIN = "bin";

  public static final String DIST_FOLDER = "eosgi-dist";

  public static final String DIST_LOG = "log";

  public static final String LINUX_START = "runConsole.sh";

  public static final String WIN_START = "runConsole.bat";

  /**
   * Get a dist start command by build directory and environment id.
   * 
   * @param buildDirectory
   *          build directory.
   * @param environmentId
   *          environment id.
   * @return command String.
   */
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
