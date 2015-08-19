package org.everit.e4.eosgi.plugin.dist;

import java.util.Objects;

import org.eclipse.ui.console.MessageConsoleStream;
import org.everit.e4.eosgi.plugin.dist.DistTask.DistStoppedCallback;
import org.everit.e4.eosgi.plugin.dist.gogo.GogoClient.DisconnecedCallback;

/**
 * Dist runner class.
 */
public class EosgiDistRunner implements DistRunner, DistStoppedCallback, DisconnecedCallback {
  private DistTask distTask;

  private DistStatusListener statusListener;

  /**
   * Constructor.
   * 
   * @param consolePort
   *          port for osgi console.
   * @param distPath
   *          root path of the dist.
   * @param environmentName
   *          name of the environment.
   * @param statusListener
   *          listener class for handle status changes.
   */
  public EosgiDistRunner(final int consolePort, final String distPath,
      final String environmentName, final DistStatusListener statusListener,
      final MessageConsoleStream messageStream) {
    Objects.requireNonNull(distPath, "distPath cannot be null");
    Objects.requireNonNull(environmentName, "environmentName cannot be null");

    // if (consolePort > 0) {
    // gogoClient = new GogoClient(LOCALHOST, consolePort, this);
    // }

    this.distTask = new DistTask(distPath, environmentName, this, messageStream);
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
    statusListener.distStatusChanged(DistStatus.STARTED);
  }

  @Override
  public void stop() {
    statusListener.distStatusChanged(DistStatus.STOPPING);
    this.distTask.stop();
  }

}
