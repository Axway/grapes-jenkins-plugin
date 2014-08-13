package org.axway.grapes.jenkins.slicing;


import configurationslicing.BooleanSlicer;
import configurationslicing.UnorderedStringSlicer;
import hudson.Extension;
import hudson.model.AbstractProject;
import jenkins.model.Jenkins;
import org.axway.grapes.jenkins.GrapesNotifier;
import org.axway.grapes.jenkins.GrapesPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Build Info Notification Slicer
 *
 * <p>Optional implementation of an extension point provided by Jenkins configuration slicing plugin.
 * Manages the build info configuration slicing</p>
 *
 * @author jdcoffre
 */
@Extension(optional = true)
public class BuildInfoNotificationSlicer extends UnorderedStringSlicer {

    public BuildInfoNotificationSlicer() {
        super(new BuildInfoNotificationSpec());
    }

    @Override
    public boolean isLoaded() {
        final BuildInfoNotificationSpec spec = new BuildInfoNotificationSpec();
        return spec.getWorkDomain().size() > 0;
    }

    private static class BuildInfoNotificationSpec extends UnorderedStringSlicerSpec<AbstractProject<?, ?>> {
        @Override
        public String getName() {
            return "Grapes Build Info notification";
        }

        @Override
        public String getUrl() {
            return "grapesbuildinfo";
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
        public List<String> getCommonValueStrings() {
            List<String> values = new ArrayList<String>();
            values.add(String.valueOf(true));
            values.add(String.valueOf(false));
            return values;
        }

        @Override
        public boolean isBlankNeededForValues() {
            return false;
        }

        @Override
        public List<String> getValues(final AbstractProject<?, ?> item) {
            final GrapesNotifier notifier = GrapesPlugin.getGrapesNotifier(item);
            if(notifier != null){
                final Boolean manageBuildInfo = notifier.getManageBuildInfo();
                return Collections.singletonList(String.valueOf(manageBuildInfo));
            }
            // should never happen
            return Collections.singletonList("false");
        }

        @Override
        public String getName(final AbstractProject<?, ?> item) {
            return item.getFullName();
        }

        @Override
        public boolean setValues(final AbstractProject<?, ?> item, final List<String> values) {
            final GrapesNotifier notifier = GrapesPlugin.getGrapesNotifier(item);
            if(notifier != null){
                final String configuration = values.get(0);
                notifier.setManageBuildInfo(Boolean.valueOf(configuration));

                try {
                    item.save();
                } catch (IOException e) {
                    GrapesPlugin.getLogger().log(Level.SEVERE, "Failed to update build info notification for " + item.getName() + " using slicing.", e);
                    return false;
                }
                return true;
            }
            // should never happen
            return false;
        }

        @Override
        public String getDefaultValueString() {
            return null;
        }
    }
}
