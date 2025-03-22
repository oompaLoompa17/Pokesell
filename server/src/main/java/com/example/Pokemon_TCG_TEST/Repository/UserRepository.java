package com.example.Pokemon_TCG_TEST.Repository;

import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import com.example.Pokemon_TCG_TEST.Model.User;

// This interface defines a repository for the User entity, extending CrudRepository for basic CRUD operations
// CrudRepository<User, Long> means it manages User objects with a Long primary key (id)
public interface UserRepository extends CrudRepository<User, Long> {
    
    // Checks if a user with the given email exists in the database
    // Used during registration to prevent duplicate email addresses
    // @Query runs a custom SQL query; here, it uses EXISTS to return true/false efficiently
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    boolean existsByEmail(String email);

    // Retrieves a user by their email, wrapped in an Optional to handle "not found" gracefully
    // Used for login (to verify credentials) and linking favorites to a user via email
    // @Query runs a custom SQL query to select all columns (*) from the users table
    // The result is mapped to a User object; Optional.empty() if no match
    @Query("SELECT * FROM users WHERE email = :email")
    Optional<User> findByEmail(String email);

    @Query("SELECT * FROM users WHERE telegram_subscription_code = :telegram_subscription_code")
    Optional<User> findByTelegramSubscriptionCode(String telegramSubscriptionCode);
}

// when defining an interface extending CrudRepository, Spring Data dynamically creates a concrete implementation (a proxy class) at runtime.
// You don’t write the class yourself - Spring uses libraries like CGLIB or JDK Dynamic Proxies to generate it based on the interface’s method signatures and annotations (e.g., @Query).
// Benefit: Saves you from writing boilerplate SQL execution code (e.g., JdbcTemplate calls) for every operation.