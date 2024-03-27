package com.grs.api.model.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResolutionTypeInfoDTO {
    Long acceptedGrievanceCount;
    Long trueGrievanceCount;
    Long fakeGrievanceCount;
    Long departmentalRecommendationCount;
    Long totalResolvedCount;
    Double rateOfResolution;
}
