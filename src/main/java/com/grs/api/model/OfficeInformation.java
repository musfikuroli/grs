package com.grs.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 10/4/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficeInformation {
    private Long officeId;
    private String officeNameBangla;
    private String officeNameEnglish;
    private Long officeMinistryId;
    private Long officeOriginId;
    private String name;
    private String designation;
    private Long employeeRecordId;
    private Long officeUnitOrganogramId;
    private Long layerLevel;
    private Long geoDivisionId;
    private Long geoDistrictId;
}
