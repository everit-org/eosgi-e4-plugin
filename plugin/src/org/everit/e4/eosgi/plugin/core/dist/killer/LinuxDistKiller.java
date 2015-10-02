package org.everit.e4.eosgi.plugin.core.dist.killer;

import java.util.List;

/**
 * {@link DistKiller} implementation for Linux OS.
 */
@Deprecated
public class LinuxDistKiller extends DistKiller {

  protected LinuxDistKiller(final List<String> processFilters) {
    super(processFilters);
  }

  @Override
  public void kill() {
    List<String> pids = getRelevantJavaPids();
    if (pids != null) {
      for (String pid : pids) {
        runKillerProcess(new String[] { "kill", "-2", pid });
      }
    }
  }

}
