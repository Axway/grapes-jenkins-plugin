package org.axway.grapes.jenkins.notifications;

import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.model.Describable;
import jenkins.model.Jenkins;

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

    public static enum NotificationType {POST_MODULE, POST_MODULE_BUILD_INFO, PROMOTE}

    /**
     * Returns the kind of notification the Grapes Jenkins notifier should perform
     *
     * @return Action
     */
    public abstract NotificationType getNotificationAction();

    /**
     * Returns the file path of the mime that should be sent to Grapes server during the notification
     * <p> Required for NotificationType that implies to send a MIME to Grapes server </p>
     *
     * @return FilePath
     */
    public abstract FilePath getMimePath();

    /**
     * Sets the mimePath
     * <p>Required to update the mime after it has been backed-up in build folder</p>
     *
     * @param reportFile FilePath
     */
    public abstract void setMimePath(final FilePath reportFile);

    /**
     * Returns the module name that is targeted by the notification
     *
     * @return String
     */
    public abstract String getModuleName();

    /**
     * Returns the module version that is targeted by the notification
     *
     * @return String
     */
    public abstract String getModuleVersion();

    public GrapesNotificationDescriptor getDescriptor() {
        return (GrapesNotificationDescriptor) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }


}
