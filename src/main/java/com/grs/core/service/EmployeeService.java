package com.grs.core.service;

import com.grs.api.model.response.ComplainantResponseDTO;
import com.grs.core.dao.EmployeeRecordDAO;
import com.grs.core.domain.projapoti.EmployeeRecord;
import com.grs.core.domain.Gender;
import com.grs.core.domain.projapoti.Office;
import com.grs.utils.DateTimeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Acer on 20-Dec-17.
 */
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRecordDAO employeeRecordDAO;

    public EmployeeRecord findOne(Long id){
        return this.employeeRecordDAO.findEmployeeRecordById(id);
    }

    public List<EmployeeRecord> findByOffice(Office office) {
        return this.employeeRecordDAO.findByOffice(office);
    }

    public ComplainantResponseDTO getPersonalInfoOfEmployeeIntoComplainantResponseDTO(Long employeeRecordId) {
        EmployeeRecord employeeRecord = this.employeeRecordDAO.findEmployeeRecordById(employeeRecordId);
        return this.convertToComplainantResponseDTO(employeeRecord);
    }

    public String getGenderForEmployee(EmployeeRecord employeeRecord) {
        String gender = "";
        switch (employeeRecord.getGender()) {
            case "1":
                gender = "MALE";
                break;
            case "2":
                gender = "FEMALE";
                break;
            default:
                gender = "OTHER";
                break;
        }
        return gender;
    }

    public ComplainantResponseDTO convertToComplainantResponseDTO(EmployeeRecord employeeRecord) {
        return ComplainantResponseDTO.builder()
                .birthDate(DateTimeConverter.convertDateToStringForTimeline(employeeRecord.getDateOfBirth()))
                .education("")
                .gender(Gender.valueOf(getGenderForEmployee(employeeRecord)))
                .name(employeeRecord.getNameBangla())
                .nationality("বাংলাদেশী")
                .email(employeeRecord.getPersonalEmail())
                .nidOrBcn(employeeRecord.getNationalId())
                .permanentAddressDistrictId(null)
                .permanentAddressDistrictNameBng("")
                .permanentAddressDivisionId(null)
                .permanentAddressDivisionNameBng("")
                .permanentAddressStreet("")
                .permanentAddressHouse("")
                .permanentAddressTypeId(null)
                .permanentAddressTypeNameBng("")
                .permanentAddressTypeValue(null)
                .presentAddressDistrictId(null)
                .presentAddressDistrictNameBng("")
                .presentAddressDivisionId(null)
                .presentAddressDivisionNameBng("")
                .presentAddressStreet("")
                .presentAddressHouse("")
                .presentAddressTypeId(null)
                .presentAddressTypeNameBng("")
                .presentAddressTypeValue(null)
                .permanentAddressCountryId(15L)
                .presentAddressCountryId(15L)
                .permanentAddressCountryName("বাংলাদেশ")
                .presentAddressCountryName("বাংলাদেশ")
                .foreignPermanentAddressLine1("")
                .foreignPermanentAddressLine2("")
                .foreignPermanentAddressCity("")
                .foreignPermanentAddressZipCode("")
                .foreignPermanentAddressState("")
                .foreignPresentAddressLine1("")
                .foreignPresentAddressLine2("")
                .foreignPresentAddressCity("")
                .foreignPresentAddressZipCode("")
                .foreignPresentAddressState("")
                .education("")
                .occupation("")
                .phoneNumber(employeeRecord.getPersonalMobile())
                .build();
    }
}
