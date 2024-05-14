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

import com.veda.central.core.mapper.tenant.AttributeUpdateMetadataMapper;
import com.veda.central.core.mapper.tenant.StatusUpdateMetadataMapper;
import com.veda.central.core.mapper.tenant.TenantMapper;
import com.veda.central.core.model.tenant.Tenant;
import com.veda.central.core.model.tenant.TenantAttributeUpdateMetadata;
import com.veda.central.core.model.tenant.TenantStatusUpdateMetadata;
import com.veda.central.core.repo.tenant.ContactRepository;
import com.veda.central.core.repo.tenant.RedirectURIRepository;
import com.veda.central.core.repo.tenant.TenantAttributeUpdateMetadataRepository;
import com.veda.central.core.repo.tenant.TenantRepository;
import com.veda.central.core.repo.tenant.TenantStatusUpdateMetadataRepository;
import com.veda.central.core.tenant.profile.api.GetAllTenantsForUserRequest;
import com.veda.central.core.tenant.profile.api.GetAllTenantsForUserResponse;
import com.veda.central.core.tenant.profile.api.GetAllTenantsResponse;
import com.veda.central.core.tenant.profile.api.GetAttributeUpdateAuditTrailResponse;
import com.veda.central.core.tenant.profile.api.GetAuditTrailRequest;
import com.veda.central.core.tenant.profile.api.GetStatusUpdateAuditTrailResponse;
import com.veda.central.core.tenant.profile.api.GetTenantRequest;
import com.veda.central.core.tenant.profile.api.GetTenantResponse;
import com.veda.central.core.tenant.profile.api.GetTenantsRequest;
import com.veda.central.core.tenant.profile.api.IsTenantExistRequest;
import com.veda.central.core.tenant.profile.api.IsTenantExistResponse;
import com.veda.central.core.tenant.profile.api.TenantStatus;
import com.veda.central.core.tenant.profile.api.UpdateStatusRequest;
import com.veda.central.core.tenant.profile.api.UpdateStatusResponse;
import com.veda.central.service.exceptions.AuthenticationException;
import com.veda.central.service.exceptions.InternalServerException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The TenantProfileService class is responsible for managing tenant profiles.
 * It provides methods to add a new tenant, update an existing tenant, retrieve tenants based on certain criteria,
 * and retrieve audit trail information for attribute and status updates for a tenant.
 */
