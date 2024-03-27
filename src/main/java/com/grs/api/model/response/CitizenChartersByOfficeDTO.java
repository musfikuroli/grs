package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitizenChartersByOfficeDTO {
    public String officeNameBangla;
    public String officeNameEnglish;
    public String websiteUrl;
    public CitizensCharterOriginDTO visionMission;
    public List<ServiceOriginDTO> citizenServices;
    public List<ServiceOriginDTO> officialServices;
    public List<ServiceOriginDTO> internalServices;
    public EmployeeRecordDTO officeGRO;
    public EmployeeRecordDTO officeAO;
}
