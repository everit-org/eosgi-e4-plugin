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

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.everit.osgi.dev.dist.util.attach.EOSGiVMManager;
import org.everit.osgi.dev.dist.util.attach.EOSGiVMManagerParameter;
import org.everit.osgi.dev.e4.plugin.testresult.TestResultTracker;
import org.everit.osgi.dev.e4.plugin.ui.navigator.DistLabelProvider;

/**
 * Storing and managing {@link EOSGiProject} instances.
 */
public class EOSGiProjectManager implements Closeable {

  private static final long EOSGI_VM_MANAGER_UPDATE_PERIOD = 1000;

  private final AtomicBoolean closed = new AtomicBoolean(false);

  private final Map<IProject, EOSGiProject> eosgiProjects = new HashMap<>();

  private final EOSGiVMManager eosgiVMManager;

  private final AtomicLong eosgiVMManagerLastUpdateTime = new AtomicLong();

  private final Map<DistLabelProvider, Boolean> labelProviders = new ConcurrentHashMap<>();

  private final TestResultTracker testResultTracker;

  private final Runnable vmStateChangeHandler;

  /**
   * Constructor.
   */
  public EOSGiProjectManager() {

    EOSGiVMManagerParameter vmManagerParam = new EOSGiVMManagerParameter();
    vmManagerParam.classLoader = EOSGiVMManager.class.getClassLoader();
    vmManagerParam.exceptionDuringAttachVMHandler = (eventData) -> {
      EOSGiEclipsePlugin.getDefault().getEOSGiLog()
          .warning("Error during attaching VM: " + eventData.virtualMachineId, eventData.cause);
      return true;
    };
    vmManagerParam.deadlockEventHandler = (eventData) -> {
      EOSGiEclipsePlugin.getDefault().getEOSGiLog().warning("Deadlock in Attach API during getting"
          + " information about JVM: " + eventData.virtualMachineId);
    };

    eosgiVMManager = new EOSGiVMManager(vmManagerParam);
    vmStateChangeHandler = () -> {
      for (DistLabelProvider labelProvider : labelProviders.keySet()) {
        for (EOSGiProject eosgiProject : eosgiProjects.values()) {
          Set<ExecutableEnvironment> executableEnvironments =
              eosgiProject.getExecutableEnvironmentContainer().getExecutableEnvironments();

          for (ExecutableEnvironment executableEnvironment : executableEnvironments) {
            labelProvider.executableEnvironmentChanged(executableEnvironment);
          }
        }
      }
    };
    eosgiVMManager.addStateChangeListener(vmStateChangeHandler);

    this.testResultTracker = new TestResultTracker(eosgiVMManager);
  }

  public void addLabelProvider(final DistLabelProvider labelProvider) {
    labelProviders.put(labelProvider, Boolean.TRUE);
  }

  private void checkEOSGiVMManagerUpToDate() {
    long currentTimeMillis = System.currentTimeMillis();
    long lastUpdateTime = eosgiVMManagerLastUpdateTime.get();
    if (currentTimeMillis - lastUpdateTime > EOSGI_VM_MANAGER_UPDATE_PERIOD) {
      eosgiVMManager.refresh();
    }
  }

  @Override
  public void close() {
    closed.set(true);
    eosgiVMManager.removeStateChangeListener(vmStateChangeHandler);
    testResultTracker.close();
  }

  /**
   * Returns the eosgi project that is managed by this plugin.
   *
   * @param project
   *          The eclipse plugin that has eosgi configuration.
   * @param monitor
   *          The monitor to show progress.
   * @return The eosgi project if exists, otherwise <code>null</code>.
   * @throws CoreException
   *           if something happens.
   */
  public synchronized EOSGiProject get(final IProject project, final IProgressMonitor monitor)
      throws CoreException {

    EOSGiProject eosgiProject = eosgiProjects.get(project);
    if (eosgiProject == null && project.getNature(EOSGiNature.NATURE_ID) != null) {
      putOrOverride(MavenPlugin.getMavenProjectRegistry().getProject(project), monitor);
      eosgiProject = eosgiProjects.get(project);
    }
    return eosgiProject;
  }

  /**
   * Get the vm manager of this project manager.
   *
   * @return the VM manager.
   */
  public EOSGiVMManager getEosgiVMManager() {
    return eosgiVMManager;
  }

  public TestResultTracker getTestResultTracker() {
    return testResultTracker;
  }

  /**
   * Starts tracking results and JVMs.
   */
  public void open() {
    this.testResultTracker.open();
    new Thread(() -> {
      boolean interrupted = false;
      while (!interrupted && !closed.get()) {
        try {
          eosgiVMManager.refresh();
          Thread.sleep(EOSGI_VM_MANAGER_UPDATE_PERIOD);
        } catch (InterruptedException e) {
          interrupted = true;
          Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
          EOSGiEclipsePlugin.getDefault().getEOSGiLog()
              .error("Error during querying the state of running JVMs", e);
        }
      }
    }).start();
  }

  /**
   * Adds an eosgi project to this manager or overrides if it is already managed.
   *
   * @param mavenProject
   *          The m2e project.
   * @param monitor
   *          The monitor to show progress.
   * @throws CoreException
   *           if anything happens.
   */
  public synchronized void putOrOverride(final IMavenProjectFacade mavenProject,
      final IProgressMonitor monitor) throws CoreException {
    EOSGiProject eosgiProject = eosgiProjects.get(mavenProject.getProject());
    if (eosgiProject != null) {
      checkEOSGiVMManagerUpToDate();
      eosgiProject.refresh(mavenProject, monitor);
    } else {
      resolveProject(mavenProject, monitor);
    }
  }

  public void remove(final IProject project) {
    EOSGiProject eosgiProject = eosgiProjects.remove(project);
    eosgiProject.dispose();
  }

  public void removeLabelProvider(final DistLabelProvider labelProvider) {
    labelProviders.remove(labelProvider);
  }

  private EOSGiProject resolveProject(final IMavenProjectFacade mavenProject,
      final IProgressMonitor monitor) throws CoreException {

    checkEOSGiVMManagerUpToDate();
    EOSGiProject eosgiProject =
        new EOSGiProject(mavenProject, eosgiVMManager, monitor);
    eosgiProjects.put(mavenProject.getProject(), eosgiProject);

    return eosgiProject;
  }

}
