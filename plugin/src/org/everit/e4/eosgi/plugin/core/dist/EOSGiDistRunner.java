package org.everit.e4.eosgi.plugin.core.dist;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.console.MessageConsoleStream;
import org.everit.e4.eosgi.plugin.core.util.DistUtils;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.everit.e4.eosgi.plugin.ui.EOSGiLog;
import org.rzo.yajsw.os.OperatingSystem;
import org.rzo.yajsw.os.Process;
import org.rzo.yajsw.os.ProcessManager;
import org.rzo.yajsw.os.ms.win.w32.WindowsXPProcess;
import org.rzo.yajsw.os.posix.linux.LinuxProcess;

public class EOSGiDistRunner implements DistRunner {

  private AutoCloseable closeable;

  private boolean createdStatus;

  private String directory;

  private String environmentName;

  private EOSGiLog log;

  private Process process;

  private IProject project;

  private DistChangeListener statusListener;

  // FIXME ne keljen a project!
  public EOSGiDistRunner(final String directory, final String environmentName,
      final DistChangeListener statusListener, IProject project) {
    super();
    this.directory = directory;
    this.environmentName = environmentName;
    this.statusListener = statusListener;
    this.project = project;
    this.log = new EOSGiLog(Activator.getDefault().getLog());
  }

  private Process createDistProcess() {
    process = createOsSpecificProcess();
    process.setTitle(environmentName);

    String distStartCommand = DistUtils.getDistStartCommand(directory, environmentName);

    process.setCommand(distStartCommand);
    process.setVisible(false);
    process.setTeeName(null);
    process.setPipeStreams(true, false);

    // TODO put environment settings too
    return process;
  }

  private Process createOsSpecificProcess() {
    OperatingSystem operatingSystem = OperatingSystem.instance();

    log.info("Operating system is " + operatingSystem.getOperatingSystemName());

    String lowerCaseOperatingSystemName = operatingSystem.getOperatingSystemName()
        .toLowerCase(Locale.getDefault());

    Process process;
    if (lowerCaseOperatingSystemName.contains("linux")
        || lowerCaseOperatingSystemName.startsWith("mac os x")) {
      log.info("Creating Linux process");
      process = new LinuxProcess();
    } else {
      ProcessManager processManager = operatingSystem.processManagerInstance();
      process = processManager.createProcess();
    }

    return process;
  }

  private Closeable createRedirecter(final Process process) {
    MessageConsoleStream messageConsoleStream = Activator.getDefault()
        .getConsoleWithName(environmentName);

    DaemonStreamRedirector daemonStreamRedirector = new DaemonStreamRedirector(
        process.getInputStream(), new OutputStream[] { messageConsoleStream },
        Activator.getDefault().getLog());

    try {
      daemonStreamRedirector.start();
    } catch (IOException e) {
      try {
        daemonStreamRedirector.close();
      } catch (IOException e1) {
        e.addSuppressed(e1);
      }
      log.error("Could not start stream redirector.", e);
    }
    return new Closeable() {

      @Override
      public void close() throws IOException {
        daemonStreamRedirector.close();
      }
    };
  }

  @Override
  public boolean isCreated() {
    return createdStatus;
  }

  @Override
  public void setCreatedStatus(boolean createdStatus) {
    this.createdStatus = createdStatus;
  }

  private void shutdownProcess(final Process process, final int shutdownTimeout, final int code) {
    log.info("Stopping dist process: " + process.getPid());
    if (process.isRunning()) {
      if (process instanceof WindowsXPProcess) {
        // In case of windows xp process we must kill the process with a command as there is no
        // visible
        // window and kill tree command of YAJSW does not work. Hopefully this is a temporary
        // solution.

        OperatingSystem operatingSystem = OperatingSystem.instance();
        ProcessManager processManagerInstance = operatingSystem.processManagerInstance();
        Process killProcess = processManagerInstance.createProcess();
        String killCommand = "taskkill /F /T /PID " + process.getPid();
        log.warning("Killing windows process with command: " + killCommand + "");
        killProcess.setCommand(killCommand);
        killProcess.setVisible(false);
        killProcess.start();
        process.waitFor(shutdownTimeout);
      } else {
        process.stop(shutdownTimeout, code);
      }
    }
  }

  @Override
  public void start() {
    Process process = createDistProcess();
    boolean started = process.start();
    if (started) {
      closeable = createRedirecter(process);
      statusListener.statusChangeEvent(
          new DistStatusEvent()
              .environmentName(environmentName)
              .distStatus(DistStatus.RUNNING)
              .project(project));
    } else {
      log.error("Could not start dist process.");
    }

  }

  @Override
  public void stop() {
    shutdownProcess(process, 10000, 9);
    if (process.isRunning()) {
      try {
        closeable.close();
      } catch (Exception e) {
        log.error("Could not close process stream(s).", e);
      }
    }
    statusListener.statusChangeEvent(
        new DistStatusEvent()
            .environmentName(environmentName)
            .distStatus(DistStatus.STOPPED)
            .project(project));
  }

}
