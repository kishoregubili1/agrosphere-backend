package com.agrosphere.repository;

import com.agrosphere.entity.Tenant;
import com.agrosphere.entity.User;
import com.agrosphere.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.tenant WHERE u.role = :role")
    List<User> findByRoleWithTenant(@Param("role") Role role);
    @Query("SELECT u FROM User u WHERE u.tenant=:t AND u.role='FARMER'")
    User findByTenantAndRole(@Param("t") Tenant t);
}
