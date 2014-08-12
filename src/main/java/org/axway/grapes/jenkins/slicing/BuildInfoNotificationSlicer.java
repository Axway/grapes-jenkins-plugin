package org.axway.grapes.jenkins.slicing;


import configurationslicing.BooleanSlicer;
import hudson.Extension;
import hudson.model.AbstractProject;
import jenkins.model.Jenkins;
import org.axway.grapes.jenkins.GrapesNotifier;
import org.axway.grapes.jenkins.GrapesPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Build Info Notification Slicer
 *
 * <p>Optional implementation of an extension point provided by Jenkins configuration slicing plugin.
 * Manages the build info configuration slicing</p>
 *
 * @author jdcoffre
 */
@Extension(optional = true)
public class BuildInfoNotificationSlicer extends BooleanSlicer {

    public BuildInfoNotificationSlicer() {
        super(new BuildInfoNotificationSpec());
    }

    @Override
    public boolean isLoaded() {
        final BuildInfoNotificationSpec spec = new BuildInfoNotificationSpec();
        return spec.getWorkDomain().size() > 0;
    }

    private static class BuildInfoNotificationSpec implements BooleanSlicerSpec<AbstractProject<?,?>> {
        @Override
        public String getName() {
            return "Grapes Build Info notification (bool)";
        }

        @Override
        public String getUrl() {
            return "grapesbuildinfonotif";
        }

        @Override
        public List<AbstractProject<?, ?>> getWorkDomain() {
            final List<AbstractProject<?,?>> projects = new ArrayList<AbstractProject<?, ?>>();
            for (AbstractProject<?,?> project : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
                if(GrapesPlugin.getGrapesNotifier(project) != null){
                    projects.add(project);
                }
            }
            return projects;
        }

        @Override
        public boolean getValue(final AbstractProject<?, ?> item) {
            final GrapesNotifier notifier = GrapesPlugin.getGrapesNotifier(item);
            if(notifier != null){
                return notifier.getManageBuildInfo();
            }
            // should never happen
            return false;
        }

        @Override
        public String getName(final AbstractProject<?, ?> item) {
            return item.getFullName();
        }

        @Override
        public boolean setValue(final AbstractProject<?, ?> item, boolean value) {
            final GrapesNotifier notifier = GrapesPlugin.getGrapesNotifier(item);
            if(notifier != null){
                notifier.setManageBuildInfo(value);
                return true;
            }
            // should never happen
            return false;
        }
    }
}
