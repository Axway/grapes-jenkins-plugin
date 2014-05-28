package org.axway.grapes.jenkins.notifications;

import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

import java.util.Collection;

/**
 * Grapes Notification Descripto
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
        return Jenkins.getInstance().<GrapesNotification,GrapesNotificationDescriptor>getDescriptorList(GrapesNotification.class);
    }

}
