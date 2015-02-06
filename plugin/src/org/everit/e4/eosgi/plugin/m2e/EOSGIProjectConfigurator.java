package org.everit.e4.eosgi.plugin.m2e;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.everit.e4.eosgi.plugin.m2e.model.Environments;
import org.everit.e4.eosgi.plugin.m2e.xml.ConfiguratorParser;

public class EOSGIProjectConfigurator extends AbstractProjectConfigurator {

    @Override
    public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {
        System.out.println("EOSGIProjectConfigurator.configure");
    }

    @Override
    public AbstractBuildParticipant getBuildParticipant(IMavenProjectFacade projectFacade, MojoExecution execution,
            IPluginExecutionMetadata executionMetadata) {
        System.out.println("EOSGIProjectConfigurator.getBuildParticipant");

        Environments environments = new ConfiguratorParser().parse(execution.getConfiguration());
        System.out.println(environments);

        return new EOSGIMojoExecutionBuildParticipant(execution, true);
    }


}
