package org.everit.e4.eosgi.plugin.ui.navigator;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.everit.e4.eosgi.plugin.ui.nature.EosgiNature;
import org.everit.e4.eosgi.plugin.ui.navigator.nodes.AbstractEosgiNode;
import org.everit.e4.eosgi.plugin.ui.navigator.nodes.DistNode;

public class DistContentProvider extends TreeNodeContentProvider
    implements ITreeContentProvider, EosgiNodeChangeListener {
  static final Logger LOGGER = Logger.getLogger(DistContentProvider.class.getName());

  private static final Object[] NO_CHILDREN = new Object[] {};

  private Map<AbstractEosgiNode, AbstractEosgiNode[]> eosgiNodeCache = new HashMap<>();

  private Map<IProject, AbstractEosgiNode[]> projectCache = new HashMap<>();

  private StructuredViewer viewer;

  @Override
  public void dispose() {
    projectCache = null;
    eosgiNodeCache = null;
  }

  @Override
  public Object[] getChildren(final Object parentElement) {
    Object[] children = NO_CHILDREN;
    if (parentElement == null) {
      children = NO_CHILDREN;
    } else if (parentElement instanceof IProject) {
      children = handleProject(parentElement);
    } else if (parentElement instanceof AbstractEosgiNode) {
      children = handleEosgiNode(parentElement);
    }

    return children;
  }

  @Override
  public Object[] getElements(final Object inputElement) {
    return getChildren(inputElement);
  }

  @Override
  public Object getParent(final Object element) {
    // TODO ez így jó?
    // if (element instanceof DistNode) {
    // return ((DistNode) element).getProject();
    // }
    return null;
  }

  private Runnable getRefreshRunnable(final AbstractEosgiNode node) {
    return new Runnable() {
      @Override
      public void run() {
        viewer.refresh(node);
      }
    };
  }

  private Runnable getUpdateRunnable(final AbstractEosgiNode resource) {
    return new Runnable() {
      @Override
      public void run() {
        viewer.update(resource, null);
      }
    };
  }

  private Object[] handleEosgiNode(final Object parentElement) {
    if (eosgiNodeCache.containsKey(parentElement)) {
      return eosgiNodeCache.get(parentElement);
    } else {
      AbstractEosgiNode abstractEosgiNode = (AbstractEosgiNode) parentElement;
      AbstractEosgiNode[] eosgiNodes = abstractEosgiNode.getChildren();
      eosgiNodeCache.put(abstractEosgiNode, eosgiNodes);
      return eosgiNodes;
    }
  }

  private Object[] handleProject(final Object parentElement) {
    IProject project = (IProject) parentElement;
    if (!project.isOpen()) {
      return NO_CHILDREN;
    }

    boolean eosgiNature = false;
    try {
      eosgiNature = project.hasNature(EosgiNature.NATURE_ID);
    } catch (CoreException e) {
      LOGGER.log(Level.WARNING, "get project nature", e);
    }

    if (eosgiNature) {
      if (projectCache.containsKey(project)) {
        return projectCache.get(project);
      } else {
        DistNode[] nodes = new DistNode[] { new DistNode(project, this) };
        projectCache.put(project, nodes);
        return nodes;
      }
    } else {
      return NO_CHILDREN;
    }
  }

  @Override
  public boolean hasChildren(final Object element) {
    if (element instanceof IProject) {
      return true;
    } else if (element instanceof AbstractEosgiNode) {
      AbstractEosgiNode node = (AbstractEosgiNode) element;
      AbstractEosgiNode[] children = node.getChildren();
      return children != null && children.length > 0;
    } else {
      return false;
    }
  }

  @Override
  public void inputChanged(final Viewer aviewer, final Object oldInput, final Object newInput) {
    viewer = (StructuredViewer) aviewer;
  }

  @Override
  public void nodeChanged(final EosgiNodeChangeEvent event) {
    if (event != null && event.getNode() != null) {
      AbstractEosgiNode node = event.getNode();

      eosgiNodeCache.remove(node);

      Control ctrl = viewer.getControl();
      if (ctrl == null || ctrl.isDisposed()) {
        return;
      }

      Runnable refreshRunnable = getRefreshRunnable(node);
      Collection<Runnable> refreshRunnables = Arrays.asList(refreshRunnable);

      if (ctrl.getDisplay().getThread() == Thread.currentThread()) {
        runUpdates(refreshRunnables);
      } else {
        ctrl.getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            // Abort if this happens after disposes
            Control ctrl = viewer.getControl();
            if (ctrl == null || ctrl.isDisposed()) {
              return;
            }

            runUpdates(refreshRunnables);
          }
        });
      }
    }
  }

  private void runUpdates(final Collection runnables) {
    Iterator runnableIterator = runnables.iterator();
    while (runnableIterator.hasNext()) {
      ((Runnable) runnableIterator.next()).run();
    }

  }
}
