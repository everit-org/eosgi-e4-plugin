package org.everit.e4.eosgi.plugin.m2e.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.console.MessageConsoleStream;
import org.everit.e4.eosgi.plugin.dist.DistRunner;
import org.everit.e4.eosgi.plugin.dist.DistStatus;
import org.everit.e4.eosgi.plugin.dist.DistStatusListener;
import org.everit.e4.eosgi.plugin.dist.EosgiDistRunner;
import org.everit.e4.eosgi.plugin.util.OSUtils;
import org.everit.e4.eosgi.plugin.util.OSUtils.OSType;

/**
 * Class for representing an Eosgi project.
 */
public class EosgiProject {
  public static final String DIST_BIN = "bin";

  public static final String DIST_FOLDER = "eosgi-dist";

  public static final String DIST_LOG = "log";

  public static final String LINUX_START = "runConsole.sh";

  static final Logger LOGGER = Logger.getLogger(EosgiProject.class.getName());

  public static final String WIN_START = "runConsole.bat";

  private File basedir;

  private Build build;

  private List<Dependency> dependencies;

  private boolean dist;

  private Map<String, DistRunner> distRunners = new HashMap<>();

  private Map<String, DistStatusListener> distStatusListeners = new HashMap<>();

  private Map<String, Environment> environments;

  private IProject project;

  /**
   * Constructor.
   *
   * @param project
   *          {@link IProject} reference.
   */
  public EosgiProject(final IProject project) {
    super();
    this.project = project;
    environments = new HashMap<>();
  }

  private int getDistOsgiConsolePort(final String environmentName) {
    int port = -1;

    // Environment environment = environments.get(environmentName);
    // if (environment.getSystemProperties() == null) {
    // return port;
    // }
    //
    // String osgiConsolePort = environment.getSystemProperties().get("osgi.console");
    // if (osgiConsolePort == null) {
    // return port;
    // }
    //
    // try {
    // port = Integer.valueOf(osgiConsolePort);
    // } catch (NumberFormatException e) {
    // LOGGER.warning("Invalid OSGI console port: " + osgiConsolePort);
    // }

    return port;
  }

  private String getDistStartCommand(final String environmentName) {
    String environmentId = environments.get(environmentName).getId();

    if (environmentId == null) {
      return null;
    }

    String binPath = this.build.getDirectory() + File.separator + DIST_FOLDER + File.separator
        + environmentId + File.separator + DIST_BIN + File.separator;
    if (OSType.WINDOWS == OSUtils.currentOS()) {
      binPath = binPath + WIN_START;
    } else {
      binPath = binPath + LINUX_START;
    }
    return binPath;
  }

  public List<Environment> getEnvironments() {
    return new ArrayList<>(environments.values());
  }

  public IProject getProject() {
    return project;
  }

  public boolean isDist() {
    return dist;
  }

  public void setBaseDir(final File basedir) {
    this.basedir = basedir;
  }

  public void setBuild(final Build build) {
    this.build = build;
  }

  public void setDependencies(final List<Dependency> dependencies) {
    this.dependencies = dependencies;
  }

  public void setDist(final boolean dist) {
    this.dist = dist;
  }

  public void setEnvironments(final List<Environment> environments) {
    for (Environment environment : environments) {
      this.environments.put(environment.getId(), environment);
    }
  }

  /**
   * Start a dist with a given environment name.
   * 
   * @param environmentName
   *          name of the environment.
   */
  public void startDist(final String environmentName, final MessageConsoleStream messageStream) {
    Objects.requireNonNull(environmentName, "environmentName cannot be null");

    DistStatusListener statusListener = new DistStatusListener() {

      @Override
      public void distStatusChanged(final DistStatus distStatus) {
        LOGGER.info(distStatus.toString());
      }
    };

    DistRunner distRunner = distRunners.get(environmentName);
    if (distRunner == null) {
      int osgiConsolePort = getDistOsgiConsolePort(environmentName);
      String distStartCommand = getDistStartCommand(environmentName);

      distRunner = new EosgiDistRunner(osgiConsolePort,
          distStartCommand,
          environmentName, statusListener, messageStream);

      distRunners.put(environmentName, distRunner);
      distStatusListeners.put(environmentName, statusListener);

      distRunner.start();
    } else {
      LOGGER.info("The dist is already running!");
    }
  }

  /**
   * Stop a dist with a given environment name.
   * 
   * @param environmentName
   *          name of the environment.
   */
  public void stopDist(final String environmentName) {
    Objects.requireNonNull(environmentName, "environmentName cannot be null");

    DistRunner distRunner = distRunners.remove(environmentName);
    if (distRunner == null) {
      return;
    }
    distRunner.stop();

    DistStatusListener distStatusListener = distStatusListeners.get(environmentName);
    if (distStatusListener != null) {
      distStatusListeners.remove(environmentName);
    }
  }

  @Override
  public String toString() {
    return "EosgiEclipseProject [project=" + project + ", environments=" + environments + ", dist="
        + dist + "]";
  }

}
