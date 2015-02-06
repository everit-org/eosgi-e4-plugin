package org.everit.e4.eosgi.plugin.m2e.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.everit.e4.eosgi.plugin.m2e.model.Environment;
import org.everit.e4.eosgi.plugin.m2e.model.Environments;

public class ConfiguratorParser {

    private static final String ENVIRONMENT_ID_TAG = "id";
    private static final String ENVIRONMENTS_TAG = "environments";

    private Xpp3Dom configurationDom;

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
        Environments environments = new Environments();
        environments.setEnvironments(new ArrayList<>());

        Xpp3Dom environmentsChild = this.configurationDom.getChild(ENVIRONMENTS_TAG);
        Xpp3Dom[] childrens = environmentsChild.getChildren();
        if (childrens != null) {
            for (Xpp3Dom children : childrens) {
                Environment environment = new Environment();
                String id = processSimpleTextContentById(children, ENVIRONMENT_ID_TAG);
                environment.setId(id);
                String framework = processSimpleTextContentById(children, "framework");
                environment.setFramework(framework);

                Map<String, String> systemProperties = new HashMap<>();
                environment.setSystemProperties(systemProperties);

                Xpp3Dom systemPropertiesDom = children.getChild("systemProperties");
                Xpp3Dom[] xpp3Doms = systemPropertiesDom.getChildren();
                for (Xpp3Dom xpp3Dom : xpp3Doms) {
                    environment.getSystemProperties().put(xpp3Dom.getName(), xpp3Dom.getValue());
                }
                environments.getEnvironments().add(environment);
            }
        }

        this.configurationDom = null;
        return environments;
    }

    private String processSimpleTextContentById(Xpp3Dom domElement, String id) {
        Xpp3Dom elementTag = domElement.getChild(id);
        if (elementTag != null && elementTag.getValue() != null) {
            return elementTag.getValue();
        } else {
            return "";
        }
    }

}
