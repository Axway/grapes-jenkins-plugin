package org.axway.grapes.jenkins.notifications;

import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Describable;
import jenkins.model.Jenkins;
import org.axway.grapes.jenkins.resend.ResendBuildAction;

/**
 * Grapes Notification
 *
 * <p>Extension point that could be used by other Jenkins plugin.
 * By implementing the Grapes notification, an other plugin can create notifications that will be sent
 * to the configured Grapes instance at the end of a build.</p>
 *
 * @author jdcoffre
 */
public abstract class GrapesNotification implements Describable<GrapesNotification>, ExtensionPoint {

    public static enum NotificationType {POST_MODULE, PROMOTE};

    /**
     * Returns the kind of notification the Grapes Jenkins notifier should perform
     *
     * @return Action
     */
    public abstract NotificationType getNotificationAction();

    /**
     * Returns the file path of the mime that should be sent to Grapes server during the notification
     * <p> Required for POST_MODULE Action</p>
     *
     * @return FilePath
     */
    public abstract FilePath getMimePath();

    /**
     * Returns the module name that is targeted by the notification
     * <p> Required for PROMOTE Action</p>
     *
     * @return String
     */
    public abstract String moduleName();

    /**
     * Returns the module version that is targeted by the notification
     * <p> Required for PROMOTE Action</p>
     *
     * @return String
     */
    public abstract String moduleVersion();

    public GrapesNotificationDescriptor getDescriptor() {
        return (GrapesNotificationDescriptor) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }


}
