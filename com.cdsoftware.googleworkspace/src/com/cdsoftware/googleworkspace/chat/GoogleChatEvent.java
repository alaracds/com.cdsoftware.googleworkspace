package com.cdsoftware.googleworkspace.chat;

import java.util.Map;

public class GoogleChatEvent {

    public Chat chat;
    public CommonEventObject commonEventObject;

    public static class Chat {
        public User user;
        public MessagePayload messagePayload;
        public AddedToSpacePayload addedToSpacePayload;
        public ButtonClickedPayload buttonClickedPayload;
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

    public static class AddedToSpacePayload {
        public Space space;
        public boolean interactionAdd;
    }

    public static class CommonEventObject {
        public Map<String, String> parameters;
    }

    public static class ButtonClickedPayload {
        public Space space;
        public Message message;
    }

}