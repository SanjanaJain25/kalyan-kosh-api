package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.Role;
import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
    Optional<User> findByMobileNumber(String mobile);
    Optional<User> findByEmail(String email);
List<User> findAllByRoleOrderByCreatedAtAsc(Role role);
    // ✅ Find users by role
    List<User> findByRole(Role role);

@Query("SELECT DISTINCT u FROM User u " +
       "LEFT JOIN FETCH u.departmentState s " +
       "LEFT JOIN FETCH u.departmentSambhag sa " +
       "LEFT JOIN FETCH u.departmentDistrict d " +
       "LEFT JOIN FETCH u.departmentBlock b " +
       "WHERE MONTH(u.createdAt) = :month " +
       "AND YEAR(u.createdAt) = :year")
List<User> findAllByCreatedMonthAndYear(
        @Param("month") int month,
        @Param("year") int year);
        
    // ✅ Fetch all users with their location relationships
   @Query("SELECT DISTINCT u FROM User u " +
       "LEFT JOIN FETCH u.departmentState s " +
       "LEFT JOIN FETCH u.departmentSambhag sa " +
       "LEFT JOIN FETCH u.departmentDistrict d " +
       "LEFT JOIN FETCH u.departmentBlock b " +
       "LEFT JOIN FETCH u.assignedDeathCase adc")
    List<User> findAllWithLocations();

@Query(
    value = "SELECT u FROM User u " +
            "LEFT JOIN FETCH u.departmentState s " +
            "LEFT JOIN FETCH u.departmentSambhag sa " +
            "LEFT JOIN FETCH u.departmentDistrict d " +
            "LEFT JOIN FETCH u.departmentBlock b " +
            "WHERE NOT (u.id = :reservedSuperAdminId AND u.role = :reservedSuperAdminRole)",
    countQuery = "SELECT COUNT(u) FROM User u " +
            "WHERE NOT (u.id = :reservedSuperAdminId AND u.role = :reservedSuperAdminRole)"
)
Page<User> findExportUsersPaged(
        @Param("reservedSuperAdminId") String reservedSuperAdminId,
        @Param("reservedSuperAdminRole") Role reservedSuperAdminRole,
        Pageable pageable
);

    // ✅ Paginated query - fetch users with locations
@Query(value = "SELECT u FROM User u " +
       "LEFT JOIN FETCH u.departmentState s " +
       "LEFT JOIN FETCH u.departmentSambhag sa " +
       "LEFT JOIN FETCH u.departmentDistrict d " +
       "LEFT JOIN FETCH u.departmentBlock b " +
       "LEFT JOIN FETCH u.assignedDeathCase adc",
       countQuery = "SELECT COUNT(u) FROM User u")
       Page<User> findAllWithLocations(Pageable pageable);

    // ✅ Filtered + Paginated query with Sambhag, District, Block, Name, Mobile, UserId filters
    @Query(value = "SELECT u FROM User u " +
           "LEFT JOIN FETCH u.departmentState s " +
           "LEFT JOIN FETCH u.departmentSambhag sa " +
           "LEFT JOIN FETCH u.departmentDistrict d " +
           "LEFT JOIN FETCH u.departmentBlock b " +
            "LEFT JOIN FETCH u.assignedDeathCase adc " +
           "WHERE (:sambhagId IS NULL OR sa.id = :sambhagId) " +
           "AND (:districtId IS NULL OR d.id = :districtId) " +
           "AND (:blockId IS NULL OR b.id = :blockId) " +
           "AND (:name IS NULL OR LOWER(CONCAT(u.name, ' ', COALESCE(u.surname, ''))) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "     OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "     OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:mobile IS NULL OR u.mobileNumber LIKE CONCAT('%', :mobile, '%')) " +
           "AND (:userId IS NULL OR LOWER(u.id) LIKE LOWER(CONCAT('%', :userId, '%')))",
           countQuery = "SELECT COUNT(u) FROM User u " +
           "LEFT JOIN u.departmentSambhag sa " +
           "LEFT JOIN u.departmentDistrict d " +
           "LEFT JOIN u.departmentBlock b " +
           "WHERE (:sambhagId IS NULL OR sa.id = :sambhagId) " +
           "AND (:districtId IS NULL OR d.id = :districtId) " +
           "AND (:blockId IS NULL OR b.id = :blockId) " +
           "AND (:name IS NULL OR LOWER(CONCAT(u.name, ' ', COALESCE(u.surname, ''))) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "     OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "     OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:mobile IS NULL OR u.mobileNumber LIKE CONCAT('%', :mobile, '%')) " +
           "AND (:userId IS NULL OR LOWER(u.id) LIKE LOWER(CONCAT('%', :userId, '%')))")
    Page<User> findAllWithFilters(
            @Param("sambhagId") String sambhagId,
            @Param("districtId") String districtId,
            @Param("blockId") String blockId,
            @Param("name") String name,
            @Param("mobile") String mobile,
            @Param("userId") String userId,
            Pageable pageable);
