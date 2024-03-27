package com.grs.api.model.response.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GrievanceAndAppealDailyReportDTO {
    private Long officeId;
    private String reportDate;
    private String officeName;
    private Integer officeLevel;
    private DailyReportDTO dailyGrievanceReport;
    private DailyReportDTO dailyAppealReport;
}
