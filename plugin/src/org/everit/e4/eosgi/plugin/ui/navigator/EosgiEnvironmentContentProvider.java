package org.everit.e4.eosgi.plugin.ui.navigator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.everit.e4.eosgi.plugin.m2e.model.Environment;
import org.everit.e4.eosgi.plugin.m2e.model.Environments;
import org.everit.e4.eosgi.plugin.ui.navigator.model.EosgiNode;
import org.everit.e4.eosgi.plugin.ui.navigator.model.EosgiNodeType;

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
        Environments environments = EosgiProjectController.getInstance().getProject(modelFile);
        if (environments != null) {
            cachedModelMap.put(modelFile, convertFromEnvironments(environments));
            children = cachedModelMap.get(modelFile);
        }
        return children;
    }

    private EosgiNode[] convertFromEnvironments(Environments environments) {
        EosgiNode configuration = new EosgiNode();
        configuration.setType(EosgiNodeType.CONFIGURATION);
        configuration.setLabel("EOSGI Configuration");

        List<Environment> environmentList = environments.getEnvironments();
        if (!environmentList.isEmpty()) {
            EosgiNode environmentsNode = new EosgiNode();
            environmentsNode.setType(EosgiNodeType.ENVIRONMENTS);
            environmentsNode.setLabel("Environments");

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
            environmentNode.setLabel("Environment");

            EosgiNode idNode = new EosgiNode();
            idNode.setType(EosgiNodeType.KEY_VALUE);
            idNode.setLabel(environment.getId() + "(" + environment.getFramework() + ")");

            EosgiNode sysPropsNode = new EosgiNode();
            sysPropsNode.setType(EosgiNodeType.SYSTEM_PROPS);
            sysPropsNode.setLabel("System Properties");

            EosgiNode[] sysPropsNodeArray = processSystemProperties(environment);
            sysPropsNode.setChilds(sysPropsNodeArray);

            EosgiNode[] configNodeArray = new EosgiNode[] { idNode, sysPropsNode };
            environmentNode.setChilds(configNodeArray);
            environmentNodeArray[i++] = environmentNode;
        }
        return environmentNodeArray;
    }

    private EosgiNode[] processSystemProperties(Environment environment) {
        Map<String, String> systemProperties = environment.getSystemProperties();
        EosgiNode[] sysPropsNodeArray = new EosgiNode[systemProperties.size()];
        int i = 0;
        for (Entry<String, String> systemProperty : systemProperties.entrySet()) {
            EosgiNode portNode = new EosgiNode();
            portNode.setType(EosgiNodeType.KEY_VALUE);
            portNode.setLabel(systemProperty.getKey() + ": " + systemProperty.getValue());
            sysPropsNodeArray[i++] = portNode;
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
            return data.getLabel();
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof EosgiNode) {
            return EosgiNodeType.KEY_VALUE != ((EosgiNode) element).getType();
        }
        return false;
    }

}
