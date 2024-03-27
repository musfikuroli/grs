package com.grs.api.model.response.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 20-Feb-18.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GrievanceResolutionDetailsDTO extends GrievanceBasicDetailsDTO {
    private String resolutionDate;
    private String groDecision;
    private String groPreventiveCounsel;
}
