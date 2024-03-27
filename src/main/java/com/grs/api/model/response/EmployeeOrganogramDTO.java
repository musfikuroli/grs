package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by User on 1/6/2018.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeOrganogramDTO {
    private Long officeId;
    private Long ministryId;
    private Long officeUnitOrganogramId;
    private String employeeName;
    private String employeeDesignation;
    private String officeUnitName;
}
