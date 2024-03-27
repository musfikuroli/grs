package com.grs.api.model.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppealResolutionStatisticsDTO {
    List<MonthlyGrievanceResolutionDTO> currentMonthResolutions;
    List<ExpiredGrievanceInfoDTO> timeExpiredGrievances;
}
