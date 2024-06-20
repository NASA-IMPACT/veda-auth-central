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

package com.veda.central.service.management;

import com.veda.central.core.credential.store.api.CredentialMetadata;
import com.veda.central.core.credential.store.api.Credentials;
import com.veda.central.core.credential.store.api.GetCredentialRequest;
import com.veda.central.core.credential.store.api.Type;
import com.veda.central.core.identity.api.AuthToken;
import com.veda.central.core.identity.api.AuthenticationRequest;
import com.veda.central.core.identity.api.Claim;
import com.veda.central.core.identity.api.GetAuthorizationEndpointRequest;
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
import com.veda.central.core.tenant.profile.api.GetTenantRequest;
import com.veda.central.core.tenant.profile.api.GetTenantResponse;
import com.veda.central.core.tenant.profile.api.Tenant;
import com.veda.central.core.user.profile.api.UserProfile;
import com.veda.central.service.credential.store.CredentialStoreService;
import com.veda.central.service.exceptions.InternalServerException;
import com.veda.central.service.identity.Constants;
import com.veda.central.service.identity.IdentityService;
import com.veda.central.service.profile.TenantProfileService;
import com.veda.central.service.profile.UserProfileService;
import io.grpc.Context;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * The IdentityManagementService class provides methods for managing identities, authentication, and authorization.
 */
