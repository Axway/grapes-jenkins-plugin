package org.axway.grapes.jenkins.notifications;

import hudson.model.AbstractBuild;
import hudson.model.Descriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.axway.grapes.jenkins.notifications.maven.GrapesMavenPluginNotification.GrapesMavenPluginNotificationDescriptorImpl;

import jenkins.model.Jenkins;

/**
 * Grapes Notification Descriptor
 *
 * <p>Contains the notification information that should be sent by Grapes notifier</p>
 *
 * @author jdcoffre
 */
public abstract class GrapesNotificationDescriptor extends Descriptor<GrapesNotification>{

    @Override
    public String getDisplayName() {
        return null;
    }

    /**
     * Generates a new instance of Grapes notification
     * @param build
     * @return
     */
    public abstract GrapesNotification createAutoInstance(AbstractBuild<?, ?> build);

    /**
     *  Lists all the currently registered instances of {@link GrapesNotificationDescriptor}.
     *
     * @return Collection<GrapesNotificationDescriptor>
     */
    public static Collection<GrapesNotificationDescriptor> all() {
        // use getDescriptorList and not getExtensionList to pick up legacy instances
        final ArrayList<GrapesNotificationDescriptor> descriptorList = new ArrayList<GrapesNotificationDescriptor>(Jenkins.getInstance().<GrapesNotification,GrapesNotificationDescriptor>getDescriptorList(GrapesNotification.class));
        Collections.sort(descriptorList, new Comparator<GrapesNotificationDescriptor>() {
            @Override
            public int compare(final GrapesNotificationDescriptor o1, final GrapesNotificationDescriptor o2) {
                return (o1 instanceof GrapesMavenPluginNotificationDescriptorImpl) ? -1 : 1;
            }
        });
        return descriptorList;
    }

}
