package com.fitnessapp.support.chatbot.exception;

public class ChatAiUnavailableException extends ChatbotException {

    public ChatAiUnavailableException() {
        super("AI support is temporarily unavailable. Please try again later.");
    }
}
