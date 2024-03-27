package com.grs.api.model.response.organogram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 16-Apr-18.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OfficeOriginUnitOrganogramDTO {
    private Long officeOriginId;
    private Long officeOriginUnitId;
    private Long officeOriginUnitOrganogramId;
    private String unitNameBangla;
    private String unitNameEnglish;
    private String designationEnglish;
    private String designationBangla;
}
