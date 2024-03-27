package com.grs.core.dao;

import com.grs.api.model.response.reports.GrievanceAndAppealMonthlyReportDTO;
import com.grs.api.model.response.reports.MonthlyReportDTO;
import com.grs.core.domain.grs.MonthlyReport;
import com.grs.core.repo.grs.MonthlyReportRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MonthlyReportDAO {
    @Autowired
    private MonthlyReportRepo monthlyReportRepo;

    public MonthlyReport findOne(Long id) {
        return monthlyReportRepo.findOne(id);
    }

    public MonthlyReport findByOfficeIdAndYearAndMonth(Long officeId, Integer year, Integer month) {
        return monthlyReportRepo.findByOfficeIdAndYearAndMonth(officeId, year, month);
    }

    public List<MonthlyReport> findByOfficeIdAndMonthAndYear(Long officeId, Integer month, Integer year) {
        return monthlyReportRepo.findByOfficeIdAndMonthAndYear(officeId, month, year);
    }

    public List<MonthlyReport> findSummaryByfromYearMonthToYearMonth(int fromYear, int fromMonth, int toYear, int toMonth) {
        List<Object[]> objects = monthlyReportRepo.findSummaryByfromYearMonthToYearMonth(  fromYear,   fromMonth,   toYear,   toMonth);

        return objects.stream()
                .map(e ->
                        MonthlyReport.builder()
                                .year(Integer.valueOf(e[0] + ""))
                                .month(Integer.valueOf(e[1] + ""))
                                .onlineSubmissionCount(Long.valueOf(e[2] + ""))
                                .conventionalMethodSubmissionCount(Long.valueOf(e[3] + ""))
                                .selfMotivatedAccusationCount(Long.valueOf(e[4] + ""))
                                .inheritedFromLastMonthCount(Long.valueOf(e[5] + ""))
                                .totalCount(Long.valueOf(e[6] + ""))
                                .sentToOtherCount(Long.valueOf(e[7] + ""))
                                .resolvedCount(Long.valueOf(e[8] + ""))
                                .timeExpiredCount(Long.valueOf(e[9] + ""))

                                .appealOnlineSubmissionCount(Long.valueOf(e[10] + ""))
                                .appealInheritedFromLastMonthCount(Long.valueOf(e[11] + ""))
                                .appealTotalCount(Long.valueOf(e[12] + ""))
                                .appealResolvedCount(Long.valueOf(e[13] + ""))
                                .appealRunningCount(Long.valueOf(e[14] + ""))
                                .appealTimeExpiredCount(Long.valueOf(e[15] + ""))

                                .build()

                )
                .collect(Collectors.toList());
    }

    public List<MonthlyReport> findByOfficeIdInAndMonthAndYear(List<Long> officeId, Integer month, Integer year) {
        if (officeId.size() == 0 || month == null || year == null) return new ArrayList<MonthlyReport>();
        return monthlyReportRepo.findByOfficeIdInAndMonthAndYear(officeId, month, year);
    }

    public List<MonthlyReport> findByOfficeIdsAndYearAndMonthGroupByYearAndMonthOrderByYearAndMonth(List<Long> officeId, Integer year, Integer month) {
        if (officeId.size() == 0 || year == null) return new ArrayList<MonthlyReport>();

        List<Object[]> objects = monthlyReportRepo.findByOfficeIdsAndYearAndMonthGroupByYearAndMonthOrderByYearAndMonth(officeId, year, month);
        return objects.stream()
                .map(e ->
                        MonthlyReport.builder()
                                .year(Integer.valueOf(e[0] + ""))
                                .month(Integer.valueOf(e[1] + ""))
                                .totalCount(Long.valueOf(e[2] + ""))
                                .resolvedCount(Long.valueOf(e[3] + ""))

                                .appealTotalCount(Long.valueOf(e[4] + ""))
                                .appealResolvedCount(Long.valueOf(e[5] + ""))

                                .build()

                )
                .collect(Collectors.toList());
    }

    public List<MonthlyReport> findByOfficeIdsAndYearGroupByYearAndMonthOrderByYearAndMonth(List<Long> officeId, Integer year) {
        if (officeId.size() == 0 || year == null) return new ArrayList<MonthlyReport>();
        List<Object[]> objects = monthlyReportRepo.findByOfficeIdsAndYearGroupByYearAndMonthOrderByYearAndMonth(officeId, year);
        return objects.stream()
                .map(e ->
                        MonthlyReport.builder()
                                .year(Integer.valueOf(e[0] + ""))
                                .month(Integer.valueOf(e[1] + ""))
                                .totalCount(Long.valueOf(e[2] + ""))
                                .resolvedCount(Long.valueOf(e[3] + ""))

                                .appealTotalCount(Long.valueOf(e[4] + ""))
                                .appealResolvedCount(Long.valueOf(e[5] + ""))

                                .build()

                )
                .collect(Collectors.toList());
    }

    public List<MonthlyReport> findByOfficeIdAndYear(Long officeId, Integer year) {
        return monthlyReportRepo.findByOfficeIdAndYear(officeId, year);
    }

    public List<MonthlyReport> findByOfficeIdInAndYear(List<Long> officeId, Integer year) {
        if (officeId.size() == 0 || year == null) return new ArrayList<MonthlyReport>();
        return monthlyReportRepo.findByOfficeIdInAndYear(officeId, year);
    }

    public List<MonthlyReport> findByOfficeIdGroupByYear(Long officeId, Integer year) {
        List<Object[]> byOfficeIdGroupByYear = new ArrayList<>();
        if (year != null) {
            byOfficeIdGroupByYear = monthlyReportRepo.findByOfficeIdAndYearGroupByYear(officeId, year);
        } else {
            byOfficeIdGroupByYear = monthlyReportRepo.findByOfficeIdGroupByYear(officeId);
        }
        return byOfficeIdGroupByYear.stream()
                .map(e -> new MonthlyReport(Integer.valueOf(e[0] + ""), Long.valueOf(e[1] + ""), Long.valueOf(e[2] + "")))
                .collect(Collectors.toList());

//        return new ArrayList<>();
    }

    public Integer countGeneratedReportByMonthAndYear(Integer month, Integer year) {
        return monthlyReportRepo.countByMonthAndYear(month, year);
    }

    public MonthlyReport save(MonthlyReport monthlyReport) {
        return monthlyReportRepo.save(monthlyReport);
    }

    public List<MonthlyReport> save(List<MonthlyReport> allMonthlyReports) {
        return monthlyReportRepo.save(allMonthlyReports);
    }

    public MonthlyReport convertToMonthlyReport(GrievanceAndAppealMonthlyReportDTO dto) {
        MonthlyReportDTO grievanceReportDTO = dto.getMonthlyGrievanceReport();
        MonthlyReportDTO appealReportDTO = dto.getMonthlyAppealReport();
        return MonthlyReport.builder()
                .officeId(dto.getOfficeId())
                .year(dto.getYear())
                .month(dto.getMonth())
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
                .createdAt(new Date())
                .build();
    }

    public GrievanceAndAppealMonthlyReportDTO convertToGrievanceAndAppealMonthlyReportDTO(MonthlyReport report, Boolean hasAppealReport) {
        Double rate = 0d;
        Long totalDecided = report.getResolvedCount() + report.getSentToOtherCount();
        if (report.getTotalCount() > 0) {
            rate = ((double) totalDecided / (double) report.getTotalCount()) * 100;
            rate = (double) Math.round(rate * 100) / 100;
        }
        MonthlyReportDTO grievanceMonthlyReportDTO = MonthlyReportDTO.builder()
                .onlineSubmissionCount(report.getOnlineSubmissionCount())
                .conventionalMethodSubmissionCount(report.getConventionalMethodSubmissionCount())
                .selfMotivatedAccusationCount(report.getSelfMotivatedAccusationCount())
                .inheritedFromLastMonthCount(report.getInheritedFromLastMonthCount())
                .totalCount(report.getTotalCount())
                .sentToOtherCount(report.getSentToOtherCount())
                .resolvedCount(report.getResolvedCount())
                .timeExpiredCount(report.getTimeExpiredCount())
                .runningCount(report.getRunningCount())
                .rate(rate)
                .build();
        MonthlyReportDTO appealMonthlyReportDTO = null;
        if (hasAppealReport) {
            rate = 0d;
            if (report.getAppealTotalCount() > 0) {
                rate = (double) (((double) report.getAppealResolvedCount() / ((double) report.getAppealTotalCount())) * 100);
                rate = (double) Math.round(rate * 100) / 100;
            }
            appealMonthlyReportDTO = MonthlyReportDTO.builder()
                    .onlineSubmissionCount(report.getAppealOnlineSubmissionCount())
                    .inheritedFromLastMonthCount(report.getAppealInheritedFromLastMonthCount())
                    .totalCount(report.getAppealTotalCount())
                    .resolvedCount(report.getAppealResolvedCount())
                    .timeExpiredCount(report.getAppealTimeExpiredCount())
                    .runningCount(report.getAppealRunningCount())
                    .rate(rate)
                    .build();
        }
        return GrievanceAndAppealMonthlyReportDTO.builder()
                .officeId(report.getOfficeId())
                .officeName(report.getOfficeName())
                .year(report.getYear())
                .month(report.getMonth())
                .monthlyGrievanceReport(grievanceMonthlyReportDTO)
                .monthlyAppealReport(appealMonthlyReportDTO)
                .build();
    }
}
