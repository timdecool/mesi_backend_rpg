package com.ipi.mesi_backend_rpg.repository;

import com.ipi.mesi_backend_rpg.model.ModuleRating;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleRatingRepository extends JpaRepository<ModuleRating, Long> {

    @Query("SELECT r FROM ModuleRating r WHERE r.module.id = :moduleId AND r.user.id = :userId ORDER BY r.updatedAt DESC LIMIT 1")
    ModuleRating findModuleRatingByModuleIdAndUserId(Long moduleId, Long userId);
    ModuleRating findModuleRatingByModuleVersionIdAndUserId(Long moduleVersionId, Long userId);
    int  countByModuleId(Long moduleId);
    int countByModuleVersionId(Long moduleVersionId);

    @Query("SELECT AVG(r.rating) FROM ModuleRating r WHERE r.module.id = :moduleId")
    Float findAverageRatingByModuleId(Long moduleId);

    @Query("SELECT AVG(r.rating) FROM ModuleRating r WHERE r.moduleVersion.id = :moduleVersionId")
    Float findAverageRatingByModuleVersionId(Long moduleVersionId);

    List<ModuleRating> findAllByModule(com.ipi.mesi_backend_rpg.model.Module module);
}
