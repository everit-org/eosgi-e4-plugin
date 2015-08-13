package org.everit.e4.eosgi.plugin.dist;

import org.everit.e4.eosgi.plugin.dist.DistTask.DistStoppedCallback;
import org.everit.e4.eosgi.plugin.dist.gogo.GogoClient;
import org.everit.e4.eosgi.plugin.dist.gogo.GogoClient.DisconnecedCallback;
import org.everit.e4.eosgi.plugin.dist.gogo.GogoShellCommand;

public class EosgiDistRunner implements DistRunner, DistStoppedCallback, DisconnecedCallback {
  private static final String LOCALHOST = "localhost";

  private DistTask distTask;

  private GogoClient gogoClient;

  private DistStatusListener statusListener;

  /**
   * Constructor.
   * 
   * @param consolePort
   *          port for osgi console.
   * @param distPath
   *          root path of the dist.
   * @param statusListener
   *          listener class for handle status changes.
   */
  public EosgiDistRunner(final int consolePort, final String distPath,
      final DistStatusListener statusListener) {
    super();
    if (consolePort > 0) {
      gogoClient = new GogoClient(LOCALHOST, consolePort, this);
    }
    this.distTask = new DistTask(distPath, this);
    this.statusListener = statusListener;
  }

  @Override
  public void disconnected() {
    statusListener.distStatusChanged(DistStatus.STOPPING);
    this.distTask.stop();
  }

  @Override
  public void distStopped() {
    this.statusListener.distStatusChanged(DistStatus.STOPPED);
  }

  @Override
  public void onStatusChanged(final DistStatus distStatus) {
    if (statusListener != null) {
      statusListener.distStatusChanged(distStatus);
    }
  }

  @Override
  public void start() {
    statusListener.distStatusChanged(DistStatus.STARTING);
    new Thread(distTask).start();

    if (gogoClient == null) {
      statusListener.distStatusChanged(DistStatus.STARTED);
    } else {
      gogoClient.connect();
      if (gogoClient.isConnected()) {
        statusListener.distStatusChanged(DistStatus.RUNNING);
      } else {
        statusListener.distStatusChanged(DistStatus.DETACHED);
      }
    }
  }

  @Override
  public void stop() {
    statusListener.distStatusChanged(DistStatus.STOPPING);
    if (gogoClient == null) {
      this.distTask.stop();
    } else {
      gogoClient.sendCommand(GogoShellCommand.CLOSE);
    }
  }

}
