package com.shailesh.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Authentication Service
 *
 * Responsible for:
 * 1. SAML SSO Integration
 *    - Receives SAML assertions from IdP
 *    - Validates SAML responses
 *    - Extracts user information from SAML assertion
 *
 * 2. User Management
 *    - Looks up users via user-service
 *    - Creates new users if not exist
 *    - Associates SAML identity with user account
 *
 * 3. JWT Token Issuance
 *    - Issues JWTs after successful SAML validation
 *    - JWTs contain user ID, email, roles, tenant info
 *    - Uses RS256 (asymmetric) signature for added security
 *
 * Flow:
 * 1. User authenticates with IdP (SAML IdP)
 * 2. IdP redirects to /auth/saml/acs with SAML response
 * 3. Auth-service validates SAML signature and content
 * 4. Auth-service calls user-service to get/create user
 * 5. Auth-service issues JWT token
 * 6. JWT is returned to client
 * 7. Client uses JWT in Authorization header for subsequent API calls
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
