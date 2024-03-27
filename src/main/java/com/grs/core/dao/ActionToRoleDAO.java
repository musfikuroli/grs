package com.grs.core.dao;

import com.grs.core.domain.grs.Action;
import com.grs.core.domain.grs.ActionToRole;
import com.grs.core.domain.grs.GrievanceStatus;
import com.grs.core.domain.grs.GrsRole;
import com.grs.core.repo.grs.ActionToRoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Acer on 9/28/2017.
 */
@Service
public class ActionToRoleDAO {
    @Autowired
    private ActionToRoleRepo actionToRoleRepo;

    public ActionToRole findOne(Long id) {
        return this.actionToRoleRepo.findOne(id);
    }

    public List<ActionToRole> findByGrievanceStatusAndRoleType(GrievanceStatus grievanceStatus, GrsRole role){
        return this.actionToRoleRepo.findByGrievanceStatusAndRole(grievanceStatus, role);
    }

    public ActionToRole findByGrievanceStatusAndRoleAndAction(GrievanceStatus grievanceStatus, GrsRole grsRole, Action action){
        return this.actionToRoleRepo.findByGrievanceStatusAndRoleAndAction(grievanceStatus,grsRole,action);
    }

    public List<ActionToRole> findGRSRoleByGrievanceStatus(GrievanceStatus grievanceStatus) {
        return this.actionToRoleRepo.findByGrievanceStatus(grievanceStatus);
    }

    public List<ActionToRole> findDistinctRoleByGrievanceStatus(GrievanceStatus grievanceStatus) {
        return this.actionToRoleRepo.findDistinctRoleByGrievanceStatus(grievanceStatus);
    }
}
