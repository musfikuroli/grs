package com.grs.api.model.response.organogram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 10/4/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OfficeUnitDTO {
    Long id;
    Long officeMinistryId;
    String officeMinistryName;
    Long officeLayerId;
    String officeLayerName;
    Long officeId;
    String officeName;

    String unitNameBng;
    String unitNameEng;

    Long parentUnitId;
    String parentUnitName;
}
