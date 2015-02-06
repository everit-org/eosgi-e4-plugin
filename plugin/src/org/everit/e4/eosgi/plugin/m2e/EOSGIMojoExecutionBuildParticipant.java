package org.everit.e4.eosgi.plugin.m2e;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;

public class EOSGIMojoExecutionBuildParticipant extends MojoExecutionBuildParticipant {

    public EOSGIMojoExecutionBuildParticipant(MojoExecution execution, boolean runOnIncremental) {
        super(execution, runOnIncremental);
        System.out.println("EOSGIMojoExecutionBuildParticipant");
    }

    public EOSGIMojoExecutionBuildParticipant(MojoExecution execution, boolean runOnIncremental,
            boolean runOnConfiguration) {
        super(execution, runOnIncremental, runOnConfiguration);
        System.out.println("EOSGIMojoExecutionBuildParticipant");
    }

}
