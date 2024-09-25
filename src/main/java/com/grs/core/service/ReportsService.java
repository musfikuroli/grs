package com.grs.core.service;

import com.grs.api.model.OfficeInformation;
import com.grs.api.model.SafetyNetSummaryResponse;
import com.grs.api.model.UserInformation;
import com.grs.api.model.response.grievance.GrievanceComplainantInfoDTO;
import com.grs.api.model.response.reports.*;
import com.grs.core.dao.MonthlyReportDAO;
import com.grs.core.dao.ReportsDAO;
import com.grs.core.domain.MediumOfSubmission;
import com.grs.core.domain.grs.MonthlyReport;
import com.grs.core.domain.grs.OfficesGRO;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.domain.projapoti.OfficeLayer;
import com.grs.core.domain.projapoti.OfficeMinistry;
import com.grs.core.repo.grs.BaseEntityManager;
import com.grs.utils.CacheUtil;
import com.grs.utils.Constant;
import com.grs.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Created by Acer on 22-Feb-18.
 */
@Slf4j
@Service
public class ReportsService {
    int updated = 0;
    @Autowired
    private ReportsDAO reportsDAO;
    @Autowired
    private OfficeService officeService;
    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private MonthlyReportDAO monthlyReportDAO;

    @Autowired
    private OfficesGroService officesGroService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ShortMessageService shortMessageService;
    @Autowired
    private GeneralSettingsService generalSettingsService;
    @Autowired
    private GrievanceService grievanceService;
    @Autowired
    private BaseEntityManager baseEntityManager;

    public GrievanceMonthlyReportsDTO getGrievanceMonthlyReportsSummarry(String month, Office office) {
        Date date = new Date(Long.valueOf(month));
        Long officeId = office.getId();
        Long runningCount = this.reportsDAO.countRunningGrievancesByOfficeIdAndDateInBetween(officeId, date),
                pendingCount = this.reportsDAO.countUnresolvedGrievancesByOfficeIdAndDateInBetween(officeId, date),
                resolvedCount = this.reportsDAO.countResolvedGrievancesByOfficeIdAndDateInBetween(officeId, date);


        return GrievanceMonthlyReportsDTO.builder()
                .countByWebsite(this.reportsDAO.countByOfficeAndMediumOfSubmissionAndDateInBetween(officeId, MediumOfSubmission.ONLINE.name(), date))
                .countByCallcenter(this.reportsDAO.countByOfficeAndMediumOfSubmissionAndDateInBetween(officeId, MediumOfSubmission.CALL_CENTER.name(), date))
                .countByOrthodox(this.reportsDAO.countByOfficeAndMediumOfSubmissionAndDateInBetween(officeId, MediumOfSubmission.CONVENTIONAL_METHOD.name(), date))
                .countBySelfMotivatedWay(this.reportsDAO.countByOfficeAndMediumOfSubmissionAndDateInBetween(officeId, MediumOfSubmission.SELF_MOTIVATED_ACCEPTANCE.name(), date))
                .pendingGrievanceOfPrevoiusMonth(this.reportsDAO.countByOfficeAndMediumOfSubmissionAndDateInBetween(officeId, MediumOfSubmission.FROM_LAST_MONTH.name(), date))
                .countByResolvedStatus(resolvedCount)
                .countByRunningStatus(runningCount)
                .countByPendingStatus(pendingCount)
                .resolutionRate((long) ((resolvedCount * 100.0) / (pendingCount + resolvedCount + runningCount)))
                .officeName(office.getNameBangla())
                .build();
    }

    public List<GrievanceMonthlyReportsDTO> getMonthlyFieldCoordinationReports(String month, UserInformation userInformation) {
        Long fieldCoordinatorsLayerLevel = userInformation.getOfficeInformation().getLayerLevel();
        List<Long> officeIdListByGeoId = null;
        if (fieldCoordinatorsLayerLevel.equals(Constant.layerThree)) {
            officeIdListByGeoId = this.officeService.getOfficeIdListByGeoDivisionId(userInformation.getOfficeInformation().getGeoDivisionId(), fieldCoordinatorsLayerLevel);
        } else if (fieldCoordinatorsLayerLevel.equals(Constant.layerFour)) {
            officeIdListByGeoId = this.officeService.getOfficeIdListByGeoDistrictId(userInformation.getOfficeInformation().getGeoDistrictId(), fieldCoordinatorsLayerLevel);
        }
        List<GrievanceMonthlyReportsDTO> grievanceMonthlyReportsDTOList = this.officeService
                .getGRSenabledOfficesFromOffices(officeIdListByGeoId)
                .stream()
                .map(x -> {
                    return this.getGrievanceMonthlyReportsSummarry(month, x);
                })
                .collect(toList());
        return grievanceMonthlyReportsDTOList;
    }
    public MonthlyReportDTO getGrievanceMonthlyReport(Long officeId, Long monthDiff) {
        Long totalSubmitted = dashboardService.countTotalComplaintsByOfficeIdV2(officeId, monthDiff);
        Long resolvedCount = dashboardService.countResolvedComplaintsByOfficeId(officeId, monthDiff);
        Long timeExpiredCount = dashboardService.countTimeExpiredComplaintsByOfficeId(officeId, monthDiff);
        Long runningGrievanceCount = dashboardService.countRunningGrievancesByOfficeId(officeId, monthDiff);
        Long sentToOtherOfficeCount = dashboardService.countDeclinedGrievancesByOfficeId(officeId, monthDiff);

        Long onlineSubmit = dashboardService.getMonthlyComplaintsCountByOfficeIdAndMediumOfSubmission(officeId, MediumOfSubmission.ONLINE, monthDiff);
        while (resolvedCount+sentToOtherOfficeCount > totalSubmitted) {
            totalSubmitted +=1;
            onlineSubmit +=1;
        }
        Double rate = 0d;
        Long totalDecided = resolvedCount + sentToOtherOfficeCount;
        if (totalSubmitted > 0) {
            rate = ((double)totalDecided / (double)totalSubmitted) * 100;
            rate = (double) Math.round(rate * 100) / 100;
        }
        return MonthlyReportDTO.builder()
                .officeId(officeId)
                .onlineSubmissionCount(onlineSubmit)
                .conventionalMethodSubmissionCount(dashboardService.getMonthlyComplaintsCountByOfficeIdAndMediumOfSubmission(officeId, MediumOfSubmission.CONVENTIONAL_METHOD, monthDiff))
                .selfMotivatedAccusationCount(dashboardService.getMonthlyComplaintsCountByOfficeIdAndMediumOfSubmission(officeId, MediumOfSubmission.SELF_MOTIVATED_ACCEPTANCE, monthDiff))
                .inheritedFromLastMonthCount(dashboardService.getGrievanceAscertainCountOfPreviousMonthV2(officeId, monthDiff))
                .totalCount(totalSubmitted)
                .sentToOtherCount(sentToOtherOfficeCount)
                .resolvedCount(resolvedCount)
                .runningCount(runningGrievanceCount)
                .timeExpiredCount(timeExpiredCount)
                .rate(rate)
                .build();
    }


    public MonthlyReportDTO getAppealMonthlyReport(Long officeId, Long monthDiff) {
        Long totalSubmitted = dashboardService.countTotalAppealsByOfficeIdV2(officeId, monthDiff);
        Long resolvedCount = dashboardService.countResolvedAppealsByOfficeIdV2(officeId, monthDiff);
        Long timeExpiredCount = dashboardService.countTimeExpiredAppealsByOfficeId(officeId, monthDiff);
        Long runningGrievanceCount = dashboardService.countRunningAppealsByOfficeId(officeId, monthDiff);

        Long onlineSubmitted = dashboardService.getMonthlyAppealsCountByOfficeIdAndMediumOfSubmission(officeId, MediumOfSubmission.ONLINE, monthDiff);
        while (resolvedCount > totalSubmitted) {
            totalSubmitted +=1;
            onlineSubmitted +=1;
        }
        Double rate = 0d;
        if (totalSubmitted > 0) {
            rate = ((double)resolvedCount / (double)totalSubmitted) * 100;
            rate = (double) Math.round(rate * 100) / 100;
        }
        return MonthlyReportDTO.builder()
                .officeId(officeId)
                .onlineSubmissionCount(onlineSubmitted)
                .inheritedFromLastMonthCount(dashboardService.getAppealAscertainCountOfPreviousMonth(officeId, monthDiff))
                .totalCount(totalSubmitted)
                .resolvedCount(resolvedCount)
                .runningCount(runningGrievanceCount)
                .timeExpiredCount(timeExpiredCount)
                .rate(rate)
                .build();
    }

