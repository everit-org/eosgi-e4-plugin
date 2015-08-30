package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

import org.eclipse.core.resources.IProject;

public class ManifestNode extends AbstractNode {

  private IProject project;

  public ManifestNode(final IProject project) {
    this.project = project;
    setLabel("No exists");
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
    return "Manifest";
  }

}
