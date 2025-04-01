package com.ipi.mesi_backend_rpg.repository;

import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.ModuleAccess;
import com.ipi.mesi_backend_rpg.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleAccessRepository extends JpaRepository<ModuleAccess, Integer> {

    List<ModuleAccess> findAllByModule(Module module);

    List<ModuleAccess> findAllByUser(User user);

    ModuleAccess findModuleAccessBymoduleAndUser(Module module, User user);
}
