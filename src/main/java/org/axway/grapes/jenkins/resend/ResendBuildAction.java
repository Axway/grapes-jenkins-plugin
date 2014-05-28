package org.axway.grapes.jenkins.resend;

import hudson.FilePath;
import hudson.model.Action;
import org.axway.grapes.jenkins.notifications.GrapesNotification;

/**
 * Resend Action
 *
 * <p>Associated to a build, this action is created when the notification failed. The aim is to keep the information about the notification to be able to resend it.</p>
 * <p>The configuration for resend is stored in the job configuration. This action will not be displayed in the build because notification resend are performed from the administration panel.</p>
 *
 * @author jdcoffre
 */
public class ResendBuildAction extends GrapesNotification implements Action {

    private final FilePath reportPath;
    private final NotificationType action;

    private String moduleName;
    private String moduleVersion;

    public ResendBuildAction(final GrapesNotification notification) {
        this.action = notification.getNotificationAction();
        this.reportPath = notification.getMimePath();
        this.moduleName = notification.moduleName();
        this.moduleVersion = notification.moduleVersion();
    }

    @Override
    public NotificationType getNotificationAction() {
        return action;
    }

    @Override
    public FilePath getMimePath() {
        return reportPath;
    }

    @Override
    public String moduleName() {
        return moduleName;
    }

    @Override
    public String moduleVersion() {
        return moduleVersion;
    }

    // Hide the build action
    public String getIconFileName() {
        return null;
    }

    // Hide the build action
    public String getDisplayName() {
        return null;
    }

    // Hide the build action
    public String getUrlName() {
        return null;
    }

    @Override
    public boolean equals(final Object obj){
        if(obj instanceof GrapesNotification){
            final GrapesNotification notification = (GrapesNotification)obj;
            return moduleName.equals(notification.moduleName()) &&
                    moduleVersion.equals(notification.moduleVersion()) &&
                     action.equals(notification.getNotificationAction());
        }

        return false;
    }
}
