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
package org.everit.osgi.dev.e4.plugin.core.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.everit.osgi.dev.e4.plugin.EOSGiEclipsePlugin;
import org.everit.osgi.dev.e4.plugin.EOSGiLog;

/**
 * Extended {@link JavaLaunchDelegate} class for EOSGi launcher.
 */
public class EOSGILaunchConfigurationDelegate extends JavaLaunchDelegate {

  public static final String LAUNCHER_ATTR_ENVIRONMENT_ID = "environmentId";

  private final EOSGiLog log;

  /**
   * Constructor.
   */
  public EOSGILaunchConfigurationDelegate() {
    super();
    ILog iLog = EOSGiEclipsePlugin.getDefault().getLog();
    log = new EOSGiLog(iLog);
    log.info("launch configuration created");
  }

  @Override
  public void launch(final ILaunchConfiguration configuration, final String mode,
      final ILaunch launch, final IProgressMonitor monitor) throws CoreException {
    String environmentId = configuration.getAttribute(LAUNCHER_ATTR_ENVIRONMENT_ID, "");
    String projectName = configuration
        .getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");

    if ("".equals(environmentId) || "".equals(projectName)) {
      return;
    }

    super.launch(configuration, mode, launch, monitor);
  }

}
