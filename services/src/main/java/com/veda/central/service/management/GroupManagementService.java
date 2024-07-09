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

import com.veda.central.core.iam.api.GroupRepresentation;
import com.veda.central.core.iam.api.UserAttribute;
import com.veda.central.core.iam.api.UserRepresentation;
import com.veda.central.core.iam.api.UserSearchMetadata;
import com.veda.central.core.iam.api.UserSearchRequest;
import com.veda.central.core.identity.api.AuthToken;
import com.veda.central.core.identity.api.GetUserManagementSATokenRequest;
import com.veda.central.core.user.profile.api.GetAllGroupsResponse;
import com.veda.central.core.user.profile.api.GetAllUserProfilesResponse;
import com.veda.central.core.user.profile.api.Group;
import com.veda.central.core.user.profile.api.GroupAttribute;
import com.veda.central.core.user.profile.api.GroupMembership;
import com.veda.central.core.user.profile.api.GroupToGroupMembership;
import com.veda.central.core.user.profile.api.UserGroupMembershipTypeRequest;
import com.veda.central.core.user.profile.api.UserProfile;
import com.veda.central.core.user.profile.api.UserProfileRequest;
import com.veda.central.service.exceptions.InternalServerException;
import com.veda.central.service.iam.IamAdminService;
import com.veda.central.service.identity.IdentityService;
import com.veda.central.service.profile.UserProfileService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GroupManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupManagementService.class);

    private final IamAdminService iamAdminService;
    private final UserProfileService userProfileService;
    private final IdentityService identityService;

    public GroupManagementService(IamAdminService iamAdminService, UserProfileService userProfileService, IdentityService identityService) {
        this.iamAdminService = iamAdminService;
        this.userProfileService = userProfileService;
        this.identityService = identityService;
    }

    public Group createGroup(com.veda.central.core.user.profile.api.GroupRequest request) {
        try {
            LOGGER.debug("Request received to createGroup " + request.getGroup().getName() + " of tenant " + request.getTenantId());

            String id = request.getGroup().getId();

            updateProfile(request.getClientId(), request.getClientSec(),
                    request.getTenantId(), request.getGroup().getOwnerId());
            if (StringUtils.isNotBlank(id)) {
                Group group = Group.newBuilder().setId(id).build();

                com.veda.central.core.user.profile.api.GroupRequest groupRequest = com.veda.central.core.user.profile.api.GroupRequest
                        .newBuilder().
                        setTenantId(request.getTenantId()).
                        setPerformedBy(request.getPerformedBy()).
                        setGroup(group).build();
                Group exGroup = userProfileService.getGroup(groupRequest);

                if (StringUtils.isNotBlank(exGroup.getName())) {
                    String msg = "Group already exist with given id " + id;
                    LOGGER.error(msg);
                    throw new IllegalArgumentException(msg);
                }

            } else {
                id = request.getGroup().getName().toLowerCase().replace(" ", "_") + "_" + UUID.randomUUID();
            }

            Group group = request.getGroup().toBuilder().setId(id).build();
            request = request.toBuilder().setGroup(group).build();

            return userProfileService.createGroup(request);

        } catch (Exception ex) {
            String msg = "Error occurred at createGroup " + ex.getMessage();
            LOGGER.error(msg, ex);
            throw new InternalServerException(msg, ex);
        }
    }

    public Group updateGroup(com.veda.central.core.user.profile.api.GroupRequest request) {
        try {
            LOGGER.debug("Request received to updateGroup for  group  " + request.getGroup().getId() + " of tenant " + request.getTenantId());

            if (StringUtils.isNotBlank(request.getGroup().getId())) {
                throw new IllegalArgumentException("No Group ID was provided to update the group");
            }

            Group exGroup = userProfileService.getGroup(request);
            if (StringUtils.isNotBlank(exGroup.getName())) {
                Group group = request.getGroup().toBuilder().setParentId(exGroup.getParentId()).build();
                request = request.toBuilder().setGroup(group).build();
                return userProfileService.updateGroup(request);

            } else {
                String msg = "Cannot find a group with id " + request.getId();
                LOGGER.error(msg);
                throw new NotFoundException(msg);
            }

        } catch (Exception ex) {
            String msg = "Error occurred at updateGroup " + ex.getMessage();
            LOGGER.error(msg, ex);
            throw new InternalServerException(msg, ex);
        }
    }

    public com.veda.central.core.user.profile.api.Status deleteGroup(com.veda.central.core.user.profile.api.GroupRequest request) {
        try {
            LOGGER.debug("Request received to deleteGroup for group " + request.getGroup().getId() + " of tenant " + request.getTenantId());
            if (StringUtils.isNotBlank(request.getId())) {
                Group group = request.getGroup().toBuilder().setId(request.getId()).build();
                request = request.toBuilder().setGroup(group).build();
            }
            userProfileService.deleteGroup(request);
            return com.veda.central.core.user.profile.api.Status.newBuilder().setStatus(true).build();

        } catch (Exception ex) {
            String msg = "Error occurred at removeUserFromGroup " + ex.getMessage();
            LOGGER.error(msg, ex);
            throw new InternalServerException(msg, ex);
        }
    }

    public Group findGroup(com.veda.central.core.user.profile.api.GroupRequest request) {
        try {
            LOGGER.debug("Request received to findGroup of tenant " + request.getTenantId());
            return userProfileService.getGroup(request);

        } catch (Exception ex) {
            String msg = "Error occurred at removeUserFromGroup " + ex.getMessage();
            LOGGER.error(msg, ex);
            throw new InternalServerException(msg, ex);
        }
    }

    public GetAllGroupsResponse getAllGroups(com.veda.central.core.user.profile.api.GroupRequest request) {
        try {
            LOGGER.debug("Request received to getAllGroups of tenant " + request.getTenantId());
            return userProfileService.getAllGroups(request);

        } catch (Exception ex) {
            String msg = "Error occurred at getAllGroups " + ex.getMessage();
            LOGGER.error(msg, ex);
            throw new InternalServerException(msg, ex);
        }
    }

    public com.veda.central.core.user.profile.api.Status addUserToGroup(GroupMembership request) {
        try {
            LOGGER.debug("Request received to addUserToGroup for  user  " + request.getUsername() + " of tenant " + request.getTenantId());

            updateProfile(request.getClientId(), request.getClientSec(), request.getTenantId(), request.getUsername());
            return userProfileService.addUserToGroup(request);

        } catch (Exception ex) {
            String msg = "Error occurred at addUserToGroup " + ex.getMessage();
            LOGGER.error(msg, ex);
            throw new InternalServerException(msg, ex);
        }
    }

    public com.veda.central.core.user.profile.api.Status removeUserFromGroup(GroupMembership request) {
        try {
            LOGGER.debug("Request received to removeUserFromGroup for  user  " + request.getUsername() + " of tenant "
                    + request.getTenantId());
            return userProfileService.removeUserFromGroup(request);

        } catch (Exception ex) {
            String msg = "Error occurred at removeUserFromGroup " + ex.getMessage();
            LOGGER.error(msg, ex);
            throw new InternalServerException(msg, ex);
        }
    }

    public com.veda.central.core.user.profile.api.Status addChildGroupToParentGroup(GroupToGroupMembership request) {
        try {
            LOGGER.debug("Request received to addChildGroupToParentGroup for  group  " + request.getChildId() +
                    " to add " + request.getParentId() + " of tenant " + request.getTenantId());

            return userProfileService.addChildGroupToParentGroup(request);

        } catch (Exception ex) {
            String msg = "Error occurred at addChildGroupToParentGroup " + ex.getMessage();
            LOGGER.error(msg, ex);
            throw new InternalServerException(msg, ex);
        }
    }

    public com.veda.central.core.user.profile.api.Status removeChildGroupFromParentGroup(GroupToGroupMembership request) {
        try {
            LOGGER.debug("Request received to removeUserFromGroup for  group  " + request.getChildId() +
                    " to remove " + request.getParentId() + " of tenant " + request.getTenantId());

            return userProfileService.removeChildGroupFromParentGroup(request);

        } catch (Exception ex) {
            String msg = "Error occurred at removeChildGroupFromParentGroup " + ex.getMessage();
            LOGGER.error(msg, ex);
            throw new InternalServerException(msg, ex);
        }
    }

    public GetAllGroupsResponse getAllGroupsOfUser(UserProfileRequest request) {
        try {
            LOGGER.debug("Request received to getAllGroupsOfUser for  user  " + request.getProfile().getUsername() + " of tenant " + request.getTenantId());
            return userProfileService.getAllGroupsOfUser(request);

        } catch (Exception ex) {
            String msg = "Error occurred at getAllGroupsOfUser " + ex.getMessage();
            LOGGER.error(msg, ex);
            throw new InternalServerException(msg, ex);
        }
    }

    public GetAllGroupsResponse getAllParentGroupsOfGroup(com.veda.central.core.user.profile.api.GroupRequest request) {
        try {
            LOGGER.debug("Request received to getAllParentGroupsOfGroup for  group  "
                    + request.getGroup().getId() + " of tenant " + request.getTenantId());

            return userProfileService.getAllParentGroupsOfGroup(request);

        } catch (Exception ex) {
            String msg = "Error occurred at getAllParentGroupsOfGroup " + ex.getMessage();
            LOGGER.error(msg, ex);
            throw new InternalServerException(msg, ex);
        }
    }

    public GetAllUserProfilesResponse getAllChildUsers(com.veda.central.core.user.profile.api.GroupRequest request) {
        try {
            LOGGER.debug("Request received to getAllChildUsers for  group  "
                    + request.getGroup().getId() + " of tenant " + request.getTenantId());

            return userProfileService.getAllChildUsers(request);

        } catch (Exception ex) {
            String msg = "Error occurred at getAllChildUsers " + ex.getMessage();
            LOGGER.error(msg, ex);
            throw new InternalServerException(msg, ex);
        }
    }

    public GetAllGroupsResponse getAllChildGroups(com.veda.central.core.user.profile.api.GroupRequest request) {
        try {
            LOGGER.debug("Request received to getAllChildGroups for  group  "
                    + request.getGroup().getId() + " of tenant " + request.getTenantId());

            return userProfileService.getAllChildGroups(request);

        } catch (Exception ex) {
            String msg = "Error occurred at getAllChildGroups " + ex.getMessage();
            LOGGER.error(msg, ex);
            throw new InternalServerException(msg, ex);
        }
    }

    public com.veda.central.core.user.profile.api.Status changeUserMembershipType(GroupMembership request) {
        try {
            LOGGER.debug("Request received to changeUserMembershipType for  user  "
                    + request.getUsername() + " of tenant " + request.getTenantId());

            return userProfileService.changeUserMembershipType(request);

        } catch (Exception ex) {
            String msg = "Error occurred at changeUserMembershipType " + ex.getMessage();
            LOGGER.error(msg, ex);
            throw new InternalServerException(msg, ex);
        }
    }

    public com.veda.central.core.user.profile.api.Status hasAccess(GroupMembership request) {
        try {
            LOGGER.debug("Request received to hasAccess for  user  "
                    + request.getUsername() + " of tenant " + request.getTenantId());

            return userProfileService.hasAccess(request);

        } catch (Exception ex) {
            String msg = "Error occurred at hasAccess " + ex.getMessage();
            LOGGER.error(msg, ex);
            throw new InternalServerException(msg, ex);
        }
    }

    public com.veda.central.core.user.profile.api.Status addGroupMembershipType(UserGroupMembershipTypeRequest request) {
        try {
            LOGGER.debug("Request received to addGroupMembershipType for  tenant " + request.getTenantId() + ", type " + request.getType());

            return userProfileService.addUserGroupMembershipType(request);

        } catch (Exception ex) {
            String msg = "Error occurred at addGroupMembershipType " + ex.getMessage();
            LOGGER.error(msg, ex);
            throw new InternalServerException(msg, ex);
        }
    }

    public com.veda.central.core.user.profile.api.Status removeUserGroupMembershipType(UserGroupMembershipTypeRequest request) {
        try {
            LOGGER.debug("Request received to removeUserGroupMembershipType for  tenant " + request.getTenantId()
                    + ", type " + request.getType());

            return userProfileService.removeUserGroupMembershipType(request);

        } catch (Exception ex) {
            String msg = "Error occurred at removeUserGroupMembershipType " + ex.getMessage();
            LOGGER.error(msg, ex);
            throw new InternalServerException(msg, ex);
        }
    }

    private com.veda.central.core.user.profile.api.GroupRequest createGroup(GroupRepresentation representation, String parentId, long tenantId, String performedBy) {

        List<GroupAttribute> attributes = new ArrayList<>();

        if (representation.getAttributesList() != null && !representation.getAttributesList().isEmpty()) {
            for (UserAttribute attribute : representation.getAttributesList()) {
                GroupAttribute groupAttribute = GroupAttribute.newBuilder()
                        .setKey(attribute.getKey())
                        .addAllValue(attribute.getValuesList()).build();
                attributes.add(groupAttribute);
            }
        }

        Group group = Group.newBuilder()
                .setId(representation.getId())
                .setName(representation.getName())
                .addAllClientRoles(representation.getClientRolesList())
                .addAllRealmRoles(representation.getRealmRolesList())
                .addAllAttributes(attributes)
                .build();

        if (parentId != null) {
            group = group.toBuilder().setParentId(parentId).build();
        }

        if (StringUtils.isNotBlank(representation.getOwnerId())) {
            group = group.toBuilder().setOwnerId(representation.getOwnerId()).build();
        }

        if (StringUtils.isNotBlank(representation.getDescription())) {
            group = group.toBuilder().setDescription(representation.getDescription()).build();
        }

        return com.veda.central.core.user.profile.api.GroupRequest.newBuilder()
                .setTenantId(tenantId)
                .setPerformedBy(performedBy)
                .setGroup(group).build();
    }

    private void updateProfile(String clientId, String clientSec, long tenantId, String username) {

        UserProfile userProfile = UserProfile.newBuilder().setUsername(username).build();
        UserProfileRequest userProfileRequest = UserProfileRequest.newBuilder()
                .setTenantId(tenantId)
                .setProfile(userProfile)
                .build();

        UserProfile exUser = userProfileService.getUserProfile(userProfileRequest);
        if (exUser.getUsername().isBlank()) {
            GetUserManagementSATokenRequest userManagementSATokenRequest = GetUserManagementSATokenRequest
                    .newBuilder()
                    .setClientId(clientId)
                    .setClientSecret(clientSec)
                    .setTenantId(tenantId)
                    .build();

            AuthToken token = identityService.getUserManagementServiceAccountAccessToken(userManagementSATokenRequest);
            UserSearchMetadata userSearchMetadata = UserSearchMetadata
                    .newBuilder().setUsername(username).build();

            UserSearchRequest searchRequest = UserSearchRequest.newBuilder()
                    .setClientId(clientId)
                    .setTenantId(tenantId)
                    .setAccessToken(token.getAccessToken())
                    .setUser(userSearchMetadata)
                    .build();

            UserRepresentation representation = iamAdminService.getUser(searchRequest);
            UserProfile profile = UserProfile.newBuilder()
                    .setUsername(username)
                    .setFirstName(representation.getFirstName())
                    .setLastName(representation.getLastName())
                    .setEmail(representation.getEmail())
                    .build();

            UserProfileRequest profileRequest = UserProfileRequest.newBuilder()
                    .setTenantId(tenantId)
                    .setProfile(profile)
                    .build();

            userProfileService.createUserProfile(profileRequest);
        }
    }

    private List<com.veda.central.core.user.profile.api.GroupRequest> getAllGroupRequests(List<GroupRepresentation> groupRepresentations,
                                                                                          String parentId, long tenantId, String performedBy) {

        List<com.veda.central.core.user.profile.api.GroupRequest> groupRequests = new ArrayList<>();
        for (GroupRepresentation representation : groupRepresentations) {

            com.veda.central.core.user.profile.api.GroupRequest groupRequest = createGroup(representation, parentId, tenantId, performedBy);
            groupRequests.add(groupRequest);

            if (representation.getSubGroupsList() != null && !representation.getSubGroupsList().isEmpty()) {
                List<com.veda.central.core.user.profile.api.GroupRequest> list = getAllGroupRequests(representation.getSubGroupsList(),
                        representation.getId(), tenantId, performedBy);

                if (!list.isEmpty()) {
                    groupRequests.addAll(list);
                }
            }
        }

        return groupRequests;
    }
}
