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

package com.veda.central.service.auth;

import com.veda.central.api.exception.UnauthorizedException;
import com.veda.central.core.credential.store.api.GetAllCredentialsResponse;
import com.veda.central.core.credential.store.api.TokenRequest;
import com.veda.central.core.credential.store.api.Type;
import com.veda.central.core.tenant.profile.api.GetTenantRequest;
import com.veda.central.core.tenant.profile.api.GetTenantResponse;
import com.veda.central.core.tenant.profile.api.TenantStatus;
import com.veda.central.service.credential.store.CredentialStoreService;
import com.veda.central.service.identity.IdentityService;
import com.veda.central.service.profile.TenantProfileService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TokenAuthorizer {

    private final CredentialStoreService credentialStoreService;
    private final TenantProfileService tenantProfileService;
    private final IdentityService identityService;


    public TokenAuthorizer(CredentialStoreService credentialStoreService, TenantProfileService tenantProfileService, IdentityService identityService) {
        this.credentialStoreService = credentialStoreService;
        this.tenantProfileService = tenantProfileService;
        this.identityService = identityService;
    }

    public Optional<AuthClaim> authorize(HttpHeaders headers) {
        try {
            String formattedToken = getToken(headers);

            if (formattedToken == null) {
                throw new UnauthorizedException("Token not found", null);
            }
            return authorize(formattedToken);
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid token: " + ex.getMessage(), ex);
        }
    }

    public String getToken(HttpHeaders headers) {
        String tokenWithBearer = headers.getFirst("Authorization");
        if (tokenWithBearer == null) {
            return null;
        }
        String prefix = "Bearer ";
        if (tokenWithBearer.startsWith(prefix)) {
            String token = tokenWithBearer.substring(prefix.length());
            return token.trim();
        }
        return null;
    }

    public Optional<AuthClaim> authorize(String formattedToken) {
        try {
            TokenRequest request = TokenRequest.newBuilder()
                    .setToken(formattedToken)
                    .build();
            GetAllCredentialsResponse response = credentialStoreService.getAllCredentialsFromToken(request);
            return getAuthClaim(response);
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid token: " + ex.getMessage(), ex);
        }
    }

    private Optional<AuthClaim> getAuthClaim(GetAllCredentialsResponse response) {
        if (response == null || response.getSecretListList().isEmpty()) {
            return Optional.empty();
        }

        AuthClaim authClaim = new AuthClaim();

        authClaim.setPerformedBy(response.getRequesterUserEmail());
        authClaim.setUsername(response.getRequesterUsername());
        response.getSecretListList().forEach(metadata -> {
                    if (metadata.getType() == Type.VEDA) {
                        authClaim.setTenantId(metadata.getOwnerId());
                        authClaim.setVedaId(metadata.getId());
                        authClaim.setVedaSecret(metadata.getSecret());
                        authClaim.setVedaIdIssuedAt(metadata.getClientIdIssuedAt());
                        authClaim.setVedaSecretExpiredAt(metadata.getClientSecretExpiredAt());
                        authClaim.setAdmin(metadata.getSuperAdmin());
                        authClaim.setSuperTenant(metadata.getSuperTenant());

                    } else if (metadata.getType() == Type.IAM) {
                        authClaim.setIamAuthId(metadata.getId());
                        authClaim.setIamAuthSecret(metadata.getSecret());

                    } else if (metadata.getType() == Type.CILOGON) {
                        authClaim.setCiLogonId(metadata.getId());
                        authClaim.setCiLogonSecret(metadata.getSecret());

                    } else if (metadata.getType() == Type.AGENT_CLIENT) {
                        authClaim.setAgentClientId(metadata.getId());
                        authClaim.setAgentClientSecret(metadata.getSecret());

                    } else if (metadata.getType() == Type.AGENT) {
                        authClaim.setAgentId(metadata.getId());
                        authClaim.setAgentPassword(metadata.getInternalSec());
                    }
                }
        );

        GetTenantRequest tenantRequest = GetTenantRequest.newBuilder()
                .setTenantId(authClaim.getTenantId())
                .build();

        GetTenantResponse tentResp = tenantProfileService.getTenant(tenantRequest);

        if (tentResp.getTenant().getTenantStatus().equals(TenantStatus.ACTIVE)) {
            return Optional.of(authClaim);
        }
        return Optional.empty();
    }
}
