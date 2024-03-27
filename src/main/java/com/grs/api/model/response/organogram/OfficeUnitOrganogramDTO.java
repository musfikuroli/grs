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
public class OfficeUnitOrganogramDTO {
    Long id;
    Long officeId;
    String officeName;
    Long officeUnitId;
    String officeUnitName;

    String designationEng;
    String designationBng;

    Long ministryId;
    Long layerId;
    String ministryName;
    String layerName;
}
