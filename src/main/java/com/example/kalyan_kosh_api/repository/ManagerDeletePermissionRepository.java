package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.ManagerDeletePermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ManagerDeletePermissionRepository extends JpaRepository<ManagerDeletePermission, Long> {

    Optional<ManagerDeletePermission> findByUser_Id(String userId);

    boolean existsByUser_IdAndEnabledTrue(String userId);

    @Query(
            value = """
                    SELECT p
                    FROM ManagerDeletePermission p
                    LEFT JOIN FETCH p.user u
                    LEFT JOIN FETCH p.grantedBy gb
                    LEFT JOIN FETCH p.revokedBy rb
                    ORDER BY p.grantedAt DESC, p.id DESC
                    """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM ManagerDeletePermission p
                    """
    )
    Page<ManagerDeletePermission> findAllWithUsers(Pageable pageable);
}