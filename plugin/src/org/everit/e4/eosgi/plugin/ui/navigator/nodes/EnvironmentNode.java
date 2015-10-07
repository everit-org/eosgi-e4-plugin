package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

import java.util.Observable;
import java.util.Observer;

import org.everit.e4.eosgi.plugin.core.EOSGiContext;
import org.everit.e4.eosgi.plugin.core.EventType;
import org.everit.e4.eosgi.plugin.core.ModelChangeEvent;
import org.everit.e4.eosgi.plugin.core.dist.DistRunner;
import org.everit.e4.eosgi.plugin.core.dist.DistStatus;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeEvent;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeListener;

/**
 * Project explorer node for show environment.
 */
public class EnvironmentNode extends AbstractNode implements Observer {

  private EOSGiContext context;

  private DistStatus distStatus = DistStatus.NONE;

  private String environmentId;

  /**
   * Constructor.
   * 
   * @param project
   *          listener for model changes.
   * @param environmentId
   *          id of the environment.
   * @param eosgiNodeChangeListener
   */
  public EnvironmentNode(final EOSGiContext context, final String environmentId,
      final EosgiNodeChangeListener eosgiNodeChangeListener) {
    super(environmentId, eosgiNodeChangeListener, null);
    this.context = context;
    this.environmentId = environmentId;
    context.delegateObserver(this);
  }

  @Override
  public void dispose() {
    context.removeObserver(this);
    System.out.println("remove node: " + this);
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
    return "EnvironmentNode [context=" + context + ", distStatus=" + distStatus + ", environmentId="
        + environmentId + "]";
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
    DistRunner distRunner = context.runner(environmentId).get();
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
