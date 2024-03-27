package com.grs.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by HP on 4/8/2018.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OfficeLayerDTO {
    private Long id;
    private String layerNameBangla;
    private String layerNameEnglish;
    private Integer layerLevel;
}
