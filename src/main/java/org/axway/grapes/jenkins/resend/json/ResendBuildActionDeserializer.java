package org.axway.grapes.jenkins.resend.json;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
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
    public ResendBuildAction deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException, JsonProcessingException {

        final ObjectCodec oc = jsonParser.getCodec();
        final JsonNode node = oc.readTree(jsonParser);

        final String mimePath = node.get("mimePath").asText();
        final String notificationAction = node.get("notificationAction").asText();
        final String moduleName = node.get("moduleName").asText();
        final String moduleVersion = node.get("moduleVersion").asText();

        return new ResendBuildAction(GrapesNotification.NotificationType.valueOf(notificationAction),new FilePath(new File(mimePath)), moduleName, moduleVersion);
    }
}
