package org.everit.e4.eosgi.plugin.core.dist.killer;

import java.util.ArrayList;
import java.util.List;

/**
 * DistKiller implementation for Windows.
 */
@Deprecated
public class WindowsDistKiller extends DistKiller {

  protected WindowsDistKiller(final List<String> processFilters) {
    super(processFilters);
  }

  @Override
  public void kill() {
    List<String> pids = getRelevantJavaPids();
    List<String> killerCommand = new ArrayList<>();
    killerCommand.add("taskkill");
    killerCommand.add("/f");
    for (String pid : pids) {
      killerCommand.add("/pid");
      killerCommand.add(pid);
    }
    runKillerProcess(killerCommand.toArray(new String[] {}));
  }

}
