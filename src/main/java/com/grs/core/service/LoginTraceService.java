package com.grs.core.service;


import com.grs.api.model.LoginType;
import com.grs.api.model.OISFUserType;
import com.grs.api.model.UserInformation;
import com.grs.api.model.response.ActionToRoleDTO;
import com.grs.core.dao.*;
import com.grs.core.domain.grs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.WeakHashMap;

/**
 * Created by Acer on 02-Oct-17.
 */
@Service
public class LoginTraceService {

    @Autowired
    LoginTraceDAO loginTraceDAO;

    public LoginTrace save(LoginTrace loginTrace) {
        return loginTraceDAO.save(loginTrace);
    }

    public LoginTrace saveSSOLogin(UserInformation userInformation) {
        return save(LoginTrace.builder()
                .loginType(LoginType.OISF.name())
                .userId(userInformation.getUserId())
                .username(userInformation.getUsername())
                .officeId(userInformation.getOfficeInformation().getOfficeId())
                .officeNameBangla(userInformation.getOfficeInformation().getOfficeNameBangla())
                .officeNameEnglish(userInformation.getOfficeInformation().getOfficeNameEnglish())
                .officeMinistryId(userInformation.getOfficeInformation().getOfficeMinistryId())
                .officeOriginId(userInformation.getOfficeInformation().getOfficeOriginId())
                .designation(userInformation.getOfficeInformation().getDesignation())
                .employeeRecordId(userInformation.getOfficeInformation().getEmployeeRecordId())
                .officeUnitOrganogramId(userInformation.getOfficeInformation().getOfficeUnitOrganogramId())
                .layerLevel(userInformation.getOfficeInformation().getLayerLevel())
                .geoDivisionId(userInformation.getOfficeInformation().getGeoDivisionId())
                .geoDistrictId(userInformation.getOfficeInformation().getGeoDistrictId())
                .build()
        );
    }

    public LoginTrace saveMyGovLogin(UserInformation userInformation) {
        return save(LoginTrace.builder()
                .loginType(LoginType.MYGOV.name())
                .userId(userInformation.getUserId())
                .username(userInformation.getUsername())
                .mobileNo(userInformation.getUsername())
                .build()
        );
    }

    public LoginTrace saveGRSLogin(UserInformation userInformation) {
        return save(LoginTrace.builder()
                .loginType(LoginType.GRS.name())
                .userId(userInformation.getUserId())
                .username(userInformation.getUsername())
                .mobileNo(userInformation.getUsername())
                .build()
        );
    }

}
