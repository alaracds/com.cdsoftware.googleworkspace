package com.cdsoftware.googleworkspace.chat.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Properties;

import org.compiere.model.MRequest;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

class RequestUpdatesCardBuilderTest {

    @Test
    void buildsEmptyStateWithoutAccessingTheDatabase() {
        MRequest request = mock(MRequest.class);
        when(request.getDocumentNo()).thenReturn("REQ-42");

        JsonObject card = new RequestUpdatesCardBuilder().build(
                new Properties(),
                request,
                Collections.emptyList());

        assertThat(card.getAsJsonObject("header").get("title").getAsString())
                .isEqualTo("Actualizaciones de solicitud REQ-42");
        assertThat(card.getAsJsonObject("header").get("subtitle").getAsString())
                .isEqualTo("0 actualizaciones");
        assertThat(card.getAsJsonArray("sections").toString())
                .contains("Esta solicitud no tiene actualizaciones.");
    }
}
