package com.ipi.mesi_backend_rpg.repository;

// import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleVersionRepository extends JpaRepository<ModuleVersion, Long> {
    List<ModuleVersion> findAllByModule(Module module);
}
