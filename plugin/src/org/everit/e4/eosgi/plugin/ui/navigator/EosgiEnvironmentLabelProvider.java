package org.everit.e4.eosgi.plugin.ui.navigator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.everit.e4.eosgi.plugin.ui.navigator.nodes.EosgiNode;
import org.everit.e4.eosgi.plugin.ui.navigator.nodes.EosgiNodeType;

public class EosgiEnvironmentLabelProvider extends LabelProvider implements ILabelProvider,
        IDescriptionProvider,
        IStyledLabelProvider {

    private static final String UNKNOWN_NODE_OBJECT = "[unknown node object]";
    private static final int MAX_ALLOWED_LENGTH = 50;
    private ImageDescriptor everitLogo;

    @Override
    public String getDescription(final Object element) {
        if (element instanceof EosgiNode) {
            EosgiNode eosgiNode = (EosgiNode) element;
            if ((eosgiNode.getLabel() != null) && (EosgiNodeType.ENVIRONMENT == eosgiNode.getType())) {
                return "id: " + eosgiNode.getName() + ", framework: " + eosgiNode.getLabel();
            } else {
                return eosgiNode.getName();
            }
        }
        return UNKNOWN_NODE_OBJECT;
    }

    public ImageDescriptor getEveritLogo() {
        if (everitLogo == null) {
            everitLogo = Activator.getImageDescriptor("icons/everit.gif");
        }
        return everitLogo;
    }

    @Override
    public Image getImage(final Object element) {
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

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof EosgiNode) {
            StyledString styledString = new StyledString(getText(element));
            EosgiNode eosgiNode = (EosgiNode) element;
            if (eosgiNode.hasLabel()) {
                int startPos = eosgiNode.getName().length();
                int length = eosgiNode.getLabel().length() + 3;
                styledString.setStyle(startPos, length,
                        StyledString.QUALIFIER_STYLER);
            }
            return styledString;
        }
        return null;
    }

    @Override
    public String getText(final Object element) {
        String resultString = null;
        if (element instanceof EosgiNode) {
            EosgiNode eosgiNode = (EosgiNode) element;
            if (eosgiNode.getLabel() != null) {
                resultString = eosgiNode.getName() + " (" + eosgiNode.getLabel() + ")";
            } else {
                resultString = eosgiNode.getName();
            }
        } else {
            resultString = "-";
        }
        return trimLongStrings(resultString, MAX_ALLOWED_LENGTH);
    }

    private Image resolvEnvironmentIcon(final EosgiNode node) {
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

    private String trimLongStrings(final String resultString, final int maxAllowedLength) {
        return resultString.substring(0, Math.min(resultString.length(), 50));
    }

}