@Query(
    value = "SELECT u FROM User u " +
            "LEFT JOIN FETCH u.departmentState s " +
            "LEFT JOIN FETCH u.departmentSambhag sa " +
            "LEFT JOIN FETCH u.departmentDistrict d " +
            "LEFT JOIN FETCH u.departmentBlock b " +
            "WHERE NOT (u.id = :reservedSuperAdminId AND u.role = :reservedSuperAdminRole) " +
            "AND (:userId IS NULL OR LOWER(u.id) LIKE LOWER(CONCAT('%', :userId, '%'))) " +
            "AND (:name IS NULL OR LOWER(CONCAT(COALESCE(u.name, ''), ' ', COALESCE(u.surname, ''))) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "     OR LOWER(COALESCE(u.name, '')) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "     OR LOWER(COALESCE(u.surname, '')) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:email IS NULL OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :email, '%'))) " +
            "AND (:mobileNumber IS NULL OR COALESCE(u.mobileNumber, '') LIKE CONCAT('%', :mobileNumber, '%')) " +
            "AND (:role IS NULL OR u.role = :role) " +
            "AND (:status IS NULL OR u.status = :status) " +
            "AND (:sambhag IS NULL OR LOWER(COALESCE(sa.name, '')) LIKE LOWER(CONCAT('%', :sambhag, '%'))) " +
            "AND (:district IS NULL OR LOWER(COALESCE(d.name, '')) LIKE LOWER(CONCAT('%', :district, '%'))) " +
            "AND (:block IS NULL OR LOWER(COALESCE(b.name, '')) LIKE LOWER(CONCAT('%', :block, '%')))",
    countQuery = "SELECT COUNT(u) FROM User u " +
            "LEFT JOIN u.departmentSambhag sa " +
            "LEFT JOIN u.departmentDistrict d " +
            "LEFT JOIN u.departmentBlock b " +
            "WHERE NOT (u.id = :reservedSuperAdminId AND u.role = :reservedSuperAdminRole) " +
            "AND (:userId IS NULL OR LOWER(u.id) LIKE LOWER(CONCAT('%', :userId, '%'))) " +
            "AND (:name IS NULL OR LOWER(CONCAT(COALESCE(u.name, ''), ' ', COALESCE(u.surname, ''))) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "     OR LOWER(COALESCE(u.name, '')) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "     OR LOWER(COALESCE(u.surname, '')) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:email IS NULL OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :email, '%'))) " +
            "AND (:mobileNumber IS NULL OR COALESCE(u.mobileNumber, '') LIKE CONCAT('%', :mobileNumber, '%')) " +
            "AND (:role IS NULL OR u.role = :role) " +
            "AND (:status IS NULL OR u.status = :status) " +
            "AND (:sambhag IS NULL OR LOWER(COALESCE(sa.name, '')) LIKE LOWER(CONCAT('%', :sambhag, '%'))) " +
            "AND (:district IS NULL OR LOWER(COALESCE(d.name, '')) LIKE LOWER(CONCAT('%', :district, '%'))) " +
            "AND (:block IS NULL OR LOWER(COALESCE(b.name, '')) LIKE LOWER(CONCAT('%', :block, '%')))"
)
Page<User> searchAdminUsers(
        @Param("userId") String userId,
        @Param("name") String name,
        @Param("email") String email,
        @Param("mobileNumber") String mobileNumber,
        @Param("role") Role role,
        @Param("status") UserStatus status,
        @Param("sambhag") String sambhag,
        @Param("district") String district,
        @Param("block") String block,
        @Param("reservedSuperAdminId") String reservedSuperAdminId,
        @Param("reservedSuperAdminRole") Role reservedSuperAdminRole,
        Pageable pageable
);
    // ✅ Fetch single user with location relationships
    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.departmentState s " +
           "LEFT JOIN FETCH u.departmentSambhag sa " +
           "LEFT JOIN FETCH u.departmentDistrict d " +
           "LEFT JOIN FETCH u.departmentBlock b " +
            "LEFT JOIN FETCH u.assignedDeathCase adc " +
           "WHERE u.id = :id")
    Optional<User> findByIdWithLocations(String id);

    // ✅ Find non-donors (users who haven't donated in a specific month/year)
    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.departmentState s " +
           "LEFT JOIN FETCH u.departmentSambhag sa " +
           "LEFT JOIN FETCH u.departmentDistrict d " +
           "LEFT JOIN FETCH u.departmentBlock b " +
           "WHERE u.id NOT IN (" +
           "    SELECT DISTINCT r.user.id FROM Receipt r " +
           "    WHERE MONTH(r.paymentDate) = :month " +
           "    AND YEAR(r.paymentDate) = :year" +
           ")")
    List<User> findNonDonors(@Param("month") int month, @Param("year") int year);

    // ✅ Find non-donors with pagination
    @Query(value = "SELECT u FROM User u " +
           "LEFT JOIN FETCH u.departmentState s " +
           "LEFT JOIN FETCH u.departmentSambhag sa " +
           "LEFT JOIN FETCH u.departmentDistrict d " +
           "LEFT JOIN FETCH u.departmentBlock b " +
           "WHERE u.id NOT IN (" +
           "    SELECT DISTINCT r.user.id FROM Receipt r " +
           "    WHERE MONTH(r.paymentDate) = :month " +
           "    AND YEAR(r.paymentDate) = :year" +
           ")",
           countQuery = "SELECT COUNT(u) FROM User u " +
           "WHERE u.id NOT IN (" +
           "    SELECT DISTINCT r.user.id FROM Receipt r " +
           "    WHERE MONTH(r.paymentDate) = :month " +
           "    AND YEAR(r.paymentDate) = :year" +
           ")")
    Page<User> findNonDonorsPaginated(@Param("month") int month, @Param("year") int year, Pageable pageable);

