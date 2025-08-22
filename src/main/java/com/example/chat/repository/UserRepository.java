package com.example.chat.repository;

import com.example.chat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.messages WHERE u.username = :username")
    Optional<User> findByUsernameWithMessages(@Param("username") String username);
}
