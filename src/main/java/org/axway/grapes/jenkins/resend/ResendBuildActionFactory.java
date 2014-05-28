package org.axway.grapes.jenkins.resend;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Run;
import hudson.model.TransientBuildActionFactory;
import org.axway.grapes.commons.utils.FileUtils;
import org.axway.grapes.jenkins.GrapesPlugin;
import org.axway.grapes.jenkins.notifications.NotificationHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Resend Build Action Factory
 *
 * <p>Generates Resend Actions for builds that need one</p>
 *
 * @author jdcoffre
 */
@Extension
public class ResendBuildActionFactory extends TransientBuildActionFactory{

    /**
     * Creates resend-actions for the given build.
     *
     * @param target for which the action objects are requested. Never null.
     * @return Can be empty but must not be null.
     */
    @Override
    public Collection<? extends Action> createFor(Run target) {
        if (target instanceof AbstractBuild ) {
            final AbstractBuild<?, ?> build = (AbstractBuild)target;
            return getResendAction(build);
        }
        else {
            return Collections.emptyList();
        }
    }

    /**
     * Return the list of ResendAction of a build
     *
     * @param build AbstractBuild<?,?>
     * @return List<ResendBuildAction>
     */
    private  List<ResendBuildAction> getResendAction(final AbstractBuild<?,?> build) {
        final List<ResendBuildAction> resendActions = new ArrayList<ResendBuildAction>();

        try{
            final File reportFolder = new File(GrapesPlugin.getBuildReportFolder(build).toURI());
            if(reportFolder.exists()){

                // list all files in the Grapes folder
                for(File file : reportFolder.listFiles()){

                    // checks those that have a resend suffix
                    if(file.getName().endsWith(NotificationHandler.TO_RESEND_SUFFIX)){

                        // load the object from the file
                        final String serializedNotif = FileUtils.read(file);final ObjectMapper mapper = new ObjectMapper();
                        mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
                        resendActions.add(mapper.readValue(serializedNotif,ResendBuildAction.class));
                    }
                }
            }

        }catch (Exception e){
            GrapesPlugin.getLogger().log(Level.SEVERE, "[GRAPES] Failed to generate a resend action instance ", e);
        }

        return resendActions;
    }

}
