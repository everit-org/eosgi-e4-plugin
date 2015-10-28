package org.everit.e4.eosgi.plugin.ui.launcher;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class LaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

  @Override
  public void createTabs(final ILaunchConfigurationDialog dialog, final String mode) {
    ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[1];
    // tabs[0] = new ServerLaunchConfigurationTab(new String[] { "org.eclipse.wst.server.preview"
    // });
    // tabs[0].setLaunchConfigurationDialog(dialog);

    // tabs[0] = new EnvironmentTab();
    // tabs[0].setLaunchConfigurationDialog(dialog);
    tabs[0] = new CommonTab();
    tabs[0].setLaunchConfigurationDialog(dialog);
    setTabs(tabs);
  }

}
