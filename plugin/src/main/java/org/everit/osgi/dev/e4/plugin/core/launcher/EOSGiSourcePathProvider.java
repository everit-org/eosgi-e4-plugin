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
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.m2e.jdt.internal.launch.MavenSourcePathProvider;

@SuppressWarnings("restriction")
public class EOSGiSourcePathProvider extends MavenSourcePathProvider {

  @Override
  public IRuntimeClasspathEntry[] computeUnresolvedClasspath(
      final ILaunchConfiguration configuration) throws CoreException {

    return super.computeUnresolvedClasspath(new CustomizedLaunchConfiguration(configuration));
  }

  @Override
  public IRuntimeClasspathEntry[] resolveClasspath(final IRuntimeClasspathEntry[] entries,
      final ILaunchConfiguration configuration) throws CoreException {

    return super.resolveClasspath(entries, new CustomizedLaunchConfiguration(configuration));
  }

}
