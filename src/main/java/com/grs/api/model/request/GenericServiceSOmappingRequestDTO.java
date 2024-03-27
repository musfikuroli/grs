package com.grs.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.grs.api.model.response.organogram.OfficeOriginUnitOrganogramDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 18-Apr-18.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericServiceSOmappingRequestDTO {
    private ServiceRequestDTO service;
    private OfficeOriginUnitOrganogramDTO serviceOfficer;
}
