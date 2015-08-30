package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

import org.eclipse.core.resources.IProject;

public class ManifestNode extends AbstractNode {

  private IProject project;

  public ManifestNode(final IProject project) {
    this.project = project;
    setName("Manifest");
    setLabel(" (Not implemented, yet)");
  }

  @Override
  public AbstractNode[] getChildren() {
    return null;
  }

  @Override
  public String getIcon() {
    return "icons/plugin_mf_obj.gif";
  }

  @Override
  public String getText() {
    return getName() + getLabel();
  }

}
