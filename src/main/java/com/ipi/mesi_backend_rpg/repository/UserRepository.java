package com.ipi.mesi_backend_rpg.repository;

import com.ipi.mesi_backend_rpg.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
