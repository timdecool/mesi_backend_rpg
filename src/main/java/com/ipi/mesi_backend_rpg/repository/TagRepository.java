package com.ipi.mesi_backend_rpg.repository;

import com.ipi.mesi_backend_rpg.model.Tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Tag findByName(String name);

    List<Tag> findByNameContainingIgnoreCase(String query);

    @Query("SELECT t FROM Tag t LEFT JOIN t.modules m GROUP BY t.id ORDER BY COUNT(m.id) DESC")
    List<Tag> findAllOrderByModuleCountDesc();

    @Query("SELECT t FROM Tag t JOIN t.modules m WHERE m.id = :moduleId")
    List<Tag> findByModuleId(@Param("moduleId") Long moduleId);

}