@Service
public class TenantProfileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TenantProfileService.class);

    private final TenantRepository tenantRepository;
    private final TenantStatusUpdateMetadataRepository tenantStatusUpdateMetadataRepository;
    private final TenantAttributeUpdateMetadataRepository tenantAttributeUpdateMetadataRepository;
    private final ContactRepository contactRepository;
    private final RedirectURIRepository redirectURIRepository;

    public TenantProfileService(TenantRepository tenantRepository, TenantStatusUpdateMetadataRepository tenantStatusUpdateMetadataRepository,
                                TenantAttributeUpdateMetadataRepository tenantAttributeUpdateMetadataRepository,
                                ContactRepository contactRepository, RedirectURIRepository redirectURIRepository) {
        this.tenantRepository = tenantRepository;
        this.tenantStatusUpdateMetadataRepository = tenantStatusUpdateMetadataRepository;
        this.tenantAttributeUpdateMetadataRepository = tenantAttributeUpdateMetadataRepository;
        this.contactRepository = contactRepository;
        this.redirectURIRepository = redirectURIRepository;
    }

    public com.veda.central.core.tenant.profile.api.Tenant addTenant(com.veda.central.core.tenant.profile.api.Tenant request) {
        try {
            LOGGER.debug("Add tenant request received for tenant " + TenantMapper.getTenantInfoAsString(request));

            Tenant tenant = TenantMapper.createTenantEntityFromTenant(request);
            tenant.setStatus(TenantStatus.REQUESTED.name());

            Set<TenantStatusUpdateMetadata> metadataSet = StatusUpdateMetadataMapper.createStatusUpdateMetadataEntity(tenant, tenant.getRequesterEmail());
            tenant.setStatusUpdateMetadata(metadataSet);
            Tenant savedTenant = tenantRepository.save(tenant);
            return request.toBuilder().setTenantId(savedTenant.getId()).build();

        } catch (Exception ex) {
            String msg = "Exception occurred while adding the tenant";
            LOGGER.error(msg, ex);
            throw new InternalServerException(msg, ex);
        }
    }

    public com.veda.central.core.tenant.profile.api.Tenant updateTenant(com.veda.central.core.tenant.profile.api.Tenant tenant) {
        try {
            LOGGER.debug("Update tenant request received for tenant " + TenantMapper.getTenantInfoAsString(tenant));
            String updatedBy = "Tenant Admin";
            Long tenantId = tenant.getTenantId();

            if (!isUpdatable(tenantId)) {
                String msg = "Tenant not exist";
                LOGGER.error(msg);
                throw new AuthenticationException(msg);
            }

            Optional<Tenant> opt = tenantRepository.findById(tenantId);
            if (opt.isPresent()) {
                Tenant exTenant = opt.get();
                tenant = tenant.toBuilder().setParentTenantId(exTenant.getParentId()).build();
                Tenant tenantEntity = TenantMapper.createTenantEntityFromTenant(tenant);
                tenantEntity.setCreatedAt(exTenant.getCreatedAt());

                Set<TenantAttributeUpdateMetadata> metadata = AttributeUpdateMetadataMapper.createAttributeUpdateMetadataEntity(exTenant, tenantEntity, updatedBy);
                tenantEntity.setAttributeUpdateMetadata(metadata);

                contactRepository.deleteAllByTenantId(tenantId);
                redirectURIRepository.deleteAllByTenantId(tenantId);
                tenantRepository.save(tenantEntity);

                return tenant;

            } else {
                LOGGER.error("No tenant found to update. Tenant Id: {}", tenantId);
                throw new EntityNotFoundException("No tenant found to update. Tenant Id: " + tenantId);
            }
        } catch (Exception ex) {
            String msg = "Exception occurred while updating the tenant";
            LOGGER.error(msg);
            throw new InternalServerException(msg, ex);
        }
    }

    public GetAllTenantsResponse getAllTenants(GetTenantsRequest request) {
        try {
            LOGGER.debug("Get all tenants request received");

            String status = null;

            if (!request.getStatus().equals(TenantStatus.UNKNOWN)) {
                status = request.getStatus().name();
            }

            int offset = request.getOffset();
            int limit = request.getLimit();
            long parentId = request.getParentId();
            String tenantType = request.getType().name();

            String requesterEmail = request.getRequesterEmail();

            List<Tenant> tenants;
            List<Tenant> total_tenants;

            tenants = tenantRepository.searchTenants(requesterEmail, status, parentId, limit, offset, tenantType);
            total_tenants = tenantRepository.searchTenants(requesterEmail, status, parentId, -1, -1, tenantType);

            List<com.veda.central.core.tenant.profile.api.Tenant> tenantList = new ArrayList<>();

            for (Tenant tenant : tenants) {
                com.veda.central.core.tenant.profile.api.Tenant t = TenantMapper.createTenantFromTenantEntity(tenant);
                tenantList.add(t);
            }

            return GetAllTenantsResponse
                    .newBuilder()
                    .setTotalNumOfTenants(total_tenants.size())
                    .addAllTenant(tenantList).build();

        } catch (Exception ex) {
            String msg = "Exception occurred while retrieving tenants";
            LOGGER.error(msg);
            throw new InternalServerException(msg, ex);
        }
    }

    public GetAllTenantsForUserResponse getAllTenantsForUser(GetAllTenantsForUserRequest request) {
        try {
            LOGGER.debug("Get all tenants for user " + request.getRequesterEmail() + " received");

            String username = request.getRequesterEmail();
            List<Tenant> tenants = tenantRepository.findByRequesterEmail(username);
            List<com.veda.central.core.tenant.profile.api.Tenant> tenantList = new ArrayList<>();

            for (Tenant tenant : tenants) {
                com.veda.central.core.tenant.profile.api.Tenant t = TenantMapper.createTenantFromTenantEntity(tenant);
                tenantList.add(t);
            }

            return GetAllTenantsForUserResponse.newBuilder().addAllTenant(tenantList).build();

        } catch (Exception ex) {
            String msg = "Exception occurred while retrieving tenants";
            LOGGER.error(msg);
            throw new InternalServerException(msg, ex);
        }
    }

    public GetTenantResponse getTenant(GetTenantRequest request) {
        try {
            LOGGER.debug("Get tenant with Id " + request.getTenantId() + " received");

            Long id = request.getTenantId();
            Optional<Tenant> tenant = tenantRepository.findById(id);
            com.veda.central.core.tenant.profile.api.Tenant t;

            if (tenant.isPresent()) {
                t = TenantMapper.createTenantFromTenantEntity(tenant.get());
                return GetTenantResponse.newBuilder().setTenant(t).build();
            } else {
                String msg = "Cannot find the tenant with Id " + request.getTenantId();
                LOGGER.error(msg);
                throw new EntityNotFoundException(msg);
            }

        } catch (Exception ex) {
            String msg = "Exception occurred while retrieving tenants";
            LOGGER.error(msg);
            throw new InternalServerException(msg, ex);
        }
    }

    public GetAttributeUpdateAuditTrailResponse getTenantAttributeUpdateAuditTrail(GetAuditTrailRequest request) {
        try {
            LOGGER.debug("Get tenant attribute update audit trail for " + request.getTenantId());

            Long id = request.getTenantId();

            List<TenantAttributeUpdateMetadata> tenantList = tenantAttributeUpdateMetadataRepository.findAllByTenantId(id);
            List<com.veda.central.core.tenant.profile.api.TenantAttributeUpdateMetadata> metadata = new ArrayList<>();

            for (TenantAttributeUpdateMetadata attributeUpdateMetadata : tenantList) {
                com.veda.central.core.tenant.profile.api.TenantAttributeUpdateMetadata updatedMetadata = AttributeUpdateMetadataMapper.createAttributeUpdateMetadataFromEntity(attributeUpdateMetadata);
                metadata.add(updatedMetadata);
            }

            return GetAttributeUpdateAuditTrailResponse
                    .newBuilder()
                    .addAllMetadata(metadata)
                    .build();

        } catch (Exception ex) {
            String msg = "Exception occurred while retrieving attribute status update metadata";
            LOGGER.error(msg);
            throw new InternalServerException(msg, ex);
        }
    }

    public GetStatusUpdateAuditTrailResponse getTenantStatusUpdateAuditTrail(GetAuditTrailRequest request) {
        try {
            LOGGER.debug("Get tenant attribute update audit trail for " + request.getTenantId());

            Long id = request.getTenantId();
            List<TenantStatusUpdateMetadata> tenantList = tenantStatusUpdateMetadataRepository.findAllByTenantId(id);
            List<com.veda.central.core.tenant.profile.api.TenantStatusUpdateMetadata> metadata = new ArrayList<>();

            for (TenantStatusUpdateMetadata statusUpdateMetadata : tenantList) {
                com.veda.central.core.tenant.profile.api.TenantStatusUpdateMetadata updatedMetadata = StatusUpdateMetadataMapper.createTenantStatusMetadataFrom(statusUpdateMetadata);
                metadata.add(updatedMetadata);
            }

            return GetStatusUpdateAuditTrailResponse
                    .newBuilder()
                    .addAllMetadata(metadata)
                    .build();

        } catch (Exception ex) {
            String msg = "Exception occurred while retrieving status update metadata";
            LOGGER.error(msg);
            throw new InternalServerException(msg, ex);
        }
    }

    public IsTenantExistResponse isTenantExist(IsTenantExistRequest request) {
        try {
            LOGGER.debug("Is tenant exist " + request.getTenantId() + " received");

            Long id = request.getTenantId();
            Optional<Tenant> tenant = tenantRepository.findById(id);

            return IsTenantExistResponse.newBuilder().setIsExist(tenant.isPresent()).build();

        } catch (Exception ex) {
            String msg = "Exception occurred while retrieving tenants";
            LOGGER.error(msg);
            throw new InternalServerException(msg, ex);
        }
    }

    public UpdateStatusResponse updateTenantStatus(UpdateStatusRequest request) {
        try {
            LOGGER.debug("Update tenant request status received for " + request.getTenantId() + " received");

            long id = request.getTenantId();
            String status = request.getStatus().name();
            String updatedBy = request.getUpdatedBy();
            Optional<Tenant> tenant = tenantRepository.findById(id);

            if (tenant.isPresent()) {
                Tenant t = tenant.get();
                t.setStatus(status);
                Set<TenantStatusUpdateMetadata> metadata = StatusUpdateMetadataMapper.createStatusUpdateMetadataEntity(t, updatedBy);
                t.setStatusUpdateMetadata(metadata);
                tenantRepository.save(t);

                return UpdateStatusResponse.newBuilder()
                        .setTenantId(id)
                        .setStatus(request.getStatus())
                        .build();
            } else {
                LOGGER.error("Tenant is not found to update the tenant status, tenant Id: {}", id);
                throw new EntityNotFoundException("Tenant is not found to update the tenant status, tenant Id: {}" + id);
            }

        } catch (Exception ex) {
            String msg = "Exception occurred while updating tenant status";
            LOGGER.error(msg);
            throw new InternalServerException(msg, ex);
        }
    }

    private boolean isUpdatable(Long tenantId) {
        Optional<Tenant> opt = tenantRepository.findById(tenantId);
        return opt.isPresent();
    }
}