    public MonthlyReportDTO getGrievanceMonthlyReportForGenerate(Long officeId, Long monthDiff) {
        Long totalSubmitted = dashboardService.countTotalComplaintsByOfficeIdV2(officeId, monthDiff);
        Long resolvedCount = dashboardService.countResolvedComplaintsByOfficeIdV2(officeId, monthDiff);
        Long timeExpiredCount = dashboardService.countTimeExpiredComplaintsByOfficeIdV3(officeId, monthDiff);
        Long runningGrievanceCount = dashboardService.countRunningGrievancesByOfficeIdV2(officeId, monthDiff);
        Long sentToOtherOfficeCount = dashboardService.countForwardedGrievancesByOfficeIdV2(officeId, monthDiff);
        Long onlineSubmission = dashboardService.getMonthlyComplaintsCountByOfficeIdAndMediumOfSubmission(officeId, MediumOfSubmission.ONLINE, monthDiff);

//        Long inherited = dashboardService.countRunningGrievancesByOfficeIdV2(officeId, monthDiff-1);//dashboardService.getGrievanceAscertainCountOfPreviousMonthV2(officeId, monthDiff);
        Long inherited = dashboardService.countInheritedComplaintsByOfficeId(officeId, monthDiff);

        if (totalSubmitted == null ) {
            totalSubmitted = 0L;
        }
        if (inherited != null && inherited >0) {
            totalSubmitted +=inherited;
        }
        Double rate = 0d;
        Long totalDecided = resolvedCount + sentToOtherOfficeCount;
        if (totalSubmitted > 0) {
            rate = (double) totalDecided / (double)totalSubmitted * 100;
            rate = (double) Math.round(rate * 100) / 100;
        }

        return MonthlyReportDTO.builder()
                .officeId(officeId)
                .onlineSubmissionCount(onlineSubmission)
                .conventionalMethodSubmissionCount(dashboardService.getMonthlyComplaintsCountByOfficeIdAndMediumOfSubmission(officeId, MediumOfSubmission.CONVENTIONAL_METHOD, monthDiff))
                .selfMotivatedAccusationCount(dashboardService.getMonthlyComplaintsCountByOfficeIdAndMediumOfSubmission(officeId, MediumOfSubmission.SELF_MOTIVATED_ACCEPTANCE, monthDiff))
                .inheritedFromLastMonthCount(inherited)
                .totalCount(totalSubmitted)
                .sentToOtherCount(sentToOtherOfficeCount)
                .resolvedCount(resolvedCount)
                .runningCount(runningGrievanceCount)
                .timeExpiredCount(timeExpiredCount)
                .rate(rate)
                .build();
    }
    public MonthlyReportDTO getAppealMonthlyReportForGenerate(Long officeId, Long monthDiff) {
        Long totalSubmitted = dashboardService.countTotalAppealsByOfficeIdV2(officeId, monthDiff);
        Long resolvedCount = dashboardService.countResolvedAppealsByOfficeIdV2(officeId, monthDiff);
        Long timeExpiredCount = dashboardService.countTimeExpiredAppealsByOfficeIdV2(officeId, monthDiff);
        Long runningGrievanceCount = dashboardService.countRunningAppealsByOfficeIdV2(officeId, monthDiff);

        Long onlineSubmitted = dashboardService.getMonthlyAppealsCountByOfficeIdAndMediumOfSubmissionV2(officeId, MediumOfSubmission.ONLINE, monthDiff);
        Double rate = 0d;
        if (totalSubmitted > 0) {
            rate = ((double)resolvedCount / (double)totalSubmitted) * 100;
            rate = (double) Math.round(rate * 100) / 100;
        }
        return MonthlyReportDTO.builder()
                .officeId(officeId)
                .onlineSubmissionCount(onlineSubmitted)
                .inheritedFromLastMonthCount(dashboardService.countRunningAppealsByOfficeIdV2(officeId, monthDiff-1))
                .totalCount(totalSubmitted)
                .resolvedCount(resolvedCount)
                .runningCount(runningGrievanceCount)
                .timeExpiredCount(timeExpiredCount)
                .rate(rate)
                .build();
    }

