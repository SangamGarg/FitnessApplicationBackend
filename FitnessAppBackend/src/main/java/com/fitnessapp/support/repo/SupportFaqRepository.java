package com.fitnessapp.support.repo;

import com.fitnessapp.support.models.entity.SupportFaqEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupportFaqRepository extends JpaRepository<SupportFaqEntity, Long> {
    List<SupportFaqEntity> findByActiveTrueAndPopularTrueOrderBySortOrderAscUpdatedAtDesc(Pageable pageable);

    Page<SupportFaqEntity> findByActiveTrue(Pageable pageable);

    Page<SupportFaqEntity> findByActiveTrueAndCategory_CategoryKeyIgnoreCase(String categoryKey, Pageable pageable);

    Page<SupportFaqEntity> findByActiveTrueAndQuestionContainingIgnoreCase(String query, Pageable pageable);

    Page<SupportFaqEntity> findByActiveTrueAndCategory_CategoryKeyIgnoreCaseAndQuestionContainingIgnoreCase(String categoryKey, String query, Pageable pageable);

    Optional<SupportFaqEntity> findByIdAndActiveTrue(Long id);





    @Query("""
        SELECT f
        FROM SupportFaqEntity f
        JOIN FETCH f.category c
        WHERE f.active = true
          AND c.active = true
          AND (
                LOWER(f.question) LIKE LOWER(CONCAT('%', :term, '%'))
                OR LOWER(f.answer) LIKE LOWER(CONCAT('%', :term, '%'))
          )
        ORDER BY f.popular DESC, f.sortOrder ASC, f.updatedAt DESC
        """)
    List<SupportFaqEntity> searchActiveFaqs(
            @Param("term") String term,
            Pageable pageable
    );
}
