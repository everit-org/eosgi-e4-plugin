package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

import java.util.Objects;
import java.util.Observable;

import org.everit.e4.eosgi.plugin.core.EOSGiContext;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeListener;

public class DistNode extends AbstractNode {

  private EOSGiContext context;

  public DistNode(final EOSGiContext context,
      final EosgiNodeChangeListener listener) {
    super("ESOGI Configuration", listener, null);
    Objects.requireNonNull(context, "context cannot be null");
    this.context = context;
    setListener(listener);
    outdated = true;
    this.context.delegateObserver(this);
  }

  @Override
  public AbstractNode[] getChildren() {
    if (outdated) {
      children = new AbstractNode[] { new EnvironmentsNode(context, getListener()) };
      outdated = false;
    }
    return children;
  }

  @Override
  public String getIcon() {
    return "icons/everit.gif";
  }

  @Override
  public String getText() {
    return getName();
  }

  @Override
  public void update(Observable o, Object arg) {
  }

}
