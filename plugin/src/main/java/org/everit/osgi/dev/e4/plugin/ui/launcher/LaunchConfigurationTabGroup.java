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
package org.everit.osgi.dev.e4.plugin.ui.launcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;

/**
 * Basic launcher tab group implemention.
 */
public class LaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

  /**
   * Create tabs for EOSGi dist launcher with only a common tab.
   */
  @Override
  public void createTabs(final ILaunchConfigurationDialog dialog, final String mode) {
    List<ILaunchConfigurationTab> tabs = new ArrayList<>();

    JavaMainTab javaMainTab = new JavaMainTab();
    javaMainTab.setLaunchConfigurationDialog(dialog);
    tabs.add(javaMainTab);

    JavaArgumentsTab javaArgumentsTab = new JavaArgumentsTab();
    javaArgumentsTab.setLaunchConfigurationDialog(dialog);
    tabs.add(javaArgumentsTab);

    JavaJRETab jreTab = new JavaJRETab();
    jreTab.setLaunchConfigurationDialog(dialog);
    tabs.add(jreTab);

    SourceLookupTab sourceLookupTab = new SourceLookupTab();
    sourceLookupTab.setLaunchConfigurationDialog(dialog);
    tabs.add(sourceLookupTab);

    setTabs(tabs.toArray(new ILaunchConfigurationTab[0]));
  }

}
