package com.veda.central.core.repo.user;

import com.veda.central.core.model.user.UserAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAttributeRepository extends JpaRepository<UserAttribute, Long> {
}
