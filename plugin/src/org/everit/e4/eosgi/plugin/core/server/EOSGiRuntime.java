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
package org.everit.e4.eosgi.plugin.core.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.model.RuntimeDelegate;

/**
 * Empty runtime implementation.
 */
public class EOSGiRuntime extends RuntimeDelegate {

  public static final String RUNTIME_ID = "org.everit.e4.eosgi.plugin.runtime";

  public static final String RUNTIME_NAME = "EOSGi Runtime";

  /**
   * Create a runtime with the default name (EOSGi Runtime).
   *
   * @param monitor
   *          optinal {@link IProgressMonitor} instance.
   * @return IRuntime instance.
   * @throws CoreException
   *           throws this, if an error ocurred.
   */
  public static IRuntime createRuntime(final IProgressMonitor monitor) throws CoreException {
    return createRuntime(RUNTIME_NAME, monitor);
  }

  /**
   * Create a runtime with the given name.
   *
   * @param runtimeName
   *          name of the runtime.
   * @param monitor
   *          optinal {@link IProgressMonitor} instance.
   * @return IRuntime instance.
   * @throws CoreException
   *           throws this, if an error ocurred.
   */
  public static IRuntime createRuntime(final String runtimeName, final IProgressMonitor monitor)
      throws CoreException {
    IRuntime runtime = ServerCore.findRuntime(runtimeName);
    IRuntimeType runtimeType = ServerCore.findRuntimeType(RUNTIME_ID);
    if (runtime == null || !runtime.getRuntimeType().equals(runtimeType)) {
      IRuntimeWorkingCopy runtimeWorkingCopy = runtimeType.createRuntime(runtimeName, monitor);
      runtime = runtimeWorkingCopy.save(true, monitor);
    }
    return runtime;
  }

}
