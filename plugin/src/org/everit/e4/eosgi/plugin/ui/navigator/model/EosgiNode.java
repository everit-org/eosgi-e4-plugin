package org.everit.e4.eosgi.plugin.ui.navigator.model;

public class EosgiNode {
    private String label;
    private EosgiNodeType type;
    private EosgiNode[] childs;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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

}
