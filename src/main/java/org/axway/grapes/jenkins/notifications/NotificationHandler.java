package org.axway.grapes.jenkins.notifications;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import org.axway.grapes.commons.datamodel.Module;
import org.axway.grapes.commons.utils.FileUtils;
import org.axway.grapes.commons.utils.JsonUtils;
import org.axway.grapes.jenkins.GrapesPlugin;
import org.axway.grapes.jenkins.config.GrapesConfig;
import org.axway.grapes.jenkins.reports.GrapesBuildAction;
import org.axway.grapes.jenkins.resend.ResendBuildAction;
import org.axway.grapes.utils.client.GrapesClient;
import org.axway.grapes.utils.client.GrapesCommunicationException;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Notification Handler
 *
 * <p>Manage notification send / re-send / deprecation</p>
 *
 * @author jdcoffre
 */
public class NotificationHandler {

    public static final String TO_RESEND_SUFFIX = "-to-resend";

    private final GrapesClient client;
    private String password;
    private  String user;

    public NotificationHandler(final GrapesConfig config) {
        client = new GrapesClient(config.getHost(), String.valueOf(config.getPort()));

        if (config.getPublisherCredentials() != null) {
            user = config.getPublisherCredentials().getUsername();
            password = config.getPublisherCredentials().getPassword();
        }
    }


    /**
     * Sends a resendBuildActions to Grapes server
     *
     * @param resendBuildActions List<ResendBuildAction>
     * @throws GrapesCommunicationException
     */
    public void send(final AbstractBuild<?, ?> build, final List<ResendBuildAction> resendBuildActions) throws GrapesCommunicationException {
        for(ResendBuildAction resendBuildAction: resendBuildActions){
            send(resendBuildAction, build);
        }
    }


    /**
     * Sends a notification to Grapes server
     *
     * @param notification GrapesNotification
     * @throws GrapesCommunicationException
     */
    public void send(final GrapesNotification notification, final AbstractBuild<?,?> build) throws GrapesCommunicationException {
        try{
            // perform the notification
            switch (notification.getNotificationAction()){
                case POST_MODULE:
                    // Send the module
                    final FilePath moduleFilePath = notification.getMimePath();
                    final Module module = GrapesPlugin.getModule(moduleFilePath);
                    client.postModule(module, user, password);

                    // Generate build action with the dependency report
                    final GrapesBuildAction buildAction = new GrapesBuildAction(module, client);

                    // Add dependency report to the build
                    if (buildAction.isInitOk()) {
                        build.addAction(buildAction);
                    }
                    break;
                case PROMOTE:
                    client.promoteModule(notification.getModuleName(), notification.getModuleVersion(), user, password);
                    break;
                case POST_MODULE_BUILD_INFO:
                    final FilePath buildInfoPath = notification.getMimePath();
                    final Map<String, String> buildInfo = GrapesPlugin.getBuildInfo(buildInfoPath);
                    client.postBuildInfo(notification.getModuleName(), notification.getModuleVersion(), buildInfo, user, password);
                    break;
                default:break;
            }

            //discard old resend action if matches moduleName moduleVersion notification type
            discardOldResend(notification, build.getProject());
        }

        catch (Exception e) {
            GrapesPlugin.getLogger().log(Level.SEVERE, "[GRAPES] An error occurred during notification sending ", e);
            discardOldResend(notification, build.getProject());
            saveNotification(notification, build);
            throw new GrapesCommunicationException(e.getMessage(), 500);

        }
    }

    /**
     * Serialize a resend action to be able to re-send the notification later
     *
     * @param notification GrapesNotification
     * @param build AbstractBuild
     */
    private void saveNotification(final GrapesNotification notification, final AbstractBuild<?, ?> build) {
        final ResendBuildAction resendAction = new ResendBuildAction(notification);

        // Check if the notification is valid before serializing it
        if(!isValid(resendAction)){
            GrapesPlugin.getLogger().log(Level.SEVERE, "[GRAPES] Malformed Grapes Notification: Grapes plugin is trying to serialize a notification provided by another plugin but failed to resolve the notification.");
            return;
        }

        try{
            final String serializedResend = JsonUtils.serialize(resendAction);
            final File reportFolder = new File(GrapesPlugin.getBuildReportFolder(build).toURI());
            FileUtils.serialize(reportFolder, serializedResend, getNotificationId(notification));
        }catch (Exception e){
            GrapesPlugin.getLogger().log(Level.SEVERE, "[GRAPES] Failed to serialized a resend action ", e);
        }
    }

    /**
     * Checks if a notification is valid
     *
     * @param resendAction ResendBuildAction
     * @return boolean
     */
    private boolean isValid(final ResendBuildAction resendAction) {
        if(resendAction.getModuleVersion() == null ||
              resendAction.getModuleName() == null ||
                 resendAction.getNotificationAction() == null ){
            return false;
        }
        return true;
    }


    /**
     * Discard old resend actions
     *  @param notification GrapesNotification
     * @param project AbstractBuild<?, ?>
     */
    private void discardOldResend(final GrapesNotification notification, final AbstractProject<?, ?> project) {
        try{
            for(AbstractBuild<?,?> build : project.getBuilds()){
                final File reportFolder = new File(GrapesPlugin.getBuildReportFolder(build).toURI());
                final File notifFile = new File(reportFolder, getNotificationId(notification));

                if(notifFile.exists()){
                    notifFile.delete();
                }
            }

        }catch (Exception e){
            GrapesPlugin.getLogger().log(Level.SEVERE, "[GRAPES] Failed to discard a resend action ", e);
        }
    }


    /**
     * Generates an Id for a notification
     *
     * @param notification GrapesNotification
     * @return String
     */
    private static String getNotificationId(final GrapesNotification notification) {
        assert notification != null;

        final StringBuilder sb = new StringBuilder();
        sb.append('.');
        sb.append(notification.getModuleName());
        sb.append('-');
        sb.append(notification.getModuleVersion());
        sb.append('-');
        sb.append(notification.getNotificationAction());
        sb.append(TO_RESEND_SUFFIX);

        return sb.toString();
    }
}
