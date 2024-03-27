package com.grs.api.model.response.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GrievanceAndAppealMonthlyReportDTO {
    private Long officeId;
    private Integer year;
    private Long sl;
    private Integer month;
    private String officeName;
    private Integer officeLevel;
    private MonthlyReportDTO monthlyGrievanceReport;
    private MonthlyReportDTO monthlyAppealReport;
}
