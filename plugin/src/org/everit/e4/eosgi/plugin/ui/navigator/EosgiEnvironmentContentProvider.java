package org.everit.e4.eosgi.plugin.ui.navigator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.model.Dependency;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.everit.e4.eosgi.plugin.m2e.model.Bundle;
import org.everit.e4.eosgi.plugin.m2e.model.BundleSettings;
import org.everit.e4.eosgi.plugin.m2e.model.Environment;
import org.everit.e4.eosgi.plugin.m2e.model.EosgiProject;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.everit.e4.eosgi.plugin.ui.OsgiProject;
import org.everit.e4.eosgi.plugin.ui.OsgiProjects;
import org.everit.e4.eosgi.plugin.ui.navigator.nodes.EosgiNode;
import org.everit.e4.eosgi.plugin.ui.navigator.nodes.EosgiNodeType;

/**
 * Tree content provider for EosgiNodes.
 */
public class EosgiEnvironmentContentProvider implements ITreeContentProvider {

  private static final Object[] NO_CHILDREN = new Object[0];

  private final Map<IProject, EosgiNode[]> cachedModelMap = new HashMap<>();

  private StructuredViewer viewer;

  private EosgiNode[] convertFromEnvironments(final List<Environment> environmentList,
      final List<OsgiProject> relevantProjects) {
    EosgiNode configuration = new EosgiNode();
    configuration.setType(EosgiNodeType.CONFIGURATION);
    configuration.setName("EOSGI Configuration");

    List<EosgiNode> configurationNodes = new ArrayList<>();

    EosgiNode bundlesNode = new EosgiNode();
    bundlesNode.setName("Bundles");
    bundlesNode.setType(EosgiNodeType.BUNDLE_PROJECTS);
    if (relevantProjects.isEmpty()) {
      bundlesNode.setLabel("No relevant bundle in workspace");
    } else {
      EosgiNode[] bundleArray = new EosgiNode[relevantProjects.size()];
      int i = 0;
      for (OsgiProject osgiProject : relevantProjects) {
        EosgiNode eosgiNode = new EosgiNode();
        eosgiNode.setName(osgiProject.getProject().getName());
        eosgiNode.setType(EosgiNodeType.KEY_VALUE);
        eosgiNode.setValue(osgiProject.getMavenProject().getVersion());
        bundleArray[i++] = eosgiNode;
      }
      bundlesNode.setChilds(bundleArray);
    }

    configurationNodes.add(bundlesNode);

    if (!environmentList.isEmpty()) {
      EosgiNode environmentsNode = new EosgiNode();
      environmentsNode.setType(EosgiNodeType.ENVIRONMENTS);
      environmentsNode.setName("Environments");

      EosgiNode[] environmentNodeArray = provessEnvironmentList(environmentList);
      environmentsNode.setChilds(environmentNodeArray);

      configurationNodes.add(environmentsNode);
    }

    configuration.setChilds(configurationNodes.toArray(new EosgiNode[] {}));
    return new EosgiNode[] { configuration };

  }

  @Override
  public void dispose() {
    cachedModelMap.clear();
  }

  @Override
  public Object[] getChildren(final Object parentElement) {
    Object[] children = null;
    if (parentElement instanceof IProject) {
      children = handleIProject(parentElement, children);
    } else if (parentElement instanceof EosgiNode) {
      EosgiNode pluginContent = (EosgiNode) parentElement;
      if (EosgiNodeType.KEY_VALUE == pluginContent.getType()) {
        children = NO_CHILDREN;
      } else {
        children = pluginContent.getChilds();
      }
    }
    return children != null ? children : NO_CHILDREN;
  }

  @Override
  public Object[] getElements(final Object inputElement) {
    return getChildren(inputElement);
  }

  @Override
  public Object getParent(final Object element) {
    if (element instanceof EosgiNode) {
      EosgiNode data = (EosgiNode) element;
      return data.getName();
    }
    return null;
  }

  private Object[] handleIProject(final Object parentElement, Object[] children) {
    IProject modelFile = (IProject) parentElement;
    if (modelFile.isOpen()) {
      EosgiProject eosgiEclipseProject = Activator.getDefault().getEosgiProjectController()
          .getProject(modelFile);

      List<OsgiProject> relevantProjects = new ArrayList<>();
      if (eosgiEclipseProject != null) {
        OsgiProjects osgiProjects = Activator.getDefault().getOsgiProjects();
        List<Dependency> dependencies = eosgiEclipseProject.getDependencies();
        for (Dependency dependency : dependencies) {
          String artifactId = dependency.getArtifactId();
          OsgiProject osgiProject = osgiProjects.getProjectBy(artifactId);
          if (osgiProject != null) {
            relevantProjects.add(osgiProject);
          }
        }
      }
      if ((eosgiEclipseProject != null) && (eosgiEclipseProject.getEnvironments() != null)) {
        cachedModelMap.put(modelFile,
            convertFromEnvironments(eosgiEclipseProject.getEnvironments(), relevantProjects));
        children = cachedModelMap.get(modelFile);
      }
    } else {
      Activator.getDefault().getEosgiProjectController().removeProject(modelFile);
      children = NO_CHILDREN;
    }
    return children;
  }

