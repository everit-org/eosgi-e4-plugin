package org.everit.e4.eosgi.plugin.dist;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.Logger;

/**
 * Runnable for wrapper.
 */
public class DistTask implements Runnable {

  /**
   * Callback interface for notify the dist stopped event.
   */
  public interface DistStoppedCallback {
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

  private int getPidFromProcess() {
    int pid = -1;
    try {
      Field field = process.getClass().getDeclaredField("pid");
      field.setAccessible(true);
      pid = field.getInt(process);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException("determine dist process pid", e);
    } catch (SecurityException e) {
      throw new RuntimeException("determine dist process pid", e);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("determine dist process pid", e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("determine dist process pid", e);
    }
    return pid;
  }

  public synchronized boolean isStopped() {
    return stopped;
  }

  private void killRelevantProcesses() {
    ProcessBuilder processBuilder = new ProcessBuilder(
        new String[] { "/home/zsoltdoma/bin/kill-dist.sh", "raca-production-main" });
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
    this.stoppedCallback.distStopped();
    return 0;
  }

  private void stopProcessIfRunning() {
    if (!stopped && process != null) {
      process.destroy();
      LOGGER.info("Process stopped");
    }
  }

}
