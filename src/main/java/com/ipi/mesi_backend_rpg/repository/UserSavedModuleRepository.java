package com.ipi.mesi_backend_rpg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ipi.mesi_backend_rpg.model.UserSavedModule;

@Repository
public interface UserSavedModuleRepository extends JpaRepository<UserSavedModule, Long> {
    List<UserSavedModule> findByUserId(Long userId);

    List<UserSavedModule> findByFolderId(Long folderId);

    List<UserSavedModule> findByUserIdAndFolderId(Long userId, Long folderId);

    List<UserSavedModule> findByUserIdAndModuleId(Long userId, Long moduleId);

    List<UserSavedModule> findByUserIdAndAliasContaining(Long userId, String alias);
}