  @Override
  public boolean hasChildren(final Object element) {
    if (element instanceof EosgiNode) {
      EosgiNode eosgiNode = (EosgiNode) element;
      return (EosgiNodeType.KEY_VALUE != eosgiNode.getType())
          || (EosgiNodeType.VALUE != eosgiNode.getType());
    }
    return false;
  }

  @Override
  public void inputChanged(final Viewer aviewer, final Object oldInput, final Object newInput) {
    if ((oldInput != null) && !oldInput.equals(newInput)) {
      cachedModelMap.clear();
    }
    viewer = (StructuredViewer) aviewer;
  }

  private EosgiNode[] processBundleSettings(final BundleSettings bundleSettings) {
    List<Bundle> bundles = bundleSettings.getBundles();
    EosgiNode[] bundlesArray = null;

    if (!bundles.isEmpty()) {
      bundlesArray = new EosgiNode[bundles.size()];
      int i = 0;
      for (Bundle bundle : bundles) {
        Map<String, String> bundlePropertiesMap = bundle.getBundlePropertiesMap();
        EosgiNode bundleNode = new EosgiNode();
        bundleNode.setType(EosgiNodeType.BUNDLE);
        bundleNode.setName(bundlePropertiesMap.get("symbolicName"));
        bundleNode.setLabel("startLevel: " + bundlePropertiesMap.get("startLevel"));
        bundlesArray[i++] = bundleNode;
      }
    }
    return bundlesArray;
  }

  private EosgiNode[] processSystemProperties(final Environment environment) {
    Map<String, String> systemProperties = environment.getSystemProperties();
    EosgiNode[] sysPropsNodeArray = null;
    if (!systemProperties.isEmpty()) {
      sysPropsNodeArray = new EosgiNode[systemProperties.size()];
      int i = 0;
      for (Entry<String, String> systemProperty : systemProperties.entrySet()) {
        EosgiNode portNode = new EosgiNode();
        portNode.setType(EosgiNodeType.KEY_VALUE);
        portNode.setName(systemProperty.getKey());
        portNode.setValue(systemProperty.getValue());
        sysPropsNodeArray[i++] = portNode;
      }
    }
    return sysPropsNodeArray;
  }

  private EosgiNode[] processVMOptions(final Environment environment) {
    List<String> vmOptions = environment.getVmOptions();
    EosgiNode[] vmOptionsArray = null;
    if (!vmOptions.isEmpty()) {
      vmOptionsArray = new EosgiNode[vmOptions.size()];
      int i = 0;
      for (String vmOption : vmOptions) {
        EosgiNode vmOptionNode = new EosgiNode();
        vmOptionNode.setType(EosgiNodeType.VALUE);
        vmOptionNode.setName(vmOption);
        vmOptionsArray[i++] = vmOptionNode;
      }
    }
    return vmOptionsArray;
  }

  private EosgiNode[] provessEnvironmentList(final List<Environment> environmentList) {
    EosgiNode[] environmentNodeArray = new EosgiNode[environmentList.size()];
    int i = 0;
    for (Environment environment : environmentList) {
      EosgiNode environmentNode = new EosgiNode();
      environmentNode.setType(EosgiNodeType.ENVIRONMENT);
      environmentNode.setName(environment.getId());
      environmentNode.setLabel(environment.getFramework());

      List<EosgiNode> eosgiNodes = new ArrayList<EosgiNode>();

      EosgiNode[] sysPropsNodeArray = processSystemProperties(environment);
      EosgiNode sysPropsNode;
      if (sysPropsNodeArray != null) {
        sysPropsNode = new EosgiNode();
        sysPropsNode.setType(EosgiNodeType.SYSTEM_PROPS);
        sysPropsNode.setName("System Properties");
        sysPropsNode.setChilds(sysPropsNodeArray);
        eosgiNodes.add(sysPropsNode);
      }

      BundleSettings bundleSettings = environment.getBundleSettings();
      EosgiNode[] bundleSettingsArray = processBundleSettings(bundleSettings);
      EosgiNode bundleSettingsNode;
      if (bundleSettingsArray != null) {
        bundleSettingsNode = new EosgiNode();
        bundleSettingsNode.setType(EosgiNodeType.BUNDLE_SETTINGS);
        bundleSettingsNode.setName("Bundle Settings");
        bundleSettingsNode.setChilds(bundleSettingsArray);
        eosgiNodes.add(bundleSettingsNode);
      }

      EosgiNode[] vmOptionsArray = processVMOptions(environment);
      EosgiNode vmOptionsNode;
      if (vmOptionsArray != null) {
        vmOptionsNode = new EosgiNode();
        vmOptionsNode.setType(EosgiNodeType.SYSTEM_PROPS);
        vmOptionsNode.setName("VM Options");
        vmOptionsNode.setChilds(vmOptionsArray);
        eosgiNodes.add(vmOptionsNode);
      }

      EosgiNode[] configNodeArray = eosgiNodes.toArray(new EosgiNode[] {});
      environmentNode.setChilds(configNodeArray);
      environmentNodeArray[i++] = environmentNode;
    }
    return environmentNodeArray;
  }

}
