/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.osgi.dev.e4.plugin.ui.navigator;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.everit.osgi.dev.e4.plugin.ui.EOSGiEclipsePlugin;
import org.everit.osgi.dev.e4.plugin.ui.navigator.nodes.AbstractNode;

/**
 * LabelProvider implementation.
 */
public class DistLabelProvider extends LabelProvider implements
    IDescriptionProvider, IStyledLabelProvider {

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
      return EOSGiEclipsePlugin.getImageDescriptor(node.getIcon()).createImage();
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
