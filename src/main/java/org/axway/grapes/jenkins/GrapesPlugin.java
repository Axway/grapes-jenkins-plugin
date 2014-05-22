package org.axway.grapes.jenkins;

import hudson.FilePath;
import hudson.Plugin;
import hudson.PluginWrapper;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.tasks.Publisher;
import org.axway.grapes.commons.datamodel.Module;
import org.axway.grapes.commons.utils.FileUtils;
import org.axway.grapes.commons.utils.JsonUtils;
import org.axway.grapes.jenkins.notifications.GrapesNotification;
import org.axway.grapes.jenkins.notifications.GrapesNotificationDescriptor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    public static final String REPORT_FILE = "grapesReports/module.json";

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
     * Provides build report file for Grapes archives
     *
     * @param build AbstractBuild
     * @return FilePath
     */
    public static FilePath getBuildReportFile(final AbstractBuild<?, ?> build) {
        assert build != null;
        final File reportFolder = new File(build.getRootDir(), REPORT_FILE);
        return  new FilePath(reportFolder);
    }

    /**
     * Un-serialize a Module from Json file
     *
     * @param moduleFile
     * @return
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
     * Returns the Grapes Notifier instance of the build (null if there is none)
     *
     * @param build AbstractBuild<?, ?>
     * @return GrapesNotifier
     */
    public static GrapesNotifier getGrapesNotifier(final AbstractBuild<?, ?> build){
        AbstractProject<?, ?> project = build.getProject();

        for(Publisher publisher : project.getPublishersList()){
            if(publisher instanceof GrapesNotifier){
               return (GrapesNotifier) publisher;
            }
        }

        return null;
    }

    /**
     * Returns all the Grapes notifications of the build
     *
     * @param build
     * @return
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
}
