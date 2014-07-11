package org.axway.grapes.jenkins.resend.json;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import hudson.FilePath;
import org.axway.grapes.jenkins.notifications.GrapesNotification;
import org.axway.grapes.jenkins.resend.ResendBuildAction;

import java.io.File;
import java.io.IOException;

/**
 * Resend Build Action Deserializer
 *
 * <p>Manages ResendBuildAction Json de-serialization</p>
 */
public class ResendBuildActionDeserializer extends JsonDeserializer<ResendBuildAction> {
    @Override
    public ResendBuildAction deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {

        final ObjectCodec oc = jsonParser.getCodec();
        final JsonNode node = oc.readTree(jsonParser);

        FilePath mimePath = null;
        if(node.get("mimePath") != null){
            final String serializedMimePath = node.get("mimePath").asText();
            mimePath = new FilePath(new File(serializedMimePath));
        }

        final String notificationAction = node.get("notificationAction").asText();
        final String moduleName = node.get("moduleName").asText();
        final String moduleVersion = node.get("moduleVersion").asText();

        if(mimePath == null){
            return new ResendBuildAction(GrapesNotification.NotificationType.valueOf(notificationAction),mimePath , moduleName, moduleVersion);
        }

        return new ResendBuildAction(GrapesNotification.NotificationType.valueOf(notificationAction),mimePath, moduleName, moduleVersion);

    }
}
