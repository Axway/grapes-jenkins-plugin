package org.axway.grapes.jenkins.resend.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.axway.grapes.jenkins.GrapesPlugin;
import org.axway.grapes.jenkins.resend.ResendBuildAction;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Resend Build Action Serializer
 *
 * <p>Manage ResendBuildAction JSON serialization</p>
 */
public class ResendBuildActionSerializer extends JsonSerializer<ResendBuildAction> {
    @Override
    public void serialize(final ResendBuildAction resendBuildAction, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
        try{
            jsonGenerator.writeStartObject();

            if(resendBuildAction.getMimePath() != null){
                jsonGenerator.writeStringField("mimePath", resendBuildAction.getMimePath().toURI().getPath());
            }

            if(resendBuildAction.getNotificationAction() != null){
                jsonGenerator.writeStringField("notificationAction", resendBuildAction.getNotificationAction().name());
            }

            jsonGenerator.writeStringField("moduleName", resendBuildAction.getModuleName());
            jsonGenerator.writeStringField("moduleVersion", resendBuildAction.getModuleVersion());
            jsonGenerator.writeEndObject();
            jsonGenerator.flush();

        }catch (Exception e){
            GrapesPlugin.getLogger().log(Level.SEVERE, "[GRAPES] Failed to serialized a resend action ", e);
        }

    }
}