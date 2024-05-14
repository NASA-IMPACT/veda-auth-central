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
import com.veda.central.core.user.management.api.LinkUserProfileRequest;
import com.veda.central.core.user.management.api.SynchronizeUserDBRequest;
import com.veda.central.core.user.management.api.UserProfileRequest;
import com.veda.central.core.user.profile.api.GetAllUserProfilesResponse;
import com.veda.central.core.user.profile.api.GetUpdateAuditTrailRequest;
import com.veda.central.core.user.profile.api.GetUpdateAuditTrailResponse;
import com.veda.central.core.user.profile.api.UserProfile;
import com.veda.central.service.management.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user-management")
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @PostMapping("/user")
    public ResponseEntity<RegisterUserResponse> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        RegisterUserResponse response = userManagementService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users")
    public ResponseEntity<RegisterUsersResponse> registerAndEnableUsers(@Valid @RequestBody RegisterUsersRequest request) {
        RegisterUsersResponse response = userManagementService.registerAndEnableUsers(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/attributes")
    public ResponseEntity<OperationStatus> addUserAttributes(@Valid @RequestBody AddUserAttributesRequest request) {
        OperationStatus response = userManagementService.addUserAttributes(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/attributes")
    public ResponseEntity<OperationStatus> deleteUserAttributes(@Valid @RequestBody DeleteUserAttributeRequest request) {
        OperationStatus response = userManagementService.deleteUserAttributes(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/activation")
    public ResponseEntity<UserRepresentation> enableUser(@Valid @RequestBody UserSearchRequest request) {
        UserRepresentation response = userManagementService.enableUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/deactivation")
    public ResponseEntity<UserRepresentation> disableUser(@Valid @RequestBody UserSearchRequest request) {
        UserRepresentation response = userManagementService.disableUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/admin")
    public ResponseEntity<OperationStatus> grantAdminPrivileges(@Valid @RequestBody UserSearchRequest request) {
        OperationStatus response = userManagementService.grantAdminPrivileges(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user/admin")
    public ResponseEntity<OperationStatus> removeAdminPrivileges(@Valid @RequestBody UserSearchRequest request) {
        OperationStatus response = userManagementService.removeAdminPrivileges(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/federatedIDPs")
    public ResponseEntity<OperationStatus> deleteExternalIDPsOfUsers(@Valid @RequestBody DeleteExternalIDPsRequest request) {
        OperationStatus response = userManagementService.deleteExternalIDPsOfUsers(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/federatedIDPs")
    public ResponseEntity<OperationStatus> addExternalIDPsOfUsers(@Valid @RequestBody AddExternalIDPLinksRequest request) {
        OperationStatus response = userManagementService.addExternalIDPsOfUsers(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/federatedIDPs")
    public ResponseEntity<GetExternalIDPsResponse> getExternalIDPsOfUsers(@Valid @RequestBody GetExternalIDPsRequest request) {
        GetExternalIDPsResponse response = userManagementService.getExternalIDPsOfUsers(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/roles")
    public ResponseEntity<OperationStatus> addRolesToUsers(@Valid @RequestBody AddUserRolesRequest request) {
        OperationStatus response = userManagementService.addRolesToUsers(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/activation/status")
    public ResponseEntity<OperationStatus> isUserEnabled(@Valid @RequestBody UserSearchRequest request) {
        OperationStatus response = userManagementService.isUserEnabled(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/availability")
    public ResponseEntity<OperationStatus> isUsernameAvailable(@Valid @RequestBody UserSearchRequest request) {
        OperationStatus response = userManagementService.isUsernameAvailable(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<UserRepresentation> getUser(@Valid @RequestBody UserSearchRequest request) {
        UserRepresentation response = userManagementService.getUser(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<FindUsersResponse> findUsers(@Valid @RequestBody FindUsersRequest request) {
        FindUsersResponse response = userManagementService.findUsers(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user/password")
    public ResponseEntity<OperationStatus> resetPassword(@Valid @RequestBody ResetUserPassword request) {
        OperationStatus response = userManagementService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user")
    public ResponseEntity<OperationStatus> deleteUser(@Valid @RequestBody UserSearchRequest request) {
        OperationStatus response = userManagementService.deleteUser(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user/roles")
    public ResponseEntity<OperationStatus> deleteUserRoles(@Valid @RequestBody DeleteUserRolesRequest request) {
        OperationStatus response = userManagementService.deleteUserRoles(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user/profile")
    public ResponseEntity<UserProfile> updateUserProfile(@Valid @RequestBody UserProfileRequest request) {
        UserProfile response = userManagementService.updateUserProfile(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/profile")
    public ResponseEntity<UserProfile> getUserProfile(@Valid @RequestBody UserProfileRequest request) {
        UserProfile response = userManagementService.getUserProfile(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user/profile")
    public ResponseEntity<UserProfile> deleteUserProfile(@Valid @RequestBody UserProfileRequest request) {
        UserProfile response = userManagementService.deleteUserProfile(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/profile")
    public ResponseEntity<GetAllUserProfilesResponse> getAllUserProfilesInTenant(@Valid @RequestBody UserProfileRequest request) {
        GetAllUserProfilesResponse response = userManagementService.getAllUserProfilesInTenant(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/profile/mapper")
    public ResponseEntity<OperationStatus> linkUserProfile(@Valid @RequestBody LinkUserProfileRequest request) {
        OperationStatus response = userManagementService.linkUserProfile(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/profile/audit")
    public ResponseEntity<GetUpdateAuditTrailResponse> getUserProfileAuditTrails(@Valid @RequestBody GetUpdateAuditTrailRequest request) {
        GetUpdateAuditTrailResponse response = userManagementService.getUserProfileAuditTrails(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/db/synchronize")
    public ResponseEntity<OperationStatus> synchronizeUserDBs(@Valid @RequestBody SynchronizeUserDBRequest request) {
        OperationStatus response = userManagementService.synchronizeUserDBs(request);
        return ResponseEntity.ok(response);
    }
}
