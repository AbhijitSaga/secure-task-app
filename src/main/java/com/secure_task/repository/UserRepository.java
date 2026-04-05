package com.secure_task.repository;

import com.secure_task.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // ✅ Used during login: load user by email
    Optional<User> findByEmail(String email);

    // ✅ Used during registration: check duplicate email
    boolean existsByEmail(String email);

}
