package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

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

    // ✅ Fetch single user with location relationships
    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.departmentState s " +
           "LEFT JOIN FETCH u.departmentSambhag sa " +
           "LEFT JOIN FETCH u.departmentDistrict d " +
           "LEFT JOIN FETCH u.departmentBlock b " +
           "WHERE u.id = :id")
    Optional<User> findByIdWithLocations(String id);
}
