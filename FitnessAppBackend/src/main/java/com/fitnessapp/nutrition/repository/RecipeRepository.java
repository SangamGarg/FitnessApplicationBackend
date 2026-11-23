package com.fitnessapp.nutrition.repository;

import com.fitnessapp.nutrition.model.entity.RecipeEntity;
import com.fitnessapp.nutrition.model.enums.DietaryPreference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipeRepository extends JpaRepository<RecipeEntity, UUID> {
    @Query("""
            select distinct r from RecipeEntity r
            where r.active = true
              and (:query ='' or lower(cast(r.title as String)) like lower(concat('%',cast(:query as String),'%')))
              and (:diet is null or :diet = com.fitnessapp.nutrition.model.enums.DietaryPreference.ALL
                   or r.dietType = :diet or r.dietType = com.fitnessapp.nutrition.model.enums.DietaryPreference.ALL)
            order by r.aiGenerated asc, r.title asc
            """)
    Page<RecipeEntity> search(@Param("query") String query,
                              @Param("diet") DietaryPreference diet, Pageable pageable);

    @Query("""
            select distinct r from RecipeEntity r
            where (:query ='' or lower(cast(r.title as String)) like lower(concat('%', cast(:query as String), '%')))
              and (:diet is null or :diet = com.fitnessapp.nutrition.model.enums.DietaryPreference.ALL
                   or r.dietType = :diet or r.dietType = com.fitnessapp.nutrition.model.enums.DietaryPreference.ALL)
            order by r.aiGenerated asc, r.title asc
            """)
    Page<RecipeEntity> searchForPortal(@Param("query") String query,
                                       @Param("diet") DietaryPreference diet,
                                       Pageable pageable);

    Optional<RecipeEntity> findByIdAndActiveTrue(UUID id);

    @Query("select r from RecipeEntity r where r.id = :id and r.active = true")
    Optional<RecipeEntity> findAccessibleRecipe(@Param("id") UUID id);
}
