package org.everit.e4.eosgi.plugin.m2e.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.everit.e4.eosgi.plugin.m2e.model.Bundle;
import org.everit.e4.eosgi.plugin.m2e.model.BundleSettings;
import org.everit.e4.eosgi.plugin.m2e.model.Environment;
import org.everit.e4.eosgi.plugin.m2e.model.Environments;

public class ConfiguratorParser {

  private static final String BUNDLE_TAG = "bundle";

  private static final String VM_OPTIONS_TAG = "vmOptions";

  private static final String BUNDLE_SETTINGS_TAG = "bundleSettings";

  private static final String SYSTEM_PROPERTIES_TAG = "systemProperties";

  private static final String FRAMEWORK_TAG = "framework";

  private static final String ENVIRONMENT_ID_TAG = "id";

  private static final String ENVIRONMENTS_TAG = "environments";

  private Xpp3Dom configurationDom;

  private Environments environments;

  public ConfiguratorParser() {
    super();
  }

  // TODO refactor
  public Environments parse(final Xpp3Dom configurationDom) {
    if (configurationDom == null) {
      // TODO log this
      return new Environments();
    }

    this.configurationDom = configurationDom;
    environments = new Environments();
    environments.setEnvironments(new ArrayList<>());

    Xpp3Dom environmentsChild = this.configurationDom.getChild(ENVIRONMENTS_TAG);
    Xpp3Dom[] childrens = environmentsChild.getChildren();
    if (childrens != null) {
      for (Xpp3Dom children : childrens) {
        processEnvironments(children);
      }
    }

    this.configurationDom = null;
    return environments;
  }

  private Bundle processBundle(final Xpp3Dom bundlesDom) {
    Bundle bundle = new Bundle();
    Map<String, String> bundleProperties = new HashMap<String, String>();
    bundle.setBundlePropertiesMap(bundleProperties);
    Xpp3Dom[] bundleChildrenDom = bundlesDom.getChildren();
    if (bundleChildrenDom != null) {
      for (Xpp3Dom bundleDom : bundleChildrenDom) {
        String key = bundleDom.getName();
        String value = bundleDom.getValue();
        bundleProperties.put(key, value);
      }
    }
    return bundle;
  }

  private BundleSettings processBundleSettings(final Xpp3Dom children) {
    BundleSettings bundleSettings = new BundleSettings();
    List<Bundle> bundles = new ArrayList<Bundle>();
    bundleSettings.setBundles(bundles);
    Xpp3Dom bundleSettingsDom = children.getChild(BUNDLE_SETTINGS_TAG);
    if (bundleSettingsDom != null) {
      Xpp3Dom[] bundleSettingsChildren = bundleSettingsDom.getChildren();
      if (bundleSettingsChildren != null) {
        for (Xpp3Dom bundlesDom : bundleSettingsChildren) {
          if (BUNDLE_TAG.equals(bundlesDom.getName())) {
            Bundle bundle = processBundle(bundlesDom);
            bundles.add(bundle);
          }
        }
      }
    }
    return bundleSettings;
  }

  private void processEnvironments(final Xpp3Dom children) {
    Environment environment = new Environment();
    String id = processSimpleTextContentById(children, ENVIRONMENT_ID_TAG);
    environment.setId(id);
    String framework = processSimpleTextContentById(children, FRAMEWORK_TAG);
    environment.setFramework(framework);

    Map<String, String> systemProperties = new HashMap<>();
    environment.setSystemProperties(systemProperties);

    Xpp3Dom systemPropertiesDom = children.getChild(SYSTEM_PROPERTIES_TAG);
    if (systemPropertiesDom != null) {
      Xpp3Dom[] xpp3Doms = systemPropertiesDom.getChildren();
      if (xpp3Doms != null) {
        for (Xpp3Dom xpp3Dom : xpp3Doms) {
          environment.getSystemProperties().put(xpp3Dom.getName(), xpp3Dom.getValue());
        }
      }
    }
    List<String> vmOptions = processVmOptions(children);
    environment.setVmOptions(vmOptions);

    BundleSettings bundleSettings = processBundleSettings(children);
    environment.setBundleSettings(bundleSettings);
    environments.getEnvironments().add(environment);
  }

  private String processSimpleTextContentById(final Xpp3Dom domElement, final String id) {
    Xpp3Dom elementTag = domElement.getChild(id);
    if ((elementTag != null) && (elementTag.getValue() != null)) {
      return elementTag.getValue();
    } else {
      return "";
    }
  }

  private List<String> processVmOptions(final Xpp3Dom children) {
    List<String> vmOptions = new ArrayList<String>();
    Xpp3Dom vmOptionsDom = children.getChild(VM_OPTIONS_TAG);
    if (vmOptionsDom != null) {
      Xpp3Dom[] vmOptionDomChildren = vmOptionsDom.getChildren();
      for (Xpp3Dom vmOptionDom : vmOptionDomChildren) {
        String content = vmOptionDom.getValue();
        if (content != null) {
          vmOptions.add(content);
        }
      }
    }
    return vmOptions;
  }

}
