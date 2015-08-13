package org.everit.e4.eosgi.plugin.m2e.dist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.everit.e4.eosgi.plugin.dist.DistStatus;
import org.everit.e4.eosgi.plugin.dist.EosgiDistRunner;
import org.junit.Before;
import org.junit.Test;

public class DistRunnerTest {
  private static class Receiver implements Runnable {
    private BufferedReader bufferedReader;

    public Receiver(final InputStream inputStream) {
      super();
      Objects.requireNonNull(inputStream, "inputStream cannot be null");
      bufferedReader = new BufferedReader(
          new InputStreamReader(inputStream, Charset.forName(UTF_8)));
    }

    @Override
    public void run() {
      try {
        String line = "";
        System.out.println("* receiver is waiting");
        while ((line = bufferedReader.readLine()) != null) {
          System.out.println(">> " + line);
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if (bufferedReader != null) {
          try {
            bufferedReader.close();
            System.out.println("stream closed");
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  private static final Logger LOGGER = Logger.getLogger(DistRunnerTest.class.getName());

  private static final int THREAD_WAIT_MILIS = 1000;

  private static final String UTF_8 = "UTF-8";

  private boolean stopped = false;

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testConnect() throws Exception {
    // String distPath = "/home/zsoltdoma/dev/ide/jee-latest-2/osgi-console/";
    String distPath = "/home/zsoltdoma/svn/cams-raca/offline/trunk/dist-dev/core/target";
    EosgiDistRunner eosgiDistRunner = new EosgiDistRunner(6667, distPath, distStatus -> {
      LOGGER.log(Level.INFO, distStatus.toString());
      if (DistStatus.STOPPED == distStatus) {
        stopped = true;
      }
    });

    eosgiDistRunner.start();

    Thread.sleep(30000);
    // System.out.println();

    eosgiDistRunner.stop();

    LOGGER.info("Wait for STOPPED status");
    while (!stopped) {
      Thread.sleep(1000);
    }
  }

  @Test
  public void testConnectGogoShell() throws Exception {

    Socket socket = null;
    try {
      socket = new Socket("localhost", 6667);
      new Thread(new Receiver(socket.getInputStream())).start();
      OutputStream outputStream = socket.getOutputStream();
      BufferedWriter bufferedWriter = new BufferedWriter(
          new OutputStreamWriter(outputStream/* , Charset.forName(UTF_8) */));

      String command = "";
      BufferedReader bufferedReader = new BufferedReader(
          new InputStreamReader(System.in, Charset.forName(UTF_8)));
      System.out.print("$> ");
      while (!"halt".equals(command = bufferedReader.readLine())) {
        bufferedWriter.write(command);
        bufferedWriter.newLine();
        bufferedWriter.flush();
        System.out.print("$> ");
      }
      System.out.println("exiting...");
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        socket.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

}
