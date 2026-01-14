package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, String> {
    // Removed username-based methods since username field is removed
    Optional<User> findByMobileNumber(String mobile);
    Optional<User> findByEmail(String email);  // Added email-based lookup for authentication
    Optional<User> findByDepartmentUniqueId(String departmentUniqueId);

    // ✅ Fetch all users with their location relationships
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.departmentState s " +
           "LEFT JOIN FETCH u.departmentSambhag sa " +
           "LEFT JOIN FETCH u.departmentDistrict d " +
           "LEFT JOIN FETCH u.departmentBlock b")
    List<User> findAllWithLocations();

    // ✅ Paginated query - fetch users with locations (insertion order by createdAt ASC)
    @Query(value = "SELECT u FROM User u " +
           "LEFT JOIN FETCH u.departmentState s " +
           "LEFT JOIN FETCH u.departmentSambhag sa " +
           "LEFT JOIN FETCH u.departmentDistrict d " +
           "LEFT JOIN FETCH u.departmentBlock b",
           countQuery = "SELECT COUNT(u) FROM User u")
    Page<User> findAllWithLocationsPaginated(Pageable pageable);

    // ✅ Filtered + Paginated query with Sambhag, District, Block, Name, Mobile filters
    @Query(value = "SELECT u FROM User u " +
           "LEFT JOIN FETCH u.departmentState s " +
           "LEFT JOIN FETCH u.departmentSambhag sa " +
           "LEFT JOIN FETCH u.departmentDistrict d " +
           "LEFT JOIN FETCH u.departmentBlock b " +
           "WHERE (:sambhagId IS NULL OR sa.id = :sambhagId) " +
           "AND (:districtId IS NULL OR d.id = :districtId) " +
           "AND (:blockId IS NULL OR b.id = :blockId) " +
           "AND (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "     OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:mobile IS NULL OR u.mobileNumber LIKE CONCAT('%', :mobile, '%'))",
           countQuery = "SELECT COUNT(u) FROM User u " +
           "LEFT JOIN u.departmentSambhag sa " +
           "LEFT JOIN u.departmentDistrict d " +
           "LEFT JOIN u.departmentBlock b " +
           "WHERE (:sambhagId IS NULL OR sa.id = :sambhagId) " +
           "AND (:districtId IS NULL OR d.id = :districtId) " +
           "AND (:blockId IS NULL OR b.id = :blockId) " +
           "AND (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "     OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:mobile IS NULL OR u.mobileNumber LIKE CONCAT('%', :mobile, '%'))")
    Page<User> findAllWithFilters(
            @Param("sambhagId") UUID sambhagId,
            @Param("districtId") UUID districtId,
            @Param("blockId") UUID blockId,
            @Param("name") String name,
            @Param("mobile") String mobile,
            Pageable pageable);

    // ✅ Fetch single user with location relationships
    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.departmentState s " +
           "LEFT JOIN FETCH u.departmentSambhag sa " +
           "LEFT JOIN FETCH u.departmentDistrict d " +
           "LEFT JOIN FETCH u.departmentBlock b " +
           "WHERE u.id = :id")
    Optional<User> findByIdWithLocations(String id);

    // ✅ Optimized: Find non-donors (users who haven't donated in a specific month/year)
    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.departmentState s " +
           "LEFT JOIN FETCH u.departmentSambhag sa " +
           "LEFT JOIN FETCH u.departmentDistrict d " +
           "LEFT JOIN FETCH u.departmentBlock b " +
           "WHERE u.id NOT IN (" +
           "    SELECT DISTINCT r.user.id FROM Receipt r " +
           "    WHERE MONTH(r.paymentDate) = :month " +
           "    AND YEAR(r.paymentDate) = :year " +
           "    AND r.status = 'VERIFIED'" +
           ")")
    List<User> findNonDonors(@Param("month") int month, @Param("year") int year);

    // ✅ Optimized: Find non-donors with pagination
    @Query(value = "SELECT u FROM User u " +
           "LEFT JOIN FETCH u.departmentState s " +
           "LEFT JOIN FETCH u.departmentSambhag sa " +
           "LEFT JOIN FETCH u.departmentDistrict d " +
           "LEFT JOIN FETCH u.departmentBlock b " +
           "WHERE u.id NOT IN (" +
           "    SELECT DISTINCT r.user.id FROM Receipt r " +
           "    WHERE MONTH(r.paymentDate) = :month " +
           "    AND YEAR(r.paymentDate) = :year " +
           "    AND r.status = 'VERIFIED'" +
           ")",
           countQuery = "SELECT COUNT(u) FROM User u " +
           "WHERE u.id NOT IN (" +
           "    SELECT DISTINCT r.user.id FROM Receipt r " +
           "    WHERE MONTH(r.paymentDate) = :month " +
           "    AND YEAR(r.paymentDate) = :year " +
           "    AND r.status = 'VERIFIED'" +
           ")")
    Page<User> findNonDonorsPaginated(@Param("month") int month, @Param("year") int year, Pageable pageable);
}