@Service
public class IdentityManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityManagementService.class);

    private final IdentityService identityService;
    private final TenantProfileService tenantProfileService;
    private final CredentialStoreService credentialStoreService;
    private final UserProfileService userProfileService;


    public IdentityManagementService(IdentityService identityService, TenantProfileService tenantProfileService, CredentialStoreService credentialStoreService, UserProfileService userProfileService) {
        this.identityService = identityService;
        this.tenantProfileService = tenantProfileService;
        this.credentialStoreService = credentialStoreService;
        this.userProfileService = userProfileService;
    }

    /**
     * Authenticates the user based on the provided authentication request.
     *
     * @param request an object of type AuthenticationRequest containing the user's credentials
     * @return an object of type AuthToken representing the authentication token
     * @throws InternalServerException if an exception occurs during the authentication process
     */
    public AuthToken authenticate(AuthenticationRequest request) {
        try {
            LOGGER.debug("Request received to authenticate for " + request.getUsername());
            return identityService.authenticate(request);

        } catch (Exception ex) {
            String msg = "Exception occurred while authentication " + ex.getMessage();
            LOGGER.error(msg);
            throw new InternalServerException(msg, ex);
        }
    }

    /**
     * Retrieves the user information based on the provided authentication token.
     *
     * @param request an object of type AuthToken containing the authentication token
     * @return an object of type User representing the user information
     * @throws InternalServerException if an exception occurs while fetching the user
     */
    public User getUser(AuthToken request) {
        try {
            LOGGER.debug("Request received for getUser");
            return identityService.getUser(request);

        } catch (Exception ex) {
            String msg = "Exception occurred while fetching user " + ex.getMessage();
            LOGGER.error(msg);
            throw new InternalServerException(msg, ex);
        }
    }

    /**
     * Retrieves the access token for the user management service account.
     *
     * @param request an object of type GetUserManagementSATokenRequest containing the necessary information for fetching the access token
     * @return an object of type AuthToken representing the access token for the user management service account
     * @throws InternalServerException if an exception occurs during the process of fetching the access token
     */
    public AuthToken getUserManagementServiceAccountAccessToken(GetUserManagementSATokenRequest request) {
        try {
            LOGGER.debug("Request received to authenticate for " + request.getTenantId());
            return identityService.getUserManagementServiceAccountAccessToken(request);

        } catch (Exception ex) {
            String msg = "Exception occurred while fetching service account " + ex.getMessage();
            LOGGER.error(msg);
            throw new InternalServerException(msg, ex);
        }
    }

    /**
     * Determines whether a given authentication token is authenticated.
     *
     * @param request an object of type AuthToken containing the authentication token
     * @return an object of type IsAuthenticatedResponse representing the authenticated status of the token
     * @throws InternalServerException if an exception occurs while fetching the authenticated status
     */
    public IsAuthenticatedResponse isAuthenticated(AuthToken request) {
        try {
            LOGGER.debug("Request received to isAuthenticated ");
            return identityService.isAuthenticated(request);

        } catch (Exception ex) {
            String msg = "Exception occurred while fetching authenticated status " + ex.getMessage();
            LOGGER.error(msg);
            throw new InternalServerException(msg, ex);
        }
    }

    /**
     * Authorizes a client request.
     *
     * @param request an object of type AuthorizationRequest containing the authorization request data
     * @return an object of type AuthorizationResponse containing the login URI for the authorization request
     * @throws InternalServerException if an exception occurs during the authorization process
     */
    public AuthorizationResponse authorize(AuthorizationRequest request) {
        try {
            LOGGER.debug("Request received  to authorize " + request.getClientId());

            GetCredentialRequest req = GetCredentialRequest.newBuilder().setId(request.getClientId()).build();
            CredentialMetadata metadata = credentialStoreService.getVedaCredentialFromClientId(req);
            req = req.toBuilder().setType(Type.IAM).setOwnerId(metadata.getOwnerId()).build();
            CredentialMetadata iamMetadata = credentialStoreService.getCredential(req);
            GetTenantRequest tenantRequest = GetTenantRequest.newBuilder().setTenantId(metadata.getOwnerId()).build();
            GetTenantResponse tenantResponse = tenantProfileService.getTenant(tenantRequest);
            Tenant tenant = tenantResponse.getTenant();

            if (tenant.getRedirectUrisCount() == 0) {
                LOGGER.error("No redirect_uri has been associated with the Tenant, tenant Id: {}", tenant.getTenantId());
                throw new IllegalArgumentException("No redirect_uri has been associated with the Tenant, tenant Id: " + tenant.getTenantId());
            }

            boolean matched = false;
            for (String redirectURI : tenant.getRedirectUrisList()) {
                if (request.getRedirectUri().equals(redirectURI)) {
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                LOGGER.error("No matching redirect_uri is found for the Tenant with the Id: {}", tenant.getTenantId());
                throw new IllegalArgumentException("No matching redirect_uri is found for the Tenant with the Id: " + tenant.getTenantId());
            }

            GetAuthorizationEndpointRequest getAuthorizationEndpointRequest = GetAuthorizationEndpointRequest.newBuilder().setTenantId(metadata.getOwnerId()).build();
            com.veda.central.core.identity.api.AuthorizationResponse response = identityService.getAuthorizeEndpoint(getAuthorizationEndpointRequest);
            String endpoint = response.getAuthorizationEndpoint();

            String loginURL = endpoint + "?" + "client_id=" + iamMetadata.getId() + "&" + "redirect_uri="
                    + request.getRedirectUri() + "&" + "response_type="
                    + Constants.AUTHORIZATION_CODE + "&" + "scope=" + request.getScope() + "&" + "state=" + request.getState()
                    + "&kc_idp_hint=oidc";

            return AuthorizationResponse.newBuilder().setLoginURI(loginURL).build();

        } catch (Exception ex) {
            String msg = "Exception occurred while formulating login uri " + ex.getMessage();
            LOGGER.error(msg);
            throw new InternalServerException(msg, ex);
        }
    }

    /**
     * Retrieves the access token for the user based on the provided token request.
     *
     * @param request an object of type GetTokenRequest containing the necessary information for fetching the access token
     * @return an object of type TokenResponse representing the access token and other related information
     * @throws InternalServerException if an exception occurs during the process of fetching the access token
     */
    public TokenResponse token(GetTokenRequest request) {
        try {
            LOGGER.debug("Request received  to token endpoint " + request.getTenantId());

            TokenResponse response = identityService.getToken(request);

            Context ctx = Context.current().fork();
            ctx.run(() -> {
                long tenantId = request.getTenantId();
                if (StringUtils.isNotBlank(response.getAccessToken())) {
                    LOGGER.debug(response.getAccessToken());
                    String clientId = request.getClientId();
                    AuthToken authToken = AuthToken.newBuilder()
                            .setAccessToken(response.getAccessToken())
                            .addClaims(Claim.newBuilder().setKey("clientId").setValue(clientId).build())
                            .addClaims(Claim.newBuilder().setKey("username").setValue("veda-auth-user"))
                            .addClaims(Claim.newBuilder().setKey("tenantId").setValue(String.valueOf(tenantId))
                                    .build()).build();
                    User user = identityService.getUser(authToken);
                    LOGGER.debug("User" + user.getUsername());

                    UserProfile userProfile = UserProfile.newBuilder()
                            .setUsername(user.getUsername())
                            .setFirstName(user.getFirstName())
                            .setLastName(user.getLastName())
                            .setEmail(user.getEmailAddress())
                            .build();
                    com.veda.central.core.user.profile.api.UserProfileRequest req = com.veda.central.core.user.profile.api.UserProfileRequest
                            .newBuilder()
                            .setTenantId(request.getTenantId())
                            .setProfile(userProfile)
                            .build();

                    Map<String, String> values = new HashMap<>();
                    values.put("param:username", user.getUsername());
                    values.put("param:first_name", user.getFirstName());
                    values.put("param:last_name", user.getLastName());
                    values.put("param:email", user.getEmailAddress());
                    values.put("param:tenant_id", request.getClientId());

                    userProfileService.createUserProfile(req);
                    com.veda.central.core.tenant.profile.api.GetTenantRequest tenantReq = com.veda.central.core.tenant.profile.api.GetTenantRequest.newBuilder()
                            .setTenantId(request.getTenantId()).build();

                    com.veda.central.core.tenant.profile.api.GetTenantResponse tenantResponse = tenantProfileService.getTenant(tenantReq);
                    values.put("param:tenant_name", tenantResponse.getTenant().getClientName());
                    // TODO send an email to the tenant
                }
            });

            return response;

        } catch (Exception ex) {
            String msg = "Exception occurred while fetching access token " + ex.getMessage();
            LOGGER.error(msg);
            throw new InternalServerException(msg, ex);
        }
    }

    /**
     * Retrieves the OIDC configuration based on the provided request.
     *
     * @param request an object of type GetOIDCConfiguration containing the necessary information for fetching the OIDC configuration
     * @return an object of type OIDCConfiguration representing the fetched OIDC configuration
     * @throws InternalServerException if an exception occurs during the process of retrieving the OIDC configuration
     */
    public OIDCConfiguration getOIDCConfiguration(GetOIDCConfiguration request) {
        try {
            LOGGER.debug("Request received  to fetch OIDC configuration " + request.getTenantId());

            String clientId = request.getClientId();
            GetCredentialRequest req = GetCredentialRequest.newBuilder().setId(clientId).build();
            CredentialMetadata metadata = credentialStoreService.getVedaCredentialFromClientId(req);

            GetCredentialRequest iamCredentialRequest = GetCredentialRequest.newBuilder()
                    .setType(Type.IAM)
                    .setOwnerId(metadata.getOwnerId())
                    .setId(request.getClientId()).build();

            CredentialMetadata iamCredential = credentialStoreService.getCredential(iamCredentialRequest);
            request = request.toBuilder().setTenantId(metadata.getOwnerId()).setClientId(iamCredential.getId()).build();

            return identityService.getOIDCConfiguration(request);

        } catch (Exception ex) {
            String msg = "Exception occurred while retrieving OIDC configuration " + ex.getMessage();
            LOGGER.error(msg);
            throw new InternalServerException(msg, ex);
        }
    }

    /**
     * Retrieves the credentials for a given request.
     *
     * @param request an object of type GetCredentialsRequest containing the request for credentials
     * @return an object of type Credentials representing the retrieved credentials
     * @throws IllegalArgumentException if no matching credential is found for the defined client Id in the request
     * @throws InternalServerException if an exception occurs while retrieving the credentials
     */
    public Credentials getCredentials(GetCredentialsRequest request) {
        try {
            LOGGER.debug("Request received to get Credentials for token " + request.getCredentials().getVedaClientId());

            if (request.getClientId().equals(request.getCredentials().getVedaClientId())) {
                return request.getCredentials();
            } else {
                LOGGER.error("No matching credential found for the defined client Id: {}", request.getCredentials().getVedaClientId());
                throw new IllegalArgumentException("No matching credential found for the defined client Id: " + request.getCredentials().getVedaClientId());
            }
        } catch (Exception ex) {
            String msg = "Exception occurred while retrieving Credentials " + ex.getMessage();
            LOGGER.error(msg);
            throw new InternalServerException(msg, ex);
        }
    }

    /**
     * Ends the user session based on the provided request.
     *
     * @param request the request object containing the necessary information for ending the user session
     * @return an object of type OperationStatus representing the status of the operation
     * @throws InternalServerException if an exception occurs during the process of ending the user session
     */
    public OperationStatus endUserSession(EndSessionRequest request) {
        try {
            LOGGER.debug("Request received to endUserSession endpoint " + request.getBody().getTenantId());

            return identityService.endSession(request.getBody());

        } catch (Exception ex) {
            String msg = "Exception occurred while fetching agent access token " + ex.getMessage();
            LOGGER.error(msg);
            throw new InternalServerException(msg, ex);
        }
    }
}
