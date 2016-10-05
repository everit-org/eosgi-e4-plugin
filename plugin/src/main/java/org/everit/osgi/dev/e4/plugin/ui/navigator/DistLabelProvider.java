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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.everit.osgi.dev.dist.util.attach.EOSGiVMManager;
import org.everit.osgi.dev.e4.plugin.EOSGiEclipsePlugin;
import org.everit.osgi.dev.e4.plugin.EOSGiProject;
import org.everit.osgi.dev.e4.plugin.ExecutableEnvironment;

/**
 * LabelProvider implementation.
 */
public class DistLabelProvider extends LabelProvider {

  private static final Image IMAGE_ENVIRONMENT;

  private static final Image IMAGE_EVERIT_LOGO;

  private static final Image IMAGE_RUNNING_ENVIRONMENT;

  static {
    Class<DistLabelProvider> clazz = DistLabelProvider.class;

    IMAGE_EVERIT_LOGO = ImageDescriptor.createFromFile(clazz, "/icons/everit.gif").createImage();

    IMAGE_ENVIRONMENT =
        ImageDescriptor.createFromFile(clazz, "/icons/console_view.gif").createImage();

    ImageDescriptor lrunImageDescriptor =
        ImageDescriptor.createFromFile(clazz, "/icons/lrun_obj_shifted.gif");
    Rectangle environmentImageBounds = IMAGE_ENVIRONMENT.getBounds();
    ImageData scaledNavGoImageData =
        lrunImageDescriptor.getImageData().scaledTo((int) (environmentImageBounds.width / 1.4),
            (int) (environmentImageBounds.height / 1.4));

    ImageDescriptor scaledNavGoImageDescriptor =
        ImageDescriptor.createFromImageData(scaledNavGoImageData);

    IMAGE_RUNNING_ENVIRONMENT =
        new DecorationOverlayIcon(IMAGE_ENVIRONMENT, scaledNavGoImageDescriptor,
            IDecoration.BOTTOM_RIGHT).createImage();

  }

  public DistLabelProvider() {
    EOSGiEclipsePlugin.getDefault().getEOSGiManager().addLabelProvider(this);
  }

  @Override
  public void dispose() {
    EOSGiEclipsePlugin.getDefault().getEOSGiManager().removeLabelProvider(this);
    super.dispose();
  }

  public void executableEnvironmentChanged(final ExecutableEnvironment executableEnvironment) {
    Display.getDefault().asyncExec(
        () -> fireLabelProviderChanged(new LabelProviderChangedEvent(this, executableEnvironment)));
  }

  @Override
  public Image getImage(final Object element) {
    if (element instanceof EOSGiProject) {
      return IMAGE_EVERIT_LOGO;
    } else if (element instanceof ExecutableEnvironment) {
      ExecutableEnvironment executableEnvironment = (ExecutableEnvironment) element;
      EOSGiVMManager eosgiVMManager =
          EOSGiEclipsePlugin.getDefault().getEOSGiManager().getEosgiVMManager();

      if (!eosgiVMManager.getRuntimeInformations(executableEnvironment.getEnvironmentId(),
          executableEnvironment.getRootFolder()).isEmpty()) {
        return IMAGE_RUNNING_ENVIRONMENT;
      }
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
