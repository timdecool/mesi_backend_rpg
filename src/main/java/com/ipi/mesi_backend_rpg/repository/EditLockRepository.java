package com.ipi.mesi_backend_rpg.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ipi.mesi_backend_rpg.model.EditLock;
import com.ipi.mesi_backend_rpg.model.ResourceType;
import com.ipi.mesi_backend_rpg.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface EditLockRepository extends JpaRepository<EditLock, Long> {

        @Query("SELECT e FROM EditLock e WHERE e.resourceType = :resourceType AND e.resourceId = :resourceId")
        List<EditLock> findActiveLocksForResource(
                        @Param("resourceType") ResourceType resourceType,
                        @Param("resourceId") Long resourceId);

        @Query("SELECT e FROM EditLock e WHERE e.resourceType = :resourceType AND e.resourceId = :resourceId AND e.expiresAt > :now")
        List<EditLock> findNonExpiredLocksForResource(
                        @Param("resourceType") ResourceType resourceType,
                        @Param("resourceId") Long resourceId,
                        @Param("now") LocalDateTime now);

        Optional<EditLock> findByLockToken(String lockToken);

        @Query("SELECT e FROM EditLock e WHERE e.lockedBy = :user AND e.expiresAt > :now")
        List<EditLock> findActiveLocksForUser(@Param("user") User user, @Param("now") LocalDateTime now);

        @Modifying
        @Query("DELETE FROM EditLock e WHERE e.expiresAt < :now")
        void deleteExpiredLocks(@Param("now") LocalDateTime now);

        @Query("SELECT COUNT(e) FROM EditLock e WHERE e.resourceType = :resourceType AND e.resourceId = :resourceId AND e.expiresAt > :now")
        long countActiveLocksForResource(
                        @Param("resourceType") ResourceType resourceType,
                        @Param("resourceId") Long resourceId,
                        @Param("now") LocalDateTime now);

        @Query("SELECT e FROM EditLock e WHERE e.resourceType = :resourceType AND e.resourceId = :resourceId AND e.lockedBy = :user AND e.expiresAt > :now")
        Optional<EditLock> findUserLockForResource(
                        @Param("resourceType") ResourceType resourceType,
                        @Param("resourceId") Long resourceId,
                        @Param("user") User user,
                        @Param("now") LocalDateTime now);
}
