package org.everit.e4.eosgi.plugin.dist.gogo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.everit.e4.eosgi.plugin.dist.gogo.GogoReceiver.ReceivedMessageCallback;

/**
 * Class for manage GoGo shell connection.
 */
public class GogoClient implements ReceivedMessageCallback {

  /**
   * Callback interface for disconnection.
   */
  public static interface DisconnecedCallback {
    void disconnected();
  }

  private static final int DEFAULT_CONNECTION_TIMEOUT_IN_SECONDS = 100;

  private static final Logger LOGGER = Logger.getLogger(GogoClient.class.getName());

  private static final int ONE_SEC_IN_MILLIS = 1000;

  private BufferedWriter bufferedWriter;

  private DisconnecedCallback disconnecedCallback;

  private GogoReceiver gogoReceiver;

  private String host = "localhost";

  private int port;

  private Socket socket;

  private int tryCounter;

  /**
   * Constructor with host and port for connection.
   * 
   * @param host
   *          osgi dist host.
   * @param port
   *          osgi dist console port.
   */
  public GogoClient(final String host, final int port,
      final DisconnecedCallback disconnecedCallback) {
    super();
    this.host = host;
    this.port = port;
    this.disconnecedCallback = disconnecedCallback;
  }

  /**
   * Connecting to dist on given port. If dist not running, the method waiting to start it.
   */
  public void connect() {
    tryCounter = 0;
    while (tryCounter < DEFAULT_CONNECTION_TIMEOUT_IN_SECONDS && this.socket == null) {
      try {
        this.socket = new Socket(host, port);
        LOGGER.log(Level.INFO,
            "connected to " + socket.getRemoteSocketAddress());
        createAndStartGoGoReceiver();
        bufferedWriter = new BufferedWriter(
            new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
      } catch (UnknownHostException e) {
        throw new RuntimeException("connecting to osgi dist", e);
      } catch (IOException e) {
        waitOneSecond();
        tryCounter++;
      }
    }
  }

  private void createAndStartGoGoReceiver() throws IOException {
    gogoReceiver = new GogoReceiver(socket.getInputStream(), this);
    new Thread(gogoReceiver).start();
  }

  /**
   * Close gogo shell client socket.
   */
  public void disconnect() {
    if (this.socket != null) {
      try {
        this.socket.close();
        this.socket = null;
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "close socket", e);
      }
    }
  }

  /**
   * Connection check.
   * 
   * @return <code>true</code> if connection is alive, <code>false</code> otherwise.
   */
  public boolean isConnected() {
    return this.socket != null && socket.isConnected();
  }

  @Override
  public void receivedMessage(final String message) {
    if (disconnecedCallback != null) {
      if (message == null) {
        disconnecedCallback.disconnected();
      } else if ("prompt".equals(message)) {
        LOGGER.info("gogo ok");
      }
    }
  }

  private void send(final String message) {
    try {
      this.bufferedWriter.write(message);
      this.bufferedWriter.newLine();
      this.bufferedWriter.newLine();
      this.bufferedWriter.flush();
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "sending message", e);
    }
  }

  /**
   * Send command to the gogo server.
   * 
   * @param gogoShellCommand
   *          command.
   */
  public void sendCommand(final GogoShellCommand gogoShellCommand) {
    LOGGER.log(Level.INFO, gogoShellCommand.getCommand());
    if (this.bufferedWriter != null) {
      send(gogoShellCommand.getCommand());
    }
  }

  private void waitOneSecond() {
    try {
      Thread.sleep(ONE_SEC_IN_MILLIS);
    } catch (InterruptedException e) {
      LOGGER.log(Level.SEVERE, "wait for dist started", e);
    }
  }

}
