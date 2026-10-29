package com.fitnessapp.appAuth.repo;

import com.fitnessapp.appAuth.model.entity.OtpEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OtpRepo extends JpaRepository<OtpEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from OtpEntity c where c.email = :email and c.purpose = :purpose")
    Optional<OtpEntity> findForUpdate(
            @Param("email") String email,
            @Param("purpose") String purpose
    );
}
