package org.axway.grapes.jenkins.resend;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import org.axway.grapes.jenkins.config.GrapesConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resend Project Action
 *
 * <p>Gathers all the resend action to perform on a project.
 * This action will not be displayed in the project view because notification resend are performed from the administration panel.</p>
 *
 * @author jdcoffre
 */
public class ResendProjectAction implements Action {

    private final Map<AbstractBuild<?,?> , List<ResendBuildAction>> resendActionPerBuild;
    private final GrapesConfig config;

    public ResendProjectAction(final Map<AbstractBuild<?,?>, List<ResendBuildAction>> resendBuildActions, final GrapesConfig config) {
        this.resendActionPerBuild = resendBuildActions;
        this.config = config;
    }

    // Hide the build action
    public String getIconFileName() {
        return null;
    }

    // Hide the build action
    public String getDisplayName() {
        return null;
    }

    // Hide the build action
    public String getUrlName() {
        return null;
    }

    public GrapesConfig getConfig() {
        return config;
    }

    public Map<AbstractBuild<?,?> , List<ResendBuildAction>> getResendActionPerBuild() {
        return resendActionPerBuild;
    }

    /**
     * Returns the list of modules information
     *
     * @return Map<String, String>
     */
    public Map<String, String> getModulesInfo() {
        final List<ResendBuildAction> resendBuildActions = new ArrayList<ResendBuildAction>();
        for(List<ResendBuildAction> buildActions: resendActionPerBuild.values()){
            resendBuildActions.addAll(buildActions);
        }

        final Map<String, String> modulesInfo = new HashMap<String, String>();
        for(ResendBuildAction action: resendBuildActions){
            modulesInfo.put(action.moduleName(), action.moduleVersion());
        }
        return modulesInfo;
    }
}
