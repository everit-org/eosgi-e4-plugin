package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.everit.e4.eosgi.plugin.core.m2e.EosgiManager;
import org.everit.e4.eosgi.plugin.core.m2e.EosgiModelChangeListener;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeEvent;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeListener;

public class EnvironmentsNode extends AbstractEosgiNode
    implements EosgiModelChangeListener {

  private IProject project;

  public EnvironmentsNode(final IProject project, final EosgiNodeChangeListener listener) {
    this.project = project;
    setListener(listener);
    Activator.getDefault().getEosgiManager().addModelChangeListener(this);
    outdated = true;
  }

  @Override
  public AbstractEosgiNode[] getChildren() {
    if (outdated) {
      EosgiManager eosgiManager = Activator.getDefault().getEosgiManager();
      List<String> environments = eosgiManager.fetchEnvironmentsBy(project);
      List<EnvironmentNode> nodes = new ArrayList<>();
      for (String environment : environments) {
        EnvironmentNode node = new EnvironmentNode(environment, getListener());
        nodes.add(node);
      }
      children = nodes.toArray(new EnvironmentNode[] {});
      outdated = false;
    }
    return children;
  }

  @Override
  public String getIcon() {
    return "icons/console_view.gif";
  }

  @Override
  public String getText() {
    return "Environments";
  }

  @Override
  public void modelChanged(final Object object) {
    if (object instanceof IProject) {
      IProject proj = (IProject) object;
      if (project.equals(proj) && getListener() != null) {
        outdated = true;
        getListener().nodeChanged(new EosgiNodeChangeEvent(this));
      }
    }
  }
}
