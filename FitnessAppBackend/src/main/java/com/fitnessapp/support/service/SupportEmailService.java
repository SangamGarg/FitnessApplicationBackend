package com.fitnessapp.support.service;


import com.fitnessapp.support.models.entity.SupportTicketEntity;

public interface SupportEmailService {
    void sendTicketConfirmationEmail(SupportTicketEntity ticket);

    void sendTicketResolutionEmail(SupportTicketEntity ticket);
}