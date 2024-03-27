package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpProgramGroDto {
    public Integer id;
    public String spProgramName;
    public String officeName;
    public String officeUnitOrganogramName;
    public String officeGroName;
    public String officeGroDesignation;
    public String officeGroEmail;
    public String officeGroPhoneNumber;
}
