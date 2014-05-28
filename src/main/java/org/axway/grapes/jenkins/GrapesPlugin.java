package org.axway.grapes.jenkins;

import hudson.FilePath;
import hudson.Plugin;
import hudson.PluginWrapper;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Hudson;
import hudson.tasks.Publisher;
import org.axway.grapes.commons.datamodel.Module;
import org.axway.grapes.commons.utils.FileUtils;
import org.axway.grapes.commons.utils.JsonUtils;
import org.axway.grapes.jenkins.config.GrapesConfig;
import org.axway.grapes.jenkins.notifications.GrapesNotification;
import org.axway.grapes.jenkins.notifications.GrapesNotificationDescriptor;
import org.axway.grapes.jenkins.resend.ResendBuildAction;
import org.axway.grapes.jenkins.resend.ResendProjectAction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Placeholder for plugin entry point.
 * This class also acts as a container for methods used throughout the plugin.
 *
 * @author jdcoffre
 *
 */
public class GrapesPlugin extends Plugin {

    public static final String REPORT_FOLDER = "grapesReports";

    public static final String REPORT_FILE = REPORT_FOLDER + "/module.json";

    public static final String GRAPES_WORKING_FOLDER = "grapes";

    public static final String GRAPES_MODULE_FILE = "module.json";

    /**
     * Returns Grapes Jenkins plugin logger
     *
     * @return Logger
     */
    public static Logger getLogger(){
        return LogManager.getLogManager().getLogger("hudson.WebAppMain");
    }

    /**
     * Returns the path or URL to access web resources from this plugin.
     *
     * @return resource path
     */
    public static String getPluginResourcePath() {
        final PluginWrapper wrapper = Hudson.getInstance().getPluginManager()
                .getPlugin(GrapesPlugin.class);

        return "/plugin/" + wrapper.getShortName() + "/";
    }

    /**
     * Returns the build report folder for Grapes build archives
     *
     * @param build AbstractBuild
     * @return FilePath
     */
    public static FilePath getBuildReportFolder(final AbstractBuild<?, ?> build) {
        assert build != null;
        final File reportFile = new File(build.getRootDir(), REPORT_FOLDER);
        return  new FilePath(reportFile);
    }

    /**
     * Returns the build report file for Grapes module reports
     *
     * @param build AbstractBuild
     * @return FilePath
     */
    public static FilePath getBuildReportFile(final AbstractBuild<?, ?> build) {
        assert build != null;
        final File reportFile = new File(build.getRootDir(), REPORT_FILE);
        return  new FilePath(reportFile);
    }

    /**
     * Un-serialize a Module from Json file
     *
     * @param moduleFile File
     * @return Module
     * @throws IOException
     * @throws InterruptedException
     */
    public static Module getModule(final File moduleFile) throws IOException, InterruptedException {
        if (moduleFile.exists()) {
            final String serializedModule= FileUtils.read(moduleFile);
            return JsonUtils.unserializeModule(serializedModule);
        }

        return null;
    }


    /**
     * Returns the Grapes Notifier instance of a project (null if there is none)
     *
     * @param project AbstractProject<?, ?>
     * @return GrapesNotifier
     */
    public static GrapesNotifier getGrapesNotifier(final AbstractProject<?, ?> project){
        for(Publisher publisher : project.getPublishersList()){
            if(publisher instanceof GrapesNotifier){
               return (GrapesNotifier) publisher;
            }
        }

        return null;
    }


    /**
     * Returns the Grapes configuration of a project (null if there is none)
     *
     * @param project
     * @return
     */
    public static GrapesConfig getGrapesConfiguration(final AbstractProject<?, ?> project) {
        final GrapesNotifier notifier = getGrapesNotifier(project);
        return notifier == null ? null : notifier.getConfig();
    }

    /**
     * Returns all the Grapes notifications of a build (never null, empty list if there is none)
     *
     * @param build AbstractBuild<?, ?>
     * @return List<GrapesNotification>
     */
    public static List<GrapesNotification> getAllNotifications(final AbstractBuild<?, ?> build) {
        final List<GrapesNotification> notifications = new ArrayList<GrapesNotification>();

        for(GrapesNotificationDescriptor notifDescriptor: GrapesNotificationDescriptor.all()){
            final GrapesNotification notification = notifDescriptor.newAutoInstance(build);

            if(notification != null){
                notifications.add(notification);
            }
        }

        return notifications;
    }

    /**
     * Returns all the Grapes resend action of the build (never null, empty list if there is none)
     *
     * @param build AbstractBuild<?, ?>
     * @return List<ResendBuildAction
     */
    public static List<ResendBuildAction> getAllResendActions(final AbstractBuild<?, ?> build) {
        final List<ResendBuildAction> resendActions = new ArrayList<ResendBuildAction>();

        for(Action transientAction: build.getTransientActions()){

            if(transientAction instanceof ResendBuildAction){
                resendActions.add((ResendBuildAction)transientAction);
            }
        }

        return resendActions;
    }

    /**
     * Returns all the Grapes resend Action of a project (null if empty)
     *
     * @param project AbstractProject<?, ?>
     * @return ResendProjectAction
     */
    public static ResendProjectAction getAllResendActions(final AbstractProject<?, ?> project) {
        final List<ResendProjectAction> resendProjectActions = project.getActions(ResendProjectAction.class);

        final GrapesConfig config = GrapesPlugin.getGrapesConfiguration(project);
        final Map<AbstractBuild<?,?> , List<ResendBuildAction>> resendBuildActions = new HashMap<AbstractBuild<?, ?>, List<ResendBuildAction>>();
        for(Object run : project.getBuilds()){
            if(run instanceof AbstractBuild){
                final AbstractBuild<?,?> build = (AbstractBuild)run;
                resendBuildActions.put(build, GrapesPlugin.getAllResendActions(build));
            }
        }

        if(!resendBuildActions.isEmpty() &&  config != null){
            return new ResendProjectAction(resendBuildActions, config);
        }

        return null;
    }

    /**
     * Add info in Grapes folder to mark the notification as sent notification
     *
     * @param notification GrapesNotification
     * @param build AbstractBuild<?, ?>
     */
    public static void markAsSent(final GrapesNotification notification, final AbstractBuild<?, ?> build){
        try{
            final File reportFolder = new File(getBuildReportFolder(build).toURI());
            FileUtils.touch(reportFolder, getNotificationId(notification));
        }catch (Exception e){
            GrapesPlugin.getLogger().log(Level.SEVERE, "[GRAPES] Failed to mark notification as sent: ", e);
        }
    }

    /**
     * Checks if a notification has been sent
     *
     * @param notification GrapesNotification
     * @param build AbstractBuild<?, ?>
     * @return boolean
     */
    public static boolean sent(final GrapesNotification notification, final AbstractBuild<?, ?> build){
        try {
            final File reportFolder = new File(getBuildReportFolder(build).toURI());

            if(reportFolder.exists()){
                final File sentFile = new File(reportFolder, getNotificationId(notification));
                return sentFile.exists();
            }
        }catch (Exception e){
            GrapesPlugin.getLogger().log(Level.SEVERE, "[GRAPES] Failed check is notification has been sent: ", e);
        }

        return false;
    }

    private static String getNotificationId(final GrapesNotification notification) {
        assert notification != null;

        final StringBuilder sb = new StringBuilder();
        sb.append('.');
        sb.append(notification.moduleName());
        sb.append('-');
        sb.append(notification.moduleVersion());
        sb.append('-');
        sb.append(notification.getNotificationAction());
        sb.append("-sent");

        return sb.toString();
    }
}
