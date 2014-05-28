package org.axway.grapes.jenkins.resend;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Run;
import hudson.model.TransientBuildActionFactory;
import org.axway.grapes.jenkins.GrapesPlugin;
import org.axway.grapes.jenkins.notifications.GrapesNotification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
        if (target instanceof AbstractBuild) {
            final AbstractBuild<?, ?> build = (AbstractBuild)target;
            List<ResendBuildAction> resendActions = new ArrayList<ResendBuildAction>();

            for(GrapesNotification notification: GrapesPlugin.getAllNotifications(build)){
                if(!GrapesPlugin.sent(notification, build)){
                    resendActions.add(new ResendBuildAction(notification));
                }
            }
            return resendActions;
        }
        else {
            return Collections.emptyList();
        }
    }

}
