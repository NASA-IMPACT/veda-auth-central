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

import com.veda.central.api.util.ProtobufJsonUtil;
import com.veda.central.core.credential.store.api.Credentials;
import com.veda.central.core.identity.api.AuthToken;
import com.veda.central.core.identity.api.AuthenticationRequest;
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
public class IdentityManagementController {

    private final IdentityManagementService identityManagementService;
    private final TokenAuthorizer tokenAuthorizer;

    public IdentityManagementController(IdentityManagementService identityManagementService, TokenAuthorizer tokenAuthorizer) {
        this.identityManagementService = identityManagementService;
        this.tokenAuthorizer = tokenAuthorizer;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthToken> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        AuthToken response = identityManagementService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/authenticate/status")
    public ResponseEntity<IsAuthenticatedResponse> isAuthenticated(@Valid @RequestBody AuthToken token) {
        IsAuthenticatedResponse response = identityManagementService.isAuthenticated(token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<User> getUser(@Valid @RequestBody AuthToken token) {
        User response = identityManagementService.getUser(token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/account/token")
    public ResponseEntity<AuthToken> getUserManagementServiceAccountAccessToken(@Valid @RequestBody GetUserManagementSATokenRequest request) {
        AuthToken response = identityManagementService.getUserManagementServiceAccountAccessToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/logout")
    public ResponseEntity<OperationStatus> endUserSession(@Valid @RequestBody EndSessionRequest request) {
        OperationStatus response = identityManagementService.endUserSession(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/authorize")
    public ResponseEntity<AuthorizationResponse> authorize(@Valid @RequestBody AuthorizationRequest request) {
        AuthorizationResponse response = identityManagementService.authorize(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> token(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        // Expects the Base64 encoded value 'clientId:clientSecret' for Authorization Header
        GetTokenRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, GetTokenRequest.newBuilder());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers);

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            builder.setTenantId(authClaim.getTenantId())
                    .setClientId(authClaim.getIamAuthId())
                    .setClientSecret(authClaim.getIamAuthSecret());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        TokenResponse response = identityManagementService.token(builder.build());
        return ResponseEntity.ok(ProtobufJsonUtil.protobufToJson(response));
    }

    @GetMapping("/credentials")
    public ResponseEntity<Credentials> getCredentials(@Valid @RequestBody GetCredentialsRequest request) {
        Credentials response = identityManagementService.getCredentials(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/.well-known/openid-configuration")
    public ResponseEntity<OIDCConfiguration> getOIDCConfiguration(@Valid @RequestBody GetOIDCConfiguration request) {
        OIDCConfiguration response = identityManagementService.getOIDCConfiguration(request);
        return ResponseEntity.ok(response);
    }
}
