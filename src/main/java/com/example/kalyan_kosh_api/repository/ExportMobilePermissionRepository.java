package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.ExportMobilePermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ExportMobilePermissionRepository extends JpaRepository<ExportMobilePermission, Long> {

    Optional<ExportMobilePermission> findByUser_Id(String userId);

    boolean existsByUser_IdAndEnabledTrue(String userId);

    @Query(
            value = """
                    SELECT p
                    FROM ExportMobilePermission p
                    LEFT JOIN FETCH p.user u
                    LEFT JOIN FETCH p.grantedBy gb
                    LEFT JOIN FETCH p.revokedBy rb
                    ORDER BY p.grantedAt DESC, p.id DESC
                    """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM ExportMobilePermission p
                    """
    )
    Page<ExportMobilePermission> findAllWithUsers(Pageable pageable);
}