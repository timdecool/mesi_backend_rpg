package com.ipi.mesi_backend_rpg.repository;

import com.ipi.mesi_backend_rpg.model.GameSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameSystemRepository extends JpaRepository<GameSystem, Long> {
}
