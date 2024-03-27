package com.grs.api.model.response.grievance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SafetyNetGrievanceSummaryListDto {
    public List<com.grs.api.model.response.grievance.SafetyNetGrievanceSummaryDto> safetyNetGrievanceSummaryList;
    public Integer recordCount;
}
