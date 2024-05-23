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

package com.veda.central.api.tenant;

import com.veda.central.core.tenant.management.api.CreateTenantResponse;
import com.veda.central.core.tenant.management.api.Credentials;
import com.veda.central.core.tenant.management.api.DeleteTenantRequest;
import com.veda.central.core.tenant.management.api.GetTenantRequest;
import com.veda.central.core.tenant.management.api.UpdateTenantRequest;
import com.veda.central.core.tenant.profile.api.Tenant;
import com.veda.central.core.tenant.profile.api.TenantStatus;
import com.veda.central.core.tenant.profile.api.UpdateStatusRequest;
import com.veda.central.core.tenant.profile.api.UpdateStatusResponse;
import com.veda.central.service.auth.AuthClaim;
import com.veda.central.service.auth.TokenAuthorizer;
import com.veda.central.service.identity.Constants;
import com.veda.central.service.management.TenantManagementService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tenant-management")
@Tag(name = "Tenant Management")
public class TenantManagementController {

    private final TenantManagementService tenantManagementService;
    private final TokenAuthorizer tokenAuthorizer;


    public TenantManagementController(TenantManagementService tenantManagementService, TokenAuthorizer tokenAuthorizer) {
        this.tenantManagementService = tenantManagementService;
        this.tokenAuthorizer = tokenAuthorizer;
    }

    @PostMapping("/oauth2/tenant")
    public ResponseEntity<CreateTenantResponse> createTenant(@Valid @RequestBody Tenant request, @RequestHeader HttpHeaders headers) {
        String token = tokenAuthorizer.getToken(headers);

        if (StringUtils.isBlank(token)) {
            Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers);
            if (claim.isPresent()) {
                AuthClaim authClaim = claim.get();
                request = request.toBuilder().setParentTenantId(authClaim.getTenantId()).build();
            }
        }

        CreateTenantResponse response = tenantManagementService.createTenant(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/oauth2/tenant")
    public ResponseEntity<Tenant> getTenant(@Valid @RequestBody GetTenantRequest request, @RequestHeader HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorizeUsingUserToken(headers);
        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            Credentials credentials = getCredentials(authClaim);

            request = request.toBuilder()
                    .setTenantId(authClaim.getTenantId())
                    .setCredentials(credentials)
                    .build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized, token not found");
        }

        Tenant response = tenantManagementService.getTenant(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user/profile")
    public ResponseEntity<Tenant> updateTenant(@Valid @RequestBody UpdateTenantRequest request, @RequestHeader HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorizeUsingUserToken(headers);
        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            Credentials credentials = getCredentials(authClaim);

            request = request.toBuilder()
                    .setTenantId(authClaim.getTenantId())
                    .setCredentials(credentials)
                    .build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized, token not found");
        }

        Tenant response = tenantManagementService.updateTenant(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user")
    public ResponseEntity<?> deleteTenant(@Valid @RequestBody DeleteTenantRequest request, @RequestHeader HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorizeUsingUserToken(headers);
        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            Credentials credentials = getCredentials(authClaim);

            request = request.toBuilder()
                    .setTenantId(authClaim.getTenantId())
                    .setCredentials(credentials)
                    .build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized, token not found");
        }

        tenantManagementService.deleteTenant(request);
        return ResponseEntity.noContent().build();
    }


    private Credentials getCredentials(AuthClaim claim) {
        return Credentials.newBuilder()
                .setVedaClientId(claim.getVedaId())
                .setVedaClientSecret(claim.getVedaSecret())
                .setVedaClientIdIssuedAt(claim.getVedaIdIssuedAt())
                .setVedaClientSecretExpiredAt(claim.getVedaSecretExpiredAt())
                .setIamClientId(claim.getIamAuthId())
                .setIamClientSecret(claim.getIamAuthSecret())
                .setCiLogonClientId(claim.getCiLogonId())
                .setCiLogonClientSecret(claim.getCiLogonSecret()).build();
    }
}
