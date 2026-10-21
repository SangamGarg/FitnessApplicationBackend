package com.fitnessapp.portal.repository;

import com.fitnessapp.portal.tableEntity.PortalAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortalAccountRepository extends JpaRepository<PortalAccountEntity, Long> {

    /**
     * Finds a portal account by email address, ignoring case sensitivity.
     *
     * @param email User email address
     * @return Optional containing the matching account entity, or empty if not found
     */
    Optional<PortalAccountEntity> findByEmailIgnoreCase(String email);

    /**
     * Checks if a portal account exists with the specified email address, ignoring case sensitivity.
     *
     * @param email User email address
     * @return True if an account exists with the given email, false otherwise
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Finds a portal account by its hashed one-time setup token.
     *
     * @param setupTokenHash Hashed setup token string
     * @return Optional containing the matching account entity, or empty if not found
     */
    Optional<PortalAccountEntity> findBySetupTokenHash(String setupTokenHash);

    /**
     * Retrieves all accounts associated with a specific role ID, ordered by creation date descending.
     *
     * @param roleId Role identifier
     * @return List of matching portal account entities
     */
    List<PortalAccountEntity> findAllByRoleIdOrderByCreatedAtDesc(Integer roleId);
}