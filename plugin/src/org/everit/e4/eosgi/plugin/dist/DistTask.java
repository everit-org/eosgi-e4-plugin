package org.everit.e4.eosgi.plugin.dist;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Runnable for wrapper.
 */
public class DistTask implements Runnable {

  /**
   * Callback interface for notify the dist stopped event.
   */
  public static interface DistStoppedCallback {
    void distStopped();
  }

  private static final Logger LOGGER = Logger.getLogger(DistTask.class.getName());

  /**
   * Path of the executable file.
   */
  private String path;

  private Process process;

  private volatile boolean stopped;

  private DistStoppedCallback stoppedCallback;

  public DistTask(final String path, final DistStoppedCallback stoppedCallback) {
    super();
    this.path = path;
    this.stoppedCallback = stoppedCallback;
  }

  public synchronized boolean isStopped() {
    return stopped;
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
      stoppedCallback.distStopped();
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
    int waitFor = 0;
    if (this.process != null) {
      this.process.destroy();
    }
    return waitFor;
  }

}
