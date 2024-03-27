package com.grs.core.dao;

import com.grs.api.model.response.EmployeeRecordDTO;
import com.grs.api.model.response.OfficeUnitWithDesignationDTO;
import com.grs.core.domain.projapoti.EmployeeRecord;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.domain.projapoti.OfficeUnit;
import com.grs.core.domain.projapoti.OfficeUnitOrganogram;
import com.grs.core.repo.projapoti.EmployeeRecordRepo;
import com.grs.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Acer on 9/13/2017.
 */
@Repository
public class EmployeeRecordDAO {
    @Autowired
    private EmployeeRecordRepo employeeRecordRepo;

    public List<EmployeeRecord> findByOffice(Office office) {
        return this.employeeRecordRepo.findByOffices(office);
    }

    public EmployeeRecord findEmployeeRecordById(Long id) {
        return this.employeeRecordRepo.findOne(id);
    }

    public EmployeeRecordDTO getEmployeeRecordDTObyEmployeeRecordId(Long id) {
        EmployeeRecord employeeRecord = findEmployeeRecordById(id);
        return convertToEmployeeRecordDTO(employeeRecord);
    }

    public Page<EmployeeRecordDTO> findAllByIdInAsPageable(Pageable pageable, List<Long> employeeRecordIdList) {
        return employeeRecordRepo.findByIdInOrderByIdAsc(employeeRecordIdList, pageable).map(employeeRecord -> this.convertToEmployeeRecordDTO(employeeRecord));
    }

    public List<EmployeeRecordDTO> findAllByOffice(Office office) {
        List<EmployeeRecordDTO> employeeRecordDTOList = new ArrayList<>();
        employeeRecordRepo.findByOffices(office).forEach(employeeRecord -> {
            employeeRecordDTOList.add(this.convertToEmployeeRecordDTO(employeeRecord));
        });
        return employeeRecordDTOList;
    }

    public EmployeeRecordDTO convertToEmployeeRecordDTO(EmployeeRecord employeeRecord) {
        List<OfficeUnitWithDesignationDTO> officeUnitWithDesignations = new ArrayList<>();
        List<String> designations = new ArrayList<>();
        employeeRecord.getEmployeeOffices().stream().forEach((employeeOffice) -> {
            if(employeeOffice.getStatus()) {
                OfficeUnitWithDesignationDTO unitByDesignation = new OfficeUnitWithDesignationDTO();
                OfficeUnitOrganogram officeUnitOrganogram = employeeOffice.getOfficeUnitOrganogram();
                String designation = employeeOffice.getDesignation();
                if(StringUtil.isValidString(employeeOffice.getInchargeLabel())) {
                    designation += " (" + employeeOffice.getInchargeLabel() + ")";
                } else if (officeUnitOrganogram != null && officeUnitOrganogram.getOfficeUnit() != null && officeUnitOrganogram.getOfficeUnit().getUnitNameBangla() != null) {
                    designation += " (" + officeUnitOrganogram.getOfficeUnit().getUnitNameBangla() + ")";
                } else {
                    designation += "";
                }
                designations.add(designation);
                unitByDesignation.setDesignation(designation);
                unitByDesignation.setOfficeUnitOrganogramId(officeUnitOrganogram != null ? officeUnitOrganogram.getId() : null);
                OfficeUnit officeUnit = employeeOffice.getOfficeUnit();
                if(officeUnit != null) {
                    unitByDesignation.setOfficeUnitNameBangla(officeUnit.getUnitNameBangla());
                    unitByDesignation.setOfficeUnitNameEnglish(officeUnit.getUnitNameEnglish());
                }
                officeUnitWithDesignations.add(unitByDesignation);
            }
        });

        return EmployeeRecordDTO.builder()
                .id(employeeRecord.getId().toString())
                .name(employeeRecord.getNameBangla())
                .designation(String.join(", ", designations))
                .email(employeeRecord.getPersonalEmail())
                .phoneNumber(employeeRecord.getPersonalMobile())
                .officeUnitWithDesignations(officeUnitWithDesignations)
                .build();
    }
}
