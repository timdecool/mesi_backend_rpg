package com.ipi.mesi_backend_rpg.repository;

import com.ipi.mesi_backend_rpg.model.Block;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {
    @Query("SELECT b FROM Block b WHERE b.moduleVersion = :moduleVersion")
    List<Block> findAllByModuleVersion(@Param("moduleVersion") ModuleVersion moduleVersion);
}
