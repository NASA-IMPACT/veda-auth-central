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

package com.veda.central.service.profile;

import com.veda.central.core.mapper.user.AttributeUpdateMetadataMapper;
import com.veda.central.core.mapper.user.GroupMapper;
import com.veda.central.core.mapper.user.UserProfileMapper;
import com.veda.central.core.model.user.AttributeUpdateMetadata;
import com.veda.central.core.model.user.Group;
import com.veda.central.core.model.user.UserGroupMembership;
import com.veda.central.core.model.user.UserGroupMembershipType;
import com.veda.central.core.model.user.UserProfile;
import com.veda.central.core.repo.user.AttributeUpdateMetadataRepository;
import com.veda.central.core.repo.user.GroupAttributeRepository;
import com.veda.central.core.repo.user.GroupMembershipRepository;
import com.veda.central.core.repo.user.GroupMembershipTypeRepository;
import com.veda.central.core.repo.user.GroupRepository;
import com.veda.central.core.repo.user.GroupRoleRepository;
import com.veda.central.core.repo.user.GroupToGroupMembershipRepository;
import com.veda.central.core.repo.user.StatusUpdateMetadataRepository;
import com.veda.central.core.repo.user.UserAttributeRepository;
import com.veda.central.core.repo.user.UserProfileRepository;
import com.veda.central.core.repo.user.UserRoleRepository;
import com.veda.central.core.user.api.UserProfileRequest;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class UserProfileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileService.class);

    @Autowired
    private UserProfileRepository repository;

    @Autowired
    private StatusUpdateMetadataRepository statusUpdaterRepository;

    @Autowired
    private AttributeUpdateMetadataRepository attributeUpdateMetadataRepository;

    @Autowired
    private UserAttributeRepository userAttributeRepository;

    @Autowired
    private UserRoleRepository roleRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupRoleRepository groupRoleRepository;

    @Autowired
    private GroupAttributeRepository groupAttributeRepository;

    @Autowired
    private GroupMembershipRepository groupMembershipRepository;

    @Autowired
    private GroupToGroupMembershipRepository groupToGroupMembershipRepository;

    @Autowired
    private GroupMembershipTypeRepository groupMembershipTypeRepository;


    public void createUserProfile(UserProfileRequest request) {
        try {
            LOGGER.debug("Request received to createUserProfile for " + request.getUserProfile().getUsername() + "at " + request.getTenantId());

            String userId = request.getUserProfile().getUsername() + "@" + request.getTenantId();

            Optional<UserProfile> op = repository.findById(userId);

            if (op.isEmpty()) {
                UserProfile entity = UserProfileMapper.createUserProfileEntityFromUserProfile(request.getUserProfile());
                entity.setId(userId);
                entity.setTenantId(request.getTenantId());
                repository.save(entity);
            }

        } catch (Exception ex) {
            String msg = "Error occurred while creating user profile for " + request.getUserProfile().getUsername() + "at "
                    + request.getTenantId() + " reason :" + ex.getMessage();
            LOGGER.error(msg);
        }

    }

    public void updateUserProfile(UserProfileRequest request) {
        try {
            LOGGER.debug("Request received to updateUserProfile for " + request.getUserProfile().getUsername() + "at " + request.getTenantId());

            String userId = request.getUserProfile().getUsername() + "@" + request.getTenantId();

            Optional<UserProfile> exEntity = repository.findById(userId);


            if (exEntity.isPresent()) {
                UserProfile entity = UserProfileMapper.createUserProfileEntityFromUserProfile(request.getUserProfile());
                Set<AttributeUpdateMetadata> metadata = AttributeUpdateMetadataMapper.
                        createAttributeUpdateMetadataEntity(exEntity.get(), entity, request.getPerformedBy());

                entity.setAttributeUpdateMetadata(metadata);
                entity.setId(userId);
                entity.setTenantId(request.getTenantId());
                entity.setCreatedAt(exEntity.get().getCreatedAt());

                UserProfile exProfile = exEntity.get();

                if (exProfile.getUserAttribute() != null) {
                    userAttributeRepository.deleteAll(exProfile.getUserAttribute());
                }

                if (exProfile.getUserRole() != null) {
                    roleRepository.deleteAll(exProfile.getUserRole());
                }

                repository.save(entity);

            } else {
                LOGGER.error("Cannot find a user profile for " + userId);
            }

        } catch (Exception ex) {
            String msg = "Error occurred while updating user profile for " + request.getUserProfile().getUsername() + "at "
                    + request.getTenantId() + " reason :" + ex.getMessage();
            LOGGER.error(msg, ex);
        }
    }

    public com.veda.central.core.user.UserProfile getUserProfile(UserProfileRequest request) {
        try {
            LOGGER.debug("Request received to getUserProfile for " + request.getUserProfile().getUsername() + "at " + request.getTenantId());

            String userId = request.getUserProfile().getUsername() + "@" + request.getTenantId();

            Optional<UserProfile> entity = repository.findById(userId);

            if (entity.isPresent()) {
                UserProfile profileEntity = entity.get();
                return UserProfileMapper.createUserProfileFromUserProfileEntity(profileEntity, null);

            } else {
                throw new EntityNotFoundException("Could not find the UserProfile with the id: " + userId);
            }

        } catch (Exception ex) {
            String msg = "Error occurred while fetching user profile for " + request.getUserProfile().getUsername() + "at " + request.getTenantId();
            LOGGER.error(msg);
            throw new RuntimeException(msg, ex);
        }
    }

    public void deleteUserProfile(UserProfileRequest request) {
        try {
            LOGGER.debug("Request received to deleteUserProfile for " + request.getUserProfile().getUsername() + "at " + request.getTenantId());
            long tenantId = request.getTenantId();

            String username = request.getUserProfile().getUsername();

            String userId = username + "@" + tenantId;

            Optional<UserProfile> profileEntity = repository.findById(userId);

            if (profileEntity.isPresent()) {
                repository.delete(profileEntity.get());

            } else {
                throw new EntityNotFoundException("Could not find the UserProfile with the id: " + userId);
            }

        } catch (Exception ex) {
            String msg = "Error occurred while deleting user profile for " + request.getUserProfile().getUsername() + "at " + request.getTenantId();
            LOGGER.error(msg);
        }
    }

    public com.veda.central.core.user.Group createGroup(com.veda.central.core.user.api.GroupRequest request) {
        try {
            LOGGER.debug("Request received to createGroup from tenant" + request.getTenantId());

            String groupId = request.getGroup().getId();
            long tenantId = request.getTenantId();

            String effectiveId = groupId + "@" + tenantId;

            Optional<Group> op = groupRepository.findById(effectiveId);

            String ownerId = request.getGroup().getOwnerId() + "@" + tenantId;

            Optional<UserProfile> userProfile = repository.findById(ownerId);

            if (userProfile.isEmpty()) {
                String msg = "Error occurred while creating  Group for " + request.getTenantId()
                        + " reason : Owner  not found";
                LOGGER.error(msg);
                throw new RuntimeException(msg);
            }

            Group savedGroup = null;
            if (op.isEmpty()) {

                Group entity = GroupMapper.createGroupEntity(request.getGroup(), request.getTenantId());

                String parentId = entity.getParentId();
                if (parentId != null && !parentId.trim().equals("")) {

                    Optional<Group> parent = groupRepository.findById(parentId);

                    if (parent.isEmpty()) {
                        String msg = "Error occurred while creating  Group for " + request.getTenantId()
                                + " reason : Parent group not found";
                        LOGGER.error(msg);
                        throw new RuntimeException(msg);
                    }

                    GroupMapper.setParentGroupMembership(parent.get(), entity);
                }

                savedGroup = groupRepository.save(entity);
            }

            Optional<Group> exOP = groupRepository.findById(effectiveId);

            if (exOP.isPresent()) {
                String type = com.veda.central.core.user.DefaultGroupMembershipTypes.OWNER.name();

                Optional<UserGroupMembershipType> groupMembershipType = groupMembershipTypeRepository.findById(type);
                UserGroupMembershipType exist = null;

                if (groupMembershipType.isEmpty()) {
                    exist = new UserGroupMembershipType();
                    exist.setId(type);
                    groupMembershipTypeRepository.save(exist);
                }

                exist = groupMembershipType.get();

                UserGroupMembership userGroupMembership = new UserGroupMembership();
                userGroupMembership.setGroup(savedGroup);
                userGroupMembership.setUserProfile(userProfile.get());
                userGroupMembership.setTenantId(tenantId);

                userGroupMembership.setUserGroupMembershipType(exist);
                groupMembershipRepository.save(userGroupMembership);

                return GroupMapper.createGroup(exOP.get(), userGroupMembership.getUserProfile().getUsername());
            } else {

                String msg = "Error occurred while creating Group for " + request.getTenantId()
                        + " reason : DB error";
                LOGGER.error(msg);
                throw new RuntimeException(msg);
            }


        } catch (Exception ex) {
            String msg = "Error occurred while creating Group for " + request.getTenantId() +
                    " reason :" + ex.getMessage();
            LOGGER.error(msg);
            throw new RuntimeException(msg, ex);
        }

    }
}
