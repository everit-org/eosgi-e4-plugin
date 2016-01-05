package org.everit.e4.eosgi.plugin.core.m2e;

import org.eclipse.osgi.util.NLS;

/**
 * I18N constants.
 */
public final class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.everit.e4.eosgi.plugin.core.m2e.messages";

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
