package org.everit.e4.eosgi.plugin.m2e.model;

import java.util.List;

public class BundleSettings {
    private List<Bundle> bundles;

    public List<Bundle> getBundles() {
        return bundles;
    }

    public void setBundles(List<Bundle> bundles) {
        this.bundles = bundles;
    }

    @Override
    public String toString() {
        return "BundleSettings [bundles=" + bundles + "]";
    }

}
