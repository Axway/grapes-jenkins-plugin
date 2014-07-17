package org.axway.grapes.jenkins.notifications.maven;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import org.axway.grapes.commons.datamodel.Module;
import org.axway.grapes.jenkins.GrapesNotifier;
import org.axway.grapes.jenkins.GrapesPlugin;
import org.axway.grapes.jenkins.notifications.GrapesNotification;
import org.axway.grapes.jenkins.notifications.GrapesNotificationDescriptor;

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
    public void setMimePath(final FilePath reportFile) {
        this.moduleFilePath = reportFile;
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public String getModuleVersion() {
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
        public GrapesNotification createAutoInstance(AbstractBuild<?, ?> build) {
            GrapesMavenPluginNotification notification = null;
            try{
                final GrapesNotifier notifier = GrapesPlugin.getGrapesNotifier(build.getProject());
                if(notifier == null || !notifier.getManageGrapesMavenPlugin()){
                    return null;
                }

                final FilePath moduleFilePath = GrapesPlugin.getBuildModuleFile(build);
                if(!moduleFilePath.exists()){
                    return null;
                }

                final Module module = GrapesPlugin.getModule(moduleFilePath);

                notification = new GrapesMavenPluginNotification();
                notification.setModuleName(module.getName());
                notification.setModuleVersion(module.getVersion());
                notification.setModuleFilePath(moduleFilePath);
            } catch (Exception e) {
                GrapesPlugin.getLogger().log(Level.SEVERE, "[GRAPES] Failed to get build Maven Grapes report ", e);
            }

            return notification;
        }
    }
}
