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

package com.veda.central.api.user;

import com.veda.central.core.constants.Constants;
import com.veda.central.core.iam.api.AddExternalIDPLinksRequest;
import com.veda.central.core.iam.api.AddUserAttributesRequest;
import com.veda.central.core.iam.api.AddUserRolesRequest;
import com.veda.central.core.iam.api.DeleteExternalIDPsRequest;
import com.veda.central.core.iam.api.DeleteUserAttributeRequest;
import com.veda.central.core.iam.api.DeleteUserRolesRequest;
import com.veda.central.core.iam.api.FindUsersRequest;
import com.veda.central.core.iam.api.FindUsersResponse;
import com.veda.central.core.iam.api.GetExternalIDPsRequest;
import com.veda.central.core.iam.api.GetExternalIDPsResponse;
import com.veda.central.core.iam.api.OperationStatus;
import com.veda.central.core.iam.api.RegisterUserRequest;
import com.veda.central.core.iam.api.RegisterUserResponse;
import com.veda.central.core.iam.api.RegisterUsersRequest;
import com.veda.central.core.iam.api.RegisterUsersResponse;
import com.veda.central.core.iam.api.ResetUserPassword;
import com.veda.central.core.iam.api.UserRepresentation;
import com.veda.central.core.iam.api.UserSearchRequest;
import com.veda.central.core.identity.api.AuthToken;
import com.veda.central.core.user.management.api.LinkUserProfileRequest;
import com.veda.central.core.user.management.api.SynchronizeUserDBRequest;
import com.veda.central.core.user.management.api.UserProfileRequest;
import com.veda.central.core.user.profile.api.GetAllUserProfilesResponse;
import com.veda.central.core.user.profile.api.GetUpdateAuditTrailRequest;
import com.veda.central.core.user.profile.api.GetUpdateAuditTrailResponse;
import com.veda.central.core.user.profile.api.UserProfile;
import com.veda.central.service.auth.AuthClaim;
import com.veda.central.service.auth.TokenAuthorizer;
import com.veda.central.service.management.UserManagementService;
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

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user-management")
@Tag(name = "User Management")
public class UserManagementController {

    private final UserManagementService userManagementService;
    private final TokenAuthorizer tokenAuthorizer;

    public UserManagementController(UserManagementService userManagementService, TokenAuthorizer tokenAuthorizer) {
        this.userManagementService = userManagementService;
        this.tokenAuthorizer = tokenAuthorizer;
    }

    @PostMapping("/user")
    public ResponseEntity<RegisterUserResponse> registerUser(@RequestBody RegisterUserRequest request, @RequestHeader HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, request.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();

            request = request.toBuilder().setTenantId(authClaim.getTenantId())
                    .setClientId(authClaim.getIamAuthId())
                    .setClientSec(authClaim.getIamAuthSecret()).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }
        RegisterUserResponse response = userManagementService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users")
    public ResponseEntity<RegisterUsersResponse> registerAndEnableUsers(@RequestBody RegisterUsersRequest request, @RequestHeader HttpHeaders headers) {
        headers = attachUserToken(headers, request.getClientId());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, request.getClientId());

