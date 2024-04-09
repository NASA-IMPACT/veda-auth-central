package com.veda.central.core.repo.user;

import com.veda.central.core.model.user.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, String> {
}
