package org.axway.grapes.jenkins.notifications.maven;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import org.axway.grapes.commons.datamodel.Module;
import org.axway.grapes.jenkins.GrapesNotifier;
import org.axway.grapes.jenkins.GrapesPlugin;
import org.axway.grapes.jenkins.notifications.GrapesNotification;
import org.axway.grapes.jenkins.notifications.GrapesNotificationDescriptor;

import java.io.File;
import java.util.logging.Level;

/**
 * Grapes Maven plugin Notification
 *
 * <p>Implementation of Grapes notification for Grapes Maven plugin. It generates the </p>
 *
 * @author jdcoffre
 */
public class GrapesMavenPluginNotification extends GrapesNotification {

    private FilePath moduleFilePath;
    private String moduleName;
    private String moduleVersion;

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
        return moduleName;
    }

    @Override
    public String moduleVersion() {
        return moduleVersion;
    }

    public void setModuleFilePath(final FilePath moduleFilePath) {
        this.moduleFilePath = moduleFilePath;
    }

    public void setModuleVersion(final String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

    @Extension
    public static final class GrapesMavenPluginNotificationDescriptorImpl extends GrapesNotificationDescriptor{

        @Override
        public GrapesNotification newAutoInstance(AbstractBuild<?, ?> build) {
            GrapesMavenPluginNotification notification = null;
            try{
                final GrapesNotifier notifier = GrapesPlugin.getGrapesNotifier(build.getProject());
                if(notifier == null || !notifier.getManageGrapesMavenPlugin()){
                    return null;
                }

                final FilePath moduleFilePath = getModuleFilePath(build);
                if(!moduleFilePath.exists()){
                    return null;
                }

                final Module module = GrapesPlugin.getModule(new File(String.valueOf(moduleFilePath)));

                notification = new GrapesMavenPluginNotification();
                notification.setModuleName(module.getName());
                notification.setModuleVersion(module.getVersion());
                notification.setModuleFilePath(moduleFilePath);
            } catch (Exception e) {
                GrapesPlugin.getLogger().log(Level.SEVERE, "[GRAPES] Failed to get build Maven Grapes report ", e);
            }

            return notification;
        }

        /**
         * Returns the location of Grapes module file
         *
         * @param build AbstractBuild<?, ?>
         * @return FilePath
         */
        private FilePath getModuleFilePath(final AbstractBuild<?, ?> build) {
            // If the build is running the module file is in the workspace
            if(build.isBuilding()){
                // Use root module in stead of Workspace dir in case of custom checkout
                final FilePath moduleRoot = build.getModuleRoot();
                return moduleRoot.child("target/" + GrapesPlugin.GRAPES_WORKING_FOLDER + "/" + GrapesPlugin.GRAPES_MODULE_FILE);
            }

            // Otherwise the notification has been saved into the build folder
            return GrapesPlugin.getBuildReportFile(build);

        }
    }
}
