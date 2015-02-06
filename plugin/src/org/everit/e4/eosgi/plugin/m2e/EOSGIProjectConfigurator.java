package org.everit.e4.eosgi.plugin.m2e;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;

public class EOSGIProjectConfigurator extends AbstractProjectConfigurator {

    @Override
    public void configure(ProjectConfigurationRequest arg0, IProgressMonitor arg1) throws CoreException {
        System.out.println("EOSGIProjectConfigurator.configure");
    }

    @Override
    public AbstractBuildParticipant getBuildParticipant(IMavenProjectFacade projectFacade, MojoExecution execution,
            IPluginExecutionMetadata executionMetadata) {
        System.out.println("EOSGIProjectConfigurator.getBuildParticipant");
        return new EOSGIMojoExecutionBuildParticipant(execution, true);
    }

}
