package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IProject;
import org.everit.e4.eosgi.plugin.core.EventType;
import org.everit.e4.eosgi.plugin.core.ModelChangeEvent;
import org.everit.e4.eosgi.plugin.core.dist.DistRunner;
import org.everit.e4.eosgi.plugin.core.dist.DistStatus;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeEvent;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeListener;

/**
 * Project explorer node for show environment.
 */
public class EnvironmentNode extends AbstractNode implements Observer {

  private DistStatus distStatus = DistStatus.NONE;

  private String environmentId;

  private Activator plugin = Activator.getDefault();

  private IProject project;

  /**
   * Constructor.
   * 
   * @param project
   *          listener for model changes.
   * @param environmentId
   *          id of the environment.
   * @param eosgiNodeChangeListener
   */
  public EnvironmentNode(final IProject project, final String environmentId,
      final EosgiNodeChangeListener eosgiNodeChangeListener) {
    super(environmentId, eosgiNodeChangeListener, null);
    this.project = project;
    this.environmentId = environmentId;

    Observable observable = (Observable) plugin.getEosgiManager();
    observable.addObserver(this);
  }

  @Override
  public void dispose() {
    Observable eosgiManager = (Observable) plugin.getEosgiManager();
    eosgiManager.deleteObserver(this);
    super.dispose();
  }

  @Override
  public AbstractNode[] getChildren() {
    return null;
  }

  public DistStatus getDistStatus() {
    return distStatus;
  }

  public String getDistStatusString() {
    return " " + distStatus.name().toLowerCase();
  }

  @Override
  public String getIcon() {
    return "icons/ExecutionEnvironment.gif";
  }

  @Override
  public String getText() {
    if (getLabel() == null) {
      return getName();
    } else {
      return getName() + getLabel();
    }
  }

  public boolean isOutdated() {
    return outdated;
  }

  @Override
  public String toString() {
    return "EnvironmentNode [distStatus=" + distStatus + ", environmentId=" + environmentId + "]";
  }

  @Override
  public void update(Observable o, Object arg) {
    ModelChangeEvent event = null;
    if (arg != null && arg instanceof ModelChangeEvent) {
      event = (ModelChangeEvent) arg;
    }

    if (event != null && event.eventType == EventType.ENVIRONMENT) {
      String environmentName = (String) event.arg;
      if (environmentName.equals(environmentId)) {
        outdated = true;
        updateDistStatus();
        getListener().nodeChanged(new EosgiNodeChangeEvent(this));
      }
    }
  }

  private void updateDistStatus() {
    DistRunner distRunner = plugin.getEosgiManager().getDistRunner(project, environmentId);
    if (distRunner == null) {
      distStatus = DistStatus.NONE;
    } else {
      if (distRunner.isRunning()) {
        distStatus = DistStatus.RUNNING;
      } else {
        distStatus = DistStatus.STOPPED;
      }
      if (distRunner instanceof Observable) {
        ((Observable) distRunner).addObserver(this);
      }
    }
  }

}
