package com.grs.api.model.response.officeSelection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OfficeSearchDTO {
    private Long id;
    private Long originId;
    private Long layerId;
    private Integer layerLevel;
    private Integer customLayerId;
    private String nameBangla;
    private String nameEnglish;
}
