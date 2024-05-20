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

import com.veda.central.api.util.ProtobufJsonUtil;
import com.veda.central.api.util.RestUtil;
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
public class UserManagementController {

    private final UserManagementService userManagementService;
    private final TokenAuthorizer tokenAuthorizer;

    public UserManagementController(UserManagementService userManagementService, TokenAuthorizer tokenAuthorizer) {
        this.userManagementService = userManagementService;
        this.tokenAuthorizer = tokenAuthorizer;
    }

    @PostMapping("/user")
    public ResponseEntity<String> registerUser(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        RegisterUserRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, RegisterUserRequest.newBuilder());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, builder.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();

            builder.setTenantId(authClaim.getTenantId())
                    .setClientId(authClaim.getIamAuthId())
                    .setClientSec(authClaim.getIamAuthSecret());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }
        RegisterUserResponse response = userManagementService.registerUser(builder.build());
        return RestUtil.extractOkResponse(response);
    }

    @PostMapping("/users")
    public ResponseEntity<String> registerAndEnableUsers(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        RegisterUsersRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, RegisterUsersRequest.newBuilder());
        headers = attachUserToken(headers, builder.getClientId());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, builder.getClientId());

        if (claim.isPresent()) {
            Optional<String> userTokenOp = tokenAuthorizer.getUserTokenFromUserTokenHeader(headers);
            String userToken = userTokenOp.isEmpty()
                    ? tokenAuthorizer.getToken(headers)
                    : userTokenOp.get();

            AuthClaim authClaim = claim.get();

            builder.setClientId(authClaim.getIamAuthId())
                    .setTenantId(authClaim.getTenantId())
                    .setAccessToken(userToken)
                    .setPerformedBy(authClaim.getPerformedBy());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        RegisterUsersResponse response = userManagementService.registerAndEnableUsers(builder.build());
        return RestUtil.extractOkResponse(response);
    }

    @PostMapping("/attributes")
    public ResponseEntity<String> addUserAttributes(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        AddUserAttributesRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, AddUserAttributesRequest.newBuilder());
        headers = attachUserToken(headers, builder.getClientId());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, builder.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            AuthToken authToken = tokenAuthorizer.getSAToken(authClaim.getIamAuthId(), authClaim.getIamAuthSecret(), authClaim.getTenantId());

            if (authToken == null || StringUtils.isBlank(authToken.getAccessToken())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized. Service Account token is invalid");
            }

            builder.setClientId(authClaim.getIamAuthId())
                    .setTenantId(authClaim.getTenantId())
                    .setAccessToken(authToken.getAccessToken())
                    .setPerformedBy(authClaim.getPerformedBy());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        OperationStatus response = userManagementService.addUserAttributes(builder.build());
        return RestUtil.extractOkResponse(response);
    }

    @DeleteMapping("/attributes")
    public ResponseEntity<String> deleteUserAttributes(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        DeleteUserAttributeRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, DeleteUserAttributeRequest.newBuilder());
        headers = attachUserToken(headers, builder.getClientId());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, builder.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            AuthToken authToken = tokenAuthorizer.getSAToken(authClaim.getIamAuthId(), authClaim.getIamAuthSecret(), authClaim.getTenantId());

            if (authToken == null || StringUtils.isBlank(authToken.getAccessToken())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized. Service Account token is invalid");
            }

            builder.setClientId(authClaim.getIamAuthId())
                    .setTenantId(authClaim.getTenantId())
                    .setAccessToken(authToken.getAccessToken())
                    .setPerformedBy(authClaim.getPerformedBy());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        OperationStatus response = userManagementService.deleteUserAttributes(builder.build());
        return RestUtil.extractOkResponse(response);
    }

    @PostMapping("/user/activation")
    public ResponseEntity<String> enableUser(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        UserRepresentation response = userManagementService.enableUser(generateUserSearchRequestWithoutAdditionalHeader(request, headers).build());
        return RestUtil.extractOkResponse(response);
    }

    @PostMapping("/user/deactivation")
    public ResponseEntity<String> disableUser(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        UserRepresentation response = userManagementService.disableUser(generateUserSearchRequestWithoutAdditionalHeader(request, headers).build());
        return RestUtil.extractOkResponse(response);
    }

    @PostMapping("/user/admin")
    public ResponseEntity<String> grantAdminPrivileges(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        OperationStatus response = userManagementService.grantAdminPrivileges(generateUserSearchRequest(request, headers).build());
        return RestUtil.extractOkResponse(response);
    }

    @DeleteMapping("/user/admin")
    public ResponseEntity<String> removeAdminPrivileges(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        OperationStatus response = userManagementService.removeAdminPrivileges(generateUserSearchRequest(request, headers).build());
        return RestUtil.extractOkResponse(response);
    }

    @DeleteMapping("/users/federatedIDPs")
    public ResponseEntity<String> deleteExternalIDPsOfUsers(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        DeleteExternalIDPsRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, DeleteExternalIDPsRequest.newBuilder());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, builder.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();

            builder.setTenantId(authClaim.getTenantId())
                    .setClientId(authClaim.getIamAuthId());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }
        OperationStatus response = userManagementService.deleteExternalIDPsOfUsers(builder.build());
        return RestUtil.extractOkResponse(response);
    }

    @PostMapping("/users/federatedIDPs")
    public ResponseEntity<String> addExternalIDPsOfUsers(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        AddExternalIDPLinksRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, AddExternalIDPLinksRequest.newBuilder());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, builder.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();

            builder.setTenantId(authClaim.getTenantId())
                    .setClientId(authClaim.getIamAuthId());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        OperationStatus response = userManagementService.addExternalIDPsOfUsers(builder.build());
        return RestUtil.extractOkResponse(response);
    }

    @GetMapping("/users/federatedIDPs")
    public ResponseEntity<String> getExternalIDPsOfUsers(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        GetExternalIDPsRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, GetExternalIDPsRequest.newBuilder());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, builder.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();

            builder.setTenantId(authClaim.getTenantId())
                    .setClientId(authClaim.getIamAuthId());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }
        GetExternalIDPsResponse response = userManagementService.getExternalIDPsOfUsers(builder.build());
        return RestUtil.extractOkResponse(response);
    }

    @PostMapping("/users/roles")
    public ResponseEntity<String> addRolesToUsers(@Valid @RequestBody String request, @RequestHeader HttpHeaders headers) {
        AddUserRolesRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, AddUserRolesRequest.newBuilder());
        headers = attachUserToken(headers, builder.getClientId());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, builder.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            AuthToken authToken = tokenAuthorizer.getSAToken(authClaim.getIamAuthId(), authClaim.getIamAuthSecret(), authClaim.getTenantId());

            if (authToken == null || StringUtils.isBlank(authToken.getAccessToken())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized. Service Account token is invalid");
            }

            builder.setClientId(authClaim.getIamAuthId())
                    .setTenantId(authClaim.getTenantId())
                    .setAccessToken(authToken.getAccessToken())
                    .setPerformedBy(authClaim.getPerformedBy());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        OperationStatus response = userManagementService.addRolesToUsers(builder.build());
        return RestUtil.extractOkResponse(response);
    }

    @GetMapping("/user/activation/status")
    public ResponseEntity<String> isUserEnabled(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        OperationStatus response = userManagementService.isUserEnabled(generateUserSearchRequestWithoutAdditionalHeader(request, headers).build());
        return RestUtil.extractOkResponse(response);
    }

    @GetMapping("/user/availability")
    public ResponseEntity<String> isUsernameAvailable(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        OperationStatus response = userManagementService.isUsernameAvailable(generateUserSearchRequestWithoutAdditionalHeader(request, headers).build());
        return RestUtil.extractOkResponse(response);
    }

    @GetMapping("/user")
    public ResponseEntity<String> getUser(@Valid @RequestBody String request, @RequestHeader HttpHeaders headers) {
        UserSearchRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, UserSearchRequest.newBuilder());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, builder.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            builder.setClientId(authClaim.getIamAuthId())
                    .setClientSec(authClaim.getIamAuthSecret())
                    .setTenantId(claim.get().getTenantId());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        UserRepresentation response = userManagementService.getUser(builder.build());
        return RestUtil.extractOkResponse(response);
    }

    @GetMapping("/users")
    public ResponseEntity<String> findUsers(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        FindUsersRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, FindUsersRequest.newBuilder());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, builder.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            builder.setClientId(authClaim.getIamAuthId())
                    .setClientSec(authClaim.getIamAuthSecret())
                    .setTenantId(claim.get().getTenantId());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        FindUsersResponse response = userManagementService.findUsers(builder.build());
        return RestUtil.extractOkResponse(response);
    }

    @PutMapping("/user/password")
    public ResponseEntity<String> resetPassword(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        ResetUserPassword.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, ResetUserPassword.newBuilder());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, builder.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            builder.setClientId(authClaim.getIamAuthId())
                    .setClientSec(authClaim.getIamAuthSecret())
                    .setTenantId(claim.get().getTenantId());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }
        OperationStatus response = userManagementService.resetPassword(builder.build());
        return RestUtil.extractOkResponse(response);
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        OperationStatus response = userManagementService.deleteUser(generateUserSearchRequest(request, headers).build());
        return RestUtil.extractOkResponse(response);
    }

    @DeleteMapping("/user/roles")
    public ResponseEntity<String> deleteUserRoles(@Valid @RequestBody String request, @RequestHeader HttpHeaders headers) {
        DeleteUserRolesRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, DeleteUserRolesRequest.newBuilder());
        headers = attachUserToken(headers, builder.getClientId());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, builder.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            AuthToken authToken = tokenAuthorizer.getSAToken(authClaim.getIamAuthId(), authClaim.getIamAuthSecret(), authClaim.getTenantId());

            if (authToken == null || StringUtils.isBlank(authToken.getAccessToken())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized. Service Account token is invalid");
            }

            builder.setClientId(authClaim.getIamAuthId())
                    .setTenantId(authClaim.getTenantId())
                    .setAccessToken(authToken.getAccessToken())
                    .setPerformedBy(authClaim.getPerformedBy().isEmpty() ? Constants.SYSTEM : authClaim.getPerformedBy());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        OperationStatus response = userManagementService.deleteUserRoles(builder.build());
        return RestUtil.extractOkResponse(response);
    }

    @PutMapping("/user/profile")
    public ResponseEntity<String> updateUserProfile(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        UserProfileRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, UserProfileRequest.newBuilder());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, builder.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            AuthToken authToken = tokenAuthorizer.getSAToken(authClaim.getIamAuthId(), authClaim.getIamAuthSecret(), authClaim.getTenantId());

            if (authToken == null || StringUtils.isBlank(authToken.getAccessToken())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized. Service Account token is invalid");
            }

            builder.setClientId(authClaim.getIamAuthId())
                    .setClientSecret(authClaim.getIamAuthSecret())
                    .setTenantId(authClaim.getTenantId())
                    .setAccessToken(authToken.getAccessToken())
                    .setPerformedBy(Constants.SYSTEM);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }


        UserProfile response = userManagementService.updateUserProfile(builder.build());
        return RestUtil.extractOkResponse(response);
    }

    @GetMapping("/user/profile")
    public ResponseEntity<String> getUserProfile(@Valid @RequestBody String request, @RequestHeader HttpHeaders headers) {
        UserProfileRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, UserProfileRequest.newBuilder());
        Optional<AuthClaim> claim = tokenAuthorizer.authorizeUsingUserToken(headers);

        if (claim.isPresent()) {
            builder.setTenantId(claim.get().getTenantId());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        UserProfile response = userManagementService.getUserProfile(builder.build());
        return RestUtil.extractOkResponse(response);
    }

    @DeleteMapping("/user/profile")
    public ResponseEntity<String> deleteUserProfile(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        UserProfileRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, UserProfileRequest.newBuilder());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, builder.getClientId());

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();
            builder.setClientId(authClaim.getIamAuthId())
                    .setClientSecret(authClaim.getIamAuthSecret())
                    .setTenantId(authClaim.getTenantId());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        UserProfile response = userManagementService.deleteUserProfile(builder.build());
        return RestUtil.extractOkResponse(response);
    }

    @GetMapping("/users/profile")
    public ResponseEntity<String> getAllUserProfilesInTenant(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        UserProfileRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, UserProfileRequest.newBuilder());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers, builder.getClientId());

        if (claim.isPresent()) {
            builder.setTenantId(claim.get().getTenantId());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        GetAllUserProfilesResponse response = userManagementService.getAllUserProfilesInTenant(builder.build());
        return RestUtil.extractOkResponse(response);
    }

    @PostMapping("/user/profile/mapper")
    public ResponseEntity<String> linkUserProfile(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        LinkUserProfileRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, LinkUserProfileRequest.newBuilder());
        String token = tokenAuthorizer.getToken(headers);
        Optional<AuthClaim> claim = tokenAuthorizer.authorizeUsingUserToken(headers);

        if (claim.isPresent()) {
            AuthClaim authClaim = claim.get();

            builder.setIamClientId(authClaim.getIamAuthId())
                    .setIamClientSecret(authClaim.getIamAuthSecret())
                    .setTenantId(authClaim.getTenantId())
                    .setAccessToken(token)
                    .setPerformedBy(authClaim.getPerformedBy());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        OperationStatus response = userManagementService.linkUserProfile(builder.build());
        return RestUtil.extractOkResponse(response);
    }

    @GetMapping("/user/profile/audit")
    public ResponseEntity<String> getUserProfileAuditTrails(@RequestBody String request, @RequestHeader HttpHeaders headers) {
        GetUpdateAuditTrailRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, GetUpdateAuditTrailRequest.newBuilder());
        Optional<AuthClaim> claim = tokenAuthorizer.authorize(headers);

        if (claim.isPresent()) {
            builder.setTenantId(claim.get().getTenantId());
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request is not authorized");
        }

        GetUpdateAuditTrailResponse response = userManagementService.getUserProfileAuditTrails(builder.build());
        return RestUtil.extractOkResponse(response);
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

    private UserSearchRequest.Builder generateUserSearchRequest(String request, HttpHeaders headers) {
        UserSearchRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, UserSearchRequest.newBuilder());
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

    private UserSearchRequest.Builder generateUserSearchRequestWithoutAdditionalHeader(String request, HttpHeaders headers) {
        UserSearchRequest.Builder builder = ProtobufJsonUtil.jsonToProtobuf(request, UserSearchRequest.newBuilder());
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
