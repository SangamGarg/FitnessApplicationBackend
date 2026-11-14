package com.fitnessapp.support.chatbot.exception;

public class ChatConversationNotFoundException extends ChatbotException {

    public ChatConversationNotFoundException() {
        super("Conversation was not found");
    }
}
