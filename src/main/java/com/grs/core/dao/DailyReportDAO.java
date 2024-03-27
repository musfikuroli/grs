package com.grs.core.dao;

import com.grs.api.model.response.reports.DailyReportDTO;
import com.grs.api.model.response.reports.GrievanceAndAppealDailyReportDTO;
import com.grs.core.domain.grs.DailyReport;
import com.grs.core.repo.grs.DailyReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;

@Slf4j
@Service
public class DailyReportDAO {

    private final DailyReportRepository reportRepository;

    public DailyReportDAO(DailyReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public DailyReport findByOfficeIdAndDate(Long officeId, String date) {
        return this.reportRepository.findByOfficeIdAndDate(officeId, date);
    }

    public DailyReport save(DailyReport dailyReport) {
        return this.reportRepository.save(dailyReport);
    }


    public DailyReport convertToDailyReport(GrievanceAndAppealDailyReportDTO dto) throws ParseException {
        DailyReportDTO grievanceReportDTO = dto.getDailyGrievanceReport();
        DailyReportDTO appealReportDTO = dto.getDailyAppealReport();
        return DailyReport.builder()
                .officeId(dto.getOfficeId())
                .reportDate(new SimpleDateFormat("yyyy-MM-dd").parse(dto.getReportDate()))
                .onlineSubmissionCount(grievanceReportDTO.getOnlineSubmissionCount())
                .conventionalMethodSubmissionCount(grievanceReportDTO.getConventionalMethodSubmissionCount())
                .selfMotivatedAccusationCount(grievanceReportDTO.getSelfMotivatedAccusationCount())
                .inheritedFromLastMonthCount(grievanceReportDTO.getInheritedFromLastMonthCount())
                .totalCount(grievanceReportDTO.getTotalCount())
                .sentToOtherCount(grievanceReportDTO.getSentToOtherCount())
                .resolvedCount(grievanceReportDTO.getResolvedCount())
                .timeExpiredCount(grievanceReportDTO.getTimeExpiredCount())
                .runningCount(grievanceReportDTO.getRunningCount())
                .resolveRate(grievanceReportDTO.getRate())
                .appealOnlineSubmissionCount(appealReportDTO.getOnlineSubmissionCount())
                .appealInheritedFromLastMonthCount(appealReportDTO.getInheritedFromLastMonthCount())
                .appealTotalCount(appealReportDTO.getTotalCount())
                .appealResolvedCount(appealReportDTO.getResolvedCount())
                .appealTimeExpiredCount(appealReportDTO.getTimeExpiredCount())
                .appealRunningCount(appealReportDTO.getRunningCount())
                .appealResolveRate(appealReportDTO.getRate())
                .build();
    }
}
