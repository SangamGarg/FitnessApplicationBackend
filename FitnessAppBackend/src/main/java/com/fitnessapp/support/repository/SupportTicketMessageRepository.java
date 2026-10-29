package com.fitnessapp.support.repository;

import com.fitnessapp.support.models.SupportTicketMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketMessageRepository extends JpaRepository<SupportTicketMessageEntity, Long> {

    List<SupportTicketMessageEntity> findAllByTicketTicketNumberAndInternalNoteFalseOrderByCreatedAtAsc(
            String ticketNumber
    );

    List<SupportTicketMessageEntity> findAllByTicketTicketNumberOrderByCreatedAtAsc(String ticketNumber);
}
