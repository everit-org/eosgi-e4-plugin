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
package org.everit.osgi.dev.e4.plugin.core.util;

import java.util.Locale;

/**
 * Utility method for OS specific staffs.
 */
public final class OSUtils {

  /**
   * OS type.
   */
  public enum OSType {
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
    String osName = System.getProperty("os.name").toLowerCase(Locale.getDefault());
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
