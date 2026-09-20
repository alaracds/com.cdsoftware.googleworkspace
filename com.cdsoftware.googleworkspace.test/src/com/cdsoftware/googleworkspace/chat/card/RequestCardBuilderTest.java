package com.cdsoftware.googleworkspace.chat.card;

import static com.cdsoftware.googleworkspace.test.util.ReflectionTestUtil.setFieldValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.compiere.model.MRequest;
import org.compiere.model.POInfo;
import org.compiere.util.CLogger;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class RequestCardBuilderTest {

    @Test
    void buildsRequestDetailCardAndUpdatesAction()
            throws ReflectiveOperationException {
        MRequest request = mock(MRequest.class);
        when(request.getDocumentNo()).thenReturn("REQ-42");
        when(request.getSummary()).thenReturn("Cannot approve invoice");
        when(request.getR_Request_ID()).thenReturn(42);
        prepareCreatedAccess(request);

        JsonObject card = new RequestCardBuilder().build(request);

        assertThat(card.getAsJsonObject("header").get("title").getAsString())
                .isEqualTo("Solicitud REQ-42");

        JsonArray widgets = card.getAsJsonArray("sections")
                .get(0).getAsJsonObject()
                .getAsJsonArray("widgets");

        assertThat(widgets.get(1).getAsJsonObject()
                .getAsJsonObject("textParagraph").get("text").getAsString())
                .isEqualTo("Cannot approve invoice");
        assertThat(widgets.get(2).getAsJsonObject()
                .getAsJsonObject("decoratedText").get("text").getAsString())
                .isEqualTo("-");

        JsonObject action = widgets.get(3).getAsJsonObject()
                .getAsJsonObject("buttonList")
                .getAsJsonArray("buttons").get(0).getAsJsonObject()
                .getAsJsonObject("onClick")
                .getAsJsonObject("action");

        assertThat(action.getAsJsonArray("parameters").toString())
                .contains("requestUpdates")
                .contains("42");
    }

    @Test
    void substitutesMissingSummaryAndCreatedDate()
            throws ReflectiveOperationException {
        MRequest request = mock(MRequest.class);
        when(request.getDocumentNo()).thenReturn("REQ-EMPTY");
        prepareCreatedAccess(request);

        JsonObject card = new RequestCardBuilder().build(request);
        JsonArray widgets = card.getAsJsonArray("sections")
                .get(0).getAsJsonObject()
                .getAsJsonArray("widgets");

        assertThat(widgets.get(1).getAsJsonObject()
                .getAsJsonObject("textParagraph").get("text").getAsString())
                .isEqualTo("-");
        assertThat(widgets.get(2).getAsJsonObject()
                .getAsJsonObject("decoratedText").get("text").getAsString())
                .isEqualTo("-");
    }

    private void prepareCreatedAccess(MRequest request)
            throws ReflectiveOperationException {
        POInfo poInfo = mock(POInfo.class);
        when(poInfo.getColumnIndex("Created")).thenReturn(0);

        setFieldValue(request, "p_info", poInfo);
        setFieldValue(request, "log", mock(CLogger.class));
    }
}
