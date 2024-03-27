package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 23-Apr-18.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CitizensCharterOriginDTO {
    private Long id;
    private Long officeOriginId;
    private Long layerLevel;
    private String visionBangla;
    private String visionEnglish;
    private String missionBangla;
    private String missionEnglish;
    private String expectationBangla;
    private String expectationEnglish;
}
