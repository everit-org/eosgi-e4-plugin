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
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.everit.osgi.dev.e4.plugin.EOSGiEclipsePlugin;
import org.everit.osgi.dev.e4.plugin.EOSGiProject;
import org.everit.osgi.dev.e4.plugin.ExecutableEnvironment;

/**
 * LabelProvider implementation.
 */
public class DistLabelProvider extends LabelProvider {

  private static final Image IMAGE_ENVIRONMENT;

  private static final Image IMAGE_EVERIT_LOGO;

  static {
    URL everitLogoIconURL = DistLabelProvider.class.getResource("/icons/everit.gif");
    IMAGE_EVERIT_LOGO = ImageDescriptor.createFromURL(everitLogoIconURL).createImage();

    URL environmentImageURL = DistLabelProvider.class.getResource("/icons/console_view.gif");
    IMAGE_ENVIRONMENT = ImageDescriptor.createFromURL(environmentImageURL).createImage();
  }

  public DistLabelProvider() {
    EOSGiEclipsePlugin.getDefault().getEOSGiManager().addLabelProvider(this);
  }

  @Override
  public void dispose() {
    EOSGiEclipsePlugin.getDefault().getEOSGiManager().removeLabelProvider(this);
    super.dispose();
  }

  public void eosgiProjectChanged(final EOSGiProject eosgiProject) {
    fireLabelProviderChanged(new LabelProviderChangedEvent(this, eosgiProject));
  }

  @Override
  public Image getImage(final Object element) {
    if (element instanceof EOSGiProject) {
      return IMAGE_EVERIT_LOGO;
    } else if (element instanceof ExecutableEnvironment) {
      return IMAGE_ENVIRONMENT;
    } else {
      return super.getImage(element);
    }
  }

  @Override
  public String getText(final Object element) {
    if (element instanceof EOSGiProject) {
      return "OSGi Environments";
    } else if (element instanceof ExecutableEnvironment) {
      ExecutableEnvironment eosgiEnvironment = (ExecutableEnvironment) element;
      return eosgiEnvironment.getEnvironmentId() + " ("
          + eosgiEnvironment.getMojoExecution().getExecutionId() + ")";
    } else {
      return super.getText(element);
    }
  }
}
