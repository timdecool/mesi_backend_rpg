package com.ipi.mesi_backend_rpg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ipi.mesi_backend_rpg.model.UserSavedModule;

import jakarta.transaction.Transactional;

@Repository
public interface UserSavedModuleRepository extends JpaRepository<UserSavedModule, Long> {
    List<UserSavedModule> findByUserId(Long userId);

    List<UserSavedModule> findByFolderId(Long folderId);

    List<UserSavedModule> findByUserIdAndFolderId(Long userId, Long folderId);

    List<UserSavedModule> findByUserIdAndModule_Id(Long userId, Long moduleId);

    List<UserSavedModule> findByUserIdAndAliasContaining(Long userId, String alias);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserSavedModule u WHERE u.module.id = :moduleId")
    void deleteByModule_Id(@Param("moduleId") Long moduleId);
}