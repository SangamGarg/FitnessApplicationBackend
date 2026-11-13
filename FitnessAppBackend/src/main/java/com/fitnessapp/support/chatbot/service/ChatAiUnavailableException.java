package com.fitnessapp.support.chatbot.service;

public class ChatAiUnavailableException extends ChatbotException {

    public ChatAiUnavailableException() {
        super("AI support is temporarily unavailable. Please try again later.");
    }
}
