package com.fitnessapp.support.repository;

import com.fitnessapp.support.models.SupportTicketEntity;
import com.fitnessapp.support.models.TicketCategory;
import com.fitnessapp.support.models.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicketEntity, Long> {

    Optional<SupportTicketEntity> findByTicketNumber(String ticketNumber);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, TicketStatus status);

    // For Android client user history
    List<SupportTicketEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    // For Dev / Admin portal with filtering & pagination
    Page<SupportTicketEntity> findAllByStatus(TicketStatus status, Pageable pageable);

    Page<SupportTicketEntity> findAllByCategory(TicketCategory category, Pageable pageable);

    @Query(
            value = """
                    select t from SupportTicketEntity t
                    where (:status is null or t.status = :status)
                      and (:category is null or t.category = :category)
                      and (:query is null
                           or lower(t.ticketNumber) like concat('%', lower(:query), '%')
                           or lower(t.subject) like concat('%', lower(:query), '%')
                           or lower(t.userEmail) like concat('%', lower(:query), '%'))
                    """,
            countQuery = """
                    select count(t) from SupportTicketEntity t
                    where (:status is null or t.status = :status)
                      and (:category is null or t.category = :category)
                      and (:query is null
                           or lower(t.ticketNumber) like concat('%', lower(:query), '%')
                           or lower(t.subject) like concat('%', lower(:query), '%')
                           or lower(t.userEmail) like concat('%', lower(:query), '%'))
                    """
    )
    Page<SupportTicketEntity> searchForAdmin(
            @Param("status") TicketStatus status,
            @Param("category") TicketCategory category,
            @Param("query") String query,
            Pageable pageable
    );
}