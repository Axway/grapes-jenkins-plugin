package org.axway.grapes.jenkins.slicing;


import configurationslicing.Slicer;
import hudson.Extension;
import hudson.maven.AbstractMavenProject;
import jenkins.model.Jenkins;
import org.axway.grapes.jenkins.GrapesNotifier;
import org.axway.grapes.jenkins.GrapesNotifier.GrapesNotifierDescriptor;
import org.axway.grapes.jenkins.GrapesPlugin;
import org.axway.grapes.jenkins.config.GrapesConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Grapes Configuration Slicer
 *
 * <p>Optional implementation of an extension point provided by Jenkins configuration slicing plugin.
 * Manages Grapes configuration slicing over Jenkins jobs</p>
 *
 * @author jdcoffre
 */
@Extension(optional = true)
public class GrapesConfigSlicer implements Slicer<GrapesConfigSlice, AbstractMavenProject> {

    @Override
    public String getName() {
        return "Grapes server configuration";
    }

    @Override
    public String getUrl() {
        return "grapesserverconfig";
    }

    @Override
    public List<AbstractMavenProject> getWorkDomain() {
        return Jenkins.getInstance().getAllItems(AbstractMavenProject.class);
    }

    @Override
    public GrapesConfigSlice getInitialAccumulator() {
        final GrapesNotifierDescriptor descriptor = (GrapesNotifierDescriptor) Jenkins.getInstance().getDescriptor(GrapesNotifier.class);

        if(descriptor != null && descriptor.getServers() != null){
            final List<String> serverNames = new ArrayList<String>();
            for (GrapesConfig grapesConfig : descriptor.getServers()) {
                serverNames.add(grapesConfig.getName());
            }
            serverNames.add(GrapesConfigSlice.DISABLED);

            return new GrapesConfigSlice(serverNames);
        }

        return null;
    }

    @Override
    public GrapesConfigSlice accumulate(final GrapesConfigSlice grapesSlice, final AbstractMavenProject project) {
        String configName = GrapesConfigSlice.DISABLED;
        final GrapesNotifier grapesNotifier = GrapesPlugin.getGrapesNotifier(project);
        if(grapesNotifier != null){
            configName = grapesNotifier.getConfigName();
        }
        grapesSlice.add(project.getName(), configName);
        return grapesSlice;
    }

    @Override
    public boolean transform(final GrapesConfigSlice grapesSlice, final AbstractMavenProject abstractProject) {
        final String configurationName = grapesSlice.getConfiguration(abstractProject.getName());
        GrapesNotifier notifier = GrapesPlugin.getGrapesNotifier(abstractProject);

        if(configurationName == null || configurationName.equals(GrapesConfigSlice.DISABLED)){
            if(notifier != null){
                abstractProject.getPublishersList().remove(notifier);
                try{
                    abstractProject.save();
                } catch (IOException e) {
                    GrapesPlugin.getLogger().log(Level.SEVERE, "Failed disable Grapes notification for " + abstractProject.getName() + " using slicing.", e);
                    return false;
                }
            }
        }
        else {
            if(notifier == null){
                notifier = new GrapesNotifier(configurationName, false, false);
                abstractProject.getPublishersList().add(notifier);
            }
            else{
                notifier.setConfigName(configurationName);
            }
            try{
                abstractProject.save();
            } catch (IOException e) {
                GrapesPlugin.getLogger().log(Level.SEVERE, "Failed to update Grapes configuration for " + abstractProject.getName() + " using slicing.", e);
                return false;
            }
        }

        return true;
    }


    @Override
    public boolean isLoaded() {
        return getInitialAccumulator() != null;
    }

    @Override
    public int compareTo(final Slicer<GrapesConfigSlice, AbstractMavenProject> o) {
        return getName().compareToIgnoreCase(o.getName());
    }
}
