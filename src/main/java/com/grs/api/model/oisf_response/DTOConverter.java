package com.grs.api.model.oisf_response;

import com.grs.api.model.OfficeLayerDTO;
import com.grs.api.model.response.OfficesGroDTO;
import com.grs.api.model.response.officeSelection.OfficeSearchDTO;
import com.grs.core.domain.grs.OfficesGRO;
import com.grs.core.domain.projapoti.*;
import com.grs.utils.DateTimeConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class DTOConverter {


    public static OfficesGroDTO convertOfficesGROToDTO(OfficesGRO officesGRO) {
        return OfficesGroDTO.builder()
                .officeId(officesGRO.getOfficeId())
                .officeName(officesGRO.getOfficeNameBangla())
                .build();

    }

    public static OfficesGroDTO convertOfficeSearchDTOtoOfficesGroDTO(OfficeSearchDTO officeSearchDTO) {
        return OfficesGroDTO.builder()
                .officeId(officeSearchDTO.getId())
                .officeName(officeSearchDTO.getNameBangla())
                .build();

    }

    public static class ShortER {
        Long id;
        String employeeNameEng;
        String employeeNameBng;
        String username;
        String employeeEmail;
        String employeeMobile;

        ShortER(Long id, String employeeNameBng, String employeeNameEng, String username, String employeeEmail, String employeeMobile) {
            this.id = id;
            this.employeeNameBng = employeeNameBng;
            this.employeeNameEng = employeeNameEng;
            this.username = username;
            this.employeeEmail = employeeEmail;
            this.employeeMobile = employeeMobile;
        }
    }

    public static class TupleER {
        ShortER shortER;
        List<EmployeeOffice> employeeOffice;

        TupleER(ShortER shortER, EmployeeOffice employeeOffices) {
            this.shortER = new ShortER(shortER.id, shortER.employeeNameBng, shortER.employeeNameEng, shortER.username, shortER.employeeEmail, shortER.employeeMobile);
            this.employeeOffice = new ArrayList<>();
            this.employeeOffice.add(employeeOffices);
        }

        void setEmployeeOffice(List<EmployeeOffice> employeeOffices) {
            this.employeeOffice = employeeOffices;
        }
    }
}
