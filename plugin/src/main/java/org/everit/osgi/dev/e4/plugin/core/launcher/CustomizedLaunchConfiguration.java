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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

/**
 * Helper class that returns a {@link CustomizedLaunchConfigurationType} and always says that
 * default classpath is used.
 */
public class CustomizedLaunchConfiguration implements ILaunchConfiguration {

  private final ILaunchConfiguration wrapped;

  public CustomizedLaunchConfiguration(final ILaunchConfiguration wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public boolean contentsEqual(final ILaunchConfiguration configuration) {
    return wrapped.contentsEqual(configuration);
  }

  @Override
  public ILaunchConfigurationWorkingCopy copy(final String name) throws CoreException {
    return wrapped.copy(name);
  }

  @Override
  public void delete() throws CoreException {
    wrapped.delete();
  }

  @Override
  public boolean exists() {
    return wrapped.exists();
  }

  @Override
  public <T> T getAdapter(final Class<T> adapter) {
    return wrapped.getAdapter(adapter);
  }

  @Override
  public boolean getAttribute(final String attributeName, final boolean defaultValue)
      throws CoreException {

    if (IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH.equals(attributeName)) {
      return true;
    }
    return wrapped.getAttribute(attributeName, defaultValue);
  }

  @Override
  public int getAttribute(final String attributeName, final int defaultValue)
      throws CoreException {
    return wrapped.getAttribute(attributeName, defaultValue);
  }

  @Override
  public List<String> getAttribute(final String attributeName, final List<String> defaultValue)
      throws CoreException {
    return wrapped.getAttribute(attributeName, defaultValue);
  }

  @Override
  public Map<String, String> getAttribute(final String attributeName,
      final Map<String, String> defaultValue)
      throws CoreException {
    return wrapped.getAttribute(attributeName, defaultValue);
  }

  @Override
  public Set<String> getAttribute(final String attributeName, final Set<String> defaultValue)
      throws CoreException {
    return wrapped.getAttribute(attributeName, defaultValue);
  }

  @Override
  public String getAttribute(final String attributeName, final String defaultValue)
      throws CoreException {
    return wrapped.getAttribute(attributeName, defaultValue);
  }

  @Override
  public Map<String, Object> getAttributes() throws CoreException {
    return wrapped.getAttributes();
  }

  @Override
  public String getCategory() throws CoreException {
    return wrapped.getCategory();
  }

  @Override
  public IFile getFile() {
    return wrapped.getFile();
  }

  @Override
  public IPath getLocation() {
    return wrapped.getLocation();
  }

  @Override
  public IResource[] getMappedResources() throws CoreException {
    return wrapped.getMappedResources();
  }

  @Override
  public String getMemento() throws CoreException {
    return wrapped.getMemento();
  }

  @Override
  public Set<String> getModes() throws CoreException {
    return wrapped.getModes();
  }

  @Override
  public String getName() {
    return wrapped.getName();
  }

  @Override
  public ILaunchDelegate getPreferredDelegate(final Set<String> modes) throws CoreException {
    return wrapped.getPreferredDelegate(modes);
  }

  @Override
  public ILaunchConfigurationType getType() throws CoreException {
    return new CustomizedLaunchConfigurationType(wrapped.getType());
  }

  @Override
  public ILaunchConfigurationWorkingCopy getWorkingCopy() throws CoreException {
    return wrapped.getWorkingCopy();
  }

  @Override
  public boolean hasAttribute(final String attributeName) throws CoreException {
    return wrapped.hasAttribute(attributeName);
  }

  @Override
  public boolean isLocal() {
    return wrapped.isLocal();
  }

  @Override
  public boolean isMigrationCandidate() throws CoreException {
    return wrapped.isMigrationCandidate();
  }

  @Override
  public boolean isReadOnly() {
    return wrapped.isReadOnly();
  }

  @Override
  public boolean isWorkingCopy() {
    return wrapped.isWorkingCopy();
  }

  @Override
  public ILaunch launch(final String mode, final IProgressMonitor monitor) throws CoreException {
    return wrapped.launch(mode, monitor);
  }

  @Override
  public ILaunch launch(final String mode, final IProgressMonitor monitor, final boolean build)
      throws CoreException {
    return wrapped.launch(mode, monitor, build);
  }

  @Override
  public ILaunch launch(final String mode, final IProgressMonitor monitor, final boolean build,
      final boolean register)
      throws CoreException {
    return wrapped.launch(mode, monitor, build, register);
  }

  @Override
  public void migrate() throws CoreException {
    wrapped.migrate();
  }

  @Override
  public boolean supportsMode(final String mode) throws CoreException {
    return wrapped.supportsMode(mode);
  }

}
