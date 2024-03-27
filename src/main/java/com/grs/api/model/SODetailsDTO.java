package com.grs.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by HP on 4/15/2018.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SODetailsDTO {
    private Long id;
    private Long serviceId;
    private String name;
    private String mobileNumber;
    private String designation;
    private Long officeId;
    private Long officeUnitId;
    private Long officeUnitOrgId;
    private String officeUnitName;
    private String officeName;
    private Boolean status;
}
