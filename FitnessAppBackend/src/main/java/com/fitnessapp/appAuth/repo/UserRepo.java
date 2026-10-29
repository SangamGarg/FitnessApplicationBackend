package com.fitnessapp.appAuth.repo;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.portal.dto.MobileUserAdminResponseDto;
import com.fitnessapp.appAuth.model.entity.MobileAccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<UserEntity, String> {

    boolean existsByEmailIgnoreCase(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserEntity u where u.id = :id")
    Optional<UserEntity> findByIdForUpdate(@Param("id") String id);

    @Query(
            value = """
                    select new com.fitnessapp.portal.dto.MobileUserAdminResponseDto(
                        u.id, u.email, d.name, d.gender, d.age, d.profileImageUrl,
                        d.heightCm, d.weightKg, d.activityLevel, d.goal, u.accountStatus
                    )
                    from UserEntity u
                    left join u.userDetailsEntity d
                    where (:status is null or u.accountStatus = :status)
                      and (:query is null
                           or lower(u.email) like concat('%', lower(:query), '%')
                           or lower(coalesce(d.name, '')) like concat('%', lower(:query), '%'))
                    """,
            countQuery = """
                    select count(u)
                    from UserEntity u
                    left join u.userDetailsEntity d
                    where (:status is null or u.accountStatus = :status)
                      and (:query is null
                           or lower(u.email) like concat('%', lower(:query), '%')
                           or lower(coalesce(d.name, '')) like concat('%', lower(:query), '%'))
                    """
    )
    Page<MobileUserAdminResponseDto> searchForAdmin(
            @Param("query") String query,
            @Param("status") MobileAccountStatus status,
            Pageable pageable
    );

    @Query("""
            select new com.fitnessapp.portal.dto.MobileUserAdminResponseDto(
                u.id, u.email, d.name, d.gender, d.age, d.profileImageUrl,
                d.heightCm, d.weightKg, d.activityLevel, d.goal, u.accountStatus
            )
            from UserEntity u
            left join u.userDetailsEntity d
            where u.id = :id
            """)
    Optional<MobileUserAdminResponseDto> findAdminViewById(@Param("id") String id);
}