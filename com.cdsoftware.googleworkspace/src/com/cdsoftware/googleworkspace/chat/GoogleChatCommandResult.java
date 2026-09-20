package com.cdsoftware.googleworkspace.chat;

import com.google.gson.JsonObject;

public class GoogleChatCommandResult {

    private final String text;
    private final JsonObject card;

    private GoogleChatCommandResult(
            String text,
            JsonObject card) {

        this.text = text;
        this.card = card;
    }

    public static GoogleChatCommandResult text(String text) {
        return new GoogleChatCommandResult(text, null);
    }

    public static GoogleChatCommandResult card(JsonObject card) {
        return new GoogleChatCommandResult(null, card);
    }

    public String getText() {
        return text;
    }

    public JsonObject getCard() {
        return card;
    }

    public boolean hasCard() {
        return card != null;
    }
}
