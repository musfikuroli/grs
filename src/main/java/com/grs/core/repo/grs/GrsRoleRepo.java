package com.grs.core.repo.grs;

import com.grs.core.domain.grs.GrsRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Acer on 8/30/2017.
 */
@Repository
public interface GrsRoleRepo extends JpaRepository <GrsRole, Long> {
    public GrsRole findByRole(String role);
}
