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
import com.veda.central.core.tenant.management.api.DeleteTenantRequest;
import com.veda.central.core.tenant.management.api.GetTenantRequest;
import com.veda.central.core.tenant.management.api.UpdateTenantRequest;
import com.veda.central.core.tenant.profile.api.Tenant;
import com.veda.central.service.management.TenantManagementService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenant-management")
public class TenantManagementController {

    private final TenantManagementService tenantManagementService;

    public TenantManagementController(TenantManagementService tenantManagementService) {
        this.tenantManagementService = tenantManagementService;
    }

    @PostMapping("/oauth2/tenant")
    public ResponseEntity<CreateTenantResponse> createTenant(@Valid @RequestBody Tenant request) {
        CreateTenantResponse response = tenantManagementService.createTenant(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/oauth2/tenant")
    public ResponseEntity<Tenant> getTenant(@Valid @RequestBody GetTenantRequest request) {
        Tenant response = tenantManagementService.getTenant(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user/profile")
    public ResponseEntity<Tenant> updateTenant(@Valid @RequestBody UpdateTenantRequest request) {
        Tenant response = tenantManagementService.updateTenant(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user")
    public ResponseEntity<?> deleteTenant(@Valid @RequestBody DeleteTenantRequest request) {
        tenantManagementService.deleteTenant(request);
        return ResponseEntity.noContent().build();
    }
}
