package com.grs.core.service;

import com.grs.api.model.OISFUserType;
import com.grs.api.model.UserInformation;
import com.grs.api.model.UserType;
import com.grs.core.dao.CentralDashboardRecipientDAO;
import com.grs.core.domain.grs.CentralDashboardRecipient;
import com.grs.core.domain.grs.Grievance;
import com.grs.core.domain.grs.GrievanceForwarding;
import com.grs.core.domain.grs.OfficesGRO;
import com.grs.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class AccessControlService {
    @Autowired
    GrievanceService grievanceService;
    @Autowired
    OfficesGroService officesGroService;
    @Autowired
    CentralDashboardRecipientDAO centralDashboardRecipientDAO;

    public Boolean hasPermissionToViewGrievanceDetails(Authentication authentication, Long grievanceId) {
        Grievance grievance = grievanceService.findGrievanceById(grievanceId);
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        if(userInformation.getUserType().equals(UserType.COMPLAINANT)) {
            return userInformation.getUserId().equals(grievance.getComplainantId());
        } else if(userInformation.getUserType().equals(UserType.OISF_USER)) {

            Long officeId = userInformation.getOfficeInformation().getOfficeId();
            Long officeUnitOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();
            if(grievance.getComplainantId() != null && (grievance.getComplainantId().equals(userInformation.getUserId()) || grievance.getComplainantId().equals(userInformation.getOfficeInformation().getEmployeeRecordId()))) {
                return true;
            }
            List<OfficesGRO> officesGROES = this.officesGroService.getAncestors(grievance.getOfficeId());
            for (OfficesGRO officesGRO : officesGROES){
                if((Objects.equals(officeId, officesGRO.getGroOfficeId()) && Objects.equals(officeUnitOrganogramId, officesGRO.getGroOfficeUnitOrganogramId())) ||
                        (Objects.equals(officeId, officesGRO.getGroOfficeId()) && Objects.equals(officeUnitOrganogramId, officesGRO.getAppealOfficerOfficeUnitOrganogramId()))){
                    return true;
                }
            }
            if (userInformation.getIsCentralDashboardUser()){
                return true;
            }
            if( userInformation.getOisfUserType().equals(OISFUserType.HEAD_OF_OFFICE) && Objects.equals(officeId, grievance.getOfficeId())){
                return true;
            }
            List<GrievanceForwarding> grievanceForwardingList = grievanceService.getAllComplaintMovementByGrievance(grievance);
            List<Long> permittedOrganogramIdList = new ArrayList();
            grievanceForwardingList.forEach(grievanceForwarding -> {
                permittedOrganogramIdList.add(grievanceForwarding.getFromOfficeUnitOrganogramId());
                permittedOrganogramIdList.add(grievanceForwarding.getToOfficeUnitOrganogramId());
            });
            return permittedOrganogramIdList.contains(userInformation.getOfficeInformation().getOfficeUnitOrganogramId());
        } else {
            return false;
        }
    }
}
