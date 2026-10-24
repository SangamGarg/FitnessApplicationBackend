package com.fitnessapp.support.repository;
import com.fitnessapp.support.models.SupportFaqCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.Optional;
public interface SupportFaqCategoryRepository extends JpaRepository<SupportFaqCategoryEntity,Long> {
 List<SupportFaqCategoryEntity> findByActiveTrueOrderBySortOrderAscNameAsc();
 Optional<SupportFaqCategoryEntity> findByCategoryKeyIgnoreCaseAndActiveTrue(String categoryKey);
}
