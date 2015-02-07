package org.everit.e4.eosgi.plugin.ui.navigator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
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
        if (modelFile.getName().startsWith("o")) { // foo project check
            children = cachedModelMap.get(modelFile);
            if ((children == null) && (updateModel(modelFile))) {
                children = cachedModelMap.get(modelFile);
            }
        }
        return children;
    }

    private EosgiNode[] createFooNodes() {
        EosgiNode idNode = new EosgiNode();
        idNode.setType(EosgiNodeType.KEY_VALUE);
        idNode.setLabel("id: felixtest");

        EosgiNode frameworkNode = new EosgiNode();
        frameworkNode.setType(EosgiNodeType.KEY_VALUE);
        frameworkNode.setLabel("framework: felix");

        EosgiNode portNode = new EosgiNode();
        portNode.setType(EosgiNodeType.KEY_VALUE);
        portNode.setLabel("port: 8080");

        EosgiNode testNode = new EosgiNode();
        testNode.setType(EosgiNodeType.KEY_VALUE);
        testNode.setLabel("test: true");

        EosgiNode sysPropsNode = new EosgiNode();
        sysPropsNode.setType(EosgiNodeType.SYSTEM_PROPS);
        sysPropsNode.setLabel("System properties");
        sysPropsNode.setChilds(new EosgiNode[] { portNode, testNode });

        EosgiNode environment = new EosgiNode();
        environment.setType(EosgiNodeType.ENVIRONMENT);
        environment.setLabel("Environment");
        environment.setChilds(new EosgiNode[] { idNode, frameworkNode, sysPropsNode });

        EosgiNode environments = new EosgiNode();
        environments.setType(EosgiNodeType.ENVIRONMENTS);
        environments.setLabel("Environments");
        environments.setChilds(new EosgiNode[] { environment });

        EosgiNode configuration = new EosgiNode();
        configuration.setType(EosgiNodeType.CONFIGURATION);
        configuration.setLabel("EOSGI Configuration");
        configuration.setChilds(new EosgiNode[] { environments });
        return new EosgiNode[] { configuration };
    }

    private boolean updateModel(IProject modelFile) {
        cachedModelMap.put(modelFile, createFooNodes());
        return true;
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
