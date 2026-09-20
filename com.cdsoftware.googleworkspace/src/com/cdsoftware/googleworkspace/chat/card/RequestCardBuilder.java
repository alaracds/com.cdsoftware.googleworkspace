package com.cdsoftware.googleworkspace.chat.card;

import java.text.SimpleDateFormat;

import org.compiere.model.MRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class RequestCardBuilder {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public JsonObject build(MRequest request) {

        JsonObject card = new JsonObject();

        /*
         * Header
         */
        JsonObject header = new JsonObject();

        header.addProperty(
                "title",
                "Solicitud " + request.getDocumentNo());

        header.addProperty(
                "subtitle",
                "iDempiere");

        card.add("header", header);

        /*
         * Sección principal
         */
        JsonArray sections = new JsonArray();

        JsonObject section = new JsonObject();
        JsonArray widgets = new JsonArray();

        /*
         * Resumen
         */
        widgets.add(
                createDecoratedText(
                        "Resumen",
                        ""));

        widgets.add(
                createTextParagraph(
                        request.getSummary(),
                        6));

        /*
         * Fecha de creación
         */
        String created = request.getCreated() != null
                ? DATE_FORMAT.format(request.getCreated())
                : "-";

        widgets.add(
                createDecoratedText(
                        "Creada",
                        created));

        widgets.add(
                createButton(
                        "Ver actualizaciones",
                        request.getR_Request_ID()));


        section.add("widgets", widgets);
        sections.add(section);

        card.add("sections", sections);

        return card;
    }

    private JsonObject createDecoratedText(
            String label,
            String text) {

        JsonObject widget = new JsonObject();
        JsonObject decoratedText = new JsonObject();

        decoratedText.addProperty(
                "topLabel",
                label);

        decoratedText.addProperty(
                "text",
                text != null ? text : "-");

        decoratedText.addProperty(
                "wrapText",
                true);

        widget.add(
                "decoratedText",
                decoratedText);

        return widget;
    }

    private JsonObject createTextParagraph(
            String text,
            int maxLines) {

        JsonObject widget = new JsonObject();
        JsonObject textParagraph = new JsonObject();

        textParagraph.addProperty(
                "text",
                text != null ? text : "-");

        textParagraph.addProperty(
                "maxLines",
                maxLines);

        widget.add(
                "textParagraph",
                textParagraph);

        return widget;
    }

    private JsonObject createButton(
            String text,
            int requestId) {

        JsonObject action = new JsonObject();

        // TEMPORAL PARA LA DEMO:
        // reemplaza esta URL por tu endpoint actual de Cloudflare
        action.addProperty(
                "function",
                "https://informal-enlargement-weight-pensions.trycloudflare.com/api/v1/google/chat");

        JsonArray parameters = new JsonArray();

        JsonObject actionParameter = new JsonObject();
        actionParameter.addProperty(
                "key",
                "action");
        actionParameter.addProperty(
                "value",
                "requestUpdates");

        parameters.add(actionParameter);

        JsonObject requestParameter = new JsonObject();
        requestParameter.addProperty(
                "key",
                "requestId");
        requestParameter.addProperty(
                "value",
                String.valueOf(requestId));

        parameters.add(requestParameter);

        action.add(
                "parameters",
                parameters);

        JsonObject onClick = new JsonObject();
        onClick.add(
                "action",
                action);

        JsonObject button = new JsonObject();
        button.addProperty(
                "text",
                text);
        button.add(
                "onClick",
                onClick);

        JsonArray buttons = new JsonArray();
        buttons.add(button);

        JsonObject buttonList = new JsonObject();
        buttonList.add(
                "buttons",
                buttons);

        JsonObject widget = new JsonObject();
        widget.add(
                "buttonList",
                buttonList);

        return widget;
    }
}