//no utr

// ✅ Users who never uploaded even a single UTR ever
@Query(
    value = """
        SELECT u
        FROM User u
        LEFT JOIN FETCH u.departmentState s
        LEFT JOIN FETCH u.departmentSambhag sa
        LEFT JOIN FETCH u.departmentDistrict d
        LEFT JOIN FETCH u.departmentBlock b
        LEFT JOIN FETCH u.assignedDeathCase adc
        WHERE u.role = com.example.kalyan_kosh_api.entity.Role.ROLE_USER
          AND NOT EXISTS (
              SELECT 1
              FROM Receipt r
              WHERE r.user.id = u.id
                AND r.utrNumber IS NOT NULL
                AND TRIM(r.utrNumber) <> ''
          )
        """,
    countQuery = """
        SELECT COUNT(u)
        FROM User u
        WHERE u.role = com.example.kalyan_kosh_api.entity.Role.ROLE_USER
          AND NOT EXISTS (
              SELECT 1
              FROM Receipt r
              WHERE r.user.id = u.id
                AND r.utrNumber IS NOT NULL
                AND TRIM(r.utrNumber) <> ''
          )
        """
)
Page<User> findNoUtrEverUsersPaginated(Pageable pageable);

@Query(
    value = """
        SELECT u
        FROM User u
        LEFT JOIN FETCH u.departmentState s
        LEFT JOIN FETCH u.departmentSambhag sa
        LEFT JOIN FETCH u.departmentDistrict d
        LEFT JOIN FETCH u.departmentBlock b
        LEFT JOIN FETCH u.assignedDeathCase adc
        WHERE u.role = com.example.kalyan_kosh_api.entity.Role.ROLE_USER
          AND (:name IS NULL OR
               LOWER(CONCAT(COALESCE(u.name, ''), ' ', COALESCE(u.surname, ''))) LIKE LOWER(CONCAT('%', :name, '%')) OR
               LOWER(COALESCE(u.name, '')) LIKE LOWER(CONCAT('%', :name, '%')) OR
               LOWER(COALESCE(u.surname, '')) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:mobile IS NULL OR COALESCE(u.mobileNumber, '') LIKE CONCAT('%', :mobile, '%'))
          AND (:userId IS NULL OR COALESCE(u.id, '') LIKE CONCAT('%', :userId, '%'))
          AND (:sambhag IS NULL OR LOWER(COALESCE(sa.name, '')) LIKE LOWER(CONCAT('%', :sambhag, '%')))
          AND (:district IS NULL OR LOWER(COALESCE(d.name, '')) LIKE LOWER(CONCAT('%', :district, '%')))
          AND (:block IS NULL OR LOWER(COALESCE(b.name, '')) LIKE LOWER(CONCAT('%', :block, '%')))
          AND NOT EXISTS (
              SELECT 1
              FROM Receipt r
              WHERE r.user.id = u.id
                AND r.utrNumber IS NOT NULL
                AND TRIM(r.utrNumber) <> ''
          )
        """,
    countQuery = """
        SELECT COUNT(u)
        FROM User u
        LEFT JOIN u.departmentSambhag sa
        LEFT JOIN u.departmentDistrict d
        LEFT JOIN u.departmentBlock b
        WHERE u.role = com.example.kalyan_kosh_api.entity.Role.ROLE_USER
          AND (:name IS NULL OR
               LOWER(CONCAT(COALESCE(u.name, ''), ' ', COALESCE(u.surname, ''))) LIKE LOWER(CONCAT('%', :name, '%')) OR
               LOWER(COALESCE(u.name, '')) LIKE LOWER(CONCAT('%', :name, '%')) OR
               LOWER(COALESCE(u.surname, '')) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:mobile IS NULL OR COALESCE(u.mobileNumber, '') LIKE CONCAT('%', :mobile, '%'))
          AND (:userId IS NULL OR COALESCE(u.id, '') LIKE CONCAT('%', :userId, '%'))
          AND (:sambhag IS NULL OR LOWER(COALESCE(sa.name, '')) LIKE LOWER(CONCAT('%', :sambhag, '%')))
          AND (:district IS NULL OR LOWER(COALESCE(d.name, '')) LIKE LOWER(CONCAT('%', :district, '%')))
          AND (:block IS NULL OR LOWER(COALESCE(b.name, '')) LIKE LOWER(CONCAT('%', :block, '%')))
          AND NOT EXISTS (
              SELECT 1
              FROM Receipt r
              WHERE r.user.id = u.id
                AND r.utrNumber IS NOT NULL
                AND TRIM(r.utrNumber) <> ''
          )
        """
)
Page<User> searchNoUtrEverUsersPaginated(
        @Param("name") String name,
        @Param("mobile") String mobile,
        @Param("userId") String userId,
        @Param("sambhag") String sambhag,
        @Param("district") String district,
        @Param("block") String block,
        Pageable pageable
);
// ✅ Filtered + Paginated query for pending-profile users only
@Query(
    value = """
        SELECT u
        FROM User u
        LEFT JOIN FETCH u.departmentState s
        LEFT JOIN FETCH u.departmentSambhag sa
        LEFT JOIN FETCH u.departmentDistrict d
        LEFT JOIN FETCH u.departmentBlock b
        LEFT JOIN u.assignedDeathCase adc
        WHERE u.role = com.example.kalyan_kosh_api.entity.Role.ROLE_USER
          AND (:name IS NULL OR
               LOWER(CONCAT(COALESCE(u.name, ''), ' ', COALESCE(u.surname, ''))) LIKE LOWER(CONCAT('%', :name, '%')) OR
               LOWER(COALESCE(u.name, '')) LIKE LOWER(CONCAT('%', :name, '%')) OR
               LOWER(COALESCE(u.surname, '')) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:mobile IS NULL OR COALESCE(u.mobileNumber, '') LIKE CONCAT('%', :mobile, '%'))
          AND (:userId IS NULL OR COALESCE(u.id, '') LIKE CONCAT('%', :userId, '%'))
          AND (:sambhag IS NULL OR LOWER(COALESCE(sa.name, '')) LIKE LOWER(CONCAT('%', :sambhag, '%')))
          AND (:district IS NULL OR LOWER(COALESCE(d.name, '')) LIKE LOWER(CONCAT('%', :district, '%')))
          AND (:block IS NULL OR LOWER(COALESCE(b.name, '')) LIKE LOWER(CONCAT('%', :block, '%')))
          AND (
                (:beneficiaryId IS NULL AND NOT EXISTS (
                    SELECT 1
                    FROM Receipt r
                    WHERE r.user.id = u.id
                      AND r.amount > 0
                ))
                OR
                (:beneficiaryId IS NOT NULL
                    AND adc.id = :beneficiaryId
                    AND NOT EXISTS (
                        SELECT 1
                        FROM Receipt r
                        WHERE r.user.id = u.id
                          AND r.deathCase IS NOT NULL
                          AND r.deathCase.id = :beneficiaryId
                          AND r.amount > 0
                    )
                )
          )
        """,
    countQuery = """
        SELECT COUNT(u)
        FROM User u
        LEFT JOIN u.departmentState s
        LEFT JOIN u.departmentSambhag sa
        LEFT JOIN u.departmentDistrict d
        LEFT JOIN u.departmentBlock b
        LEFT JOIN u.assignedDeathCase adc
        WHERE u.role = com.example.kalyan_kosh_api.entity.Role.ROLE_USER
          AND (:name IS NULL OR
               LOWER(CONCAT(COALESCE(u.name, ''), ' ', COALESCE(u.surname, ''))) LIKE LOWER(CONCAT('%', :name, '%')) OR
               LOWER(COALESCE(u.name, '')) LIKE LOWER(CONCAT('%', :name, '%')) OR
               LOWER(COALESCE(u.surname, '')) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:mobile IS NULL OR COALESCE(u.mobileNumber, '') LIKE CONCAT('%', :mobile, '%'))
          AND (:userId IS NULL OR COALESCE(u.id, '') LIKE CONCAT('%', :userId, '%'))
          AND (:sambhag IS NULL OR LOWER(COALESCE(sa.name, '')) LIKE LOWER(CONCAT('%', :sambhag, '%')))
          AND (:district IS NULL OR LOWER(COALESCE(d.name, '')) LIKE LOWER(CONCAT('%', :district, '%')))
          AND (:block IS NULL OR LOWER(COALESCE(b.name, '')) LIKE LOWER(CONCAT('%', :block, '%')))
          AND (
                (:beneficiaryId IS NULL AND NOT EXISTS (
                    SELECT 1
                    FROM Receipt r
                    WHERE r.user.id = u.id
                      AND r.amount > 0
                ))
                OR
                (:beneficiaryId IS NOT NULL
                    AND adc.id = :beneficiaryId
                    AND NOT EXISTS (
                        SELECT 1
                        FROM Receipt r
                        WHERE r.user.id = u.id
                          AND r.deathCase IS NOT NULL
                          AND r.deathCase.id = :beneficiaryId
                          AND r.amount > 0
                    )
                )
          )
        """
)
Page<User> searchNonDonorsByBeneficiaryPaginated(
        @Param("beneficiaryId") Long beneficiaryId,
        @Param("name") String name,
        @Param("mobile") String mobile,
        @Param("userId") String userId,
        @Param("sambhag") String sambhag,
        @Param("district") String district,
        @Param("block") String block,
        Pageable pageable
);

