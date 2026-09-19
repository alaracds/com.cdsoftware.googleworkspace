package com.cdsoftware.googleworkspace.chat;

public class GoogleChatEvent {

    public Chat chat;

    public static class Chat {
        public User user;
        public MessagePayload messagePayload;
    }

    public static class User {
        public String name;
        public String displayName;
        public String email;
        public String type;
    }

    public static class MessagePayload {
        public Space space;
        public Message message;
    }

    public static class Space {
        public String name;
        public String type;
        public String spaceType;
    }

    public static class Message {
        public String name;
        public String text;
        public String argumentText;
    }
}