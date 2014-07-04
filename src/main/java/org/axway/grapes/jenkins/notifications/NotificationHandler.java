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
        switch (notification.getNotificationAction()){
            case POST_MODULE: postModule(notification, build);
                break;
            case PROMOTE: promoteModule(notification, build);
                break;
            default:break;
        }
    }

    private void promoteModule(final GrapesNotification notification, final AbstractBuild<?, ?> build) throws GrapesCommunicationException {
        try {
            client.promoteModule(notification.getModuleName(), notification.getModuleVersion(), user, password);

            //discard old resend action if matches moduleName moduleVersion notification type
            discardOldResend(notification, build.getProject());

        } catch (Exception e) {
            GrapesPlugin.getLogger().log(Level.SEVERE, "[GRAPES] An error occurred during module promotion ", e);
            discardOldResend(notification, build.getProject());
            saveNotification(notification, build);
            throw new GrapesCommunicationException(500);

        }
    }

    private void postModule(final GrapesNotification notification, final AbstractBuild<?, ?> build) throws GrapesCommunicationException {
        final FilePath moduleFilePath = notification.getMimePath();

        try{
            // No module file, it should be a configuration error
            if(!moduleFilePath.exists()){
                GrapesPlugin.getLogger().log(Level.WARNING, "[GRAPES] Grapes Maven plugin report does not exist.");
                GrapesPlugin.getLogger().log(Level.WARNING, "[GRAPES] |-> " + moduleFilePath.toURI().toString());
                return;
            }

            // Keep the Json file in the build history
            if(build.isBuilding()){
                final FilePath reportFile = GrapesPlugin.getBuildReportFile(build);
                moduleFilePath.copyTo(reportFile);
                notification.setMimePath(reportFile);
            }

            final Module module = GrapesPlugin.getModule(moduleFilePath);

            // Post the module
            client.postModule(module, user, password);

            //discard old resend action if matches moduleName moduleVersion notification type
            discardOldResend(notification, build.getProject());

            // Generate build action with the dependency report
            final GrapesBuildAction buildAction = new GrapesBuildAction(module, client);

            if (buildAction.isInitOk()) {
                build.addAction(buildAction);
            }
        } catch (Exception e){
            GrapesPlugin.getLogger().log(Level.SEVERE, "[GRAPES] An error occurred during module post ", e);
            discardOldResend(notification, build.getProject());
            saveNotification(notification, build);
            throw new GrapesCommunicationException(500);
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
