package com.grs.core.dao;

import com.grs.api.model.response.EmployeeRecordDTO;
import com.grs.api.model.response.OfficeUnitWithDesignationDTO;
import com.grs.api.model.response.roles.RoleContainerDTO;
import com.grs.api.model.response.roles.SingleRoleDTO;
import com.grs.core.domain.projapoti.*;
import com.grs.core.repo.projapoti.EmployeeOfficeRepo;
import com.grs.utils.Constant;
import com.grs.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Acer on 25-Dec-17.
 */
@Service
public class EmployeeOfficeDAO {
    @Autowired
    private EmployeeOfficeRepo employeeOfficeRepo;

    public EmployeeOffice findEmployeeOfficeByOfficeAndIsOfficeHead(Long officeId) {
        List<EmployeeOffice> employeeOffices = this.employeeOfficeRepo.findEmployeeOfficeByOfficeIdAndIsOfficeHeadAndStatus(officeId, true, true);
        return employeeOffices.size() == 0 ? null : employeeOffices.get(0);
    }

    public RoleContainerDTO findByEmployeeRecordId(Long officeId, Long employeeRecordId, Long currentOfficeUnitOrganogramId) {
        List<EmployeeOffice> employeeOffices = this.employeeOfficeRepo.findByEmployeeRecordIdAndStatus(employeeRecordId, true);
        List<SingleRoleDTO> rolesDTO = employeeOffices.stream()
                .map(employeeOffice -> {
                    return SingleRoleDTO.builder()
                            .designation(employeeOffice.getDesignation())
                            .officeMinistryId(employeeOffice.getOffice().getOfficeMinistry().getId())
                            .officeUnitOrganogramId(employeeOffice.getOfficeUnitOrganogram().getId())
                            .officeUnitId(employeeOffice.getOfficeUnit().getId())
                            .officeId(employeeOffice.getOffice().getId())
                            .officeOriginId(employeeOffice.getOffice().getOfficeOriginId())
                            .officeUnitNameBangla(employeeOffice.getOfficeUnit().getUnitNameBangla())
                            .officeUnitNameEnglish(employeeOffice.getOfficeUnit().getUnitNameEnglish())
                            .officeNameEnglish(employeeOffice.getOffice().getNameEnglish())
                            .officeNameBangla(employeeOffice.getOffice().getNameBangla())
                            .layerLevel(Long.valueOf(employeeOffice.getOffice().getOfficeLayer().getLayerLevel()))
                            .geoDistrictId(Long.valueOf(employeeOffice.getOffice().getDistrictId()))
                            .geoDivisionId(Long.valueOf(employeeOffice.getOffice().getDivisionId()))
                            .selected(employeeOffice.getOfficeUnitOrganogram().getId().equals(currentOfficeUnitOrganogramId) && employeeOffice.getOffice().getId().equals(officeId))
                            .build();
                }).collect(Collectors.toList());

        return RoleContainerDTO.builder()
                .roles(rolesDTO)
                .build();
    }

    public SingleRoleDTO findSingleRole(Long officeId, Long officeUnitOrganogramId) {
        EmployeeOffice employeeOffice = this.findByOfficeIdAndOfficeUnitOrganogramId(officeId, officeUnitOrganogramId);
        if(employeeOffice == null){
            return null;
        }
        Office office = employeeOffice.getOffice();
        OfficeUnit officeUnit = employeeOffice.getOfficeUnit();
        String inchargeLabel = StringUtil.isValidString(employeeOffice.getInchargeLabel()) ? " (" + employeeOffice.getInchargeLabel() + ")" : "";
        return SingleRoleDTO.builder()
                .designation(employeeOffice.getDesignation() + inchargeLabel)
                .officeMinistryId(office.getOfficeMinistry().getId())
                .officeUnitOrganogramId(employeeOffice.getOfficeUnitOrganogram().getId())
                .officeUnitId(officeUnit == null ? null : officeUnit.getId())
                .officeId(office.getId())
                .officeUnitNameBangla(officeUnit == null ? "" : officeUnit.getUnitNameBangla())
                .officeUnitNameEnglish(officeUnit == null ? "" : officeUnit.getUnitNameEnglish())
                .officeNameEnglish(office.getNameEnglish())
                .officeNameBangla(office.getNameBangla())
                .layerLevel(Long.valueOf(office.getOfficeLayer().getLayerLevel()))
                .geoDistrictId(Long.valueOf(office.getDistrictId()))
                .geoDivisionId(Long.valueOf(office.getDivisionId()))
                .phone(officeUnit == null ? "" : officeUnit.getPhoneNumber())
                .email(officeUnit == null ? "" : officeUnit.getEmail())
                .selected(true)
                .build();
    }

    public EmployeeOffice findByOfficeIdAndOfficeUnitOrganogramId(Long officeId, Long officeUnitOrganogramId) {
        List<EmployeeOffice> employeeOffices = employeeOfficeRepo.findByOfficeIdAndOfficeUnitOrganogramIdAndStatus(officeId, officeUnitOrganogramId, true);
        return employeeOffices.size() == 0 ? null : employeeOffices.get(0);
    }

    public EmployeeRecordDTO getEmployeeRecordDTO(EmployeeOffice employeeOffice) {
        List<OfficeUnitWithDesignationDTO> officeUnitWithDesignations = new ArrayList<>();
        List<String> designations = new ArrayList<>();
        if (employeeOffice == null) {
            return EmployeeRecordDTO.builder().name(Constant.NO_INFO_FOUND).build();
        }
        EmployeeRecord employeeRecord = employeeOffice.getEmployeeRecord();

        OfficeUnitWithDesignationDTO unitByDesignation = new OfficeUnitWithDesignationDTO();
        OfficeUnitOrganogram officeUnitOrganogram = employeeOffice.getOfficeUnitOrganogram();
        String designation = employeeOffice.getDesignation();
        if (StringUtil.isValidString(employeeOffice.getInchargeLabel())) {
            designation += " (" + employeeOffice.getInchargeLabel() + ")";
        }
        designations.add(designation);
        unitByDesignation.setDesignation(designation);
        unitByDesignation.setOfficeUnitOrganogramId(officeUnitOrganogram != null ? officeUnitOrganogram.getId() : null);
        OfficeUnit officeUnit = employeeOffice.getOfficeUnit();
        if (officeUnit != null) {
            unitByDesignation.setOfficeUnitNameBangla(officeUnit.getUnitNameBangla());
            unitByDesignation.setOfficeUnitNameEnglish(officeUnit.getUnitNameEnglish());
        }
        officeUnitWithDesignations.add(unitByDesignation);

        return EmployeeRecordDTO.builder()
                .id(employeeRecord.getId().toString())
                .name(employeeRecord.getNameBangla())
                .designation(String.join(", ", designations))
                .email(employeeRecord.getPersonalEmail())
                .phoneNumber(employeeRecord.getPersonalMobile())
                .officeUnitWithDesignations(officeUnitWithDesignations)
                .build();
    }

    public List<EmployeeOffice> findByOfficeAndStatus(Office office, Boolean status) {
        return employeeOfficeRepo.findByOfficeAndStatus(office, status);
    }
}