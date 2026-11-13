package com.fitnessapp.support.chatbot.service;

public class ChatConversationNotFoundException extends ChatbotException {

    public ChatConversationNotFoundException() {
        super("Conversation was not found");
    }
}
