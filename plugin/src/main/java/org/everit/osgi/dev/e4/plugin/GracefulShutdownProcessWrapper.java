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
package org.everit.osgi.dev.e4.plugin;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.swt.widgets.Display;
import org.everit.osgi.dev.dist.util.attach.EOSGiVMManager;

/**
 * A Process wrapper that tries shutting down the wrapped process gracefully before calling
 * terminate on it.
 */
public class GracefulShutdownProcessWrapper implements IProcess {

  private final EOSGiVMManager eosgiVMManager;

  private final long shutdownTimeout;

  private final AtomicBoolean terminateCalled = new AtomicBoolean(false);

  private final String uniqueLaunchId;

  private final IProcess wrapped;

  /**
   * Constructor.
   *
   * @param wrapped
   *          The wrapped process.
   * @param eosgiVMManager
   *          The vm manager that is used to gracefully terminate the vm.
   * @param uniqueLaunchId
   *          The unique launch id.
   */
  public GracefulShutdownProcessWrapper(final IProcess wrapped,
      final EOSGiVMManager eosgiVMManager, final String uniqueLaunchId,
      final Long shutdownTimeout) {
    this.wrapped = wrapped;
    this.eosgiVMManager = eosgiVMManager;
    this.uniqueLaunchId = uniqueLaunchId;
    this.shutdownTimeout = shutdownTimeout;
  }

  @Override
  public boolean canTerminate() {
    return wrapped.canTerminate();
  }

  @Override
  public <T> T getAdapter(final Class<T> adapter) {
    return wrapped.getAdapter(adapter);
  }

  @Override
  public String getAttribute(final String key) {
    return wrapped.getAttribute(key);
  }

  @Override
  public int getExitValue() throws DebugException {
    return wrapped.getExitValue();
  }

  @Override
  public String getLabel() {
    return wrapped.getLabel();
  }

  @Override
  public ILaunch getLaunch() {
    return wrapped.getLaunch();
  }

  @Override
  public IStreamsProxy getStreamsProxy() {
    return wrapped.getStreamsProxy();
  }

  private void gracefulTerminate(final String virtualMachineId) throws DebugException {

    Job job = Job.create("Terminating JVM gracefully", (monitor) -> {
      SubMonitor.convert(monitor);
      try {
        eosgiVMManager.shutDownVirtualMachine(virtualMachineId, null);
      } catch (RuntimeException e) {
        try {
          wrapped.terminate();
        } catch (DebugException e1) {
          throw new RuntimeException(e1);
        }
      }
      return Status.OK_STATUS;
    });
    job.schedule();

    try {
      job.join(shutdownTimeout, new NullProgressMonitor());
    } catch (OperationCanceledException | InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public boolean isTerminated() {
    return wrapped.isTerminated();
  }

  @Override
  public void setAttribute(final String key, final String value) {
    wrapped.setAttribute(key, value);
  }

  @Override
  public void terminate() throws DebugException {
    if (terminateCalled.getAndSet(true)) {
      wrapped.terminate();
      return;
    }
    eosgiVMManager.refresh();
    final String virtualMachineId =
        eosgiVMManager.getVirtualMachineIdByIUniqueLaunchId(uniqueLaunchId);
    if (virtualMachineId == null) {
      wrapped.terminate();
      return;
    }

    Display.getDefault().asyncExec(() -> {
      try {
        gracefulTerminate(virtualMachineId);
      } catch (DebugException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
