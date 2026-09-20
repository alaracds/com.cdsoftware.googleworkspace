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
                        request.getSummary()));

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
}
