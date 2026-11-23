package com.fitnessapp.nutrition.repository;

import com.fitnessapp.nutrition.model.entity.FoodItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItemEntity, UUID> {

    @Query("""
        select f from FoodItemEntity f
        where f.active = true
          and (:query = '' or lower(f.name) like lower(concat('%', :query, '%'))
               or lower(coalesce(f.brand, '')) like lower(concat('%', :query, '%')))
        order by f.verified desc, f.name asc
        """)
    Page<FoodItemEntity> search(
                                @Param("query") String query,
                                Pageable pageable);

    @Query("""
        select f from FoodItemEntity f
        where (:query = '' or lower(f.name) like lower(concat('%', :query, '%'))
               or lower(coalesce(f.brand, '')) like lower(concat('%', :query, '%')))
        order by f.verified desc, f.name asc
        """)
    Page<FoodItemEntity> searchForPortal(@Param("query") String query, Pageable pageable);

    @Query("""
        select f from FoodItemEntity f
        where f.id = :foodId and f.active = true
        """)
    Optional<FoodItemEntity> findAccessibleFood(@Param("foodId") UUID foodId);
}
