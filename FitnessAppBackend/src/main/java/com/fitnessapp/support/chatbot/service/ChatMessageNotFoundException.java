package com.fitnessapp.support.chatbot.service;

public class ChatMessageNotFoundException extends ChatbotException {

    public ChatMessageNotFoundException() {
        super("Message was not found");
    }
}
