package com.cdsoftware.googleworkspace.chat;

public class GoogleChatCommand {

    private final String command;
    private final String arguments;

    public GoogleChatCommand(String text) {

        if (text == null || !text.trim().startsWith("/")) {
            this.command = null;
            this.arguments = null;
            return;
        }

        String value = text.trim();
        String[] parts = value.split("\\s+", 2);

        this.command = parts[0].substring(1).toLowerCase();
        this.arguments = parts.length > 1
                ? parts[1].trim()
                : "";
    }

    public boolean isCommand() {
        return command != null;
    }

    public String getCommand() {
        return command;
    }

    public String getArguments() {
        return arguments;
    }
}