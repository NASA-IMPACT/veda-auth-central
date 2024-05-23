/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package com.veda.central.api.identity;

import com.veda.central.core.credential.store.api.Credentials;
import com.veda.central.core.identity.api.AuthToken;
import com.veda.central.core.identity.api.AuthenticationRequest;
import com.veda.central.core.identity.api.Claim;
import com.veda.central.core.identity.api.GetOIDCConfiguration;
import com.veda.central.core.identity.api.GetTokenRequest;
import com.veda.central.core.identity.api.GetUserManagementSATokenRequest;
import com.veda.central.core.identity.api.IsAuthenticatedResponse;
import com.veda.central.core.identity.api.OIDCConfiguration;
import com.veda.central.core.identity.api.OperationStatus;
import com.veda.central.core.identity.api.TokenResponse;
import com.veda.central.core.identity.api.User;
import com.veda.central.core.identity.management.api.AuthorizationRequest;
import com.veda.central.core.identity.management.api.AuthorizationResponse;
import com.veda.central.core.identity.management.api.EndSessionRequest;
import com.veda.central.core.identity.management.api.GetCredentialsRequest;
import com.veda.central.service.auth.AuthClaim;
import com.veda.central.service.auth.TokenAuthorizer;
import com.veda.central.service.management.IdentityManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/identity-management")
@Tag(name = "Identity Management")
public class IdentityManagementController {

    private final IdentityManagementService identityManagementService;
    private final TokenAuthorizer tokenAuthorizer;

    public IdentityManagementController(IdentityManagementService identityManagementService, TokenAuthorizer tokenAuthorizer) {
        this.identityManagementService = identityManagementService;
        this.tokenAuthorizer = tokenAuthorizer;
    }

