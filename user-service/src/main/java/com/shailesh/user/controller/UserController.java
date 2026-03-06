package com.shailesh.user.controller;

import com.shailesh.user.entity.User;
import com.shailesh.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * User Controller
 *
 * REST endpoints for user management.
 * All operations are persisted to MySQL and automatically captured by Debezium CDC.
 *
 * Endpoints:
 * GET /users/id/{id} - Get user by ID
 * GET /users/email - Get user by email (query param: email)
 * GET /users/saml - Get user by SAML subject (query param: samlSubject)
 * POST /users - Create new user
 * PUT /users/{id} - Update user
 * DELETE /users/{id} - Delete user
 */
@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(u -> ResponseEntity.ok(toDto(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/email")
    public ResponseEntity<UserDto> getUserByEmail(@RequestParam String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(u -> ResponseEntity.ok(toDto(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/saml")
    public ResponseEntity<UserDto> getUserBySamlSubject(@RequestParam String samlSubject) {
        Optional<User> user = userRepository.findBySamlSubject(samlSubject);
        return user.map(u -> ResponseEntity.ok(toDto(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest request) {
        try {
            User user = User.builder()
                    .username(request.username())
                    .email(request.email())
                    .firstName(request.firstName())
                    .lastName(request.lastName())
                    .samlSubject(request.samlSubject())
                    .roles(request.roles() != null ? request.roles() : "ROLE_USER")
                    .build();

            User saved = userRepository.save(user);
            log.info("User created: {} ({})", saved.getEmail(), saved.getId());

            // This save operation triggers a database INSERT
            // Debezium captures this change and publishes it to Kafka topic: user-changes
            // Event includes: {op: "c", before: null, after: {id, username, email, ...}, source: {...}}

            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
        } catch (Exception e) {
            log.error("Failed to create user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            if (request.firstName() != null) user.setFirstName(request.firstName());
            if (request.lastName() != null) user.setLastName(request.lastName());
            if (request.roles() != null) user.setRoles(request.roles());

            User updated = userRepository.save(user);
            log.info("User updated: {} ({})", updated.getEmail(), updated.getId());

            // This update operation triggers a database UPDATE
            // Debezium captures this change and publishes it to Kafka

            return ResponseEntity.ok(toDto(updated));
        } catch (Exception e) {
            log.error("Failed to update user: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            if (!userRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            userRepository.deleteById(id);
            log.info("User deleted: {}", id);

            // This delete operation triggers a database DELETE
            // Debezium captures this change and publishes it to Kafka

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete user: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles(),
                user.getSamlSubject()
        );
    }

    // DTOs
    public record UserDto(
            String id,
            String username,
            String email,
            String firstName,
            String lastName,
            String roles,
            String samlSubject
    ) {}

    public record CreateUserRequest(
            String username,
            String email,
            String firstName,
            String lastName,
            String samlSubject,
            String roles
    ) {}

    public record UpdateUserRequest(
            String firstName,
            String lastName,
            String roles
    ) {}
}
