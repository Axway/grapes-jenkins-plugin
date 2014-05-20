package org.axway.grapes.jenkins.notifications.maven;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import org.axway.grapes.jenkins.GrapesNotifier;
import org.axway.grapes.jenkins.GrapesPlugin;
import org.axway.grapes.jenkins.notifications.GrapesNotification;
import org.axway.grapes.jenkins.notifications.GrapesNotificationDescriptor;
import org.axway.grapes.jenkins.resend.ResendBuildAction;

/**
 * Grapes Maven plugin Notification
 *
 * <p>Implementation of Grapes notification for Grapes Maven plugin. It generates the </p>
 *
 * @author jdcoffre
 */
public class GrapesMavenPluginNotification extends GrapesNotification {

    private FilePath moduleFilePath;

    @Override
    public NotificationType getNotificationAction() {
        return NotificationType.POST_MODULE;
    }

    @Override
    public FilePath getMimePath() {
        return moduleFilePath;
    }

    @Override
    public String moduleName() {
        // no need to implement it for POST MODULE action
        return null;
    }

    @Override
    public String moduleVersion() {
        // no need to implement it for POST MODULE action
        return null;
    }

    public void setModuleFilePath(final FilePath moduleFilePath) {
        this.moduleFilePath = moduleFilePath;
    }

    @Extension
    public static final class GrapesMavenPluginNotificationDescriptorImpl extends GrapesNotificationDescriptor{

        @Override
        public GrapesNotification newAutoInstance(AbstractBuild<?, ?> build) {
            final GrapesNotifier notifier = GrapesPlugin.getGrapesNotifier(build);
            if(notifier == null || !notifier.getManageGrapesMavenPlugin()){
                return null;
            }

            final FilePath moduleFilePath = getModuleFilePath(build);

            GrapesMavenPluginNotification notification = new GrapesMavenPluginNotification();
            notification.setModuleFilePath(moduleFilePath);

            return notification;
        }

        /**
         * Returns the location of Grapes module file
         *
         * @param build AbstractBuild<?, ?>
         * @return FilePath
         */
        private FilePath getModuleFilePath(final AbstractBuild<?, ?> build) {
            return build.getWorkspace().child("target/" + GrapesPlugin.GRAPES_WORKING_FOLDER + "/" + GrapesPlugin.GRAPES_MODULE_FILE);
        }
    }
}
