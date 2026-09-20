package com.cdsoftware.googleworkspace.chat.card;

import java.util.List;

import org.compiere.model.MRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class RequestsCardBuilder {

    private static final String CHAT_ENDPOINT =
            "https://TU-TUNNEL.trycloudflare.com/api/v1/google/chat";

    public JsonObject build(
            String title,
            List<MRequest> requests) {

        JsonObject card = new JsonObject();

        JsonObject header = new JsonObject();
        header.addProperty("title", title);
        header.addProperty(
                "subtitle",
                requests.size() + " solicitudes");

        card.add("header", header);

        JsonArray sections = new JsonArray();

        if (requests.isEmpty()) {

            JsonObject section = new JsonObject();
            JsonArray widgets = new JsonArray();

            widgets.add(
                    createTextParagraph(
                            "No se encontraron solicitudes."));

            section.add("widgets", widgets);
            sections.add(section);

        } else {

            for (MRequest request : requests) {

                JsonObject section = new JsonObject();
                JsonArray widgets = new JsonArray();

                widgets.add(
                        createDecoratedText(
                                request.getDocumentNo(),
                                request.getSummary()));

                widgets.add(
                        createButton(
                                "Ver detalle",
                                request.getR_Request_ID()));

                section.add("widgets", widgets);
                sections.add(section);
            }
        }

        card.add("sections", sections);

        return card;
    }

    private JsonObject createDecoratedText(
            String label,
            String text) {

        JsonObject decoratedText =
                new JsonObject();

        decoratedText.addProperty(
                "topLabel",
                label);

        decoratedText.addProperty(
                "text",
                text != null ? text : "-");

        decoratedText.addProperty(
                "wrapText",
                true);

        JsonObject widget =
                new JsonObject();

        widget.add(
                "decoratedText",
                decoratedText);

        return widget;
    }

    private JsonObject createTextParagraph(
            String text) {

        JsonObject textParagraph =
                new JsonObject();

        textParagraph.addProperty(
                "text",
                text);

        JsonObject widget =
                new JsonObject();

        widget.add(
                "textParagraph",
                textParagraph);

        return widget;
    }

    private JsonObject createButton(
            String text,
            int requestId) {

        JsonObject action =
                new JsonObject();

        action.addProperty(
                "function",
                CHAT_ENDPOINT);

        JsonArray parameters =
                new JsonArray();

        JsonObject actionParameter =
                new JsonObject();

        actionParameter.addProperty(
                "key",
                "action");

        actionParameter.addProperty(
                "value",
                "requestDetail");

        parameters.add(
                actionParameter);

        JsonObject requestParameter =
                new JsonObject();

        requestParameter.addProperty(
                "key",
                "requestId");

        requestParameter.addProperty(
                "value",
                String.valueOf(requestId));

        parameters.add(
                requestParameter);

        action.add(
                "parameters",
                parameters);

        JsonObject onClick =
                new JsonObject();

        onClick.add(
                "action",
                action);

        JsonObject button =
                new JsonObject();

        button.addProperty(
                "text",
                text);

        button.add(
                "onClick",
                onClick);

        JsonArray buttons =
                new JsonArray();

        buttons.add(button);

        JsonObject buttonList =
                new JsonObject();

        buttonList.add(
                "buttons",
                buttons);

        JsonObject widget =
                new JsonObject();

        widget.add(
                "buttonList",
                buttonList);

        return widget;
    }
}