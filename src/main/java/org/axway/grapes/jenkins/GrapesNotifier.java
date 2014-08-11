package org.axway.grapes.jenkins;


import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.maven.AbstractMavenProject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.axway.grapes.commons.utils.JsonUtils;
import org.axway.grapes.jenkins.config.GrapesConfig;
import org.axway.grapes.jenkins.notifications.GrapesNotification;
import org.axway.grapes.jenkins.notifications.GrapesNotificationDescriptor;
import org.axway.grapes.jenkins.notifications.NotificationHandler;
import org.axway.grapes.jenkins.notifications.buildinfo.BuildInfoNotification;
import org.axway.grapes.utils.client.GrapesClient;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Grapes Notifier
 *
 * <p>Handle the notification to Grapes server. It gets the report from the build, it perform the notification to the Grapes server.
 * It handles also the resend actions creation in case notification failure.</p>
 *
 * @author jdcoffre
 */
public class GrapesNotifier extends Notifier {

    // Name of current Grapes configuration
    public String configName;

    // Manage the reports of Grapes Maven plugin
    public Boolean manageGrapesMavenPlugin = false;

    // Manage the Build info
    private boolean manageBuildInfo;

    public Boolean getManageGrapesMavenPlugin() {
        return manageGrapesMavenPlugin;
    }

    public boolean getManageBuildInfo() {
        return manageBuildInfo;
    }

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public GrapesNotifier(final String configName, final boolean manageGrapesMavenPlugin, final boolean manageBuildInfo) {
        this.configName = configName;
        this.manageGrapesMavenPlugin = manageGrapesMavenPlugin;
        this.manageBuildInfo = manageBuildInfo;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.STEP;
    }

