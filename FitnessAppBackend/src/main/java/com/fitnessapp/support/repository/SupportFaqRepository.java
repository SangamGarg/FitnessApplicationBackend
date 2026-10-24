package com.fitnessapp.support.repository;
import com.fitnessapp.support.models.SupportFaqEntity;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List; import java.util.Optional;
public interface SupportFaqRepository extends JpaRepository<SupportFaqEntity,Long> {
 List<SupportFaqEntity> findByActiveTrueAndPopularTrueOrderBySortOrderAscUpdatedAtDesc(Pageable pageable);
 Page<SupportFaqEntity> findByActiveTrue(Pageable pageable);
 Page<SupportFaqEntity> findByActiveTrueAndCategory_CategoryKeyIgnoreCase(String categoryKey, Pageable pageable);
 Page<SupportFaqEntity> findByActiveTrueAndQuestionContainingIgnoreCase(String query, Pageable pageable);
 Page<SupportFaqEntity> findByActiveTrueAndCategory_CategoryKeyIgnoreCaseAndQuestionContainingIgnoreCase(String categoryKey, String query, Pageable pageable);
 Optional<SupportFaqEntity> findByIdAndActiveTrue(Long id);
}
