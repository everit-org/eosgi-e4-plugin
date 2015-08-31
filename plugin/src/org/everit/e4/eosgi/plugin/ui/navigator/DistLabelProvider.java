package org.everit.e4.eosgi.plugin.ui.navigator;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.everit.e4.eosgi.plugin.ui.Activator;
import org.everit.e4.eosgi.plugin.ui.navigator.nodes.AbstractNode;

public class DistLabelProvider extends LabelProvider implements ILabelProvider,
    IDescriptionProvider, IStyledLabelProvider {

  // public DistLabelProvider() {
  // super();
  // if (!isListenerAttached()) {
  // addListener(new ILabelProviderListener() {
  //
  // @Override
  // public void labelProviderChanged(LabelProviderChangedEvent event) {
  // event.getElement();
  // }
  // });
  // }
  // }

  @Override
  public String getDescription(final Object element) {
    if (element instanceof AbstractNode) {
      AbstractNode node = (AbstractNode) element;
      return node.getText();
    } else {
      return "";
    }
  }

  @Override
  public Image getImage(final Object element) {
    if (element instanceof AbstractNode) {
      AbstractNode node = (AbstractNode) element;
      return Activator.getImageDescriptor(node.getIcon()).createImage();
    } else {
      return super.getImage(element);
    }
  }

  @Override
  public StyledString getStyledText(final Object element) {
    if (element == null) {
      return null;
    }
    String text = getText(element);
    StyledString styledString = new StyledString(text);

    if (element instanceof AbstractNode) {
      AbstractNode node = (AbstractNode) element;
      if (node.getLabel() != null) {
        int startPos = node.getName().length();
        int length = node.getLabel().length();
        length = Math.min(length, text.length());
        styledString.setStyle(startPos, length,
            StyledString.DECORATIONS_STYLER);
      } else if (node.getValue() != null) {
        int startPos = node.getName().length();
        int length = node.getValue().length();
        length = Math.min(length, text.length());
        styledString.setStyle(startPos, length,
            StyledString.QUALIFIER_STYLER);
      }
    }
    return styledString;
  }

  @Override
  public String getText(final Object element) {
    if (element instanceof AbstractNode) {
      AbstractNode node = (AbstractNode) element;
      return node.getText();
    } else {
      return null;
    }
  }

}
