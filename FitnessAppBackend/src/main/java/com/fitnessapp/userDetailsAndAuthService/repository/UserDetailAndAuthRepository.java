package com.fitnessapp.userDetailsAndAuthService.repository;

import com.fitnessapp.userDetailsAndAuthService.models.entitites.UserDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface UserDetailAndAuthRepository extends JpaRepository<UserDetailsEntity, Long> {
    UserDetailsEntity findByUserId(String userId);

    /**
     * A device token must belong to one account only. When a token is registered for a user, remove it
     * from every other user (e.g. someone logged out of this phone and another person logged in).
     * Must be called inside a transaction.
     */
    @Modifying(flushAutomatically = true)
    @Query(value = "DELETE FROM fcm_tokens WHERE firebase_cloud_messaging_token IN (:tokens) AND user_details_id <> :ownerId",
            nativeQuery = true)
    void removeTokensFromOtherUsers(@Param("tokens") Collection<String> tokens, @Param("ownerId") Long ownerId);
}