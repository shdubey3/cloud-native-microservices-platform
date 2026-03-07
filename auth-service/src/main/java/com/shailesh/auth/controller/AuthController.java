package com.shailesh.auth.controller;

import com.shailesh.auth.client.UserServiceClient;
import com.shailesh.auth.service.JwtTokenService;
import com.shailesh.auth.service.SamlValidatorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 *
 * Exposes endpoints for:
 * 1. SAML Assertion Consumer Service (ACS) - /auth/saml/acs
 *    Receives SAML response from IdP, validates it, creates/updates user, and issues JWT
 *
 * 2. Token Generation - POST /auth/token
 *    Generates JWT tokens (used for demo/testing)
 *
 * Security Note:
 * - SAML endpoint is public (no pre-auth required)
 * - Token endpoint requires valid SAML assertion (in real implementation)
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final SamlValidatorService samlValidator;
    private final JwtTokenService jwtTokenService;
    private final UserServiceClient userServiceClient;

    public AuthController(SamlValidatorService samlValidator, JwtTokenService jwtTokenService, UserServiceClient userServiceClient) {
        this.samlValidator = samlValidator;
        this.jwtTokenService = jwtTokenService;
        this.userServiceClient = userServiceClient;
    }

    /**
     * SAML Assertion Consumer Service Endpoint
     *
     * Receives SAML response from Identity Provider (IdP).
     * This is where the IdP redirects after user authentication.
     *
     * Flow:
     * 1. IdP POSTs SAML response to this endpoint
     * 2. We validate the SAML assertion
     * 3. We look up or create the user in user-service
     * 4. We generate a JWT token
     * 5. We redirect client to frontend with JWT in query param or fragment
     *
     * @param samlResponse Base64-encoded SAML response from IdP
     * @return TokenResponse with JWT token
     */
    @PostMapping("/saml/acs")
    public ResponseEntity<TokenResponse> samlAcs(@RequestParam String samlResponse) {
        try {
            // Validate SAML response and extract claims
            SamlValidatorService.SamlAssertion assertion = samlValidator.validateSamlResponse(samlResponse);
            log.info("SAML assertion validated for user: {}", assertion.email());

            // Try to find existing user by email
            UserServiceClient.UserDto user = null;
            try {
                user = userServiceClient.getUserByEmail(assertion.email()).getBody();
                log.info("Found existing user for email: {}", assertion.email());
            } catch (Exception e) {
                log.debug("User not found, will create new user for email: {}", assertion.email());
            }

            // If user doesn't exist, create them
            if (user == null) {
                var createRequest = new UserServiceClient.CreateUserRequest(
                        assertion.firstName() + "." + assertion.lastName(),
                        assertion.email(),
                        assertion.firstName(),
                        assertion.lastName(),
                        assertion.samlSubject(),
                        assertion.roles()
                );
                user = userServiceClient.createUser(createRequest).getBody();
                log.info("Created new user: {}", user.email());
            }

            // Generate JWT token
            String jwtToken = jwtTokenService.generateToken(
                    user.id(),
                    user.email(),
                    user.roles(),
                    "default" // tenant ID
            );

            return ResponseEntity.ok(new TokenResponse(jwtToken, user.id(), user.email()));

        } catch (Exception e) {
            log.error("SAML authentication failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Generate JWT Token Endpoint
     *
     * Demo endpoint to generate JWT tokens for testing.
     * In production, replace with SAML-based authentication.
     *
     * @param request TokenRequest with userId and email
     * @return TokenResponse with generated JWT
     */
    @PostMapping("/token")
    public ResponseEntity<TokenResponse> generateToken(@Valid @RequestBody TokenRequest request) {
        try {
            String token = jwtTokenService.generateToken(
                    request.userId(),
                    request.email(),
                    request.roles() != null ? request.roles() : "ROLE_USER",
                    "default"
            );
            return ResponseEntity.ok(new TokenResponse(token, request.userId(), request.email()));
        } catch (Exception e) {
            log.error("Token generation failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }

    // DTOs
    public record TokenRequest(
            @NotBlank String userId,
            @NotBlank @Email String email,
            String roles
    ) {}

    public record TokenResponse(
            String token,
            String userId,
            String email
    ) {}
}
