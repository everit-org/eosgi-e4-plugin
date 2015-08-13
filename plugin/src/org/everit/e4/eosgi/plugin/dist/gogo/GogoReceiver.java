package org.everit.e4.eosgi.plugin.dist.gogo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for handle received messages from GoGo server.
 */
public class GogoReceiver implements Runnable {
  /**
   * Callback interface for notify {@link GogoClient} that the server is disconnected.
   */
  static interface ReceivedMessageCallback {
    void receivedMessage(final String message);
  }

  private static final Logger LOGGER = Logger.getLogger(GogoReceiver.class.getName());

  private static final String UTF_8 = "UTF-8";

  private BufferedReader bufferedReader;

  private ReceivedMessageCallback receverCallback;

  /**
   * Consturctor with the input stream.
   * 
   * @param inputStream
   *          {@link InputStream} from socket.
   */
  public GogoReceiver(final InputStream inputStream,
      final ReceivedMessageCallback stoppedCallback) {
    super();
    this.receverCallback = stoppedCallback;
    Objects.requireNonNull(inputStream, "inputStream cannot be null");
    bufferedReader = new BufferedReader(
        new InputStreamReader(inputStream/* , Charset.forName(UTF_8) */));
  }

  private void closeStream() {
    try {
      bufferedReader.close();
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "closeing input stream", e);
    }
  }

  private void processLine(final String line) {
    if (line.startsWith("osgi>")) {
      this.receverCallback.receivedMessage("prompt");
    }
    LOGGER.log(Level.INFO, ">>> " + line);
  }

  @Override
  public void run() {
    LOGGER.info("receiver is started");
    try {
      String line = "";
      while ((line = bufferedReader.readLine()) != null) {
        processLine(line);
      }
      LOGGER.info("receiver stopped");
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "receiving messages", e);
    } finally {
      if (bufferedReader != null) {
        closeStream();
      }
      this.receverCallback.receivedMessage(null);
    }
  }
}
