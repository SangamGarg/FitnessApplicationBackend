package com.fitnessapp.support.repository;

import com.fitnessapp.support.models.SupportTicketEntity;
import com.fitnessapp.support.models.TicketCategory;
import com.fitnessapp.support.models.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicketEntity, Long> {

    Optional<SupportTicketEntity> findByTicketNumber(String ticketNumber);

    // For Android client user history
    List<SupportTicketEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    // For Dev / Admin portal with filtering & pagination
    Page<SupportTicketEntity> findAllByStatus(TicketStatus status, Pageable pageable);

    Page<SupportTicketEntity> findAllByCategory(TicketCategory category, Pageable pageable);
}