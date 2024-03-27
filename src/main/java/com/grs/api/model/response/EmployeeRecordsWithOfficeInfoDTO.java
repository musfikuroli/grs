package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeRecordsWithOfficeInfoDTO {
    Long officeId;
    String officeNameBangla;
    String officeNameEnglish;
    Page<EmployeeRecordDTO> employeeRecords;
}
