package org.everit.e4.eosgi.plugin.dist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

  private static final String JPS = "jps";

  private static final String JPS_PARAM_FOR_DETAILED_OUTPUT = "-l";

  private static final Map<String, String []> KILL_COMMANDS_BY_OS = new HashMap<>();

  private static final Logger LOGGER = Logger.getLogger(DistTask.class.getName());

  static {
    KILL_COMMANDS_BY_OS.put("win", new String[] { "", "", "forpid" }); // TODO win
    KILL_COMMANDS_BY_OS.put("linux", new String[] { "kill", "-2", "forpid" });
  }

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

  private List<String> getRelevantJavaPids() {
    ProcessBuilder processBuilder = new ProcessBuilder(
        new String[] { JPS, JPS_PARAM_FOR_DETAILED_OUTPUT });
    List<String> processPids = null;
    try {
      Process killerProcess = processBuilder.start();
      InputStream inputStream = killerProcess.getInputStream();
      if (inputStream != null) {
        processPids = processPids(inputStream);
      }
      killerProcess.waitFor();
    } catch (IOException e) {
      throw new RuntimeException("killing dist process", e);
    } catch (InterruptedException e) {
      throw new RuntimeException("killing dist process", e);
    }
    return processPids;
  }

  public synchronized boolean isStopped() {
    return stopped;
  }

  private void killProcessByPid(final String pid) {
    String[] killComman = KILL_COMMANDS_BY_OS.get(OSUtils.currentOS());
    killComman[2] = pid;
    ProcessBuilder processBuilder = new ProcessBuilder(
        killComman);
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

  private void killProcesses(final List<String> pids) {
    if (pids != null) {
      for (String pid : pids) {
        killProcessByPid(pid);
      }
    }
  }

  private List<String> processPids(final InputStream inputStream) throws IOException {
    List<String> pidList = new ArrayList<>();
    BufferedReader bufferedReader = null;
    try {
      bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
      String line = "";
      while ((line = bufferedReader.readLine()) != null) {
        String[] splittedLine = line.split(" ");
        if (splittedLine.length == 2 && splittedLine[1].contains(environmentName)) {
          pidList.add(splittedLine[0]);
        }
      }
    } catch (Exception e) {
      LOGGER.warning("Can't read JPS output.");
    } finally {
      if (bufferedReader != null) {
        bufferedReader.close();
      }
    }
    return pidList;
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
    killProcesses(getRelevantJavaPids());
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
