package com.cdsoftware.googleworkspace.chat.card;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MRequest;
import org.compiere.model.MRequestUpdate;
import org.compiere.model.MUser;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class RequestUpdatesCardBuilder {

    public JsonObject build(
            Properties ctx,
            MRequest request,
            List<MRequestUpdate> updates) {

        JsonObject card = new JsonObject();

        JsonObject header = new JsonObject();
        header.addProperty(
                "title",
                "Actualizaciones de solicitud "
                        + request.getDocumentNo());
        header.addProperty(
                "subtitle",
                updates.size() + " actualizaciones");

        card.add("header", header);

        JsonArray sections = new JsonArray();

        if (updates.isEmpty()) {

            JsonObject section = new JsonObject();
            JsonArray widgets = new JsonArray();

            widgets.add(
                    createTextParagraph(
                            "Esta solicitud no tiene actualizaciones."));

            section.add("widgets", widgets);
            sections.add(section);

        } else {

            for (MRequestUpdate update : updates) {

                JsonObject section = new JsonObject();
                JsonArray widgets = new JsonArray();

                String created = "-";

                if (update.getCreated() != null) {
                    created = new SimpleDateFormat(
                            "dd/MM/yyyy HH:mm")
                            .format(update.getCreated());
                }

                MUser createdBy =
                        MUser.get(
                                ctx,
                                update.getCreatedBy());

                String userName =
                        createdBy != null
                                ? createdBy.getName()
                                : "-";

                widgets.add(
                        createDecoratedText(
                                created,
                                userName));

                String result =
                        update.get_ValueAsString("Result");

                widgets.add(
                        createTextParagraph(
                                result != null
                                        && !result.isBlank()
                                                ? result
                                                : "-"));

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
                text != null ? text : "-");

        JsonObject widget =
                new JsonObject();

        widget.add(
                "textParagraph",
                textParagraph);

        return widget;
    }
}