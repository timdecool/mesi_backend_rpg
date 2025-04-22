package com.ipi.mesi_backend_rpg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ipi.mesi_backend_rpg.model.UserFolder;

@Repository
public interface UserFolderRepository extends JpaRepository<UserFolder, Long> {
    List<UserFolder> findByUserId(Long userId);

    List<UserFolder> findByParentFolder(Long parentFolder);

    List<UserFolder> findByUserIdAndParentFolderIsNull(Long userId);

    List<UserFolder> findByUserIdAndNameContaining(Long userId, String name);
}