@Query(value = "SELECT u FROM User u " +
       "LEFT JOIN FETCH u.departmentState s " +
       "LEFT JOIN FETCH u.departmentSambhag sa " +
       "LEFT JOIN FETCH u.departmentDistrict d " +
       "LEFT JOIN FETCH u.departmentBlock b " +
       "WHERE (" +
       "   u.department IS NULL OR TRIM(u.department) = '' " +
       "   OR u.departmentState IS NULL " +
       "   OR u.departmentSambhag IS NULL " +
       "   OR u.departmentDistrict IS NULL " +
       "   OR u.departmentBlock IS NULL " +
       "   OR u.schoolOfficeName IS NULL OR TRIM(u.schoolOfficeName) = ''" +
       ") " +
       "AND (:sambhagId IS NULL OR sa.id = :sambhagId) " +
       "AND (:districtId IS NULL OR d.id = :districtId) " +
       "AND (:blockId IS NULL OR b.id = :blockId) " +
       "AND (:name IS NULL OR LOWER(CONCAT(u.name, ' ', COALESCE(u.surname, ''))) LIKE LOWER(CONCAT('%', :name, '%')) " +
       "     OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
       "     OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :name, '%'))) " +
       "AND (:mobile IS NULL OR u.mobileNumber LIKE CONCAT('%', :mobile, '%')) " +
       "AND (:userId IS NULL OR LOWER(u.id) LIKE LOWER(CONCAT('%', :userId, '%')))",
       countQuery = "SELECT COUNT(u) FROM User u " +
       "LEFT JOIN u.departmentSambhag sa " +
       "LEFT JOIN u.departmentDistrict d " +
       "LEFT JOIN u.departmentBlock b " +
       "WHERE (" +
       "   u.department IS NULL OR TRIM(u.department) = '' " +
       "   OR u.departmentState IS NULL " +
       "   OR u.departmentSambhag IS NULL " +
       "   OR u.departmentDistrict IS NULL " +
       "   OR u.departmentBlock IS NULL " +
       "   OR u.schoolOfficeName IS NULL OR TRIM(u.schoolOfficeName) = ''" +
       ") " +
       "AND (:sambhagId IS NULL OR sa.id = :sambhagId) " +
       "AND (:districtId IS NULL OR d.id = :districtId) " +
       "AND (:blockId IS NULL OR b.id = :blockId) " +
       "AND (:name IS NULL OR LOWER(CONCAT(u.name, ' ', COALESCE(u.surname, ''))) LIKE LOWER(CONCAT('%', :name, '%')) " +
       "     OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
       "     OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :name, '%'))) " +
       "AND (:mobile IS NULL OR u.mobileNumber LIKE CONCAT('%', :mobile, '%')) " +
       "AND (:userId IS NULL OR LOWER(u.id) LIKE LOWER(CONCAT('%', :userId, '%')))")
