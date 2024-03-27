package com.grs.core.service;


import com.grs.api.model.OISFUserType;
import com.grs.api.model.response.ActionToRoleDTO;
import com.grs.core.dao.ActionDAO;
import com.grs.core.dao.ActionToRoleDAO;
import com.grs.core.dao.GrievanceStatusDAO;
import com.grs.core.dao.GrsRoleDAO;
import com.grs.core.domain.grs.Action;
import com.grs.core.domain.grs.ActionToRole;
import com.grs.core.domain.grs.GrievanceStatus;
import com.grs.core.domain.grs.GrsRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.WeakHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Acer on 02-Oct-17.
 */
@Service
public class ActionToRoleService {
    @Autowired
    private ActionToRoleDAO actionToRoleDAO;
    @Autowired
    private GrievanceStatusDAO grievanceStatusDAO;
    @Autowired
    private GrsRoleDAO grsRoleDAO;
    @Autowired
    private ActionDAO actionDAO;
    @Autowired
    private MessageService messageService;

    public GrsRole findGrsRole(OISFUserType oisfUserType){
        return this.grsRoleDAO.findByRole(oisfUserType.toString());
    }

    public List<ActionToRole> findByGrievanceStatusAndRoleType(GrievanceStatus grievanceStatus, GrsRole role){
        return this.actionToRoleDAO.findByGrievanceStatusAndRoleType(grievanceStatus, role);
    }

    public ActionToRole findByGrievanceStatusAndRoleAndAction(GrievanceStatus grievanceStatus, GrsRole grsRole, Action action){
        return this.actionToRoleDAO.findByGrievanceStatusAndRoleAndAction(grievanceStatus,grsRole,action);
    }

    public GrsRole getRolebyRoleName(String role){
        return this.grsRoleDAO.findByRole(role);
    }

    public GrievanceStatus findByName(String statusName){
        return this.grievanceStatusDAO.findByName(statusName);
    }

    public Action findByActionId(Long actionID) {
        return this.actionDAO.findOne(actionID);
    }

    public List<ActionToRole> findGRSRoleByGrievanceStatus(GrievanceStatus grievanceStatus) {
        return this.actionToRoleDAO.findGRSRoleByGrievanceStatus(grievanceStatus);
    }

    public ActionToRoleDTO convertToActionToRoleDTO(ActionToRole actionToRole){
        return ActionToRoleDTO.builder()
                .id(actionToRole.getId())
                .grievanceStatus(actionToRole.getGrievanceStatus().getStatusName())
                .role(actionToRole.getRole().getRole())
                .action(actionToRole.getAction().getActionBng())
                .build();
    }

    public GrievanceStatus findByGrievanceStatusId(Long grievanceStatusId) {
        return this.grievanceStatusDAO.findOne(grievanceStatusId);
    }

    public WeakHashMap<String, String> findDistinctGRSRoleByGrievanceStatus(String grievanceStatus) {
        WeakHashMap<String, String> grsRoleList = new WeakHashMap<>();
        GrievanceStatus grievanceCurrentStatus = this.grievanceStatusDAO.findByName(grievanceStatus);
        this.actionToRoleDAO.findDistinctRoleByGrievanceStatus(grievanceCurrentStatus).forEach(grsRole -> {
            String roleName = grsRole.getRole().getRole();
            String code = roleName.toLowerCase().replace('_', '.');
            grsRoleList.put(roleName,messageService.getMessage(code));
        });
        return grsRoleList;
    }

    public WeakHashMap<String, String> findActionsByGrievanceStatusAndGrsRole(String grievanceStatus, String grsRole) {
        WeakHashMap<String,String> actionList = new WeakHashMap<>();
        GrsRole grsRoleType = this.grsRoleDAO.findByRole(grsRole);
        GrievanceStatus grievanceCurrentStatus = this.grievanceStatusDAO.findByName(grievanceStatus);
        this.actionToRoleDAO.findByGrievanceStatusAndRoleType(grievanceCurrentStatus, grsRoleType).forEach(action -> {
            String actionBng = action.getAction().getActionBng();
            String code = actionBng.toLowerCase().replace('_', '.');
            actionList.put(actionBng,messageService.getMessage(code));
        });
        return actionList;
    }
}
