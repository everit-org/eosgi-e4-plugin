package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.everit.e4.eosgi.plugin.ui.navigator.EosgiNodeChangeListener;

public class DistNode extends AbstractNode {

  private IProject project;

  public DistNode(final IProject project, final EosgiNodeChangeListener listener) {
    Objects.requireNonNull(project, "project cannot be null");
    this.project = project;
    setListener(listener);
    outdated = true;
  }

  @Override
  public AbstractNode[] getChildren() {
    if (outdated) {
      children = new AbstractNode[] { new BundlesNode(project, getListener()),
          new ManifestNode(project), new EnvironmentsNode(project, getListener()) };
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
    return "ESOGI Configuration";
  }

}
