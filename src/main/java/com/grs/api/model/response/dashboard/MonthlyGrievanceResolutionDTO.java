package com.grs.api.model.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonthlyGrievanceResolutionDTO {
    Long id;
    String subject;
    String serviceName;
    Date closedDate;
    String groIdentifiedCause;
    String groDecision;
    String groSuggestion;
    String aoIdentifiedCause;
    String aoDecision;
    String aoSuggestion;
}
