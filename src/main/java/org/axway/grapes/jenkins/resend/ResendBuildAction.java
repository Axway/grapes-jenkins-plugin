package org.axway.grapes.jenkins.resend;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import hudson.FilePath;
import hudson.model.Action;
import org.axway.grapes.jenkins.notifications.GrapesNotification;
import org.axway.grapes.jenkins.resend.json.ResendBuildActionDeserializer;
import org.axway.grapes.jenkins.resend.json.ResendBuildActionSerializer;

/**
 * Resend Action
 *
 * <p>Associated to a build, this action is created when the notification failed. The aim is to keep the information about the notification to be able to resend it.</p>
 * <p>The configuration for resend is stored in the job configuration. This action will not be displayed in the build because notification resend are performed from the administration panel.</p>
 *
 * @author jdcoffre
 */
@JsonSerialize(using=ResendBuildActionSerializer.class)
@JsonDeserialize(using=ResendBuildActionDeserializer.class)
public class ResendBuildAction extends GrapesNotification implements Action {

    private final FilePath mimePath;
    private final NotificationType notificationAction;

    private final String moduleName;
    private final String moduleVersion;

    public ResendBuildAction(final NotificationType notificationAction, final FilePath mimePath, final String moduleName, final String moduleVersion) {
        this.notificationAction = notificationAction;
        this.mimePath = mimePath;
        this.moduleName = moduleName;
        this.moduleVersion = moduleVersion;
    }

    public ResendBuildAction(final GrapesNotification notification) {
        this.notificationAction = notification.getNotificationAction();
        this.mimePath = notification.getMimePath();
        this.moduleName = notification.getModuleName();
        this.moduleVersion = notification.getModuleVersion();
    }

    @Override
    public NotificationType getNotificationAction() {
        return notificationAction;
    }

    @Override
    public FilePath getMimePath() {
        return mimePath;
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public String getModuleVersion() {
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
            return moduleName.equals(notification.getModuleName()) &&
                    moduleVersion.equals(notification.getModuleVersion()) &&
                     notificationAction.equals(notification.getNotificationAction());
        }

        return false;
    }
}