Page<User> findPendingProfileUsersWithFilters(
        @Param("sambhagId") String sambhagId,
        @Param("districtId") String districtId,
        @Param("blockId") String blockId,
        @Param("name") String name,
        @Param("mobile") String mobile,
        @Param("userId") String userId,
        Pageable pageable
);

@Query("SELECT u FROM User u " +
       "LEFT JOIN FETCH u.departmentState s " +
       "LEFT JOIN FETCH u.departmentSambhag sa " +
       "LEFT JOIN FETCH u.departmentDistrict d " +
       "LEFT JOIN FETCH u.departmentBlock b " +
       "WHERE u.id NOT IN (" +
       "    SELECT DISTINCT r.user.id FROM Receipt r " +
       "    WHERE MONTH(r.paymentDate) = :month " +
       "    AND YEAR(r.paymentDate) = :year" +
       ") " +
       "AND (:name IS NULL OR LOWER(CONCAT(u.name, ' ', COALESCE(u.surname, ''))) LIKE LOWER(CONCAT('%', :name, '%')) " +
       "     OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
       "     OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :name, '%'))) " +
       "AND (:mobile IS NULL OR u.mobileNumber LIKE CONCAT('%', :mobile, '%')) " +
       "AND (:userId IS NULL OR u.id LIKE CONCAT('%', :userId, '%')) " +
       "AND (:sambhag IS NULL OR LOWER(COALESCE(sa.name, '')) LIKE LOWER(CONCAT('%', :sambhag, '%'))) " +
       "AND (:district IS NULL OR LOWER(COALESCE(d.name, '')) LIKE LOWER(CONCAT('%', :district, '%'))) " +
       "AND (:block IS NULL OR LOWER(COALESCE(b.name, '')) LIKE LOWER(CONCAT('%', :block, '%')))")
