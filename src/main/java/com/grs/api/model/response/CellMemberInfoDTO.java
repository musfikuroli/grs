package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 24-May-18.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CellMemberInfoDTO {
    private Long id;
    private Long officeUnitOrganogramId;
    private Long officeUnitId;
    private Long officeId;
    private String designation;
    private String nameBangla;
    private String nameEnglish;
    private String officeNameBangla;
    private String officeNameEnglish;
    private String officeUnitNameBangla;
    private String officeUnitNameEnglish;
    private Boolean isAppealOfficer;
    private Boolean isGro;
}
