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

import com.google.protobuf.Message;
import com.veda.central.api.util.ProtobufJsonUtil;
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
    public ResponseEntity<String> authenticate(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        AuthenticationRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, AuthenticationRequest.newBuilder());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers);

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            builder.setTenantId(authClaim.getTenantId())
                    .setClientId(authClaim.getIamAuthId())
                    .setClientSecret(authClaim.getIamAuthSecret());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        AuthToken response = identityManagementService.authenticate(builder.build());
        return extractOkResponse(response);
    }

    @PostMapping("/authenticate/status")
    public ResponseEntity<String> isAuthenticated(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        IsAuthenticatedResponse response = identityManagementService.isAuthenticated(generateAuthTokenRequest(request, headers).build());
        return extractOkResponse(response);
    }

    @GetMapping("/user")
    public ResponseEntity<String> getUser(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        User response = identityManagementService.getUser(generateAuthTokenRequest(request, headers).build());
        return extractOkResponse(response);
    }

    @GetMapping("/account/token")
    public ResponseEntity<String> getUserManagementServiceAccountAccessToken(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        GetUserManagementSATokenRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, GetUserManagementSATokenRequest.newBuilder());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers);
        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            builder.setTenantId(authClaim.getTenantId())
                    .setClientId(authClaim.getIamAuthId())
                    .setClientSecret(authClaim.getIamAuthSecret());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        AuthToken response = identityManagementService.getUserManagementServiceAccountAccessToken(builder.build());
        return extractOkResponse(response);
    }

    @PostMapping("/user/logout")
    public ResponseEntity<String> endUserSession(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        EndSessionRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, EndSessionRequest.newBuilder());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers);

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            com.veda.central.core.identity.api.EndSessionRequest endSessionRequest = com.veda.central.core.identity.api.EndSessionRequest.newBuilder()
                    .setTenantId(authClaim.getTenantId())
                    .setClientId(authClaim.getIamAuthId())
                    .setClientSecret(authClaim.getIamAuthSecret())
                    .build();
            builder.setBody(endSessionRequest);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        OperationStatus response = identityManagementService.endUserSession(builder.build());
        return extractOkResponse(response);
    }

    @GetMapping("/authorize")
    public ResponseEntity<String> authorize(@RequestBody String request) {
        AuthorizationRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, AuthorizationRequest.newBuilder());
        AuthorizationResponse response = identityManagementService.authorize(builder.build());
        return extractOkResponse(response);
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
        return extractOkResponse(response);
    }

    @GetMapping("/credentials")
    public ResponseEntity<String> getCredentials(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        GetCredentialsRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, GetCredentialsRequest.newBuilder());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, builder.getClientId());

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
            builder.setCredentials(credentials);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        Credentials response = identityManagementService.getCredentials(builder.build());
        return extractOkResponse(response);
    }

    @GetMapping("/.well-known/openid-configuration")
    public ResponseEntity<String> getOIDCConfiguration(@RequestBody String request) {
        GetOIDCConfiguration.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, GetOIDCConfiguration.newBuilder());
        OIDCConfiguration response = identityManagementService.getOIDCConfiguration(builder.build());
        return extractOkResponse(response);
    }

    private ResponseEntity<String> extractOkResponse(Message message) {
        return ResponseEntity.ok(ProtobufJsonUtil.protobufToJson(message));
    }

    private AuthToken.Builder generateAuthTokenRequest(String request, HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers);
        if (claim.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        AuthToken.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, AuthToken.newBuilder());
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