    public GrievanceAndAppealMonthlyReportDTO getMonthlyReport(Long officeId, int year, int month, long layerLevel) {
        GrievanceAndAppealMonthlyReportDTO grievanceAndAppealMonthlyReportDTO;
        Calendar calendar = Calendar.getInstance();
        int reportMonth = 12 * year + month;
        int currentMonth = 12 * calendar.get(Calendar.YEAR) + (calendar.get(Calendar.MONTH) + 1);
        int monthDiff = reportMonth - currentMonth;
        OfficesGRO officesGRO = this.officesGroService.findOfficesGroByOfficeId(officeId);
        String officeName = officesGRO == null ? "" : officesGRO.getOfficeNameBangla();
        boolean hasAppealReport = layerLevel < Constant.districtLayerLevel && officeService.hasChildOffice(officeId);
        MonthlyReportDTO appealReportDTO = null;
        if (hasAppealReport) {
            appealReportDTO = getAppealMonthlyReport(officeId, (long) monthDiff);
        }
        grievanceAndAppealMonthlyReportDTO = GrievanceAndAppealMonthlyReportDTO.builder()
                .officeId(officeId)
                .year(year)
                .month(month)
                .monthlyGrievanceReport(getGrievanceMonthlyReportForGenerate(officeId, (long) monthDiff))
                .monthlyAppealReport(appealReportDTO)
                .officeName(officeName)
                .build();
        return grievanceAndAppealMonthlyReportDTO;
    }
    public List<GrievanceAndAppealMonthlyReportDTO> getChildOfficesLastMonthReport(Long officeId) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        List<Office> offices = officeService.getOfficesByParentOfficeId(officeId);
        List<Long> officeIdList = offices.stream()
                .map(Office::getId)
                .collect(toList());
        List<Long> grsEnabledOfficeIdList = officesGroService.getGRSEnabledOfficeIdFromOfficeIdList(officeIdList);
        List<GrievanceAndAppealMonthlyReportDTO> reportList = new ArrayList();
        grsEnabledOfficeIdList.stream().forEach(id -> {
            GrievanceAndAppealMonthlyReportDTO grievanceAndAppealMonthlyReportDTO = null;
            Office office = offices.stream().filter(o -> {
                return o.getId().equals(id);
            }).findFirst().orElse(null);
            Boolean hasAppealReport = office.getOfficeLayer().getLayerLevel() < Constant.districtLayerLevel && officeService.hasChildOffice(id);
            MonthlyReport report = monthlyReportDAO.findByOfficeIdAndYearAndMonth(id, year, month);
            if (report != null) {
                grievanceAndAppealMonthlyReportDTO = monthlyReportDAO.convertToGrievanceAndAppealMonthlyReportDTO(report, hasAppealReport);
            } else {
                grievanceAndAppealMonthlyReportDTO = GrievanceAndAppealMonthlyReportDTO.builder()
                        .officeId(id)
                        .month(month)
                        .year(year)
                        .monthlyGrievanceReport(null)
                        .monthlyAppealReport(null)
                        .build();
            }
            grievanceAndAppealMonthlyReportDTO.setOfficeName(office.getNameBangla());
            reportList.add(grievanceAndAppealMonthlyReportDTO);
        });
        return reportList;
    }

    // second minute hour day-of-month month day-of-week
    //@Scheduled(cron = "0 5 6 1 */1 *")
    //@Scheduled(initialDelay = 60*1000, fixedDelay = 24*60*60*1000L)
    public void generateReportsAtEndOfMonth() {
        List<OfficesGRO> grsIncorporatedOffices = officesGroService.getCurrentlyGrsEnabledOffices();
        log.info("Monthly report generation started at " + (new Date()).toString());
        List<String> reportGeneratedForOffices = new ArrayList();
        String email = generalSettingsService.getSettingsValueByFieldName(Constant.SYSTEM_NOTIFICATION_EMAIL);
        String phoneNumber = generalSettingsService.getSettingsValueByFieldName(Constant.SYSTEM_NOTIFICATION_PHONE_NUMBER);
        grsIncorporatedOffices.stream().forEach(grsOffice -> {
            try {
                Long officeId = grsOffice.getOfficeId();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, -1);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                //log.info("===MONTH:{} YEAR:{} OFFICE:{}", month, year, officeId);
                MonthlyReport monthlyReport = monthlyReportDAO.findByOfficeIdAndYearAndMonth(officeId, year, month);
                //log.info("===FOUND:{}", monthlyReport != null);
                if (monthlyReport == null) {
                    GrievanceAndAppealMonthlyReportDTO reportDTO = GrievanceAndAppealMonthlyReportDTO.builder()
                            .officeId(officeId)
                            .year(year)
                            .month(month)
                            .monthlyGrievanceReport(getGrievanceMonthlyReportForGenerate(officeId, -1L))
                            .monthlyAppealReport(getAppealMonthlyReportForGenerate(officeId, -1L))
                            .build();
                    monthlyReport = monthlyReportDAO.convertToMonthlyReport(reportDTO);
                    //log.info("===GOING TO SAVE FOR MONTH:{} YEAR:{} OFFICE:{}", month, year, officeId);
                    monthlyReport = monthlyReportDAO.save(monthlyReport);
                    if (monthlyReport.getId() != null) {
                        reportGeneratedForOffices.add(grsOffice.getOfficeNameBangla() + "\n");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                emailService.sendEmail(email, "GRS monthly reports", "Report of last month has been generated for \n\n" + String.join("\n", reportGeneratedForOffices));
                shortMessageService.sendSMS(phoneNumber, "GRS monthly reports generated for " + reportGeneratedForOffices.size() + " Offices (Please check email for details)");
                log.error("Error occurred during report generation of office " + grsOffice.getOfficeNameBangla());
                log.error(e.getMessage());
            }
        });
//        if (reportGeneratedForOffices.size() > 0) {
//            emailService.sendEmail(email, "GRS monthly reports", "Report of last month has been generated for \n\n" + String.join("\n", reportGeneratedForOffices));
//            shortMessageService.sendSMS(phoneNumber, "GRS monthly reports generated for " + reportGeneratedForOffices.size() + " Offices (Please check email for details)");
//        } else {
//            emailService.sendEmail(email, "GRS monthly reports", "Cannot generate monthly report");
//            shortMessageService.sendSMS(phoneNumber, "Cannot generate monthly report");
//        }
        log.info("Monthly report generation finished at " + (new Date()).toString());
    }

    // second minute hour day-of-month month day-of-week
    //@Scheduled(initialDelay = 100, fixedDelay = Long.MAX_VALUE)
    public void regenerateReportsAtEndOfMonth() {
        System.out.println("## mr1");
        if (updated == 1) return;
        System.out.println("## mr2");
        updated = 1;
        List<OfficesGRO> grsIncorporatedOffices = officesGroService.getCurrentlyGrsEnabledOffices();
        log.info("Monthly report generation started at " + (new Date()).toString());
        int allMonthCountBeforeYear2022 = -38;
        int monthInYear2022 = -6;
        int totalMonthsForAdd = allMonthCountBeforeYear2022 + monthInYear2022;
        for (long monthForAdd = -1; monthForAdd >= totalMonthsForAdd; monthForAdd--) {
            for (OfficesGRO grsOffice : grsIncorporatedOffices) {
                try {
                    Long officeId = grsOffice.getOfficeId();
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MONTH, (int) monthForAdd);
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH) + 1;
                    MonthlyReport monthlyReport = monthlyReportDAO.findByOfficeIdAndYearAndMonth(officeId, year, month);
                    if (monthlyReport == null) {
                        GrievanceAndAppealMonthlyReportDTO reportDTO = GrievanceAndAppealMonthlyReportDTO.builder()
                                .officeId(officeId)
                                .year(year)
                                .month(month)
                                .monthlyGrievanceReport(getGrievanceMonthlyReportForGenerate(officeId, monthForAdd))
                                .monthlyAppealReport(getAppealMonthlyReportForGenerate(officeId, monthForAdd))
                                .build();
                        monthlyReport = monthlyReportDAO.convertToMonthlyReport(reportDTO);
                        monthlyReportDAO.save(monthlyReport);
                        log.info("==SAVED FOR OFFICE:{} MONTH:{} YEAR:{}", officeId, month, year);
                    }
                } catch (Exception e) {
                    log.error("Error occurred during report generation of office " + grsOffice.getOfficeNameBangla());
                    log.error(e.getMessage());
                }
            }
            System.out.println("done for : " + monthForAdd);
        }
        log.info("Monthly report generation finished at " + (new Date()).toString());
    }

    public Integer countGeneratedReportByMonthAndYear(Integer month, Integer year) {
        return monthlyReportDAO.countGeneratedReportByMonthAndYear(month, year);
    }

    public List<GrievanceAndAppealMonthlyReportDTO> getCustomReport(long layerLevel, Long officeId, int fromYear, int fromMonth, int toYear, int toMonth) {
        List<GrievanceAndAppealMonthlyReportDTO> reportList = new ArrayList<>();
        int from = 12 * fromYear + fromMonth;
        int to = 12 * toYear + toMonth;
        for (; from <= to; from++) {
            if (officeId.equals(CacheUtil.SELECT_ALL_OPTION_VALUE)) {
                List<Office> childOffices = this.officeService.getOfficesByLayerLevel(new Long(layerLevel).intValue(), true);
                if (childOffices != null && childOffices.size() > 0) {
                    for (Office office : childOffices) {
                        GrievanceAndAppealMonthlyReportDTO reportDTO = getMonthlyReport(office.getId(), fromYear, fromMonth, layerLevel);
                        reportList.add(reportDTO);
                    }
                }
            } else {
                GrievanceAndAppealMonthlyReportDTO reportDTO = getMonthlyReport(officeId, fromYear, fromMonth, layerLevel);
                reportList.add(reportDTO);
            }
            if (fromMonth == 12) {
                fromMonth = 1;
                fromYear++;
            } else {
                fromMonth++;
            }
        }

        if (officeId.equals(CacheUtil.SELECT_ALL_OPTION_VALUE)) {

            Map<String, List<GrievanceAndAppealMonthlyReportDTO>> peopleBySomeKey = reportList.stream().collect(Collectors.groupingBy(this::getGroupingByKey, Collectors.mapping((GrievanceAndAppealMonthlyReportDTO p) -> p, toList())));

            List<GrievanceAndAppealMonthlyReportDTO> results = new ArrayList<>();
            peopleBySomeKey.forEach((k, v) -> {
                GrievanceAndAppealMonthlyReportDTO rep = new GrievanceAndAppealMonthlyReportDTO();
                rep.setMonth(v.get(0).getMonth());
                rep.setYear(v.get(0).getYear());
                rep.setOfficeLevel(v.get(0).getOfficeLevel());
                MonthlyReportDTO monthlyGR = new MonthlyReportDTO();
                MonthlyReportDTO monthlyAR = new MonthlyReportDTO();

                for (GrievanceAndAppealMonthlyReportDTO reportDTO : v) {
                    if (reportDTO == null) {
                        continue;
                    }
                    MonthlyReportDTO monthlyGrievanceReport = reportDTO.getMonthlyGrievanceReport();
                    if (monthlyGrievanceReport != null) {
                        if (monthlyGrievanceReport.getOnlineSubmissionCount() != null) {
                            monthlyGR.setOnlineSubmissionCount(monthlyGR.getOnlineSubmissionCount() + monthlyGrievanceReport.getOnlineSubmissionCount());
                        }
                        if (monthlyGrievanceReport.getConventionalMethodSubmissionCount() != null) {
                            monthlyGR.setConventionalMethodSubmissionCount(monthlyGR.getConventionalMethodSubmissionCount() + monthlyGrievanceReport.getConventionalMethodSubmissionCount());
                        }
                        if (monthlyGrievanceReport.getSelfMotivatedAccusationCount() != null) {
                            monthlyGR.setSelfMotivatedAccusationCount(monthlyGR.getSelfMotivatedAccusationCount() + monthlyGrievanceReport.getSelfMotivatedAccusationCount());
                        }
                        if (monthlyGrievanceReport.getInheritedFromLastMonthCount() != null) {
                            monthlyGR.setInheritedFromLastMonthCount(monthlyGR.getInheritedFromLastMonthCount() + monthlyGrievanceReport.getInheritedFromLastMonthCount());
                        }
                        if (monthlyGrievanceReport.getTotalCount() != null) {
                            monthlyGR.setTotalCount(monthlyGR.getTotalCount() + monthlyGrievanceReport.getTotalCount());
                        }
                        if (monthlyGrievanceReport.getSentToOtherCount() != null) {
                            monthlyGR.setSentToOtherCount(monthlyGR.getSentToOtherCount() + monthlyGrievanceReport.getSentToOtherCount());
                        }
                        if (monthlyGrievanceReport.getResolvedCount() != null) {
                            monthlyGR.setResolvedCount(monthlyGR.getResolvedCount() + monthlyGrievanceReport.getResolvedCount());
                        }
                        if (monthlyGrievanceReport.getTimeExpiredCount() != null) {
                            monthlyGR.setTimeExpiredCount(monthlyGR.getTimeExpiredCount() + monthlyGrievanceReport.getTimeExpiredCount());
                        }
                        if (monthlyGrievanceReport.getRunningCount() != null) {
                            monthlyGR.setRunningCount(monthlyGR.getRunningCount() + monthlyGrievanceReport.getRunningCount());
                        }
                        if (monthlyGrievanceReport.getRate() != null) {
                            monthlyGR.setRate((monthlyGR.getRate() + monthlyGrievanceReport.getRate()));
                        }
                    }
                    MonthlyReportDTO monthlyAppealReport = reportDTO.getMonthlyAppealReport();
                    if (monthlyAppealReport != null) {
                        if (monthlyAppealReport.getOnlineSubmissionCount() != null) {
                            monthlyAR.setOnlineSubmissionCount(monthlyAR.getOnlineSubmissionCount() + monthlyAppealReport.getOnlineSubmissionCount());
                        }
                        if (monthlyAppealReport.getConventionalMethodSubmissionCount() != null) {
                            monthlyAR.setConventionalMethodSubmissionCount(monthlyAR.getConventionalMethodSubmissionCount() + monthlyAppealReport.getConventionalMethodSubmissionCount());
                        }
                        if (monthlyAppealReport.getSelfMotivatedAccusationCount() != null) {
                            monthlyAR.setSelfMotivatedAccusationCount(monthlyAR.getSelfMotivatedAccusationCount() + monthlyAppealReport.getSelfMotivatedAccusationCount());
                        }
                        if (monthlyAppealReport.getInheritedFromLastMonthCount() != null) {
                            monthlyAR.setInheritedFromLastMonthCount(monthlyAR.getInheritedFromLastMonthCount() + monthlyAppealReport.getInheritedFromLastMonthCount());
                        }
                        if (monthlyAppealReport.getTotalCount() != null) {
                            monthlyAR.setTotalCount(monthlyAR.getTotalCount() + monthlyAppealReport.getTotalCount());
                        }
                        if (monthlyAppealReport.getSentToOtherCount() != null) {
                            monthlyAR.setSentToOtherCount(monthlyAR.getSentToOtherCount() + monthlyAppealReport.getSentToOtherCount());
                        }
                        if (monthlyAppealReport.getResolvedCount() != null) {
                            monthlyAR.setResolvedCount(monthlyAR.getResolvedCount() + monthlyAppealReport.getResolvedCount());
                        }
                        if (monthlyAppealReport.getTimeExpiredCount() != null) {
                            monthlyAR.setTimeExpiredCount(monthlyAR.getTimeExpiredCount() + monthlyAppealReport.getTimeExpiredCount());
                        }
                        if (monthlyAppealReport.getRunningCount() != null) {
                            monthlyAR.setRunningCount(monthlyAR.getRunningCount() + monthlyAppealReport.getRunningCount());
                        }
                        if (monthlyAppealReport.getRate() != null) {
                            monthlyAR.setRate((monthlyAR.getRate() + monthlyAppealReport.getRate()));
                        }
                    }

                }

                monthlyGR.setRate((((monthlyGR.getResolvedCount() + monthlyGR.getSentToOtherCount().doubleValue()) * 100 / monthlyGR.getTotalCount().doubleValue())));
                monthlyAR.setRate((((monthlyAR.getResolvedCount() + monthlyAR.getSentToOtherCount().doubleValue()) * 100 / monthlyAR.getTotalCount().doubleValue())));
                rep.setMonthlyGrievanceReport(monthlyGR);
                rep.setMonthlyAppealReport(monthlyAR);
                results.add(rep);
            });
            return results;
        } else {
            return reportList;
        }
    }

    //TODO:: Daily report

    public List<GrievanceAndAppealDailyReportDTO> getCustomReportDaily(Long layerLevel, Long officeOrigin, Long customLayer, Long officeId, int fromYear, int fromMonth, int fromDay, int toYear, int toMonth, int toDay) {
        List<GrievanceAndAppealDailyReportDTO> reportList = new ArrayList<>();


        Date fromDate = Utility.getDate(fromDay, fromMonth, fromYear, false);
        Date toDate = Utility.getDate(toDay, toMonth, toYear, true);

        Long[] dailyReport = baseEntityManager.getDailyReport(layerLevel, officeOrigin, customLayer, officeId, fromDate, toDate);


        DailyReportDTO dailyGrievanceReport = DailyReportDTO.builder()
                .totalCount(dailyReport[0])
                .resolvedCount(dailyReport[1])
                .timeExpiredCount(dailyReport[2])
                .runningCount(dailyReport[3])
                .sentToOtherCount(dailyReport[4])
                .onlineSubmissionCount(dailyReport[5])
                .conventionalMethodSubmissionCount(dailyReport[6])
                .selfMotivatedAccusationCount(dailyReport[7])
                .inheritedFromLastMonthCount(dailyReport[8])
                .rate((((double) (dailyReport[4] + dailyReport[1]) / (double) dailyReport[0]) * 100.0))
                .build();
        DailyReportDTO dailyAppealReport = DailyReportDTO.builder()
                .onlineSubmissionCount(dailyReport[13])
                .inheritedFromLastMonthCount(dailyReport[14])
                .totalCount(dailyReport[9])
                .resolvedCount(dailyReport[10])
                .timeExpiredCount(dailyReport[11])
                .runningCount(dailyReport[12])
                .rate((((double) (dailyReport[10]) / (double) dailyReport[9]) * 100.0))
                .build();


        GrievanceAndAppealDailyReportDTO dailyReportDTO = GrievanceAndAppealDailyReportDTO.builder()
                .officeId(officeId)
                .officeLevel(layerLevel != null ? layerLevel.intValue() : null)
                .reportDate(fromDate.getMonth() + "")
                .dailyGrievanceReport(dailyGrievanceReport)
                .dailyAppealReport(dailyAppealReport)
                .build();


        reportList.add(dailyReportDTO);
        return reportList;

    }

    public List<GrievanceAndAppealMonthlyReportDTO> getCustomReportAllLayer(int fromYear, int fromMonth, int toYear, int toMonth) {
        List<GrievanceAndAppealMonthlyReportDTO> reportList = new ArrayList();
        Long sl = 0L;
        IntStream.range(fromYear, toYear).forEach(y ->
                IntStream.range(fromMonth, toMonth).forEach(
                        m -> reportList.add(GrievanceAndAppealMonthlyReportDTO.builder().year(y).month(m).sl(sl).build())
                )
        );

        List<MonthlyReport> monthlyReports = monthlyReportDAO.findSummaryByfromYearMonthToYearMonth(fromYear, fromMonth, toYear, toMonth);

        monthlyReports.stream()
                .forEach(mr -> {
                    MonthlyReportDTO monthlyGrievanceReports = MonthlyReportDTO.builder()
                            .onlineSubmissionCount(mr.getOnlineSubmissionCount())
                            .conventionalMethodSubmissionCount(mr.getConventionalMethodSubmissionCount())
                            .selfMotivatedAccusationCount(mr.getSelfMotivatedAccusationCount())
                            .inheritedFromLastMonthCount(mr.getInheritedFromLastMonthCount())
                            .totalCount(mr.getTotalCount())
                            .sentToOtherCount(mr.getSentToOtherCount())
                            .resolvedCount(mr.getResolvedCount())
                            .timeExpiredCount(mr.getTimeExpiredCount())

                            .build();
                    MonthlyReportDTO monthlyAppealReports = MonthlyReportDTO.builder()
                            .build();
                    GrievanceAndAppealMonthlyReportDTO reportDTO = GrievanceAndAppealMonthlyReportDTO
                            .builder()
                            .year(mr.getYear())
                            .month(mr.getMonth())
                            .monthlyGrievanceReport(monthlyGrievanceReports)
                            .monthlyAppealReport(monthlyAppealReports)
                            .build();
                    reportList.add(reportDTO);
                });

        return reportList;


    }


    private String getGroupingByKey(GrievanceAndAppealMonthlyReportDTO p) {
        return p.getYear() + "_" + p.getMonth();
    }


    public List<GrievanceAndAppealMonthlyReportDTO> getDcOfficeWiseReport(Long officeId, Integer fromYear, Integer fromMonth, Integer toYear, Integer toMonth) {
        if (officeId.equals(9999L)) { // Assuming 9999 is the ID for "সকল অফিস"
            List<Office> offices = this.officeService.findByOfficeOriginId(16L, true, false); // Assuming the method is called without an officeOriginId for all offices
            List<GrievanceAndAppealMonthlyReportDTO> reportDTOS = getMultipleOfficesMergedReport(offices, fromYear, fromMonth, toYear, toMonth);

            // Sort the list based on the rate of monthlyGrievanceReport in descending order
            Collections.sort(reportDTOS, Comparator.comparingDouble(dto -> {
                MonthlyReportDTO monthlyGrievanceReport = ((GrievanceAndAppealMonthlyReportDTO) dto).getMonthlyGrievanceReport();
                return monthlyGrievanceReport != null ? monthlyGrievanceReport.getRate() : 0.0;
            }).reversed());

            // Set the serial number (sl) after sorting
            long serialNumber = 1;
            for (GrievanceAndAppealMonthlyReportDTO dto : reportDTOS) {
                dto.setSl(serialNumber);
                if (dto.getMonthlyGrievanceReport() != null) {
                    dto.getMonthlyGrievanceReport().setSl(serialNumber);
                }
                if (dto.getMonthlyAppealReport() != null) {
                    dto.getMonthlyAppealReport().setSl(serialNumber);
                }
                serialNumber++;
            }

            return reportDTOS;
        } else {
            // Fetch report for a single office
            List<GrievanceAndAppealMonthlyReportDTO> reportDTOS =getCustomReport(5, officeId, fromYear, fromMonth, toYear, toMonth);

            // Set the serial number (sl) after sorting
            long serialNumber = 1;
            for (GrievanceAndAppealMonthlyReportDTO dto : reportDTOS) {
                dto.setSl(serialNumber);
                if (dto.getMonthlyGrievanceReport() != null) {
                    dto.getMonthlyGrievanceReport().setSl(serialNumber);
                }
                if (dto.getMonthlyAppealReport() != null) {
                    dto.getMonthlyAppealReport().setSl(serialNumber);
                }
                serialNumber++;
            }

            return reportDTOS;
        }
    }


    public List<GrievanceAndAppealMonthlyReportDTO> getMultipleOfficesMergedReport(List<Office> childOffices, Integer fromYear, Integer fromMonth, Integer toYear, Integer toMonth) {
        List<GrievanceAndAppealMonthlyReportDTO> grievanceAndAppealMonthlyReportDTOS = new ArrayList<>();
        for (Office childOffice : childOffices) {
            Long totalOnline = 0L;
            Long totalSelfMotivated = 0L;
            Long totalConventional = 0L;
            Long totalNew = 0L;
            Long totalInherited = -1L;
            Long sendToOtherOffices = 0L;
            Long totalResolved = 0L;
            Long totalRunning = 0L;
            Long timeExpired = 0L;
            Long totalNewAppeal = 0L;
            Long totalInheritedAppeal = -1L;
            Long totalResolvedAppeal = 0L;
            Long totalRunningAppeal = 0L;
            Long timeExpiredAppeal = 0L;
            Boolean hasAppealReportFlag = false;
            Double rate = 0d;
            Double rateAppeal = 0d;

            if (childOffice.getOfficeLayer() == null) {
                continue;
            }
            for (GrievanceAndAppealMonthlyReportDTO reportDTO : getCustomReport(childOffice.getOfficeLayer().getLayerLevel(), childOffice.getId(), fromYear, fromMonth, toYear, toMonth)) {
                MonthlyReportDTO monthlyGrievanceReport = reportDTO.getMonthlyGrievanceReport();
                MonthlyReportDTO monthlyAppealReport = reportDTO.getMonthlyAppealReport();
                if (monthlyGrievanceReport != null) {
                    if (totalInherited == -1) {
                        totalInherited = monthlyGrievanceReport.getInheritedFromLastMonthCount();
                    }
                    totalOnline += monthlyGrievanceReport.getOnlineSubmissionCount();
                    totalSelfMotivated += monthlyGrievanceReport.getSelfMotivatedAccusationCount();
                    totalConventional += monthlyGrievanceReport.getConventionalMethodSubmissionCount();
                    totalNew += (monthlyGrievanceReport.getOnlineSubmissionCount()
                            + monthlyGrievanceReport.getConventionalMethodSubmissionCount()
                            + monthlyGrievanceReport.getSelfMotivatedAccusationCount());
                    sendToOtherOffices += monthlyGrievanceReport.getSentToOtherCount();
                    totalResolved += monthlyGrievanceReport.getResolvedCount();
                    totalRunning = monthlyGrievanceReport.getRunningCount();
                    timeExpired = monthlyGrievanceReport.getTimeExpiredCount();
                }
                if (monthlyAppealReport != null) {
                    if (totalInheritedAppeal == -1) {
                        totalInheritedAppeal = monthlyAppealReport.getInheritedFromLastMonthCount();
                    }
                    totalNewAppeal += monthlyAppealReport.getOnlineSubmissionCount();
                    totalResolvedAppeal += monthlyAppealReport.getResolvedCount();
                    totalRunningAppeal = monthlyAppealReport.getRunningCount();
                    timeExpiredAppeal = monthlyAppealReport.getTimeExpiredCount();
                    hasAppealReportFlag = true;
                }
            }

            if (totalNew + totalInherited > 0) {
                rate = (double) (((totalResolved + sendToOtherOffices) * 1.0 / (totalNew + totalInherited)) * 100);
                rate = (double) Math.round(rate * 100) / 100;
            }

            if (totalNewAppeal + totalInheritedAppeal > 0) {
                rateAppeal = (double) ((totalResolvedAppeal * 1.0 / (totalNewAppeal + totalInheritedAppeal)) * 100);
                rateAppeal = (double) Math.round(rateAppeal * 100) / 100;
            }
            totalInherited = totalInherited == -1 ? 0 : totalInherited;
            totalInheritedAppeal = totalInheritedAppeal == -1 ? 0 : totalInheritedAppeal;

            grievanceAndAppealMonthlyReportDTOS.add(
                    GrievanceAndAppealMonthlyReportDTO.builder()
                            .month(fromMonth)
                            .officeId(childOffice.getId())
                            .officeName(childOffice.getNameBangla())
                            .officeLevel(childOffice.getOfficeLayer().getLayerLevel())
                            .year(fromYear)
                            .monthlyGrievanceReport(
                                    MonthlyReportDTO.builder()
                                            .onlineSubmissionCount(totalOnline)
                                            .selfMotivatedAccusationCount(totalSelfMotivated)
                                            .conventionalMethodSubmissionCount(totalConventional)
                                            .inheritedFromLastMonthCount(totalInherited)
                                            .totalCount(totalNew + totalInherited)
                                            .sentToOtherCount(sendToOtherOffices)
                                            .resolvedCount(totalResolved)
                                            .runningCount(totalRunning)
                                            .rate(rate)
                                            .timeExpiredCount(timeExpired)
                                            .build()
                            )
                            .monthlyAppealReport(
                                    MonthlyReportDTO.builder()
                                            .onlineSubmissionCount(totalNewAppeal)
                                            .inheritedFromLastMonthCount(totalInheritedAppeal)
                                            .totalCount(totalNewAppeal + totalInheritedAppeal)
                                            .resolvedCount(totalResolvedAppeal)
                                            .runningCount(totalRunningAppeal)
                                            .timeExpiredCount(timeExpiredAppeal)
                                            .rate(rateAppeal)
                                            .build()
                            )
                            .build()
            );
        }
        return grievanceAndAppealMonthlyReportDTOS.stream().sorted(Comparator.comparingInt(GrievanceAndAppealMonthlyReportDTO::getOfficeLevel)).collect(toList());
    }

    public List<GrievanceAndAppealDailyReportDTO> getMultipleOfficesMergedReport(List<Office> childOffices, Integer fromYear, Integer fromMonth, Integer fromDay,
                                                                                 Integer toYear, Integer toMonth, Integer toDay) {
        List<GrievanceAndAppealDailyReportDTO> grievanceAndAppealDailyReportDTOS = new ArrayList<>();
        for (Office childOffice : childOffices) {
            Long totalOnline = 0L;
            Long totalSelfMotivated = 0L;
            Long totalConventional = 0L;
            Long totalNew = 0L;
            Long totalInherited = -1L;
            Long sendToOtherOffices = 0L;
            Long totalResolved = 0L;
            Long totalRunning = 0L;
            Long timeExpired = 0L;
            Long totalNewAppeal = 0L;
            Long totalInheritedAppeal = -1L;
            Long totalResolvedAppeal = 0L;
            Long totalRunningAppeal = 0L;
            Long timeExpiredAppeal = 0L;
            Boolean hasAppealReportFlag = false;
            Double rate = 0d;
            Double rateAppeal = 0d;

            if (childOffice.getOfficeLayer() == null) {
                continue;
            }
            for (GrievanceAndAppealDailyReportDTO reportDTO : getCustomReportDaily(null, null, null, childOffice.getId(), fromYear, fromMonth, fromDay,
                    toYear, toMonth, toDay)) {
                DailyReportDTO dailyGrievanceReport = reportDTO.getDailyGrievanceReport();
                DailyReportDTO dailyAppealReport = reportDTO.getDailyAppealReport();
                if (dailyGrievanceReport != null) {
                    if (totalInherited == -1) {
                        totalInherited = dailyGrievanceReport.getInheritedFromLastMonthCount();
                    }
                    totalOnline += dailyGrievanceReport.getOnlineSubmissionCount();
                    totalSelfMotivated += dailyGrievanceReport.getSelfMotivatedAccusationCount();
                    totalConventional += dailyGrievanceReport.getConventionalMethodSubmissionCount();
                    totalNew += (dailyGrievanceReport.getOnlineSubmissionCount()
                            + dailyGrievanceReport.getConventionalMethodSubmissionCount()
                            + dailyGrievanceReport.getSelfMotivatedAccusationCount());
                    sendToOtherOffices += dailyGrievanceReport.getSentToOtherCount();
                    totalResolved += dailyGrievanceReport.getResolvedCount();
                    totalRunning = dailyGrievanceReport.getRunningCount();
                    timeExpired = dailyGrievanceReport.getTimeExpiredCount();
                }
                if (dailyAppealReport != null) {
                    if (totalInheritedAppeal == -1) {
                        totalInheritedAppeal = dailyAppealReport.getInheritedFromLastMonthCount();
                    }
                    totalNewAppeal += dailyAppealReport.getOnlineSubmissionCount();
                    totalResolvedAppeal += dailyAppealReport.getResolvedCount();
                    totalRunningAppeal = dailyAppealReport.getRunningCount();
                    timeExpiredAppeal = dailyAppealReport.getTimeExpiredCount();
                    hasAppealReportFlag = true;
                }
            }

            if (totalNew + totalInherited > 0) {
                rate = (double) (((totalResolved + sendToOtherOffices) * 1.0 / (totalNew + totalInherited)) * 100);
                rate = (double) Math.round(rate * 100) / 100;
            }

            if (totalNewAppeal + totalInheritedAppeal > 0) {
                rateAppeal = (double) ((totalResolvedAppeal * 1.0 / (totalNewAppeal + totalInheritedAppeal)) * 100);
                rateAppeal = (double) Math.round(rateAppeal * 100) / 100;
            }
            totalInherited = totalInherited == -1 ? 0 : totalInherited;
            totalInheritedAppeal = totalInheritedAppeal == -1 ? 0 : totalInheritedAppeal;

            grievanceAndAppealDailyReportDTOS.add(
                    GrievanceAndAppealDailyReportDTO.builder()
//                            .month(fromMonth)
                            .officeId(childOffice.getId())
                            .officeName(childOffice.getNameBangla())
                            .officeLevel(childOffice.getOfficeLayer().getLayerLevel())
//                            .year(fromYear)
                            .dailyGrievanceReport(
                                    DailyReportDTO.builder()
                                            .onlineSubmissionCount(totalOnline)
                                            .selfMotivatedAccusationCount(totalSelfMotivated)
                                            .conventionalMethodSubmissionCount(totalConventional)
                                            .inheritedFromLastMonthCount(totalInherited)
                                            .totalCount(totalNew + totalInherited)
                                            .sentToOtherCount(sendToOtherOffices)
                                            .resolvedCount(totalResolved)
                                            .runningCount(totalRunning)
                                            .rate(rate)
                                            .timeExpiredCount(timeExpired)
                                            .build()
                            )
                            .dailyAppealReport(
                                    DailyReportDTO.builder()
                                            .onlineSubmissionCount(totalNewAppeal)
                                            .inheritedFromLastMonthCount(totalInheritedAppeal)
                                            .totalCount(totalNewAppeal + totalInheritedAppeal)
                                            .resolvedCount(totalResolvedAppeal)
                                            .runningCount(totalRunningAppeal)
                                            .timeExpiredCount(timeExpiredAppeal)
                                            .rate(rateAppeal)
                                            .build()
                            )
                            .build()
            );
        }
        return grievanceAndAppealDailyReportDTOS.stream().sorted(Comparator.comparingInt(GrievanceAndAppealDailyReportDTO::getOfficeLevel)).collect(toList());
    }

    public List<GrievanceAndAppealMonthlyReportDTO> getMinistryBasedReport(Long officeId, Integer fromYear, Integer fromMonth, Integer toYear, Integer toMonth) {
        Office office = this.officeService.getOffice(officeId);
        List<Long> officeIds = this.officesGroService.findAllOffficeIds();
        List<Office> childOffices = this.officeService.getDescendantOfficesByMinistryId(office.getOfficeMinistry()).stream().filter(o -> officeIds.contains(o.getId())).collect(toList());
        return getMultipleOfficesMergedReport(childOffices, fromYear, fromMonth, toYear, toMonth);
    }

    public List<GrievanceAndAppealDailyReportDTO> getMinistryBasedReport(Long officeId, Integer fromYear, Integer fromMonth, Integer fromDay, Integer toYear, Integer toMonth, Integer toDay) {
        Office office = this.officeService.getOffice(officeId);
        List<Long> officeIds = this.officesGroService.findAllOffficeIds();
        List<Office> childOffices = this.officeService.getDescendantOfficesByMinistryId(office.getOfficeMinistry()).stream().filter(o -> officeIds.contains(o.getId())).collect(toList());
        return getMultipleOfficesMergedReport(childOffices, fromYear, fromMonth, fromDay, toYear, toMonth, toDay);
    }

    public List<GrievanceAndAppealMonthlyReportDTO> getLayerWiseBasedReport(Integer level, Integer fromYear, Integer fromMonth, Integer toYear, Integer toMonth, Authentication authentication) {
        List<Office> childOffices = this.officeService.getOfficesByLayerLevel(level, true);
        if (Utility.isUserAnOisfUser(authentication)) {
            OfficeInformation currentUserOfficeInformation = this.officeService.getCurrentLoggedInUserInformation();

            if (currentUserOfficeInformation.getOfficeId() != 28) {
                childOffices = childOffices
                        .stream()
                        .filter(office -> Objects.equals(currentUserOfficeInformation.getOfficeId(), office.getId()))
                        .collect(toList());
            }


        }


        List<GrievanceAndAppealMonthlyReportDTO> reportDTOS = getMultipleOfficesMergedReport(childOffices, fromYear, fromMonth, toYear, toMonth);
//        if (level.equals(1)) {
//            final long[] serial = {1};
//            return reportDTOS.stream()
//                    .filter(e -> CacheUtil.getOfficeOrder(e.getOfficeId()) != null)
//                    .sorted((a, b) -> Long.compare(CacheUtil.getOfficeOrder(a.getOfficeId()), CacheUtil.getOfficeOrder(b.getOfficeId())))
//                    .map(r -> {
//                        r.setSl(serial[0]);
//                        r.getMonthlyGrievanceReport().setSl(serial[0]);
//                        serial[0]++;
//                        return r;
//                    })
//                    .collect(Collectors.toList());
//
//
//        }
        if (level.equals(1)) {
            final long[] serial = {1};

            // Define the IDs of the offices you want to move to the end (using HashSet and Arrays.asList)
            Set<Long> excludedOfficeIds = new HashSet<>(Arrays.asList(2131L, 2175L, 53L));

            // Process the main list without the excluded office IDs
            List<GrievanceAndAppealMonthlyReportDTO> prioritizedList = reportDTOS.stream()
                    .filter(e -> CacheUtil.getOfficeOrder(e.getOfficeId()) != null)
                    .filter(e -> !excludedOfficeIds.contains(e.getOfficeId()))  // Exclude the specified offices
                    .sorted((a, b) -> Long.compare(CacheUtil.getOfficeOrder(a.getOfficeId()), CacheUtil.getOfficeOrder(b.getOfficeId())))
                    .map(r -> {
                        r.setSl(serial[0]);
                        r.getMonthlyGrievanceReport().setSl(serial[0]);
                        serial[0]++;
                        return r;
                    })
                    .collect(Collectors.toList());

            // Now handle the excluded offices and append them to the list
            List<GrievanceAndAppealMonthlyReportDTO> excludedOffices = reportDTOS.stream()
                    .filter(e -> excludedOfficeIds.contains(e.getOfficeId()))  // Only include the excluded offices
                    .map(r -> {
                        r.setSl(serial[0]);
                        r.getMonthlyGrievanceReport().setSl(serial[0]);
                        serial[0]++;
                        return r;
                    })
                    .collect(Collectors.toList());

            // Combine both lists (prioritized + excluded offices at the end)
            prioritizedList.addAll(excludedOffices);

            return prioritizedList;
        }
        else {
            for (int i=0;i<reportDTOS.size();i++) {
                reportDTOS.get(i).setSl(new Long(i+1));
                reportDTOS.get(i).getMonthlyGrievanceReport().setSl(new Long(i+1));
            }
            return reportDTOS.stream()
                    .collect(Collectors.toList());
        }
    }

    public List<GrievanceAndAppealMonthlyReportDTO> getLayerWiseWithChildOfficesBasedReport(Integer level, Long firstSelection, Long secondSelection, Integer fromYear, Integer fromMonth, Integer toYear, Integer toMonth) {
        List<Office> childOffices = this.officeService.getOfficesByLayerLevelWithChildOffices(level, firstSelection, secondSelection, true);
        return getMultipleOfficesMergedReport(childOffices, fromYear, fromMonth, toYear, toMonth);
    }

    public Page<GrievanceComplainantInfoDTO> getTimeWiseComplainantsReport(Integer level, Long firstSelection, Long secondSelection, Integer fromYear, Integer fromMonth, Integer toYear, Integer toMonth, Pageable pageable) {
        List<Office> childOffices = this.officeService.getOfficesByNullableLayerLevelWithChildOffices(level, firstSelection, secondSelection, true);
        return this.grievanceService.getComplainantViewForReport(childOffices, fromYear, fromMonth, toYear, toMonth, pageable);
    }

    public List<GrievanceAndAppealMonthlyReportDTO> getLocationBasedReport(Authentication authentication, Integer division, Integer district, Integer upazilla, Integer fromYear, Integer fromMonth, Integer toYear, Integer toMonth) {
        List<Office> offices = new ArrayList<Office>();
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Office usersOffice = this.officeService.getOffice(userInformation.getOfficeInformation().getOfficeId());
        OfficeLayer officeLayer = usersOffice.getOfficeLayer();
        OfficeMinistry officeMinistry = usersOffice.getOfficeMinistry();
        boolean allOfficeFlag = officeLayer.getId() == 21 || officeLayer.getId() == 22 || officeLayer.getId() == 23 || officeLayer.getId() == 43;
        List<Long> officeIds = this.officesGroService.findAllOffficeIds();


        offices = getOfficesWithGeoLocationAndMinistry(division, district, upazilla, officeMinistry);

        if (allOfficeFlag) {
            List<Office> otherMinistryOffices = getOfficesWithGeoLocation(division, district, upazilla);
            offices.addAll(otherMinistryOffices);
        }
        List<Office> filteredOffices = offices.stream().filter(o -> officeIds.contains(o.getId())).distinct().collect(toList());
        return getMultipleOfficesMergedReport(filteredOffices, fromYear, fromMonth, toYear, toMonth);
    }

    private List<Office> getOfficesWithGeoLocation(Integer divisionId, Integer districtId, Integer upazillaId) {
        if (districtId == 0 && upazillaId == 0) {
            return this.officeService.getDivisionLevelOffices(divisionId);
        } else if (districtId > 0 && upazillaId == 0) {
            return this.officeService.getDistrictLevelOffices(divisionId, districtId);
        } else {
            return this.officeService.getUpazilaLevelOffices(divisionId, districtId, upazillaId);
        }
    }

    private List<Office> getOfficesWithGeoLocationAndMinistry(Integer divisionId, Integer districtId, Integer upazillaId, OfficeMinistry officeMinistry) {
        if (districtId == 0 && upazillaId == 0) {
            return this.officeService.findByDivisionIdAndOfficeMinistry(divisionId, officeMinistry);
        } else if (districtId > 0 && upazillaId == 0) {
            return this.officeService.findByDivisionIdAndDistrictIdAndOfficeMinistry(divisionId, districtId, officeMinistry);
        } else {
            return this.officeService.findByDivisionIdAndDistrictIdAndUpazilaIdAndOfficeMinistry(divisionId, districtId, upazillaId, officeMinistry);
        }
    }

    public void regenerateReports(String year, String month) {
        List<OfficesGRO> grsIncorporatedOffices = officesGroService.getCurrentlyGrsEnabledOffices();
        log.info("Monthly report generation started at " + (new Date()).toString());
        List<String> reportGeneratedForOffices = new ArrayList();
        String email = generalSettingsService.getSettingsValueByFieldName(Constant.SYSTEM_NOTIFICATION_EMAIL);
        String phoneNumber = generalSettingsService.getSettingsValueByFieldName(Constant.SYSTEM_NOTIFICATION_PHONE_NUMBER);
        grsIncorporatedOffices.stream().forEach(grsOffice -> {
            try {
                Long officeId = grsOffice.getOfficeId();
                MonthlyReport previousReport = monthlyReportDAO.findByOfficeIdAndYearAndMonth(officeId, Integer.parseInt(year), Integer.parseInt(month));
                MonthlyReport monthlyReport;
                GrievanceAndAppealMonthlyReportDTO reportDTO = GrievanceAndAppealMonthlyReportDTO.builder()
                        .officeId(officeId)
                        .year(Integer.parseInt(year))
                        .month(Integer.parseInt(month))
                        .monthlyGrievanceReport(generateGrievanceMonthlyReport(officeId, year, month))
                        .monthlyAppealReport(generateAppealMonthlyReport(officeId, year, month))
                        .build();
                monthlyReport = monthlyReportDAO.convertToMonthlyReport(reportDTO);
                if (previousReport != null) {
                    monthlyReport.setId(previousReport.getId());
                }
                monthlyReport = monthlyReportDAO.save(monthlyReport);
                if (monthlyReport.getId() != null) {
                    reportGeneratedForOffices.add(grsOffice.getOfficeNameBangla() + "\n");
                }
            } catch (Exception e) {
//                emailService.sendEmail(email, "GRS monthly reports", "Report of last month has been generated for \n\n" + String.join("\n", reportGeneratedForOffices));
//                shortMessageService.sendSMS(phoneNumber, "GRS monthly reports generated for " + reportGeneratedForOffices.size() + " Offices (Please check email for details)");
                log.error("Error occurred during report generation of office " + grsOffice.getOfficeNameBangla());
                log.error(e.getMessage());
            }
        });
        if (reportGeneratedForOffices.size() > 0) {
//            emailService.sendEmail(email, "GRS monthly reports", "Report of last month has been generated for \n\n" + String.join("\n", reportGeneratedForOffices));
//            shortMessageService.sendSMS(phoneNumber, "GRS monthly reports generated for " + reportGeneratedForOffices.size() + " Offices (Please check email for details)");
        } else {
//            emailService.sendEmail(email, "GRS monthly reports", "Cannot generate monthly report");
//            shortMessageService.sendSMS(phoneNumber, "Cannot generate monthly report");
        }
        log.info("Monthly report generation finished at " + (new Date()).toString());
    }

    private MonthlyReportDTO generateAppealMonthlyReport(Long officeId, String year, String month) {
        Long totalSubmitted = dashboardService.countTotalAppealByOfficeIdAndYearAndMonth(officeId, year, month);
        Long resolvedCount = dashboardService.countResolvedAppealByOfficeIdAndYearAndMonth(officeId, year, month);
        Long timeExpiredCount = dashboardService.countTimeExpiredAppealByOfficeIdAndYearAndMonth(officeId, year, month);
        Long runningGrievanceCount = dashboardService.countRunningAppealByOfficeIdAndYearAndMonth(officeId, year, month);
        Double rate = 0d;
        if (totalSubmitted > 0) {
            rate = ((resolvedCount * 1.0) / (totalSubmitted)) * 100;
            rate = (double) Math.round(rate * 100) / 100;
        }
        return MonthlyReportDTO.builder()
                .officeId(officeId)
                .onlineSubmissionCount(dashboardService.getMonthlyAppealCountByOfficeIdAndMediumOfSubmissionAndYearAndMonth(officeId, MediumOfSubmission.ONLINE, year, month))
                .inheritedFromLastMonthCount(dashboardService.getAppealAscertainCountByOfficeIdAndYearAndMonth(officeId, year, month))
                .totalCount(totalSubmitted)
                .resolvedCount(resolvedCount)
                .runningCount(runningGrievanceCount)
                .timeExpiredCount(timeExpiredCount)
                .rate(rate)
                .build();
    }

    private MonthlyReportDTO generateGrievanceMonthlyReport(Long officeId, String year, String month) {
        Long totalSubmitted = dashboardService.countTotalComplaintsByOfficeIdAndYearAndMonth(officeId, year, month);
        Long resolvedCount = dashboardService.countResolvedComplaintsByOfficeIdAndYearAndMonth(officeId, year, month);
        Long timeExpiredCount = dashboardService.countTimeExpiredComplaintsByOfficeIdAndYearAndMonth(officeId, year, month);
        Long runningGrievanceCount = dashboardService.countRunningGrievancesByOfficeIdAndYearAndMonth(officeId, year, month);
        Long sentToOtherOfficeCount = dashboardService.countDeclinedGrievancesByOfficeIdAndYearAndMonth(officeId, year, month);
        Long ascertainCount = dashboardService.getGrievanceAscertainCountbyOfficeIdAndYearAndMonth(officeId, year, month);
        Long onlineSubmissionCount = dashboardService.getMonthlyComplaintsCountByOfficeIdAndMediumOfSubmissionAndYearAndMonth(officeId, MediumOfSubmission.ONLINE, year, month);
        Long conventionalSubmissionCount = dashboardService.getMonthlyComplaintsCountByOfficeIdAndMediumOfSubmissionAndYearAndMonth(officeId, MediumOfSubmission.CONVENTIONAL_METHOD, year, month);
        Long selfMotivatedSubmissionCount = dashboardService.getMonthlyComplaintsCountByOfficeIdAndMediumOfSubmissionAndYearAndMonth(officeId, MediumOfSubmission.SELF_MOTIVATED_ACCEPTANCE, year, month);
        Double rate = 0d;

        Long decidedTotal = resolvedCount + sentToOtherOfficeCount;
        if (totalSubmitted + ascertainCount > 0) {
            rate = (double) (((decidedTotal * 1.0) / (totalSubmitted + ascertainCount)) * 100);
            rate = (double) Math.round(rate * 100) / 100;
        }
        return MonthlyReportDTO.builder()
                .officeId(officeId)
                .onlineSubmissionCount(onlineSubmissionCount)
                .conventionalMethodSubmissionCount(conventionalSubmissionCount)
                .selfMotivatedAccusationCount(selfMotivatedSubmissionCount)
                .inheritedFromLastMonthCount(ascertainCount)
                .totalCount(totalSubmitted + ascertainCount)
                .sentToOtherCount(sentToOtherOfficeCount)
                .resolvedCount(resolvedCount)
                .runningCount(runningGrievanceCount)
                .timeExpiredCount(timeExpiredCount)
                .rate(rate)
                .build();
    }

    public SafetyNetSummaryResponse getSafetyNetSummary(UserInformation userInformation, Integer programId) {
        return dashboardService.getSafetyNetSummary(userInformation, programId);
    }
}
