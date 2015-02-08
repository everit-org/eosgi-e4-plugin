package org.everit.e4.eosgi.plugin.ui.navigator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.everit.e4.eosgi.plugin.ui.navigator.model.EosgiNode;
import org.everit.e4.eosgi.plugin.ui.navigator.model.EosgiNodeType;

public class EosgiEnvironmentLabelProvider extends LabelProvider implements ILabelProvider, IDescriptionProvider {

    // @Override
    // public Image decorateImage(Image image, Object element) {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public String decorateText(String decoreText, Object element) {
    // if (element instanceof EosgiNode) {
    // return "ok";
    // }
    // return null;
    // }

    private ImageDescriptor everitLogo;

    @Override
    public String getText(Object element) {
        if (element instanceof EosgiNode) {
            EosgiNode eosgiNode = (EosgiNode) element;
            if (eosgiNode.getLabel() != null) {
                return eosgiNode.getName() + " (" + eosgiNode.getLabel() + ")";
            } else {
                return eosgiNode.getName();
            }
        }
        return super.getText(element);
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof EosgiNode) {
            EosgiNode node = (EosgiNode) element;
            if (EosgiNodeType.ENVIRONMENTS == node.getType()) {
                return Activator.getImageDescriptor("icons/console_view.gif").createImage();
            } else if (EosgiNodeType.ENVIRONMENT == node.getType()) {
                return resolvEnvironmentIcon(node);
            } else if (EosgiNodeType.CONFIGURATION == node.getType()) {
                return getEveritLogo().createImage();
            } else {
                return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
            }
        }
        return null;// super.getImage(element);
    }

    private Image resolvEnvironmentIcon(EosgiNode node) {
        String environmentLogoName = null;
        if ("equinox".equals(node.getLabel())) {
            environmentLogoName = "sample.gif";
        } else if ("felix".equals(node.getLabel())) {
            environmentLogoName = "felix-logo.gif";
        } else {
            environmentLogoName = "osgi-logo.gif";
        }
        return Activator.getImageDescriptor("icons/" + environmentLogoName).createImage();
    }

    @Override
    public String getDescription(Object element) {
        if (element instanceof EosgiNode) {
            EosgiNode eosgiNode = (EosgiNode) element;
            if (eosgiNode.getLabel() != null && EosgiNodeType.ENVIRONMENT == eosgiNode.getType()) {
                return "id: " + eosgiNode.getName() + ", framework: " + eosgiNode.getLabel();
            } else {
                return eosgiNode.getName();
            }
        }
        return null;
    }

    public ImageDescriptor getEveritLogo() {
        if (everitLogo == null) {
            everitLogo = Activator.getImageDescriptor("icons/everit.gif");
        }
        return everitLogo;
    }

}
