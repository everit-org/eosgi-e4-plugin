package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

import org.everit.e4.eosgi.plugin.core.dist.DistStatus;
import org.everit.e4.eosgi.plugin.core.dist.EnvironmentChangeListener;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeEvent;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeListener;

/**
 * Project explorer node for show environment.
 */
public class EnvironmentNode extends AbstractNode implements EnvironmentChangeListener {

  private DistStatus distStatus = DistStatus.NONE;

  private String environmentId;

  /**
   * Constructor.
   * 
   * @param environmentId
   *          id of the environment.
   * @param listener
   *          listener for model changes.
   */
  public EnvironmentNode(final String environmentId, final EosgiNodeChangeListener listener) {
    super();
    this.environmentId = environmentId;
    setListener(listener);
    setName(environmentId);
    // setValue(getDistStatusString());
    Activator.getDefault().getDistManager().addEnvironmentChangeListener(this);
  }

  @Override
  public void environmentChanged(final String environmentId, final DistStatus distStatus) {
    if (this.environmentId.equals(environmentId)) {
      this.distStatus = distStatus;
      setName(environmentId);
      // setValue(getDistStatusString());
      getListener().nodeChanged(new EosgiNodeChangeEvent(this));
    }
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

}
