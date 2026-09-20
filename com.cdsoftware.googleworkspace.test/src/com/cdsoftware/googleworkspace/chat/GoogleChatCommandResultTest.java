package com.cdsoftware.googleworkspace.chat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

class GoogleChatCommandResultTest {

    @Test
    void createsTextResultWithoutCard() {
        GoogleChatCommandResult result =
                GoogleChatCommandResult.text("Solicitud no encontrada");

        assertThat(result.getText()).isEqualTo("Solicitud no encontrada");
        assertThat(result.getCard()).isNull();
        assertThat(result.hasCard()).isFalse();
    }

    @Test
    void createsCardResultWithoutText() {
        JsonObject card = new JsonObject();
        card.addProperty("title", "Solicitudes abiertas");

        GoogleChatCommandResult result =
                GoogleChatCommandResult.card(card);

        assertThat(result.getText()).isNull();
        assertThat(result.getCard()).isSameAs(card);
        assertThat(result.hasCard()).isTrue();
    }
}
