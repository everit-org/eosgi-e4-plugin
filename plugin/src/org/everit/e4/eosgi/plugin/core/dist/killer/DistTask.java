package org.everit.e4.eosgi.plugin.core.dist.killer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Objects;

import org.eclipse.ui.console.MessageConsoleStream;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.everit.e4.eosgi.plugin.ui.EOSGiLog;

/**
 * Runnable for wrapper.
 */
@Deprecated
public class DistTask implements Runnable {

  /**
   * Callback interface for notify the dist stopped event.
   */
  public interface DistStoppedCallback {
    void distStopped();
  }

  private String environmentName;

  private EOSGiLog log;

  private MessageConsoleStream messageStream;

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
      final DistStoppedCallback stoppedCallback, final MessageConsoleStream messageStream) {
    super();
    this.messageStream = messageStream;
    Objects.requireNonNull(path, "path cannot be null");
    Objects.requireNonNull(environmentName, "environmentName cannot be null");
    this.path = path;
    this.environmentName = environmentName;
    this.stoppedCallback = stoppedCallback;
    log = new EOSGiLog(Activator.getDefault().getLog());
  }

  public synchronized boolean isStopped() {
    return stopped;
  }

  private void readProcessStream(final InputStream inputStream) {
    BufferedReader bufferedReader = null;
    try {
      bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
      String line = "";
      while (bufferedReader.ready() && (line = bufferedReader.readLine()) != null && !stopped) {
        if (messageStream != null) {
          messageStream.println(line);
        }
      }
    } catch (IOException e) {
      log.error("IO error", e);
    } finally {
      if (bufferedReader != null) {
        try {
          bufferedReader.close();
        } catch (IOException e) {
          log.error("closing input stream", e);
        }
      }
    }
  }

  @Override
  public void run() {
    ProcessBuilder processBuilder = new ProcessBuilder(path);
    try {
      process = processBuilder.start();

      InputStream inputStream = process.getInputStream();
      if (inputStream != null) {
        readProcessStream(inputStream);
      }

      int resultCode = process.waitFor();
      log.info("Wrapper stopped with resultCode: " + resultCode);
      stopped = true;
    } catch (IOException e) {
      log.error("IO error", e);
      log.error("dist start error", e);
    } catch (InterruptedException e) {
      log.error("dist start error", e);
    }
  }

  /**
   * Distroy the process.
   * 
   * @return return the result code.
   */
  public int stop() {
    stopProcessIfRunning();

    DistKiller.createDistKiller(Arrays.asList(environmentName, "org.rzo.yajsw.app.WrapperJVMMain"))
        .kill();

    if (stoppedCallback != null) {
      this.stoppedCallback.distStopped();
    }
    return 0;
  }

  private void stopProcessIfRunning() {
    if (!stopped && process != null) {
      process.destroyForcibly();
      log.info("Process stopped");
    }
  }

}
