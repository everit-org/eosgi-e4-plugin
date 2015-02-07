package org.everit.e4.eosgi.plugin.ui.navigator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.everit.e4.eosgi.plugin.ui.navigator.model.EosgiNode;

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
            return ((EosgiNode) element).getLabel();
        }
        return super.getText(element);
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof EosgiNode) {
            if (everitLogo == null) {
                everitLogo = Activator.getImageDescriptor("icons/everit.gif");
            }
            return everitLogo.createImage();
        }
        return null;// super.getImage(element);
    }

    @Override
    public String getDescription(Object element) {
        if (element instanceof EosgiNode) {
            return ((EosgiNode) element).getLabel();
        }
        return null;
    }

}
