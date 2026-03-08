package com.shailesh.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign Client for User Service
 *
 * Provides inter-service communication with user-service.
 * Uses Spring Cloud's declarative HTTP client (Feign) for retry, circuit breaking, etc.
 *
 * This client allows auth-service to:
 * - Look up users by email or SAML subject
 * - Create new users after SAML authentication
 * - Update user information
 */
@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/users/email")
    ResponseEntity<UserDto> getUserByEmail(@RequestParam("email") String email);

    @GetMapping("/users/saml")
    ResponseEntity<UserDto> getUserBySamlSubject(@RequestParam("samlSubject") String samlSubject);

    @PostMapping("/users")
    ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest request);

    @GetMapping("/users/id/{id}")
    ResponseEntity<UserDto> getUserById(@PathVariable("id") String id);

    // DTOs for inter-service communication
    record UserDto(
            String id,
            String username,
            String email,
            String firstName,
            String lastName,
            String roles,
            String samlSubject
    ) {}

    record CreateUserRequest(
            String username,
            String email,
            String firstName,
            String lastName,
            String samlSubject,
            String roles
    ) {}
}