        if (claim.isPresent()) {
            Optional<String> userTokenOp = tokenAuthorizer.getUserTokenFromUserTokenHeader(headers);
            String userToken = userTokenOp.isEmpty()
                    ? tokenAuthorizer.getToken(headers)
                    : userTokenOp.get();

            AuthClaim authClaim = claim.get();

            request = request.toBuilder().setClientId(authClaim.getIamAuthId())
                    .setTenantId(authClaim.getTenantId())
                    .setAccessToken(userToken)
                    .setPerformedBy(authClaim.getPerformedBy()).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        RegisterUsersResponse response = userManagementService.registerAndEnableUsers(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/attributes")
    public ResponseEntity<OperationStatus> addUserAttributes(@RequestBody AddUserAttributesRequest request, @RequestHeader HttpHeaders headers) {
        headers = attachUserToken(headers, request.getClientId());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, request.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            AuthToken authToken = tokenAuthorizer.getSAToken(authClaim.getIamAuthId(), authClaim.getIamAuthSecret(), authClaim.getTenantId());

            if (authToken == null || StringUtils.isBlank(authToken.getAccessToken())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized. Service Account token is invalid");
            }

            request = request.toBuilder().setClientId(authClaim.getIamAuthId())
                    .setTenantId(authClaim.getTenantId())
                    .setAccessToken(authToken.getAccessToken())
                    .setPerformedBy(authClaim.getPerformedBy()).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        OperationStatus response = userManagementService.addUserAttributes(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/attributes")
    public ResponseEntity<OperationStatus> deleteUserAttributes(@RequestBody DeleteUserAttributeRequest request, @RequestHeader HttpHeaders headers) {
        headers = attachUserToken(headers, request.getClientId());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, request.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            AuthToken authToken = tokenAuthorizer.getSAToken(authClaim.getIamAuthId(), authClaim.getIamAuthSecret(), authClaim.getTenantId());

            if (authToken == null || StringUtils.isBlank(authToken.getAccessToken())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized. Service Account token is invalid");
            }

            request = request.toBuilder().setClientId(authClaim.getIamAuthId())
                    .setTenantId(authClaim.getTenantId())
                    .setAccessToken(authToken.getAccessToken())
                    .setPerformedBy(authClaim.getPerformedBy()).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        OperationStatus response = userManagementService.deleteUserAttributes(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/activation")
    public ResponseEntity<UserRepresentation> enableUser(@RequestBody UserSearchRequest request, @RequestHeader HttpHeaders headers) {
        UserRepresentation response = userManagementService.enableUser(generateUserSearchRequestWithoutAdditionalHeader(request.toBuilder(), headers).build());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/deactivation")
    public ResponseEntity<UserRepresentation> disableUser(@RequestBody UserSearchRequest request, @RequestHeader HttpHeaders headers) {
        UserRepresentation response = userManagementService.disableUser(generateUserSearchRequestWithoutAdditionalHeader(request.toBuilder(), headers).build());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/admin")
    public ResponseEntity<OperationStatus> grantAdminPrivileges(@RequestBody UserSearchRequest request, @RequestHeader HttpHeaders headers) {
        OperationStatus response = userManagementService.grantAdminPrivileges(generateUserSearchRequest(request.toBuilder(), headers).build());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user/admin")
    public ResponseEntity<OperationStatus> removeAdminPrivileges(@RequestBody UserSearchRequest request, @RequestHeader HttpHeaders headers) {
        OperationStatus response = userManagementService.removeAdminPrivileges(generateUserSearchRequest(request.toBuilder(), headers).build());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/federatedIDPs")
    public ResponseEntity<OperationStatus> deleteExternalIDPsOfUsers(@RequestBody DeleteExternalIDPsRequest request, @RequestHeader HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, request.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();

            request = request.toBuilder().setTenantId(authClaim.getTenantId())
                    .setClientId(authClaim.getIamAuthId()).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }
        OperationStatus response = userManagementService.deleteExternalIDPsOfUsers(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/federatedIDPs")
    public ResponseEntity<OperationStatus> addExternalIDPsOfUsers(@RequestBody AddExternalIDPLinksRequest request, @RequestHeader HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, request.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();

            request = request.toBuilder().setTenantId(authClaim.getTenantId())
                    .setClientId(authClaim.getIamAuthId()).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        OperationStatus response = userManagementService.addExternalIDPsOfUsers(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/federatedIDPs")
    public ResponseEntity<GetExternalIDPsResponse> getExternalIDPsOfUsers(@RequestBody GetExternalIDPsRequest request, @RequestHeader HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, request.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();

            request = request.toBuilder().setTenantId(authClaim.getTenantId())
                    .setClientId(authClaim.getIamAuthId()).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }
        GetExternalIDPsResponse response = userManagementService.getExternalIDPsOfUsers(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/roles")
    public ResponseEntity<OperationStatus> addRolesToUsers(@Valid @RequestBody AddUserRolesRequest request, @RequestHeader HttpHeaders headers) {
        headers = attachUserToken(headers, request.getClientId());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, request.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            AuthToken authToken = tokenAuthorizer.getSAToken(authClaim.getIamAuthId(), authClaim.getIamAuthSecret(), authClaim.getTenantId());

            if (authToken == null || StringUtils.isBlank(authToken.getAccessToken())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized. Service Account token is invalid");
            }

            request = request.toBuilder().setClientId(authClaim.getIamAuthId())
                    .setTenantId(authClaim.getTenantId())
                    .setAccessToken(authToken.getAccessToken())
                    .setPerformedBy(authClaim.getPerformedBy()).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        OperationStatus response = userManagementService.addRolesToUsers(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/activation/status")
    public ResponseEntity<OperationStatus> isUserEnabled(@RequestBody UserSearchRequest request, @RequestHeader HttpHeaders headers) {
        OperationStatus response = userManagementService.isUserEnabled(generateUserSearchRequestWithoutAdditionalHeader(request.toBuilder(), headers).build());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/availability")
    public ResponseEntity<OperationStatus> isUsernameAvailable(@RequestBody UserSearchRequest request, @RequestHeader HttpHeaders headers) {
        OperationStatus response = userManagementService.isUsernameAvailable(generateUserSearchRequestWithoutAdditionalHeader(request.toBuilder(), headers).build());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<UserRepresentation> getUser(@Valid @RequestBody UserSearchRequest request, @RequestHeader HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, request.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            request = request.toBuilder().setClientId(authClaim.getIamAuthId())
                    .setClientSec(authClaim.getIamAuthSecret())
                    .setTenantId(claim.get().getTenantId()).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        UserRepresentation response = userManagementService.getUser(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<FindUsersResponse> findUsers(@RequestBody FindUsersRequest request, @RequestHeader HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, request.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            request = request.toBuilder().setClientId(authClaim.getIamAuthId())
                    .setClientSec(authClaim.getIamAuthSecret())
                    .setTenantId(claim.get().getTenantId()).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        FindUsersResponse response = userManagementService.findUsers(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user/password")
    public ResponseEntity<OperationStatus> resetPassword(@RequestBody ResetUserPassword request, @RequestHeader HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, request.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            request = request.toBuilder().setClientId(authClaim.getIamAuthId())
                    .setClientSec(authClaim.getIamAuthSecret())
                    .setTenantId(claim.get().getTenantId()).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }
        OperationStatus response = userManagementService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user")
    public ResponseEntity<OperationStatus> deleteUser(@RequestBody UserSearchRequest request, @RequestHeader HttpHeaders headers) {
        OperationStatus response = userManagementService.deleteUser(generateUserSearchRequest(request.toBuilder(), headers).build());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user/roles")
    public ResponseEntity<OperationStatus> deleteUserRoles(@Valid @RequestBody DeleteUserRolesRequest request, @RequestHeader HttpHeaders headers) {
        headers = attachUserToken(headers, request.getClientId());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, request.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            AuthToken authToken = tokenAuthorizer.getSAToken(authClaim.getIamAuthId(), authClaim.getIamAuthSecret(), authClaim.getTenantId());

            if (authToken == null || StringUtils.isBlank(authToken.getAccessToken())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized. Service Account token is invalid");
            }

            request = request.toBuilder().setClientId(authClaim.getIamAuthId())
                    .setTenantId(authClaim.getTenantId())
                    .setAccessToken(authToken.getAccessToken())
                    .setPerformedBy(authClaim.getPerformedBy().isEmpty() ? Constants.SYSTEM : authClaim.getPerformedBy()).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        OperationStatus response = userManagementService.deleteUserRoles(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user/profile")
    public ResponseEntity<UserProfile> updateUserProfile(@RequestBody UserProfileRequest request, @RequestHeader HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, request.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            AuthToken authToken = tokenAuthorizer.getSAToken(authClaim.getIamAuthId(), authClaim.getIamAuthSecret(), authClaim.getTenantId());

            if (authToken == null || StringUtils.isBlank(authToken.getAccessToken())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized. Service Account token is invalid");
            }

            request = request.toBuilder().setClientId(authClaim.getIamAuthId())
                    .setClientSecret(authClaim.getIamAuthSecret())
                    .setTenantId(authClaim.getTenantId())
                    .setAccessToken(authToken.getAccessToken())
                    .setPerformedBy(Constants.SYSTEM).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }


        UserProfile response = userManagementService.updateUserProfile(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/profile")
    public ResponseEntity<UserProfile> getUserProfile(@Valid @RequestBody UserProfileRequest request, @RequestHeader HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorizeUsingUserToken(headers);

        if (claim.isPresent()) {
            request = request.toBuilder().setTenantId(claim.get().getTenantId()).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        UserProfile response = userManagementService.getUserProfile(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user/profile")
    public ResponseEntity<UserProfile> deleteUserProfile(@RequestBody UserProfileRequest request, @RequestHeader HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, request.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            request = request.toBuilder().setClientId(authClaim.getIamAuthId())
                    .setClientSecret(authClaim.getIamAuthSecret())
                    .setTenantId(authClaim.getTenantId()).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        UserProfile response = userManagementService.deleteUserProfile(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/profile")
    public ResponseEntity<GetAllUserProfilesResponse> getAllUserProfilesInTenant(@RequestBody UserProfileRequest request, @RequestHeader HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, request.getClientId());

        if (claim.isPresent()) {
            request = request.toBuilder().setTenantId(claim.get().getTenantId()).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        GetAllUserProfilesResponse response = userManagementService.getAllUserProfilesInTenant(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/profile/mapper")
    public ResponseEntity<OperationStatus> linkUserProfile(@RequestBody LinkUserProfileRequest request, @RequestHeader HttpHeaders headers) {
        String token = tokenAuthorizer.getToken(headers);
        Optional<AuthClaim> claim = tokenAuthorizer.authorizeUsingUserToken(headers);

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();

            request = request.toBuilder().setIamClientId(authClaim.getIamAuthId())
                    .setIamClientSecret(authClaim.getIamAuthSecret())
                    .setTenantId(authClaim.getTenantId())
                    .setAccessToken(token)
                    .setPerformedBy(authClaim.getPerformedBy()).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        OperationStatus response = userManagementService.linkUserProfile(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/profile/audit")
    public ResponseEntity<GetUpdateAuditTrailResponse> getUserProfileAuditTrails(@RequestBody GetUpdateAuditTrailRequest request, @RequestHeader HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers);

        if (claim.isPresent()) {
            request = request.toBuilder().setTenantId(claim.get().getTenantId()).build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        GetUpdateAuditTrailResponse response = userManagementService.getUserProfileAuditTrails(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/db/synchronize")
    public ResponseEntity<OperationStatus> synchronizeUserDBs(@Valid @RequestBody SynchronizeUserDBRequest request) {
        OperationStatus response = userManagementService.synchronizeUserDBs(request);
        return ResponseEntity.ok(response);
    }

    private HttpHeaders attachUserToken(HttpHeaders headers, String clientId) {
        if (StringUtils.isBlank(clientId)) {
            String formattedUserToken = tokenAuthorizer.getToken(headers);
            headers.add(Constants.USER_TOKEN, formattedUserToken);
            return headers;
        }

        return headers;
    }

    private UserSearchRequest.Builder generateUserSearchRequest(UserSearchRequest.Builder builder, HttpHeaders headers) {
        headers = attachUserToken(headers, builder.getClientId());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, builder.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            AuthToken authToken = tokenAuthorizer.getSAToken(authClaim.getIamAuthId(), authClaim.getIamAuthSecret(), authClaim.getTenantId());

            if (authToken == null || StringUtils.isBlank(authToken.getAccessToken())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized. Service Account token is invalid");
            }

            builder.setClientId(authClaim.getIamAuthId())
                    .setClientSec(authClaim.getIamAuthSecret())
                    .setTenantId(authClaim.getTenantId())
                    .setAccessToken(authToken.getAccessToken())
                    .setPerformedBy(Constants.SYSTEM);

            return builder;

        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }
    }

    private UserSearchRequest.Builder generateUserSearchRequestWithoutAdditionalHeader(UserSearchRequest.Builder builder, HttpHeaders headers) {
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, builder.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            builder.setClientId(authClaim.getIamAuthId())
                    .setClientSec(authClaim.getIamAuthSecret())
                    .setTenantId(authClaim.getTenantId());

            return builder;

        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }
    }
}
