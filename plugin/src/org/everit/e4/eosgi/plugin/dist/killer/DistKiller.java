package org.everit.e4.eosgi.plugin.dist.killer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.everit.e4.eosgi.plugin.util.OSUtils;

/**
 * Abstract DistKiller class.
 */
public abstract class DistKiller {

  private static final String JPS = "jps";

  private static final String JPS_PARAM_FOR_DETAILED_OUTPUT = "-l";

  private static final String LINUX = "linux";

  private static final Logger LOGGER = Logger.getLogger(DistKiller.class.getName());

  private static final String WIN = "win";

  public static DistKiller createDistKiller(final List<String> filters) {
    return createDistKiller(OSUtils.currentOS(), filters);
  }

  /**
   * Create a DistKiller class by OS and filter list.
   * 
   * @param os
   *          OS name like 'linux', "win", "mac".
   * @param filters
   *          filter list. Example.: environment name.
   * @return OS specific DistKiller implementation.
   */
  public static DistKiller createDistKiller(final String os, final List<String> filters) {
    if (LINUX.equals(os)) {
      return new LinuxDistKiller(filters);
    } else if (WIN.equals(os)) {
      return new WindowsDistKiller(filters);
    } else {
      throw new IllegalArgumentException("unknown os type");
    }
  }

  private List<String> processFilters;

  protected DistKiller(final List<String> processFilters) {
    this.processFilters = processFilters;
  }

  /**
   * Fetch relevant JAVA process ID by JPS.
   * 
   * @return list of the relevant process IDs.
   */
  protected List<String> getRelevantJavaPids() {
    ProcessBuilder processBuilder = new ProcessBuilder(
        new String[] { JPS, JPS_PARAM_FOR_DETAILED_OUTPUT });
    List<String> processPids = null;
    try {
      Process jpsProcess = processBuilder.start();
      InputStream inputStream = jpsProcess.getInputStream();
      if (inputStream != null) {
        processPids = processPids(inputStream);
      }
      jpsProcess.waitFor();
    } catch (IOException e) {
      throw new RuntimeException("killing dist process", e);
    } catch (InterruptedException e) {
      throw new RuntimeException("killing dist process", e);
    }
    return processPids;
  }

  public abstract void kill();

  private boolean matchForFilter(final String processName) {
    for (String filter : this.processFilters) {
      if (processName.contains(filter)) {
        return true;
      }
    }
    return false;
  }

  private List<String> processPids(final InputStream inputStream) throws IOException {
    List<String> pidList = new ArrayList<>();
    BufferedReader bufferedReader = null;
    try {
      bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
      String line = "";
      while ((line = bufferedReader.readLine()) != null) {
        String[] splittedLine = line.split(" ");
        if (splittedLine.length == 2 && matchForFilter(splittedLine[1])) {
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

  /**
   * Run a process by the given array argument.
   * 
   * @param killerCommandArray
   *          command array.
   */
  protected void runKillerProcess(final String[] killerCommandArray) {
    ProcessBuilder processBuilder = new ProcessBuilder(killerCommandArray);
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

}
