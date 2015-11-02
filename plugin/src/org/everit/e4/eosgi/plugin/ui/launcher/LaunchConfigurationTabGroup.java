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
package org.everit.e4.eosgi.plugin.ui.launcher;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * Basic launcher tab group implemention.
 */
public class LaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

  /**
   * Create tabs for EOSGi dist launcher with only a common tab.
   */
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
