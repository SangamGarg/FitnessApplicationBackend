package com.fitnessapp.support.repo;

import com.fitnessapp.support.models.entity.SupportTicketEntity;
import com.fitnessapp.support.models.enums.SupportIssueCategory;
import com.fitnessapp.support.models.enums.TicketStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository
        extends JpaRepository<SupportTicketEntity, Long> {

    Optional<SupportTicketEntity> findByTicketNumber(String ticketNumber);

    // Android user ticket summary
    long countByUserId(String userId);

    long countByUserIdAndStatus(
            String userId,
            TicketStatus status
    );

    // Android user ticket history
    List<SupportTicketEntity> findAllByUserIdOrderByCreatedAtDesc(
            String userId
    );

    // Admin / Developer portal
    Page<SupportTicketEntity> findAllByStatus(
            TicketStatus status,
            Pageable pageable
    );

    Page<SupportTicketEntity> findAllByCategory(
            SupportIssueCategory category,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT t
                    FROM SupportTicketEntity t
                    WHERE (:status IS NULL OR t.status = :status)
                      AND (:category IS NULL OR t.category = :category)
                      AND (
                            :query = ''
                            OR LOWER(t.ticketNumber) LIKE CONCAT('%', :query, '%')
                            OR LOWER(t.subject) LIKE CONCAT('%', :query, '%')
                            OR LOWER(t.userEmail) LIKE CONCAT('%', :query, '%')
                      )
                    """,
            countQuery = """
                    SELECT COUNT(t)
                    FROM SupportTicketEntity t
                    WHERE (:status IS NULL OR t.status = :status)
                      AND (:category IS NULL OR t.category = :category)
                      AND (
                            :query = ''
                            OR LOWER(t.ticketNumber) LIKE CONCAT('%', :query, '%')
                            OR LOWER(t.subject) LIKE CONCAT('%', :query, '%')
                            OR LOWER(t.userEmail) LIKE CONCAT('%', :query, '%')
                      )
                    """
    )
    Page<SupportTicketEntity> searchForAdmin(
            @Param("status") TicketStatus status,
            @Param("category") SupportIssueCategory category,
            @Param("query") String query,
            Pageable pageable
    );
}