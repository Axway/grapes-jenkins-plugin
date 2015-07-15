package org.axway.grapes.jenkins.notifications.buildinfo;


import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.maven.MavenModuleSet;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.apache.commons.lang.StringUtils;
import org.axway.grapes.commons.datamodel.Module;
import org.axway.grapes.jenkins.GrapesNotifier;
import org.axway.grapes.jenkins.GrapesPlugin;
import org.axway.grapes.jenkins.notifications.GrapesNotification;
import org.axway.grapes.jenkins.notifications.GrapesNotificationDescriptor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * Build Info Notification
 *
 * <p>Default implementation of the build info. This notification complete the module information with build information: SCM, Jenkins & Build configuration</p>
 *
 * @author jdcoffre
 */
public class BuildInfoNotification extends GrapesNotification {

    private String moduleName;
    private String moduleVersion;
    private FilePath reportFile;

    @Override
    public NotificationType getNotificationAction() {
        return NotificationType.POST_MODULE_BUILD_INFO;
    }

    @Override
    public FilePath getMimePath() {
        return reportFile;
    }

    @Override
    public void setMimePath(final FilePath reportFile) {
        this.reportFile = reportFile;
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public String getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleName(final String moduleNames) {
        this.moduleName = moduleNames;
    }

    public void setModuleVersion(final String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }


    @Extension
    public static final class BuildInfoNotificationDescriptorImpl extends GrapesNotificationDescriptor {

        @Override
        public GrapesNotification createAutoInstance(final AbstractBuild<?, ?> build) {
            try{
                final GrapesNotifier notifier = GrapesPlugin.getGrapesNotifier(build.getProject());
                if(notifier == null || !notifier.getManageBuildInfo()){
                    return null;
                }

                final FilePath moduleFile = GrapesPlugin.getBuildModuleFile(build);
                final Module module = GrapesPlugin.getModule(moduleFile);

                final BuildInfoNotification notification = new BuildInfoNotification();
                notification.setModuleName(module.getName());
                notification.setModuleVersion(module.getVersion());
                notification.setMimePath(GrapesPlugin.getBuildBuildInfoFile(build));

                return notification;

            } catch (Exception e) {
                GrapesPlugin.getLogger().log(Level.SEVERE, "[GRAPES] Failed to get build Module buildInfo ", e);
            }

            return null;
        }
    }

    public static Map<String,String> getBuildInfo(final AbstractBuild<?, ?> build, final BuildListener listener) throws IOException, InterruptedException {
        final Map<String, String> buildInfo = new HashMap<String, String>();
        final EnvVars env = build.getEnvironment(listener);

        // Manage SCM info
        buildInfo.put(GrapesPlugin.BUILD_INFO_SCM_TYPE, getVcsType(env));
        buildInfo.put(GrapesPlugin.BUILD_INFO_SCM_URL, getVcsURL(env));
        buildInfo.put(GrapesPlugin.BUILD_INFO_SCM_REVISION, getVcsRevision(env));

        // Manage Jenkins info
        buildInfo.put(GrapesPlugin.BUILD_INFO_JENKINS_HOST, env.get("JENKINS_URL"));
        buildInfo.put(GrapesPlugin.BUILD_INFO_JENKINS_NODE, env.get("NODE_NAME"));
        buildInfo.put(GrapesPlugin.BUILD_INFO_JENKINS_JOB_URL, env.get("JOB_URL"));
        buildInfo.put(GrapesPlugin.BUILD_INFO_JENKINS_BUILD_URL, env.get("BUILD_URL"));

        // Manage date info
        final SimpleDateFormat time_format = new SimpleDateFormat(GrapesPlugin.BUILD_INFO_DATE_FORMAT);
        time_format.setTimeZone(TimeZone.getTimeZone(GrapesPlugin.BUILD_INFO_DATE_TIME_ZONE));
        final Date date = new Date();
        buildInfo.put(GrapesPlugin.BUILD_INFO_BUILD_DATE, time_format.format(date));

        // Manage Maven/JAVA info
        final MavenModuleSet project = (MavenModuleSet) build.getProject();
        buildInfo.put(GrapesPlugin.BUILD_INFO_MAVEN_GOAL, project.getGoals());
        buildInfo.put(GrapesPlugin.BUILD_INFO_MAVEN_OPTS, project.getMavenOpts());
        buildInfo.put(GrapesPlugin.BUILD_INFO_MAVEN_VERSION, project.getRootModule().getVersion());
        buildInfo.put(GrapesPlugin.BUILD_INFO_JAVA_HOME, env.get("JAVA_HOME"));

        return buildInfo;
    }

    private static String getVcsType(final EnvVars env) {
        if(env.get("SVN_REVISION") != null){
            return "SVN";
        }
        if(env.get("GIT_COMMIT") != null){
            return "Git";
        }

        return null;
    }

    private static String getVcsURL(final EnvVars env) {
        String url = env.get("SVN_URL");
        if (StringUtils.isBlank(url)) {
            url = publicGitUrl(env.get("GIT_URL"));
        }

        return url;
    }

    /*
    *   Git publish the repository credentials in the Url,
    *   this method will discard it.
    */
    private static String publicGitUrl(String gitUrl) {
        if (gitUrl != null && gitUrl.contains("https://") && gitUrl.contains("@")) {
            StringBuilder sb = new StringBuilder(gitUrl);
            int start = sb.indexOf("https://");
            int end = sb.indexOf("@") + 1;
            sb = sb.replace(start, end, StringUtils.EMPTY);

            return "https://" + sb.toString();
        }

        return gitUrl;
    }

    private static String getVcsRevision(final EnvVars env) {
        String revision = env.get("SVN_REVISION");
        if (StringUtils.isBlank(revision)) {
            revision = env.get("GIT_COMMIT");
        }
        return revision;
    }
}
