package com.ipi.mesi_backend_rpg.repository;

import com.ipi.mesi_backend_rpg.model.Module;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    Module findById(long id);

    /**
     * Recherche les modules dont le titre OU la description contient la chaîne de
     * requête (insensible à la casse).
     * Les résultats sont paginés pour ne retourner que le nombre souhaité
     * d'éléments.
     *
     * @param query    La chaîne de caractères à rechercher.
     * @param pageable L'objet Pageable pour limiter le nombre de résultats (par
     * exemple, les 30 premiers).
     * @return Une liste de modules correspondants.
     */
    @Query("SELECT m FROM Module m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(m.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Module> findByTitleOrDescriptionContainingIgnoreCase(@Param("query") String query, Pageable pageable);

    @Query("""
                SELECT m FROM Module m
                JOIN m.savedModules usm
                WHERE EXISTS (
                    SELECT mv FROM ModuleVersion mv
                    WHERE mv.module = m AND mv.published = true
                )
                GROUP BY m.id
                ORDER BY COUNT(usm.savedModuleId) DESC
            """)
    List<Module> findMostSavedModules(Pageable pageable);

    @Query("""
            SELECT m FROM Module m
            WHERE EXISTS (
                SELECT mv FROM ModuleVersion mv
                WHERE mv.module = m AND mv.published = true
            )
            ORDER BY m.createdAt DESC
            """)
    List<Module> findMostRecentModules(Pageable pageable);

    @Query("SELECT m FROM Module m JOIN ModuleComment c ON c.module = m GROUP BY m ORDER BY COUNT(c) DESC")
    List<Module> findMostCommentedModules(Pageable pageable);

    List<Module> findAllByCreator_Id(Long creatorId);

    @Query("""
            SELECT m FROM Module m
            JOIN ModuleRating mr ON mr.module = m
            WHERE EXISTS (
                SELECT mv FROM ModuleVersion mv
                WHERE mv.module = m AND mv.published = true
            )
            GROUP BY m
            ORDER BY AVG(mr.rating) DESC
            """)
    List<Module> findMostRatedModules(Pageable pageable);
}