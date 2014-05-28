package org.axway.grapes.jenkins;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import org.axway.grapes.commons.datamodel.Module;
import org.axway.grapes.jenkins.config.GrapesConfig;
import org.axway.grapes.jenkins.notifications.GrapesNotification;
import org.axway.grapes.jenkins.reports.GrapesBuildAction;
import org.axway.grapes.jenkins.resend.ResendBuildAction;
import org.axway.grapes.jenkins.resend.ResendProjectAction;
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
        if(!client.isServerAvailable()){
            GrapesPlugin.getLogger().log(Level.SEVERE, "[GRAPES] Grapes server is not available ");
            throw new GrapesCommunicationException(404);
        }

        try {
            client.promoteModule(notification.moduleName(), notification.moduleVersion(), user, password);
            GrapesPlugin.markAsSent(notification, build);

            //discard old resend action if matches moduleName moduleVersion notification type
            discardOldResend(notification, build);

        } catch (Exception e) {
            GrapesPlugin.getLogger().log(Level.SEVERE, "[GRAPES] An error occurred during module promotion ", e);
            throw new GrapesCommunicationException(500);

        }
    }

    private void postModule(final GrapesNotification notification, final AbstractBuild<?, ?> build) throws GrapesCommunicationException {
        final FilePath moduleFilePath = notification.getMimePath();

        try{
            // No module file, it should be a configuration error
            if(!moduleFilePath.exists()){
                GrapesPlugin.getLogger().log(Level.WARNING,"[GRAPES] Grapes Maven plugin report does not exist.");
                GrapesPlugin.markAsSent(notification, build);
                return;
            }

            // Keep the Json file in the build history
            if(build.isBuilding()){
                final FilePath reportFile = GrapesPlugin.getBuildReportFile(build);
                moduleFilePath.copyTo(reportFile);
            }
        } catch (Exception e){
            GrapesPlugin.getLogger().log(Level.SEVERE, "[GRAPES] An error occurred during module post preparation ", e);
            throw new GrapesCommunicationException(500);
        }

        if(!client.isServerAvailable()){
            GrapesPlugin.getLogger().log(Level.SEVERE, "[GRAPES] Grapes server is not available ");
            throw new GrapesCommunicationException(404);
        }

        try{
            final Module module = GrapesPlugin.getModule(new File(String.valueOf(moduleFilePath)));
            final AbstractProject<?, ?> project = build.getParent();

            // Post the module
            client.postModule(module, user, password);
            GrapesPlugin.markAsSent(notification, build);

            //discard old resend action if matches moduleName moduleVersion notification type
            discardOldResend(notification, build);

            // Generate build action with the dependency report
            final GrapesBuildAction buildAction = new GrapesBuildAction(module, client);

            if (buildAction.isInitOk()) {
                build.addAction(buildAction);
            }
        } catch (Exception e){
            GrapesPlugin.getLogger().log(Level.SEVERE, "[GRAPES] An error occurred during module post ", e);
            throw new GrapesCommunicationException(500);
        }
    }

    /**
     * Discard old resend actions
     *
     * @param notification GrapesNotification
     * @param build AbstractBuild<?, ?>
     */
    private void discardOldResend(final GrapesNotification notification, final AbstractBuild<?, ?> build) {
        final ResendProjectAction resendProjectAction = GrapesPlugin.getAllResendActions(build.getProject());

        if(resendProjectAction == null){
            return;
        }

        for(Map.Entry<AbstractBuild<?,?>, List<ResendBuildAction>> resendActionPerBuild: resendProjectAction.getResendActionPerBuild().entrySet()){
            for(ResendBuildAction resendBuildAction: resendActionPerBuild.getValue()){
                if(resendBuildAction.equals(notification)){
                    GrapesPlugin.markAsSent(resendBuildAction, resendActionPerBuild.getKey());
                }
            }
        }
    }
}
