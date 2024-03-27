package com.grs.core.repo.grs;

import com.grs.core.domain.grs.Action;
import com.grs.core.domain.grs.ActionToRole;
import com.grs.core.domain.grs.GrievanceStatus;
import com.grs.core.domain.grs.GrsRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Acer on 8/30/2017.
 */
@Repository
public interface ActionToRoleRepo extends JpaRepository<ActionToRole, Long> {
    public List<ActionToRole> findByGrievanceStatusAndRole(GrievanceStatus grievanceStatus, GrsRole role);
    public ActionToRole findByGrievanceStatusAndRoleAndAction(GrievanceStatus grievanceStatus, GrsRole grsRole, Action action);
    public List<ActionToRole> findByGrievanceStatus(GrievanceStatus grievanceStatus);
    public List<ActionToRole> findDistinctRoleByGrievanceStatus(GrievanceStatus grievanceStatus);
}
