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

    // ✅ Find users by role
    List<User> findByRole(Role role);

    // ✅ Fetch all users with their location relationships
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.departmentState s " +
           "LEFT JOIN FETCH u.departmentSambhag sa " +
           "LEFT JOIN FETCH u.departmentDistrict d " +
           "LEFT JOIN FETCH u.departmentBlock b")
    List<User> findAllWithLocations();

    // ✅ Paginated query - fetch users with locations
    @Query(value = "SELECT u FROM User u " +
           "LEFT JOIN FETCH u.departmentState s " +
           "LEFT JOIN FETCH u.departmentSambhag sa " +
           "LEFT JOIN FETCH u.departmentDistrict d " +
           "LEFT JOIN FETCH u.departmentBlock b",
           countQuery = "SELECT COUNT(u) FROM User u")
    Page<User> findAllWithLocations(Pageable pageable);

    // ✅ Filtered + Paginated query with Sambhag, District, Block, Name, Mobile, UserId filters
    @Query(value = "SELECT u FROM User u " +
           "LEFT JOIN FETCH u.departmentState s " +
           "LEFT JOIN FETCH u.departmentSambhag sa " +
           "LEFT JOIN FETCH u.departmentDistrict d " +
           "LEFT JOIN FETCH u.departmentBlock b " +
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

    // ✅ Fetch single user with location relationships
    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.departmentState s " +
           "LEFT JOIN FETCH u.departmentSambhag sa " +
           "LEFT JOIN FETCH u.departmentDistrict d " +
           "LEFT JOIN FETCH u.departmentBlock b " +
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
           "AND (:userId IS NULL OR u.id LIKE CONCAT('%', :userId, '%'))",
           countQuery = "SELECT COUNT(u) FROM User u " +
           "WHERE u.id NOT IN (" +
           "    SELECT DISTINCT r.user.id FROM Receipt r " +
           "    WHERE MONTH(r.paymentDate) = :month " +
           "    AND YEAR(r.paymentDate) = :year" +
           ") " +
           "AND (:name IS NULL OR LOWER(CONCAT(u.name, ' ', COALESCE(u.surname, ''))) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "     OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "     OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:mobile IS NULL OR u.mobileNumber LIKE CONCAT('%', :mobile, '%')) " +
           "AND (:userId IS NULL OR u.id LIKE CONCAT('%', :userId, '%'))")
    Page<User> searchNonDonorsPaginated(
            @Param("month") int month,
            @Param("year") int year,
            @Param("name") String name,
            @Param("mobile") String mobile,
            @Param("userId") String userId,
            Pageable pageable);
    
    // Count methods for manager statistics
    long countByStatus(UserStatus status);
}
