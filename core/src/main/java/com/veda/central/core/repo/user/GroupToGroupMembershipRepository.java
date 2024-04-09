package com.veda.central.core.repo.user;

import com.veda.central.core.model.user.GroupToGroupMembership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupToGroupMembershipRepository extends JpaRepository<GroupToGroupMembership, String> {
}
