package com.ipi.mesi_backend_rpg.repository;

import com.ipi.mesi_backend_rpg.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface TagRepository extends JpaRepository<Tag, Integer> {

    Tag findByName(String name);
    
    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(CONCAT(:search, '%'))")
    List<Tag> findSearchTag(@Param("search") String search);


}
