package com.veda.central.core.repo.user;

import com.veda.central.core.model.user.UserGroupMembership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMembershipRepository extends JpaRepository<UserGroupMembership, String> {
}