List<User> searchNonDonorsForExport(
        @Param("month") int month,
        @Param("year") int year,
        @Param("name") String name,
        @Param("mobile") String mobile,
        @Param("userId") String userId,
        @Param("sambhag") String sambhag,
        @Param("district") String district,
        @Param("block") String block
);

@Query("SELECT u FROM User u " +
       "LEFT JOIN FETCH u.departmentState s " +
       "LEFT JOIN FETCH u.departmentSambhag sa " +
       "LEFT JOIN FETCH u.departmentDistrict d " +
       "LEFT JOIN FETCH u.departmentBlock b " +
       "WHERE (" +
       "   u.department IS NULL OR TRIM(u.department) = '' " +
       "   OR u.departmentState IS NULL " +
       "   OR u.departmentSambhag IS NULL " +
       "   OR u.departmentDistrict IS NULL " +
       "   OR u.departmentBlock IS NULL " +
       "   OR u.schoolOfficeName IS NULL OR TRIM(u.schoolOfficeName) = ''" +
       ") " +
       "AND (:sambhagId IS NULL OR sa.id = :sambhagId) " +
       "AND (:districtId IS NULL OR d.id = :districtId) " +
       "AND (:blockId IS NULL OR b.id = :blockId) " +
       "AND (:name IS NULL OR LOWER(CONCAT(u.name, ' ', COALESCE(u.surname, ''))) LIKE LOWER(CONCAT('%', :name, '%')) " +
       "     OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
       "     OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :name, '%'))) " +
       "AND (:mobile IS NULL OR u.mobileNumber LIKE CONCAT('%', :mobile, '%')) " +
       "AND (:userId IS NULL OR LOWER(u.id) LIKE LOWER(CONCAT('%', :userId, '%')))")
