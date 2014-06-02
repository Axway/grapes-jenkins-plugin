package org.axway.grapes.jenkins.resend;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.FilePath;
import org.axway.grapes.commons.utils.FileUtils;
import org.axway.grapes.commons.utils.JsonUtils;
import org.axway.grapes.jenkins.notifications.GrapesNotification;
import org.junit.Test;

import java.io.File;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

public class ResendBuildActionTest {

    @Test
    public void checkResendBuildActionJsonSerialization(){
        final ResendBuildAction resendBuildAction = new ResendBuildAction(GrapesNotification.NotificationType.POST_MODULE, new FilePath(new File("test")), "moduleName", "moduleVersion");

        Exception execution = null;

        try{
            final String serializedAction = JsonUtils.serialize(resendBuildAction);
            final ObjectMapper mapper = new ObjectMapper();
            mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
            final ResendBuildAction newResendBuildAction = mapper.readValue(serializedAction,ResendBuildAction.class);

            assertEquals(resendBuildAction, newResendBuildAction);
        }catch (Exception e){
            execution = e;
        }

        assertNull(execution);

    }

    @Test
    public void checkResendBuildActionJsonSerializationWhilePathIsNull(){
        final ResendBuildAction resendBuildAction = new ResendBuildAction(GrapesNotification.NotificationType.POST_MODULE, null, "moduleName", "moduleVersion");

        Exception execution = null;

        try{
            final String serializedAction = JsonUtils.serialize(resendBuildAction);
            final ObjectMapper mapper = new ObjectMapper();
            mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
            final ResendBuildAction newResendBuildAction = mapper.readValue(serializedAction,ResendBuildAction.class);

            assertEquals(resendBuildAction, newResendBuildAction);
        }catch (Exception e){
            execution = e;
        }

        assertNull(execution);

    }
}
