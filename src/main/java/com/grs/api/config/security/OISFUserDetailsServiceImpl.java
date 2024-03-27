package com.grs.api.config.security;

import com.grs.api.model.OISFUserType;
import com.grs.api.model.OfficeInformation;
import com.grs.api.model.UserInformation;
import com.grs.api.model.UserType;
import com.grs.api.model.oisf_response.SingleRoleDetailsDTO;
import com.grs.core.dao.*;
import com.grs.core.domain.doptor.OfficeInfo;
import com.grs.core.domain.doptor.OfficeOrganogram;
import com.grs.core.domain.doptor.UserInfo;
import com.grs.core.domain.grs.GrsRole;
import com.grs.core.domain.grs.OfficesGRO;
import com.grs.core.domain.projapoti.EmployeeOffice;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.domain.projapoti.OfficeUnitOrganogram;
import com.grs.core.domain.projapoti.User;
import com.grs.core.service.ESBConnectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Acer on 10/5/2017.
 */
@Slf4j
@Service
public class OISFUserDetailsServiceImpl implements UserDetailsService {
    @Value("${oisf.core.services.api.port}")
    private String port;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private OfficesGroDAO officesGroDAO;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private OISFUserDetailsServiceImpl oisfUserDetailsService;
    @Autowired
    private GrsRoleDAO grsRoleDAO;
    @Autowired
    private ESBConnectorService esbConnectorService;
    @Autowired
    private CentralDashboardRecipientDAO centralDashboardRecipientDAO;
    @Autowired
    private CellMemberDAO cellMemberDAO;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userDAO.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Invalid credentials");
        }

        UserInformation userInformation = this.oisfUserDetailsService.getUserInfo(user);
        GrsRole grsRole = this.grsRoleDAO.findByRole(userInformation.getOisfUserType().toString());
        List<GrantedAuthorityImpl> grantedAuthorities = grsRole.getPermissions().stream()
                .map(permission -> {
                    return GrantedAuthorityImpl.builder()
                            .role(permission.getName())
                            .build();
                }).collect(Collectors.toList());


        return UserDetailsImpl.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .isAccountAuthenticated(user.getAuthenticated())
                .grantedAuthorities(grantedAuthorities)
                .userInformation(this.getUserInfo(user))
                .build();
    }

    public UserInformation getUserInfo(User user) {
        OfficeInformation officeInformation = getOfficeInformationFromUser(user);
        List<OfficesGRO> userAsAppealOfficerList = new ArrayList();
        List<OfficesGRO> userAsOfficeAdminList = new ArrayList();
        Long officeUnitOrganogramId = officeInformation != null ? officeInformation.getOfficeUnitOrganogramId() : null;
        Boolean hasCentralDashboardAccess = false, isCellGRO = false;
        if (officeInformation != null && officeUnitOrganogramId != null) {
            userAsAppealOfficerList = this.officesGroDAO.findByAppealOfficeUnitOrganogramId(officeUnitOrganogramId);
            userAsOfficeAdminList = officesGroDAO.findByAdminOfficeUnitOrganogramId(officeUnitOrganogramId);
            hasCentralDashboardAccess = centralDashboardRecipientDAO.hasAccessToCentralDashboard(officeInformation.getOfficeId(), officeInformation.getOfficeUnitOrganogramId());
            isCellGRO = cellMemberDAO.isCellGRO(officeInformation.getOfficeId(), officeInformation.getEmployeeRecordId());
        }

        return UserInformation
                .builder()
                .userId(user.getId())
                .username(user.getUsername())
                .userType(UserType.OISF_USER)
                .oisfUserType(getOISFUserTypeFromRole(user))
                .grsUserType(null)
                .officeInformation(officeInformation)
                .isAppealOfficer(userAsAppealOfficerList.size() > 0 || isCellGRO)
                .isOfficeAdmin(userAsOfficeAdminList.size() > 0)
                .isCentralDashboardUser(hasCentralDashboardAccess)
                .isCellGRO(isCellGRO)
                .isMobileLogin(false)
                .build();
    }


    public UserInformation getUserInfo(UserInfo userInfo) {

        com.grs.core.domain.doptor.User user = userInfo.getUser();

        OfficeInformation officeInformation = getOfficeInformationFromUser(userInfo);


        List<OfficesGRO> userAsAppealOfficerList = new ArrayList<>();

        List<OfficesGRO> userAsOfficeAdminList = new ArrayList<>();

        Long officeUnitOrganogramId = officeInformation.getOfficeUnitOrganogramId();

        Boolean hasCentralDashboardAccess = false, isCellGRO = false;

        if (officeUnitOrganogramId != null) {

            userAsAppealOfficerList = this.officesGroDAO.findByAppealOfficeUnitOrganogramId(officeUnitOrganogramId);

            userAsOfficeAdminList = this.officesGroDAO.findByAdminOfficeUnitOrganogramId(officeUnitOrganogramId);

            hasCentralDashboardAccess = this.centralDashboardRecipientDAO.hasAccessToCentralDashboard(officeInformation.getOfficeId(), officeInformation.getOfficeUnitOrganogramId());

            isCellGRO = this.cellMemberDAO.isCellGRO(officeInformation.getOfficeId(), officeInformation.getEmployeeRecordId());

        }


        return
                UserInformation.builder()
                        .userId(Long.valueOf(user.getId()))
                        .username(user.getUsername())
                        .userType(UserType.OISF_USER)
                        .oisfUserType(getOISFUserTypeFromRole(userInfo))
                        .grsUserType(null)
                        .officeInformation(officeInformation)
                        .isAppealOfficer(Boolean.valueOf((userAsAppealOfficerList.size() > 0 || isCellGRO.booleanValue())))
                        .isOfficeAdmin(Boolean.valueOf((userAsOfficeAdminList.size() > 0)))
                        .isCentralDashboardUser(hasCentralDashboardAccess)
                        .isCellGRO(isCellGRO)
                        .isMobileLogin(false)
                        .build();

    }


    public OfficeInformation getOfficeInformationFromUser(User user) {
        if (user.getEmployeeRecord() == null) {
            return null;
        }
        if (user.getEmployeeRecord().getEmployeeOffices() == null) {
            return null;
        }

        List<EmployeeOffice> userEmployeeOffices = user.getEmployeeRecord()
                .getEmployeeOffices()
                .stream()
                .filter(employeeOffice -> (employeeOffice.getStatus()))
                .collect(Collectors.toList());

        if (userEmployeeOffices.size() < 1) {
            return null;
        }
        Office office = userEmployeeOffices.get(0).getOffice();
        OfficeUnitOrganogram officeUnitOrganogram = userEmployeeOffices.get(0).getOfficeUnitOrganogram();

        OfficeInformation officeInformation = OfficeInformation.builder()
                .officeId(office.getId())
                .officeOriginId(office.getOfficeOriginId())
                .officeMinistryId(office.getOfficeMinistry().getId())
                .officeNameBangla(office.getNameBangla())
                .officeNameEnglish(office.getNameEnglish())
                .designation(userEmployeeOffices.get(0).getDesignation())
                .employeeRecordId(user.getEmployeeRecord().getId())
                .name(user.getEmployeeRecord().getNameBangla())
                .officeUnitOrganogramId(officeUnitOrganogram.getId())
                .layerLevel(Long.valueOf(office.getOfficeLayer().getLayerLevel()))
                .geoDivisionId(Long.valueOf(office.getDivisionId()))
                .geoDistrictId(Long.valueOf(office.getDistrictId()))
                .build();

        return officeInformation;
    }


    public OfficeInformation getOfficeInformationFromUser(UserInfo userInfo) {

        com.grs.core.domain.doptor.User user = userInfo.getUser();
        List<OfficeInfo> officeInfos = userInfo.getOffice_info();
        HashMap<String, OfficeOrganogram> organogramInfo = userInfo.getOrganogram_info();

        if (user.getEmployee_record_id() == null) {
            return null;
        }

        if (officeInfos == null) {
            return null;
        }


        List<OfficeInfo> userEmployeeOffices = officeInfos;
//        List<OfficeInfo> userEmployeeOffices = officeInfos.stream()
////                .filter(employeeOffice -> employeeOffice.getStatus().booleanValue())
//                .filter(true)
//                .collect(Collectors.toList());


        if (userEmployeeOffices.size() < 1) {

            return null;

        }

//        Office officeDoptor = userEmployeeOffices.get(0).getOffice();
        OfficeInfo officeDoptor = officeInfos.get(0);
        OfficesGRO officesGRO = officesGroDAO.findByOfficeId(Long.valueOf(officeDoptor.getOffice_id()));
        OfficeOrganogram officeUnitOrganogram = new OfficeOrganogram();
        for (Map.Entry<String, OfficeOrganogram> oi : organogramInfo.entrySet()) {
//            OfficeUnitOrganogram officeUnitOrganogram = organogramInfo.get(0).getOfficeUnitOrganogram();
            if (officeDoptor.getOffice_unit_organogram_id().equals(Integer.parseInt(oi.getKey()))) {
//            if (officesGRO.getGroOfficeUnitOrganogramId().equals(Integer.parseInt(oi.getKey()))) {
                officeUnitOrganogram = oi.getValue();
            }
        }


        OfficeInformation officeInformation = OfficeInformation.builder()
                .officeId(Long.valueOf(officeDoptor.getOffice_id()))
                .officeOriginId(officesGRO != null ? officesGRO.getOfficeOriginId() : null)
                .officeMinistryId(officesGRO != null ? officesGRO.getOfficeMinistryId() : null)
                .officeNameBangla(officeDoptor.getOffice_name_bn())
                .officeNameEnglish(officeDoptor.getOffice_name_en())
                .designation(userEmployeeOffices.get(0).getDesignation())
                .employeeRecordId(Long.valueOf(user.getEmployee_record_id()))
//                .name(user.getEmployee_record_id().getNameBangla())
                .name(officeDoptor.getOffice_name_bn())
                .officeUnitOrganogramId(Long.valueOf(officeUnitOrganogram.getId()))
                .layerLevel(officesGRO != null ? Long.valueOf(officesGRO.getLayerLevel().intValue()) : null)
//                .geoDivisionId(Long.valueOf(officesGRO.getDivisionId().intValue()))
//                .geoDistrictId(Long.valueOf(officeDoptor.getDistrictId().intValue()))
                .build();


        return officeInformation;

    }

    public OISFUserType getOISFUserTypeFromRole(User user) {
        if (user.getEmployeeRecord() == null) {
            return null;
        }
        List<EmployeeOffice> userEmployeeOffices = user.getEmployeeRecord()
                .getEmployeeOffices()
                .stream()
                .filter(employeeOffice -> (employeeOffice.getStatus()))
                .collect(Collectors.toList());

        if (userEmployeeOffices.size() < 1) {
            return null;
        }

        Office office = userEmployeeOffices.get(0).getOffice();
        OfficeUnitOrganogram officeUnitOrganogram = userEmployeeOffices.get(0).getOfficeUnitOrganogram();
        OfficesGRO officesGRO = officesGroDAO.findOfficesGROByOfficeId(office.getId());

        if (officesGRO != null && officesGRO.getGroOfficeUnitOrganogramId() != null && officesGRO.getGroOfficeUnitOrganogramId().equals(officeUnitOrganogram.getId())) {
            return OISFUserType.GRO;
        } else if (userEmployeeOffices.get(0).getIsOfficeHead()) {
            return OISFUserType.HEAD_OF_OFFICE;
        } else {
            return OISFUserType.SERVICE_OFFICER;
        }
    }


    public OISFUserType getOISFUserTypeFromRole(UserInfo userInfo) {

        com.grs.core.domain.doptor.User user1 = userInfo.getUser();
        if (user1.getEmployee_record_id() == null) {
            return null;
        }

        List<OfficeInfo> officeInfos = userInfo.getOffice_info();
        HashMap<String, OfficeOrganogram> organogramInfo = userInfo.getOrganogram_info();

//        List<EmployeeOffice> userEmployeeOffices = userInfo.getEmployeeRecord().getEmployeeOffices().stream().filter(employeeOffice -> employeeOffice.getStatus().booleanValue()).collect(Collectors.toList());
        List<OfficeInfo> userEmployeeOffices = officeInfos;

        if (userEmployeeOffices.size() < 1) {
            return null;
        }

//        Office officeDoptor = userEmployeeOffices.get(0).getOffice();
        OfficeInfo officeDoptor = userEmployeeOffices.get(0);

//        OfficeUnitOrganogram officeUnitOrganogram = userEmployeeOffices.get(0).getOfficeUnitOrganogram();
        OfficeOrganogram officeUnitOrganogram = new OfficeOrganogram();
        OfficesGRO officesGRO = this.officesGroDAO.findOfficesGROByOfficeId(Long.valueOf(officeDoptor.getOffice_id()));

        for (Map.Entry<String, OfficeOrganogram> oi : organogramInfo.entrySet()) {
//            OfficeUnitOrganogram officeUnitOrganogram = organogramInfo.get(0).getOfficeUnitOrganogram();
            if (officeDoptor.getOffice_unit_organogram_id().equals(Integer.parseInt(oi.getKey()))) {
//            if (officesGRO.getGroOfficeUnitOrganogramId().equals(Integer.parseInt(oi.getKey()))) {
                officeUnitOrganogram = oi.getValue();
            }
        }


        if (officesGRO != null
                && officesGRO.getGroOfficeUnitOrganogramId() != null
                && officesGRO.getGroOfficeUnitOrganogramId().equals(new Long(officeUnitOrganogram.getId())))
            return OISFUserType.GRO;

        if (userEmployeeOffices.get(0).getOffice_head() == 1) {
            return OISFUserType.HEAD_OF_OFFICE;
        }

        return OISFUserType.SERVICE_OFFICER;

    }


    public UserInformation getUserInformationByApi(String username) {
        SingleRoleDetailsDTO[] singleRoleDetailsDTOs;
        /*if(port.equals("8090")){*/
        singleRoleDetailsDTOs = (SingleRoleDetailsDTO[]) esbConnectorService.getObjectFromESB("/employee/details/" + username, SingleRoleDetailsDTO[].class);
        /*}else {
            singleRoleDetailsDTOs = (SingleRoleDetailsDTO[]) esbConnectorService.getObjectFromESBWithoutToken("/employee/details/" + username, SingleRoleDetailsDTO[].class);
        }*/
        if (singleRoleDetailsDTOs.length == 0) {
            return null;
        }
        SingleRoleDetailsDTO singleRoleDetailsDTO = null;
        for (SingleRoleDetailsDTO singleRole : singleRoleDetailsDTOs) {
            if (singleRole.getIs_default_role() == 1) {
                singleRoleDetailsDTO = singleRole;
                break;
            }
        }
        if (singleRoleDetailsDTO == null) {
            singleRoleDetailsDTO = singleRoleDetailsDTOs[0];
        }

        OfficeInformation officeInformation = OfficeInformation.builder()
                .officeId(singleRoleDetailsDTO.getOffice_id())
                .officeOriginId(singleRoleDetailsDTO.getOffice_origin_id())
                .officeMinistryId(singleRoleDetailsDTO.getOffice_ministry_id())
                .officeNameBangla(singleRoleDetailsDTO.getOffice_name_bng())
                .officeNameEnglish(singleRoleDetailsDTO.getOffice_name_eng())
                .designation(singleRoleDetailsDTO.getDesignation_bng())
                .employeeRecordId(singleRoleDetailsDTO.getEmployee_record_id())
                .name(singleRoleDetailsDTO.getName_bng())
                .officeUnitOrganogramId(singleRoleDetailsDTO.getOfficeUnitOrganogramId())
                .layerLevel(singleRoleDetailsDTO.getLayer_level())
                .geoDivisionId(singleRoleDetailsDTO.getGeo_division_id())
                .geoDistrictId(singleRoleDetailsDTO.getGeo_district_id())
                .build();

        OISFUserType oisfUserType;
        OfficesGRO officesGRO = officesGroDAO.findOfficesGROByOfficeId(officeInformation.getOfficeId());
        if (officesGRO != null && officesGRO.getGroOfficeUnitOrganogramId() != null
                && (Objects.equals(officesGRO.getGroOfficeUnitOrganogramId(), officeInformation.getOfficeUnitOrganogramId()) || Objects.equals(officesGRO.getAppealOfficerOfficeUnitOrganogramId(), officeInformation.getOfficeUnitOrganogramId()))) {
            oisfUserType = OISFUserType.GRO;
        } else if (singleRoleDetailsDTO.getIs_admin() == 1) {
            oisfUserType = OISFUserType.OFFICE_ADMIN;
        } else if (singleRoleDetailsDTO.getOffice_head() == 1) {
            oisfUserType = OISFUserType.HEAD_OF_OFFICE;
        } else {
            oisfUserType = OISFUserType.SERVICE_OFFICER;
        }

        List<OfficesGRO> userAsAppealOfficerList = new ArrayList();
        List<OfficesGRO> userAsOfficeAdminList = new ArrayList();
        Long officeUnitOrganogramId = officeInformation.getOfficeUnitOrganogramId();
        if (officeInformation != null && officeUnitOrganogramId != null) {
            userAsAppealOfficerList = this.officesGroDAO.findByAppealOfficeUnitOrganogramId(officeUnitOrganogramId);
            userAsOfficeAdminList = officesGroDAO.findByAdminOfficeUnitOrganogramId(officeUnitOrganogramId);
        }

        return UserInformation
                .builder()
                .userId(singleRoleDetailsDTO.getUid())
                .username(username)
                .userType(UserType.OISF_USER)
                .oisfUserType(oisfUserType)
                .grsUserType(null)
                .officeInformation(officeInformation)
                .isAppealOfficer(userAsAppealOfficerList.size() > 0)
                .isOfficeAdmin(userAsOfficeAdminList.size() > 0)
                .isMobileLogin(false)
                .build();

    }

}
