package com.cdsoftware.googleworkspace.chat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

class GoogleChatEventTest {

    private final Gson gson = new Gson();

    @Test
    void deserializesMessageEvent() {
        String json = "{\"chat\":{\"user\":{\"name\":\"users/123\","
                + "\"displayName\":\"Ana\",\"email\":\"ana@example.com\"},"
                + "\"messagePayload\":{\"space\":{\"name\":\"spaces/AAA\"},"
                + "\"message\":{\"text\":\"@iDempiere /solicitudes\","
                + "\"argumentText\":\"/solicitudes\"}}}}";

        GoogleChatEvent event = gson.fromJson(json, GoogleChatEvent.class);

        assertThat(event.chat.user.name).isEqualTo("users/123");
        assertThat(event.chat.user.displayName).isEqualTo("Ana");
        assertThat(event.chat.messagePayload.space.name).isEqualTo("spaces/AAA");
        assertThat(event.chat.messagePayload.message.argumentText)
                .isEqualTo("/solicitudes");
    }

    @Test
    void deserializesButtonActionParameters() {
        String json = "{\"chat\":{\"buttonClickedPayload\":{"
                + "\"space\":{\"name\":\"spaces/AAA\"}}},"
                + "\"commonEventObject\":{\"parameters\":{"
                + "\"action\":\"requestDetail\",\"requestId\":\"100\"}}}";

        GoogleChatEvent event = gson.fromJson(json, GoogleChatEvent.class);

        assertThat(event.chat.buttonClickedPayload.space.name)
                .isEqualTo("spaces/AAA");
        assertThat(event.commonEventObject.parameters)
                .containsEntry("action", "requestDetail")
                .containsEntry("requestId", "100");
    }

    @Test
    void deserializesAddedToSpaceEvent() {
        String json = "{\"chat\":{\"addedToSpacePayload\":{"
                + "\"space\":{\"name\":\"spaces/NEW\",\"spaceType\":\"SPACE\"},"
                + "\"interactionAdd\":true}}}";

        GoogleChatEvent event = gson.fromJson(json, GoogleChatEvent.class);

        assertThat(event.chat.addedToSpacePayload.space.name)
                .isEqualTo("spaces/NEW");
        assertThat(event.chat.addedToSpacePayload.space.spaceType)
                .isEqualTo("SPACE");
        assertThat(event.chat.addedToSpacePayload.interactionAdd).isTrue();
    }
}
