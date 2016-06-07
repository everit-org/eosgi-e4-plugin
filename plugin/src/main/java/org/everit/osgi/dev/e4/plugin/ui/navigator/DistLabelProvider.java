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

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.everit.osgi.dev.e4.plugin.EOSGiProject;

/**
 * LabelProvider implementation.
 */
public class DistLabelProvider extends LabelProvider {

  private static final Image EVERIT_LOGO_IMAGE;

  static {
    URL everitLogoIconURL = DistLabelProvider.class.getResource("/icons/everit.gif");
    EVERIT_LOGO_IMAGE = ImageDescriptor.createFromURL(everitLogoIconURL).createImage();
  }

  @Override
  public Image getImage(final Object element) {
    if (element instanceof EOSGiProject) {
      return EVERIT_LOGO_IMAGE;
    } else if (element instanceof String) {
      return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEF_VIEW);
    } else {
      return super.getImage(element);
    }
  }

  @Override
  public String getText(final Object element) {
    if (element instanceof EOSGiProject) {
      return "OSGi Environments";
    } else if (element instanceof String) {
      return (String) element;
    } else {
      return super.getText(element);
    }
  }
}
