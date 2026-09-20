package com.cdsoftware.googleworkspace.chat.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.compiere.model.MRequest;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class RequestsCardBuilderTest {

    @Test
    void buildsOneSectionAndDetailActionPerRequest() {
        MRequest first = request(10, "REQ-10", "First request");
        MRequest second = request(20, "REQ-20", null);

        JsonObject card = new RequestsCardBuilder().build(
                "Solicitudes abiertas",
                List.of(first, second));

        assertThat(card.getAsJsonObject("header").get("title").getAsString())
                .isEqualTo("Solicitudes abiertas");
        assertThat(card.getAsJsonObject("header").get("subtitle").getAsString())
                .isEqualTo("2 solicitudes");

        JsonArray sections = card.getAsJsonArray("sections");
        assertThat(sections.size()).isEqualTo(2);
        assertThat(sections.get(1).getAsJsonObject()
                .getAsJsonArray("widgets").get(0).getAsJsonObject()
                .getAsJsonObject("decoratedText").get("text").getAsString())
                .isEqualTo("-");
        assertThat(sections.get(0).toString())
                .contains("requestDetail")
                .contains("10");
    }

    @Test
    void buildsEmptyStateWhenThereAreNoRequests() {
        JsonObject card = new RequestsCardBuilder().build(
                "Solicitudes abiertas",
                Collections.emptyList());

        assertThat(card.getAsJsonObject("header").get("subtitle").getAsString())
                .isEqualTo("0 solicitudes");
        assertThat(card.getAsJsonArray("sections").toString())
                .contains("No se encontraron solicitudes.");
    }

    private MRequest request(int id, String documentNo, String summary) {
        MRequest request = mock(MRequest.class);
        when(request.getR_Request_ID()).thenReturn(id);
        when(request.getDocumentNo()).thenReturn(documentNo);
        when(request.getSummary()).thenReturn(summary);
        return request;
    }
}
