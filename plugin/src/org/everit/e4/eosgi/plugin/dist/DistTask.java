package org.everit.e4.eosgi.plugin.dist;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Runnable for wrapper.
 */
public class DistTask implements Runnable {

  private static final String KILLER_SCRIPT_PATH = "/home/zsoltdoma/bin/kill-dist.sh";

  /**
   * Callback interface for notify the dist stopped event.
   */
  public interface DistStoppedCallback {
    void distStopped();
  }

  private static final Logger LOGGER = Logger.getLogger(DistTask.class.getName());

  private String environmentName;

  /**
   * Path of the executable file.
   */
  private String path;

  private Process process;

  private volatile boolean stopped;

  private DistStoppedCallback stoppedCallback;

  /**
   * {@link Runnable} class for running a dist.
   * 
   * @param path
   *          starter script full path.
   * @param environmentName
   *          name of the environment.
   * @param stoppedCallback
   *          callback listener.
   */
  public DistTask(final String path, final String environmentName,
      final DistStoppedCallback stoppedCallback) {
    super();
    Objects.requireNonNull(path, "path cannot be null");
    Objects.requireNonNull(environmentName, "environmentName cannot be null");
    this.path = path;
    this.environmentName = environmentName;
    this.stoppedCallback = stoppedCallback;
  }

  public synchronized boolean isStopped() {
    return stopped;
  }

  private void killRelevantProcesses() {
    ProcessBuilder processBuilder = new ProcessBuilder(
        new String[] { KILLER_SCRIPT_PATH, environmentName });
    try {
      Process killerProcess = processBuilder.start();
      int killerResult = killerProcess.waitFor();
      LOGGER.info("killer process result: " + killerResult);
    } catch (IOException e) {
      throw new RuntimeException("killing dist process", e);
    } catch (InterruptedException e) {
      throw new RuntimeException("killing dist process", e);
    }
  }

  @Override
  public void run() {
    ProcessBuilder processBuilder = new ProcessBuilder(path);
    try {
      process = processBuilder.start();
      LOGGER.info("wrapper running...");
      int resultCode = process.waitFor();
      LOGGER.info("Wrapper stopped with resultCode: " + resultCode);
      stopped = true;
    } catch (IOException e) {
      LOGGER.severe(e.getMessage());
    } catch (InterruptedException e) {
      LOGGER.severe(e.getMessage());
    }
  }

  /**
   * Distroy the process.
   * 
   * @return return the result code.
   */
  public int stop() {
    stopProcessIfRunning();
    killRelevantProcesses();
    if (stoppedCallback != null) {
      this.stoppedCallback.distStopped();
    }
    return 0;
  }

  private void stopProcessIfRunning() {
    if (!stopped && process != null) {
      process.destroy();
      LOGGER.info("Process stopped");
    }
  }

}
