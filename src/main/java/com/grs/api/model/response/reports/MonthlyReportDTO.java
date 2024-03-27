package com.grs.api.model.response.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class MonthlyReportDTO {
    private String officeName;
    private Long officeId;
    private Long onlineSubmissionCount;
    private Long conventionalMethodSubmissionCount;
    private Long selfMotivatedAccusationCount;
    private Long inheritedFromLastMonthCount;
    private Long totalCount;
    private Long sentToOtherCount;
    private Long resolvedCount;
    private Long timeExpiredCount;
    private Long runningCount;
    private Double rate;
    private Long sl;

    public MonthlyReportDTO() {
        this.officeId = 0L;
        this.officeName = "";
        this.onlineSubmissionCount = 0L;
        this.conventionalMethodSubmissionCount = 0L;
        this.selfMotivatedAccusationCount = 0L;
        this.inheritedFromLastMonthCount = 0L;
        this.totalCount = 0L;
        this.sentToOtherCount = 0L;
        this.resolvedCount = 0L;
        this.timeExpiredCount = 0L;
        this.runningCount = 0L;
        this.rate = 0.0;
    }
}
