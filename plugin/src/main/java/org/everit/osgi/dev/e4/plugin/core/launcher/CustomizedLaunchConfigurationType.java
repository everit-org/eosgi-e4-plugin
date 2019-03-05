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

import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;
import org.eclipse.m2e.jdt.internal.launch.MavenRuntimeClasspathProvider;

/**
 * Helper class that resolves classes from the classpath of the project.
 */
@SuppressWarnings("restriction")
public class CustomizedLaunchConfigurationType implements ILaunchConfigurationType {

  private final ILaunchConfigurationType wrapped;

  public CustomizedLaunchConfigurationType(final ILaunchConfigurationType wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public <T> T getAdapter(final Class<T> adapter) {
    return wrapped.getAdapter(adapter);
  }

  @Override
  public String getAttribute(final String attributeName) {
    if ("id".equals(attributeName)) {
      return MavenRuntimeClasspathProvider.JDT_JAVA_APPLICATION;
    }
    return wrapped.getAttribute(attributeName);
  }

  @Override
  public String getCategory() {
    return wrapped.getCategory();
  }

  @Override
  public String getContributorName() {
    return wrapped.getContributorName();
  }

  @Override
  public ILaunchConfigurationDelegate getDelegate() throws CoreException {
    return wrapped.getDelegate();
  }

  @Override
  public ILaunchConfigurationDelegate getDelegate(final String mode) throws CoreException {
    return wrapped.getDelegate(mode);
  }

  @Override
  public ILaunchDelegate[] getDelegates(final Set<String> modes) throws CoreException {
    return wrapped.getDelegates(modes);
  }

  @Override
  public String getIdentifier() {
    return wrapped.getIdentifier();
  }

  @Override
  public String getName() {
    return wrapped.getName();
  }

  @Override
  public String getPluginIdentifier() {
    return wrapped.getPluginIdentifier();
  }

  @Override
  public ILaunchDelegate getPreferredDelegate(final Set<String> modes) throws CoreException {
    return wrapped.getPreferredDelegate(modes);
  }

  @Override
  public ILaunchConfiguration[] getPrototypes() throws CoreException {
    return wrapped.getPrototypes();
  }

  @Override
  public String getSourceLocatorId() {
    return wrapped.getSourceLocatorId();
  }

  @Override
  public ISourcePathComputer getSourcePathComputer() {
    return wrapped.getSourcePathComputer();
  }

  @Override
  public Set<Set<String>> getSupportedModeCombinations() {
    return wrapped.getSupportedModeCombinations();
  }

  @Override
  public Set<String> getSupportedModes() {
    return wrapped.getSupportedModes();
  }

  @Override
  public boolean isPublic() {
    return wrapped.isPublic();
  }

  @Override
  public ILaunchConfigurationWorkingCopy newInstance(final IContainer container, final String name)
      throws CoreException {
    return wrapped.newInstance(container, name);
  }

  @Override
  public ILaunchConfigurationWorkingCopy newPrototypeInstance(final IContainer container,
      final String name)
      throws CoreException {
    return wrapped.newPrototypeInstance(container, name);
  }

  @Override
  public void setPreferredDelegate(final Set<String> modes, final ILaunchDelegate delegate)
      throws CoreException {
    wrapped.setPreferredDelegate(modes, delegate);
  }

  @Override
  public boolean supportsCommandLine() {
    return wrapped.supportsCommandLine();
  }

  @Override
  public boolean supportsMode(final String mode) {
    return wrapped.supportsMode(mode);
  }

  @Override
  public boolean supportsModeCombination(final Set<String> modes) {
    return wrapped.supportsModeCombination(modes);
  }

  @Override
  public boolean supportsPrototypes() {
    return wrapped.supportsPrototypes();
  }

}
