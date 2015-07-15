package org.axway.grapes.jenkins;

import hudson.FilePath;
import hudson.Plugin;
import hudson.PluginWrapper;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.tasks.Publisher;
import org.axway.grapes.commons.datamodel.Module;
import org.axway.grapes.commons.utils.JsonUtils;
import org.axway.grapes.jenkins.config.GrapesConfig;

import java.io.File;
import java.io.IOException;
import java.util.Map;
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

    public static final String MODULE_REPORT_FILE = "module.json";

    public static final String BUILD_INFO_REPORT_FILE = "buildInfo.json";

    public static final String BUILD_INFO_SCM_TYPE = "scm-type";

    public static final String BUILD_INFO_SCM_URL = "scm-url";

    public static final String BUILD_INFO_SCM_REVISION = "scm-revision";

    public static final String BUILD_INFO_JENKINS_HOST = "jenkins-host";

    public static final String BUILD_INFO_JENKINS_NODE = "jenkins-node";

    public static final String BUILD_INFO_JENKINS_JOB_URL = "jenkins-job-url";

    public static final String BUILD_INFO_JENKINS_BUILD_URL = "jenkins-build-url";

    public static final String BUILD_INFO_MAVEN_VERSION = "maven-version";

    public static final String BUILD_INFO_MAVEN_GOAL = "maven-goal";

    public static final String BUILD_INFO_MAVEN_OPTS = "maven-options";

    public static final String BUILD_INFO_JAVA_HOME = "java-home";

    public static final String BUILD_INFO_BUILD_DATE = "build-date";

    public static final String BUILD_INFO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssz";

    public static final String BUILD_INFO_DATE_TIME_ZONE = "GMT";

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
     * Returns the module build report file path
     *
     * @param build AbstractBuild
     * @return FilePath
     */
    public static FilePath getBuildModuleFile(final AbstractBuild<?, ?> build) {
        final FilePath reportFolder = getBuildReportFolder(build);
        return reportFolder.child(MODULE_REPORT_FILE);
    }

    /**
     * Returns the buildInfo build report file path
     *
     * @param build AbstractBuild
     * @return FilePath
     */
    public static FilePath getBuildBuildInfoFile(final AbstractBuild<?, ?> build) {
        final FilePath reportFolder = getBuildReportFolder(build);
        return reportFolder.child(BUILD_INFO_REPORT_FILE);
    }

    /**
     * Un-serialize a Module from Json file
     *
     * @param moduleFile File
     * @return Module
     * @throws IOException
     * @throws InterruptedException
     */
    public static Module getModule(final FilePath moduleFile) throws IOException, InterruptedException {
        if (moduleFile.exists()) {
            final String serializedModule= moduleFile.readToString();
            return JsonUtils.unserializeModule(serializedModule);
        }

        getLogger().severe("[GRAPES] Wrong module report path: " + moduleFile.toURI().getPath());
        throw new IOException("[GRAPES] Failed to get report.");
    }

    /**
     * Un-serialize a BuildInfo from Json file
     *
     * @param buildInfoFile File
     * @return Map<String,String>
     * @throws IOException
     * @throws InterruptedException
     */
    public static Map<String,String> getBuildInfo(final FilePath buildInfoFile) throws IOException, InterruptedException {
        if (buildInfoFile.exists()) {
            final String serializedBuildInfo= buildInfoFile.readToString();
            return JsonUtils.unserializeBuildInfo(serializedBuildInfo);
        }

        getLogger().severe("[GRAPES] Wrong buildInfo path: " + buildInfoFile.toURI().getPath());
        throw new IOException("[GRAPES] Failed to get build info.");
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
