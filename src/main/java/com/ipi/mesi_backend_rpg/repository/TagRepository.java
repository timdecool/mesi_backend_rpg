package com.ipi.mesi_backend_rpg.repository;

import com.ipi.mesi_backend_rpg.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface TagRepository extends JpaRepository<Tag, Integer> {

    Tag findByName(String name);

}
