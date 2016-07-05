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
package org.everit.osgi.dev.e4.plugin.m2e;

import org.eclipse.osgi.util.NLS;

/**
 * I18N constants.
 */
public final class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.everit.osgi.dev.e4.plugin.core.m2e.messages";

  public static String dialogMessageIncompatibleMavenPlugin;

  public static String dialogTitleIncompatibleMavenPlugin;

  public static String monitorCreateServer;

  public static String monitorLoadDistXML;

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {
  }
}