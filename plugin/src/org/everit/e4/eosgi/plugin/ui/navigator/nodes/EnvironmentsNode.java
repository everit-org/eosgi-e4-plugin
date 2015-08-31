package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.everit.e4.eosgi.plugin.core.m2e.EosgiManager;
import org.everit.e4.eosgi.plugin.core.m2e.EosgiModelChangeListener;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeEvent;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeListener;

/**
 * Project explorer node for show environments.
 */
public class EnvironmentsNode extends AbstractNode
    implements EosgiModelChangeListener {

  private IProject project;

  /**
   * Constructor.
   * 
   * @param project
   * @param listener
   */
  public EnvironmentsNode(final IProject project, final EosgiNodeChangeListener listener) {
    this.project = project;
    setListener(listener);
    setName("Environments");
    Activator.getDefault().getEosgiManager().addModelChangeListener(this);
    outdated = true;
  }

  @Override
  public AbstractNode[] getChildren() {
    if (outdated) {
      EosgiManager eosgiManager = Activator.getDefault().getEosgiManager();
      List<String> environments = eosgiManager.fetchEnvironmentsBy(project);
      List<EnvironmentNode> nodes;
      nodes = new ArrayList<>();
      for (String environment : environments) {
        EnvironmentNode node = new EnvironmentNode(environment, getListener());
        nodes.add(node);
      }
      children = nodes.toArray(new EnvironmentNode[] {});
      outdated = false;
    }
    if (children == null || children.length == 0) {
      setLabel(" (No environment found)");
    } else {
      setLabel(null);
    }
    return children;
  }

  @Override
  public String getIcon() {
    return "icons/console_view.gif";
  }

  @Override
  public String getText() {
    if (getLabel() == null) {
      return getName();
    } else {
      return getName() + getLabel();
    }
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

  @Override
  public String toString() {
    return "EnvironmentsNode [project=" + project + ", children=" + Arrays.toString(children)
        + ", icon=" + icon + ", label=" + label + ", name=" + name + ", outdated=" + outdated
        + ", value=" + value + "]";
  }

}
