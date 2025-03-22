package com.ipi.mesi_backend_rpg.repository;

import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.ModuleAccess;
import com.ipi.mesi_backend_rpg.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleAccessRepository extends JpaRepository<ModuleAccess, Integer> {

    ModuleAccess findByModule(Module module);
    
    List<ModuleAccess> findAllByUser(User user);
}
