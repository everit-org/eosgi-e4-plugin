package org.everit.e4.eosgi.plugin.ui.navigator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.everit.e4.eosgi.plugin.m2e.model.Bundle;
import org.everit.e4.eosgi.plugin.m2e.model.BundleSettings;
import org.everit.e4.eosgi.plugin.m2e.model.Environment;
import org.everit.e4.eosgi.plugin.m2e.model.Environments;
import org.everit.e4.eosgi.plugin.ui.navigator.nodes.EosgiNode;
import org.everit.e4.eosgi.plugin.ui.navigator.nodes.EosgiNodeType;

public class EosgiEnvironmentContentProvider implements ITreeContentProvider {

    private static final Object[] NO_CHILDREN = new Object[0];

    private final Map<IProject, EosgiNode[]> cachedModelMap = new HashMap<>();

    private StructuredViewer viewer;

    @Override
    public void dispose() {
        cachedModelMap.clear();
    }

    @Override
    public void inputChanged(Viewer aviewer, Object oldInput, Object newInput) {
        if ((oldInput != null) && !oldInput.equals(newInput)) {
            cachedModelMap.clear();
        }
        viewer = (StructuredViewer) aviewer;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
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

    private Object[] handleIProject(Object parentElement, Object[] children) {
        IProject modelFile = (IProject) parentElement;
        if (modelFile.isOpen()) {
            Environments environments = EosgiProjectController.getInstance().getProject(modelFile);
            if (environments != null) {
                cachedModelMap.put(modelFile, convertFromEnvironments(environments));
                children = cachedModelMap.get(modelFile);
            }
        } else {
            EosgiProjectController.getInstance().removeProject(modelFile);
            children = NO_CHILDREN;
        }
        return children;
    }

    private EosgiNode[] convertFromEnvironments(Environments environments) {
        EosgiNode configuration = new EosgiNode();
        configuration.setType(EosgiNodeType.CONFIGURATION);
        configuration.setName("EOSGI Configuration");

        List<Environment> environmentList = environments.getEnvironments();
        if (!environmentList.isEmpty()) {
            EosgiNode environmentsNode = new EosgiNode();
            environmentsNode.setType(EosgiNodeType.ENVIRONMENTS);
            environmentsNode.setName("Environments");

            EosgiNode[] environmentNodeArray = provessEnvironmentList(environmentList);
            environmentsNode.setChilds(environmentNodeArray);

            configuration.setChilds(new EosgiNode[] { environmentsNode });
        }

        return new EosgiNode[] { configuration };

    }

    private EosgiNode[] provessEnvironmentList(List<Environment> environmentList) {
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

    private EosgiNode[] processBundleSettings(BundleSettings bundleSettings) {
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

    private EosgiNode[] processVMOptions(Environment environment) {
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

    private EosgiNode[] processSystemProperties(Environment environment) {
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

    @Override
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof EosgiNode) {
            EosgiNode data = (EosgiNode) element;
            return data.getName();
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof EosgiNode) {
            EosgiNode eosgiNode = (EosgiNode) element;
            return EosgiNodeType.KEY_VALUE != eosgiNode.getType() || EosgiNodeType.VALUE != eosgiNode.getType();
        }
        return false;
    }

}
