package org.everit.e4.eosgi.plugin.ui.navigator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;
import org.everit.e4.eosgi.plugin.m2e.model.Environments;

public enum EosgiProjectController {
    INSTANCE;

    private Map<IProject, Environments> projects = new ConcurrentHashMap<>();
    
    public static EosgiProjectController getInstance() {
        return INSTANCE;
    }

    public void addProject(IProject project, Environments environments) {
        System.out.println("add project: " + project.getName());
        projects.put(project, environments);
    }

    public void removeProject(IProject project) {
        System.out.println("remove project: " + project.getName());
        projects.remove(project);
    }

    public Environments getProject(IProject project) {
        System.out.println("get project: " + project.getName());
        return projects.get(project);
    }

}
