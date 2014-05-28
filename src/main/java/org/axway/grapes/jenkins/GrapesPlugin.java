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
import org.axway.grapes.jenkins.config.GrapesConfig;

import java.io.File;
import java.io.IOException;
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

}