    @PostMapping("/authenticate")
    @Operation(
            summary = "User Authentication",
            description = "Authenticates the user by verifying the provided request credentials. If authenticated successfully, " +
                    "returns an AuthToken which includes authentication details and associated claims."
    )
    public ResponseEntity<AuthToken> authenticate(@RequestBody AuthenticationRequest request, @RequestHeader HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers);

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            request.toBuilder().setTenantId(authClaim.getTenantId())
                    .setClientId(authClaim.getIamAuthId())
                    .setClientSecret(authClaim.getIamAuthSecret());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        AuthToken response = identityManagementService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/authenticate/status")
    @Operation(
            summary = "Authentication Status Check",
            description = "Checks the authentication status based on the provided AuthToken. Returns an IsAuthenticatedResponse portraying the status."
    )
    public ResponseEntity<IsAuthenticatedResponse> isAuthenticated(@RequestBody AuthToken request, @RequestHeader HttpHeaders headers) {
        IsAuthenticatedResponse response = identityManagementService.isAuthenticated(generateAuthTokenRequest(request.toBuilder(), headers).build());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    @Operation(
            summary = "Retrieve User Information",
            description = "Retrieves User Information using the provided AuthToken. Returns a User object containing user details."
    )
    public ResponseEntity<User> getUser(@RequestBody AuthToken request, @RequestHeader HttpHeaders headers) {
        User response = identityManagementService.getUser(generateAuthTokenRequest(request.toBuilder(), headers).build());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/account/token")
    @Operation(
            summary = "Get User Management Service Account Access Token",
            description = "Retrieves the User Management Service Account Access Token using the provided GetUserManagementSATokenRequest. " +
                    "Returns an AuthToken for the user management service account."
    )
    public ResponseEntity<AuthToken> getUserManagementServiceAccountAccessToken(@Valid @RequestBody GetUserManagementSATokenRequest request, @RequestHeader HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers);
        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            request = request.toBuilder().setTenantId(authClaim.getTenantId())
                    .setClientId(authClaim.getIamAuthId())
                    .setClientSecret(authClaim.getIamAuthSecret()).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        AuthToken response = identityManagementService.getUserManagementServiceAccountAccessToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/logout")
    @Operation(
            summary = "End User Session",
            description = "Ends the user session based on the provided EndSessionRequest. " +
                    "Returns an OperationStatus response confirming the action."
    )
    public ResponseEntity<OperationStatus> endUserSession(@RequestBody EndSessionRequest request, @RequestHeader HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers);

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            com.veda.central.core.identity.api.EndSessionRequest endSessionRequest = com.veda.central.core.identity.api.EndSessionRequest.newBuilder()
                    .setTenantId(authClaim.getTenantId())
                    .setClientId(authClaim.getIamAuthId())
                    .setClientSecret(authClaim.getIamAuthSecret())
                    .build();
            request = request.toBuilder().setBody(endSessionRequest).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        OperationStatus response = identityManagementService.endUserSession(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/authorize")
    @Operation(
            summary = "Authorize User",
            description = "Authorizes the user by verifying the provided AuthorizationRequest. If authorized, an AuthorizationResponse is returned."
    )
    public ResponseEntity<AuthorizationResponse> authorize(@RequestBody AuthorizationRequest request) {
        AuthorizationResponse response = identityManagementService.authorize(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get Token",
            description = "Retrieves a token based on the provided GetTokenRequest. If successful, returns a TokenResponse."
    )
    public ResponseEntity<TokenResponse> token(@RequestBody GetTokenRequest request, @RequestHeader HttpHeaders headers) {
        // Expects the Base64 encoded value 'clientId:clientSecret' for Authorization Header
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers);

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            request = request.toBuilder().setTenantId(authClaim.getTenantId())
                    .setClientId(authClaim.getIamAuthId())
                    .setClientSecret(authClaim.getIamAuthSecret())
                    .build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        TokenResponse response = identityManagementService.token(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/credentials")
    @Operation(
            summary = "Get Credentials",
            description = "Retrieves credentials based on the provided GetCredentialsRequest. Returns a Credentials object."
    )
    public ResponseEntity<Credentials> getCredentials(@RequestBody GetCredentialsRequest request, @RequestHeader HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, request.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            Credentials credentials = Credentials.newBuilder()
                    .setVedaClientId(authClaim.getVedaId())
                    .setVedaClientSecret(authClaim.getVedaSecret())
                    .setVedaClientIdIssuedAt(authClaim.getVedaIdIssuedAt())
                    .setVedaClientSecretExpiredAt(authClaim.getVedaSecretExpiredAt())
                    .setCiLogonClientId(authClaim.getCiLogonId())
                    .setCiLogonClientSecret(authClaim.getCiLogonSecret())
                    .setIamClientId(authClaim.getIamAuthId())
                    .setIamClientSecret(authClaim.getIamAuthSecret())
                    .build();
            request = request.toBuilder().setCredentials(credentials).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        Credentials response = identityManagementService.getCredentials(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/.well-known/openid-configuration")
    @Operation(
            summary = "Get OIDC Configuration",
            description = "Retrieves the OpenID Connect (OIDC) configuration using the provided GetOIDCConfiguration request. " +
                    "Returns an OIDCConfiguration object."
    )
    public ResponseEntity<OIDCConfiguration> getOIDCConfiguration(@RequestBody GetOIDCConfiguration request) {
        OIDCConfiguration response = identityManagementService.getOIDCConfiguration(request);
        return ResponseEntity.ok(response);
    }


    private AuthToken.Builder generateAuthTokenRequest(AuthToken.Builder builder, HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers);
        if (claim.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        Optional<AuthClaim> opAuthClaim = tokenAuthorizer.authorizeUsingUserToken(builder.getAccessToken());

        if (opAuthClaim.isPresent()) {
            AuthClaim authClaim = claim.get();
            Claim userClaim = Claim.newBuilder().setKey("username").setValue(authClaim.getUsername()).build();
            Claim tenantClaim = Claim.newBuilder().setKey("tenantId").setValue(String.valueOf(authClaim.getTenantId())).build();
            Claim clientClaim = Claim.newBuilder().setKey("clientId").setValue(String.valueOf(authClaim.getVedaId())).build();

            builder.addClaims(userClaim);
            builder.addClaims(tenantClaim);
            builder.addClaims(clientClaim);

            return builder;

        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized, User token not found");
        }
    }
}
