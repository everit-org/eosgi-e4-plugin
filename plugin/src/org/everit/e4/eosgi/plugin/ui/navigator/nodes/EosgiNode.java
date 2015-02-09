package org.everit.e4.eosgi.plugin.ui.navigator.nodes;

import java.util.Arrays;

public class EosgiNode {
    private String name;
    private String label;
    private EosgiNodeType type;
    private EosgiNode[] childs;

    public String getName() {
        return name;
    }

    public void setName(String label) {
        this.name = label;
    }

    public EosgiNodeType getType() {
        return type;
    }

    public void setType(EosgiNodeType type) {
        this.type = type;
    }

    public EosgiNode[] getChilds() {
        return childs;
    }

    public void setChilds(EosgiNode[] childs) {
        this.childs = childs;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean hasLabel() {
        return label != null && !label.isEmpty();
    }

    @Override
    public String toString() {
        return "EosgiNode [name=" + name + ", label=" + label + ", type=" + type + ", childs="
                + Arrays.toString(childs) + "]";
    }

}
