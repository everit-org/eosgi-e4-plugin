package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IProject;
import org.everit.e4.eosgi.plugin.core.EventType;
import org.everit.e4.eosgi.plugin.core.ModelChangeEvent;
import org.everit.e4.eosgi.plugin.core.m2e.EosgiManager;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeEvent;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeListener;

/**
 * Project explorer node for show environments.
 */
public class EnvironmentsNode extends AbstractNode implements Observer {

  private Activator plugin = Activator.getDefault();

  private IProject project;

  /**
   * Constructor.
   * 
   * @param project
   *          relevant {@link IProject} reference.
   * @param listener
   *          listener for changes.
   */
  public EnvironmentsNode(final IProject project, final EosgiNodeChangeListener listener) {
    super("Environments", listener, null);
    this.project = project;
    outdated = true;

    Observable observable = (Observable) plugin.getEosgiManager();
    observable.addObserver(this);
  }

  private void disposeChildren() {
    if (children != null) {
      for (AbstractNode child : children) {
        child.dispose();
      }
    }
  }

  @Override
  public AbstractNode[] getChildren() {
    if (outdated) {
      EosgiManager eosgiManager = plugin.getEosgiManager();
      List<String> environments = eosgiManager.fetchEnvironmentsBy(project);
      disposeChildren();
      List<EnvironmentNode> nodes = new ArrayList<>();
      for (String environment : environments) {
        EnvironmentNode node = new EnvironmentNode(project, environment, getListener());
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
    if (getLabel() == null) {
      return getName();
    } else {
      return getName() + getLabel();
    }
  }

  // @Override
  // public void modelChanged(final ModelChangeEvent object) {
    // if (object.target != null && getClass().isAssignableFrom(object.target)) {
    // IProject proj = (IProject) object.arg;
    // if (project.equals(proj) && getListener() != null) {
    // outdated = true;
    // getListener().nodeChanged(new EosgiNodeChangeEvent(this));
    // }
    // }
    // if (object instanceof IProject) {
    // IProject proj = (IProject) object;
    // if (project.equals(proj) && getListener() != null) {
    // outdated = true;
    // getListener().nodeChanged(new EosgiNodeChangeEvent(this));
    // }
    // }
  // }

  @Override
  public String toString() {
    return "EnvironmentsNode [project=" + project + ", children=" + Arrays.toString(children)
        + ", icon=" + icon + ", label=" + label + ", name=" + name + ", outdated=" + outdated
        + ", value=" + value + "]";
  }

  @Override
  public void update(Observable o, Object arg) {
    ModelChangeEvent event = null;
    if (arg != null && arg instanceof ModelChangeEvent) {
      event = (ModelChangeEvent) arg;
    }
    if (event != null && event.eventType == EventType.ENVIRONMENTS && event.arg != null) {
      IProject project = (IProject) event.arg;
      if (project.getName().equals(this.project.getName())) {
        outdated = true;
        getListener().nodeChanged(new EosgiNodeChangeEvent(this));
      }
    }
  }

}
