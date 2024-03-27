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
public class GrievanceMonthlyReportsDTO {
    private Long countByWebsite;
    private Long countByCallcenter;
    private Long countByOrthodox;
    private Long countBySelfMotivatedWay;
    private Long pendingGrievanceOfPrevoiusMonth;
    private Long countByResolvedStatus;
    private Long countByRunningStatus;
    private Long countByPendingStatus;
    private Long resolutionRate;
    private String officeName;
}