List<User> findPendingProfileUsersForExport(
        @Param("sambhagId") String sambhagId,
        @Param("districtId") String districtId,
        @Param("blockId") String blockId,
        @Param("name") String name,
        @Param("mobile") String mobile,
        @Param("userId") String userId
);
@Query("""
    SELECT u
    FROM User u
    LEFT JOIN FETCH u.departmentState s
    LEFT JOIN FETCH u.departmentSambhag sa
    LEFT JOIN FETCH u.departmentDistrict d
    LEFT JOIN FETCH u.departmentBlock b
    LEFT JOIN u.assignedDeathCase adc
    WHERE u.role = com.example.kalyan_kosh_api.entity.Role.ROLE_USER
      AND (:name IS NULL OR
           LOWER(CONCAT(COALESCE(u.name, ''), ' ', COALESCE(u.surname, ''))) LIKE LOWER(CONCAT('%', :name, '%')) OR
           LOWER(COALESCE(u.name, '')) LIKE LOWER(CONCAT('%', :name, '%')) OR
           LOWER(COALESCE(u.surname, '')) LIKE LOWER(CONCAT('%', :name, '%')))
      AND (:mobile IS NULL OR COALESCE(u.mobileNumber, '') LIKE CONCAT('%', :mobile, '%'))
      AND (:userId IS NULL OR COALESCE(u.id, '') LIKE CONCAT('%', :userId, '%'))
      AND (:sambhag IS NULL OR LOWER(COALESCE(sa.name, '')) LIKE LOWER(CONCAT('%', :sambhag, '%')))
      AND (:district IS NULL OR LOWER(COALESCE(d.name, '')) LIKE LOWER(CONCAT('%', :district, '%')))
      AND (:block IS NULL OR LOWER(COALESCE(b.name, '')) LIKE LOWER(CONCAT('%', :block, '%')))
      AND (
            (:beneficiaryId IS NULL AND NOT EXISTS (
                SELECT 1
                FROM Receipt r
                WHERE r.user.id = u.id
                  AND r.amount > 0
            ))
            OR
            (:beneficiaryId IS NOT NULL
                AND adc.id = :beneficiaryId
                AND NOT EXISTS (
                    SELECT 1
                    FROM Receipt r
                    WHERE r.user.id = u.id
                      AND r.deathCase IS NOT NULL
                      AND r.deathCase.id = :beneficiaryId
                      AND r.amount > 0
                )
            )
      )
    """)
