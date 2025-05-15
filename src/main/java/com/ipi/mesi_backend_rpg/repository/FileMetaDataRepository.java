package com.ipi.mesi_backend_rpg.repository;

import com.ipi.mesi_backend_rpg.model.FileMetaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetaDataRepository extends JpaRepository<FileMetaData, Long> {
    FileMetaData findByUniqueId(String uniqueId);
}