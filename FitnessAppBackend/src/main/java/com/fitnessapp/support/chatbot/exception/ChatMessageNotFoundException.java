package com.fitnessapp.support.chatbot.exception;

public class ChatMessageNotFoundException extends ChatbotException {

    public ChatMessageNotFoundException() {
        super("Message was not found");
    }
}
