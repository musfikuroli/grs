package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OfficesGroDTO {
    private Long id;
    private Long officeId;
    private Long officeOriginId;
    private String officeName;
    private String groName;
    private String aoName;
    private String adminName;
    private String groDesignation;
    private String aoDesignation;
    private String adminDesignation;
    private String groOfficeUnitName;
    private String aoOfficeUnitName;
    private String adminOfficeUnitName;
    private String groOfficeName;
    private String aoOfficeName;
    private String adminOfficeName;
    private String groPhone;
    private String aoPhone;
    private String adminPhone;
    private Boolean isAppealOfficer;
    private Long groOfficeId;
    private Long groOfficeUnitOrganogramId;
    private Long appealOfficeId;
    private Long appealOfficerOfficeUnitOrganogramId;
    private Long adminOfficeId;
    private Long adminOfficeUnitOrganogramId;
    private Boolean status;
}
