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
 * Maven Notification Slicer
 *
 * <p>Optional implementation of an extension point provided by Jenkins configuration slicing plugin.
 * Manages Grapes Maven report notification configuration slicing</p>
 *
 * @author jdcoffre
 */
@Extension(optional = true)
public class MavenNotificationSlicer extends BooleanSlicer {

    public MavenNotificationSlicer() {
        super(new MavenNotificationSpec());
    }

    @Override
    public boolean isLoaded() {
        final MavenNotificationSpec spec = new MavenNotificationSpec();
        return spec.getWorkDomain().size() > 0;
    }

    private static class MavenNotificationSpec implements BooleanSlicerSpec<AbstractProject<?,?>> {
        @Override
        public String getName() {
            return "Grapes Maven report notification (bool)";
        }

        @Override
        public String getUrl() {
            return "grapesmavennotif";
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
                return notifier.getManageGrapesMavenPlugin();
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
                notifier.setManageGrapesMavenPlugin(value);
                return true;
            }
            // should never happen
            return false;
        }
    }
}
