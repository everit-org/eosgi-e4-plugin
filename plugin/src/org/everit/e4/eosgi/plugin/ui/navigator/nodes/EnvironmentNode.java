package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

import org.everit.e4.eosgi.plugin.core.dist.DistStatus;
import org.everit.e4.eosgi.plugin.core.dist.EnvironmentChangeListener;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeEvent;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeListener;

public class EnvironmentNode extends AbstractEosgiNode implements EnvironmentChangeListener {

  private DistStatus distStatus = DistStatus.STOPPED;

  private String environmentId;

  public EnvironmentNode(String environmentId, final EosgiNodeChangeListener listener) {
    super();
    this.environmentId = environmentId;
    setListener(listener);
    setName(environmentId);
    setLabel("(" + distStatus.name() + ")");
    Activator.getDefault().getDistManager().addEnvironmentChangeListener(this);
  }

  @Override
  public void environmentChanged(final String environmentId, final DistStatus distStatus) {
    if (this.environmentId.equals(environmentId)) {

      this.distStatus = distStatus;
      setName(environmentId);
      setLabel("(" + distStatus.name() + ")");
      getListener().nodeChanged(new EosgiNodeChangeEvent(this));
    }
  }

  @Override
  public AbstractEosgiNode[] getChildren() {
    return null;
  }

  @Override
  public String getIcon() {
    return "icons/ExecutionEnvironment.gif";
  }

  @Override
  public String getText() {
    return getName() + getLabel();
  }

}