List<User> searchNonDonorsByBeneficiaryForExport(
        @Param("beneficiaryId") Long beneficiaryId,
        @Param("name") String name,
        @Param("mobile") String mobile,
        @Param("userId") String userId,
        @Param("sambhag") String sambhag,
        @Param("district") String district,
        @Param("block") String block
);

@Query("""
    SELECT u
    FROM User u
    LEFT JOIN FETCH u.departmentState s
    LEFT JOIN FETCH u.departmentSambhag sa
    LEFT JOIN FETCH u.departmentDistrict d
    LEFT JOIN FETCH u.departmentBlock b
    WHERE u.role = com.example.kalyan_kosh_api.entity.Role.ROLE_USER
      AND NOT EXISTS (
          SELECT 1
          FROM Receipt r
          WHERE r.user.id = u.id
            AND r.amount > 0
      )
    ORDER BY u.createdAt DESC
    """)
List<User> searchAllNonDonorsForExport();

// ✅ Search non-donors by name and/or mobile and/or userId with pagination
  @Query(value = "SELECT u FROM User u " +
       "LEFT JOIN FETCH u.departmentState s " +
       "LEFT JOIN FETCH u.departmentSambhag sa " +
       "LEFT JOIN FETCH u.departmentDistrict d " +
       "LEFT JOIN FETCH u.departmentBlock b " +
       "WHERE u.id NOT IN (" +
       "    SELECT DISTINCT r.user.id FROM Receipt r " +
       "    WHERE MONTH(r.paymentDate) = :month " +
       "    AND YEAR(r.paymentDate) = :year" +
       ") " +
       "AND (:name IS NULL OR LOWER(CONCAT(u.name, ' ', COALESCE(u.surname, ''))) LIKE LOWER(CONCAT('%', :name, '%')) " +
       "     OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
       "     OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :name, '%'))) " +
       "AND (:mobile IS NULL OR u.mobileNumber LIKE CONCAT('%', :mobile, '%')) " +
       "AND (:userId IS NULL OR u.id LIKE CONCAT('%', :userId, '%')) " +
       "AND (:sambhag IS NULL OR LOWER(COALESCE(sa.name, '')) LIKE LOWER(CONCAT('%', :sambhag, '%'))) " +
       "AND (:district IS NULL OR LOWER(COALESCE(d.name, '')) LIKE LOWER(CONCAT('%', :district, '%'))) " +
       "AND (:block IS NULL OR LOWER(COALESCE(b.name, '')) LIKE LOWER(CONCAT('%', :block, '%')))",
       countQuery = "SELECT COUNT(u) FROM User u " +
       "LEFT JOIN u.departmentSambhag sa " +
       "LEFT JOIN u.departmentDistrict d " +
       "LEFT JOIN u.departmentBlock b " +
       "WHERE u.id NOT IN (" +
       "    SELECT DISTINCT r.user.id FROM Receipt r " +
       "    WHERE MONTH(r.paymentDate) = :month " +
       "    AND YEAR(r.paymentDate) = :year" +
       ") " +
       "AND (:name IS NULL OR LOWER(CONCAT(u.name, ' ', COALESCE(u.surname, ''))) LIKE LOWER(CONCAT('%', :name, '%')) " +
       "     OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
       "     OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :name, '%'))) " +
       "AND (:mobile IS NULL OR u.mobileNumber LIKE CONCAT('%', :mobile, '%')) " +
       "AND (:userId IS NULL OR u.id LIKE CONCAT('%', :userId, '%')) " +
       "AND (:sambhag IS NULL OR LOWER(COALESCE(sa.name, '')) LIKE LOWER(CONCAT('%', :sambhag, '%'))) " +
       "AND (:district IS NULL OR LOWER(COALESCE(d.name, '')) LIKE LOWER(CONCAT('%', :district, '%'))) " +
       "AND (:block IS NULL OR LOWER(COALESCE(b.name, '')) LIKE LOWER(CONCAT('%', :block, '%')))")
Page<User> searchNonDonorsPaginated(
        @Param("month") int month,
        @Param("year") int year,
        @Param("name") String name,
        @Param("mobile") String mobile,
        @Param("userId") String userId,
        @Param("sambhag") String sambhag,
        @Param("district") String district,
        @Param("block") String block,
        Pageable pageable
);
long countByStatus(UserStatus status);
}