    /**
     * Performs grapes notification, at the end of the build.
     *
     * @param build    AbstractBuild<?, ?>
     * @param launcher Launcher
     * @param listener BuildListener
     * @return boolean
     */
    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) {
        // No Publication for failed builds
        if (build.getResult() == null ||
                build.getResult().isWorseThan(Result.SUCCESS)) {
            listener.getLogger().println("[GRAPES] Skipping notification to Grapes because the result of this build is worth than success.");
            return true;
        }

        final PrintStream logger = listener.getLogger();

        // Save Grapes Maven report in build folder if necessary
        if(manageGrapesMavenPlugin){
            try {
                final FilePath moduleRoot = build.getModuleRoot();
                final FilePath reportFile = moduleRoot.child("target/" + GrapesPlugin.GRAPES_WORKING_FOLDER + "/" + GrapesPlugin.GRAPES_MODULE_FILE);

                // No module file, it should be a configuration error
                if(reportFile.exists()){
                    final FilePath savedReport = GrapesPlugin.getBuildModuleFile(build);
                    reportFile.copyTo(savedReport);

                }
                else{
                    logger.println("[GRAPES] Grapes Maven plugin report not found. Check that you still need to send Grapes Maven plugin reports for this job or contact your Grapes administrator.");
                    GrapesPlugin.getLogger().log(Level.WARNING, "[GRAPES] |-> " + reportFile.toURI().toString());
                }
            } catch (Exception e) {
                logger.println("[GRAPES] Grapes Maven plugin report backup aborted.");
                GrapesPlugin.getLogger().log(Level.SEVERE, "[GRAPES] Failed to save the Maven report file into the build folder." , e);
            }
        }

        // Generate build info if necessary
        if(manageBuildInfo){
            try{
                final Map<String, String> buildInfo = BuildInfoNotification.getBuildInfo(build, listener);
                final FilePath buildInfoFile = GrapesPlugin.getBuildBuildInfoFile(build);
                buildInfoFile.write(JsonUtils.serialize(buildInfo), null);
            } catch (Exception e) {
                logger.println("[GRAPES] Grapes build info generation aborted.");
                GrapesPlugin.getLogger().log(Level.SEVERE, "[GRAPES] Failed to generate build info file into the build folder.", e);
            }
        }

        final List<GrapesNotification> notifications = getAllNotifications(build);
        if(notifications.isEmpty()){
            logger.println("[GRAPES] No Grapes notification to send.");
            return true;
        }

        final GrapesConfig config = getConfig();
        logger.println("[GRAPES] Connection to Grapes");
        logger.println("[GRAPES] Host: " + config.getHost());
        logger.println("[GRAPES] Port: " + config.getPort());

        final NotificationHandler notifHandler = new NotificationHandler(config);
        for(int i = 0 ; i < notifications.size() ; i++){
            try{
                notifHandler.send(notifications.get(i),build);
                int count = i+1;
                logger.println("[GRAPES] Grapes notification " + count + "/" + notifications.size() + " has been performed successfully");
            } catch (Exception e) {
                logger.println("[GRAPES] One Grapes notification has been postpone. Check your Grapes server configuration & if the Grapes server is available");
                GrapesPlugin.getLogger().log(Level.WARNING, "[GRAPES] Grapes Notification failed:", e);
            }
        }

        return true;
    }

    /**
     * Returns the current Grapes configuration of the job
     *
     * @return GrapesConfig
     */
    protected GrapesConfig getConfig() {
        final GrapesNotifierDescriptor descriptor = (GrapesNotifierDescriptor) getDescriptor();
        return descriptor.getConfiguration(configName);
    }

    /**
     * Returns all the Grapes notifications of a build (never null, empty list if there is none)
     *
     * @param build AbstractBuild<?, ?>
     * @return List<GrapesNotification>
     */
    private static List<GrapesNotification> getAllNotifications(final AbstractBuild<?, ?> build) {
        final List<GrapesNotification> notifications = new ArrayList<GrapesNotification>();

        for(GrapesNotificationDescriptor notificationDescriptor: GrapesNotificationDescriptor.all()){
            final GrapesNotification notification = notificationDescriptor.createAutoInstance(build);

            if(notification != null){
                notifications.add(notification);
            }
        }

        return notifications;
    }

    /**
     * Descriptor for {@link GrapesNotifier}. Used as a singleton.
     *
     * <p>
     * See <tt>src/main/resources/org/axway/grapes/jenkins/GrapesNotifier/*.jelly</tt>
     * for the actual HTML fragment for the config screen.
     */
    @Extension
    public static final class GrapesNotifierDescriptor extends BuildStepDescriptor<Publisher> {

        private volatile List<GrapesConfig> servers;

        public GrapesNotifierDescriptor() {
            load();
        }

        public List<GrapesConfig> getServers() {
            return servers;
        }

        public void setServers(final List<GrapesConfig> servers) {
            this.servers = servers;
        }

        /**
         * Returns true if this task is applicable to the given project.
         * Limit the use of the notifier to maven jobs for now...
         *
         * @param jobType Class<? extends AbstractProject>
         * @return boolean
         */
        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") final Class<? extends AbstractProject> jobType) {
            return AbstractMavenProject.class.isAssignableFrom(jobType) &&
                    servers != null && !servers.isEmpty();
        }

        /**
         * Returns a stored Grapes configuration regarding its name
         *
         * @param configName String
         * @return GrapesConfig
         */
        public GrapesConfig getConfiguration(final String configName) {
            for(GrapesConfig config : servers){
                if(config.getName().equals(configName)){
                    return config;
                }
            }

            GrapesPlugin.getLogger().severe("[GRAPES] No Grapes configuration for " + configName);
            return null;
        }

        @Override
        public boolean configure(final StaplerRequest req, final JSONObject json) {
            if(json.containsKey("servers")){
                req.bindJSON(this, json);
            }
            else{
                servers = null;
            }

            save();
            
            return true;
        }

        /**
         * Returns the display name of the task in the job config
         *
         * @return String
         */
        @Override
        public String getDisplayName() {
            return "Configure Grapes Notifications";
        }

        // Configuration Validation

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value String This parameter receives the value that the user has typed.
         * @return Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckName(@QueryParameter final String value) {
            if (value.length() == 0) {
                return FormValidation.error("Please set a name");
            }
            if (value.length() < 4) {
                return FormValidation.warning("Isn't the name too short?");
            }

            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation of the form field 'host'.
         *
         * @param value String This parameter receives the value that the user has typed.
         * @return Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckHost(@QueryParameter final String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("Please set a valid host");
            }
            return FormValidation.ok();
        }


        /**
         * Performs on-the-fly validation of the form field 'credentials'.
         *
         * @param host String
         * @param port String
         * @param timeout String
         * @return
         */
        public FormValidation doTestConnection(@QueryParameter final String host, @QueryParameter final String port, @QueryParameter final String timeout) {
            final GrapesClient client = new GrapesClient(host, port);

            if (client.isServerAvailable()) {
                return FormValidation.ok("Success.");
            }
            else {
                return FormValidation.warning("Server not reachable!");
            }

        }
    }
}
