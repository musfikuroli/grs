package com.grs.api.model.response.roles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 25-Dec-17.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SingleRoleDTO {
    private Long officeUnitOrganogramId;
    private Long officeUnitId;
    private Long officeId;
    private Long officeOriginId;
    private Long officeMinistryId;
    private String designation;
    private String officeNameBangla;
    private String officeNameEnglish;
    private String officeUnitNameBangla;
    private String officeUnitNameEnglish;
    private Long layerLevel;
    private Long geoDivisionId;
    private Long geoDistrictId;
    private String phone;
    private String email;
    private boolean selected;
}
