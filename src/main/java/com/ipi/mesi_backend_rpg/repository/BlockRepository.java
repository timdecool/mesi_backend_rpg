package com.ipi.mesi_backend_rpg.repository;

import com.ipi.mesi_backend_rpg.model.Block;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlockRepository extends JpaRepository<Block, Long> {
    List<Block> findAllByModule_version(ModuleVersion moduleVersion);
}
