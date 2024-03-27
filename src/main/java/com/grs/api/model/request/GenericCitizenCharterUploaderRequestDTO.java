package com.grs.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Acer on 18-Apr-18.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericCitizenCharterUploaderRequestDTO {
    private Long officeOriginId;
    private String officeOriginNameBangla;
    private String officeOriginNameEnglish;
    private Long layerLevel;
    private String visionBangla;
    private String visionEnglish;
    private String missionBangla;
    private String missionEnglish;
    private List<String> expectationsBangla;
    private List<String> expectationsEnglish;
    private List<GenericServiceSOmappingRequestDTO> citizenCharters;
}
