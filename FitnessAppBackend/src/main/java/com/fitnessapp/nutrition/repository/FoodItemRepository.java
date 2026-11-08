package com.fitnessapp.nutrition.repository;

import com.fitnessapp.nutrition.model.entity.FoodItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FoodItemRepository extends JpaRepository<FoodItemEntity, UUID> {

    @Query("""
        select f from FoodItemEntity f
        where f.active = true
          and (f.owner is null or f.owner.id = :userId)
          and (:query is null or lower(f.name) like lower(concat('%', :query, '%'))
               or lower(coalesce(f.brand, '')) like lower(concat('%', :query, '%')))
        order by f.verified desc, f.name asc
        """)
    Page<FoodItemEntity> search(@Param("userId") String userId,
                                @Param("query") String query,
                                Pageable pageable);

    @Query("""
        select f from FoodItemEntity f
        where f.id = :foodId and f.active = true
          and (f.owner is null or f.owner.id = :userId)
        """)
    Optional<FoodItemEntity> findAccessibleFood(@Param("foodId") UUID foodId,
                                                 @Param("userId") String userId);
}
