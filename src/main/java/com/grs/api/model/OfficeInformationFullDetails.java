package com.grs.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 26-Feb-18.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficeInformationFullDetails {
    private Long officeId;
    private Long officeUnitId;
    private Long officeUnitOrganogramId;
    private Long employeeRecordId;
    private String employeeNameBangla;
    private String employeeNameEnglish;
    private String employeeDesignation;
    private String officeNameBangla;
    private String officeUnitNameBangla;
    private String username;
}