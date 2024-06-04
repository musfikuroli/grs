package com.grs.core.dao;

import com.grs.api.model.OfficeInformation;
import com.grs.api.model.UserInformation;
import com.grs.api.model.response.dashboard.DashboardRatingDTO;
import com.grs.api.model.response.dashboard.latest.ComplaintsCountByTypeDTO;
import com.grs.api.model.response.dashboard.latest.GRSStatisticDTO;
import com.grs.api.model.response.dashboard.latest.SubOfficesStatisticsDTO;
import com.grs.api.model.response.reports.GrievanceAndAppealMonthlyReportDTO;
import com.grs.core.domain.MediumOfSubmission;
import com.grs.core.domain.grs.GrsStatistics;
import com.grs.core.domain.grs.MonthlyReport;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.service.*;
import com.grs.utils.BeanUtil;
import com.grs.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GRSStatisticsDAO {
    @Autowired
    private DashboardDataDAO dashboardDataDAO;
    @Autowired
    private OfficeService officeService;

    @Autowired
    private ReportsService reportsService;
    @Autowired
    private OfficesGroService officesGroService;
    @Autowired
    private MessageService messageService;

    @Autowired
    private MonthlyReportDAO monthlyReport;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public GRSStatisticDTO getGRSStatistics(UserInformation userInformation, Long officeId, Integer year, Integer month) {
        Calendar calendar = Calendar.getInstance();
        int reportMonth = 12 * year + month;
        int currentMonth = 12 * calendar.get(Calendar.YEAR) + (calendar.get(Calendar.MONTH) + 1);
        long monthDiff = reportMonth - currentMonth;

        /*
        MonthlyReport mReport = monthlyReport.findByOfficeIdAndYearAndMonth(officeId, year, month);
        if (mReport == null) {


        }
        */
        GrievanceAndAppealMonthlyReportDTO reportDTO = GrievanceAndAppealMonthlyReportDTO.builder()
                .officeId(officeId)
                .year(year)
                .month(month)
                .monthlyGrievanceReport(reportsService.getGrievanceMonthlyReportForGenerate(officeId, monthDiff))
                .monthlyAppealReport(reportsService.getAppealMonthlyReportForGenerate(officeId, monthDiff))
                .build();
        MonthlyReport mReport = monthlyReport.convertToMonthlyReport(reportDTO);
        GRSStatisticDTO dto = new GRSStatisticDTO(mReport);
        this.aggregateAppealData(dto, officeId, monthDiff);
        return dto;

        //return getGRSStatistics(userInformation,officeId,year,month, false);
    }
    public GRSStatisticDTO getGRSStatistics(UserInformation userInformation, Long officeId, Integer year, Integer month, boolean fromController) {
        if(fromController) {
            log.info("===From controller office id:{}, year:{}, month:{}", officeId, year, month);
            GrsStatistics grsStatistics = dashboardDataDAO.findGrsDashboardStatistics(officeId, month, year);
            if(grsStatistics != null) {
                GRSStatisticDTO statisticDTO = new GRSStatisticDTO();
                statisticDTO.officeId = grsStatistics.getOfficeId();
                statisticDTO.year = grsStatistics.getYear();
                statisticDTO.month = grsStatistics.getMonth();
                statisticDTO.totalSubmittedGrievance = grsStatistics.getTotalSubmittedGrievance();
                statisticDTO.currentMonthAcceptance = grsStatistics.getCurrentMonthAcceptance();
                statisticDTO.ascertainOfLastMonth = grsStatistics.getAscertainOfLastMonth();
                statisticDTO.runningGrievances = grsStatistics.getRunningGrievances();
                statisticDTO.forwardedGrievances = grsStatistics.getForwardedGrievances();
                statisticDTO.timeExpiredGrievances = grsStatistics.getTimeExpiredGrievances();
                statisticDTO.resolvedGrievances = grsStatistics.getResolvedGrievances();
                statisticDTO.resolveRate = grsStatistics.getResolveRate();
                statisticDTO.rateOfAppealedGrievance = grsStatistics.getRateOfAppealedGrievance();
                statisticDTO.totalRating = grsStatistics.getTotalRating();
                statisticDTO.averageRating = grsStatistics.getAverageRating();
                statisticDTO.appealTotal = grsStatistics.getAppealTotal();
                statisticDTO.appealCurrentMonthAcceptance = grsStatistics.getAppealCurrentMonthAcceptance();
                statisticDTO.appealAscertain = grsStatistics.getAppealAscertain();
                statisticDTO.appealRunning = grsStatistics.getAppealRunning();
                statisticDTO.appealTimeExpired = grsStatistics.getAppealTimeExpired();
                statisticDTO.appealResolved = grsStatistics.getAppealResolved();
                statisticDTO.appealResolveRate = grsStatistics.getAppealResolveRate();
                statisticDTO.subOfficesTotalGrievance = grsStatistics.getSubOfficesTotalGrievance();
                statisticDTO.subOfficesTimeExpiredGrievance = grsStatistics.getSubOfficesTimeExpiredGrievance();
                statisticDTO.subOfficesResolvedGrievance = grsStatistics.getSubOfficesResolvedGrievance();
                statisticDTO.subOfficesTotalAppeal = grsStatistics.getSubOfficesTotalAppeal();
                statisticDTO.subOfficesTimeExpiredAppeal = grsStatistics.getSubOfficesTimeExpiredAppeal();
                statisticDTO.subOfficesResolvedAppeal = grsStatistics.getSubOfficesResolvedAppeal();
                statisticDTO.subOfficesGrievanceResolveRate = grsStatistics.getSubOfficesGrievanceResolveRate();
                statisticDTO.subOfficesAppealResolveRate = grsStatistics.getSubOfficesAppealResolveRate();
                return statisticDTO;
            }
        }
        boolean hasAoAccess = true;
        if(fromController) {
            hasAoAccess = officeService.hasAccessToAoAndSubOfficesDashboard(userInformation, officeId);
        }

        GRSStatisticDTO grsStatisticDTO = new GRSStatisticDTO();
        Calendar calendar = Calendar.getInstance();
        int reportMonth = 12 * year + month;
        int currentMonth = 12 * calendar.get(Calendar.YEAR) + (calendar.get(Calendar.MONTH) + 1);
        long monthDiff = reportMonth - currentMonth;

        grsStatisticDTO.officeId = officeId;
        grsStatisticDTO.year = year;
        grsStatisticDTO.month = month;
        grsStatisticDTO.totalSubmittedGrievance = dashboardDataDAO.countTotalComplaintsByOfficeIdV2(officeId, monthDiff);
        grsStatisticDTO.currentMonthAcceptance = dashboardDataDAO.countComplaintsByOfficeOfAnyMediumOfSubmissionV2(officeId, monthDiff);
        grsStatisticDTO.ascertainOfLastMonth = dashboardDataDAO.getGrievanceAscertainCountOfPreviousMonthV2(officeId, monthDiff);
        grsStatisticDTO.runningGrievances = dashboardDataDAO.countRunningGrievancesByOfficeId(officeId, monthDiff);
        grsStatisticDTO.forwardedGrievances = dashboardDataDAO.countDeclinedGrievancesByOfficeId(officeId, monthDiff);
        grsStatisticDTO.timeExpiredGrievances = dashboardDataDAO.countTimeExpiredComplaintsByOfficeId(officeId);
        grsStatisticDTO.resolvedGrievances = dashboardDataDAO.countResolvedComplaintsByOfficeIdV2(officeId, monthDiff);
        long exactDecidedGrievances = grsStatisticDTO.resolvedGrievances + grsStatisticDTO.forwardedGrievances;
        if(grsStatisticDTO.totalSubmittedGrievance > 0) {
            float value =  ((float) exactDecidedGrievances / (float)grsStatisticDTO.totalSubmittedGrievance);
            grsStatisticDTO.resolveRate = Float.parseFloat(decimalFormat.format(value * 100)) ;
        }
        long appealFromThisOffice = dashboardDataDAO.countGrievancesAppealedFromThisOffice(officeId, monthDiff);
        if(grsStatisticDTO.totalSubmittedGrievance > 0) {
            float value =  ((float) appealFromThisOffice / (float) grsStatisticDTO.totalSubmittedGrievance);
            grsStatisticDTO.rateOfAppealedGrievance = Float.parseFloat(decimalFormat.format(value * 100)) ;
        }

        DashboardRatingDTO ratingDTO = dashboardDataDAO.countAvgRatingOfComplaintsByOfficeId(officeId);
        grsStatisticDTO.totalRating = ratingDTO.getTotal();
        grsStatisticDTO.averageRating = ratingDTO.getAverage().floatValue();

        if(hasAoAccess) {
            grsStatisticDTO.appealTotal = dashboardDataDAO.countTotalAppealsByOfficeIdV2(officeId, monthDiff);
            grsStatisticDTO.appealCurrentMonthAcceptance = dashboardDataDAO.countAppealsByOfficeAndMediumOfSubmissionV2(officeId, MediumOfSubmission.ONLINE, monthDiff);
            grsStatisticDTO.appealAscertain = dashboardDataDAO.getAppealsAscertainCountOfPreviousMonthV2(officeId, monthDiff);
            grsStatisticDTO.appealRunning = dashboardDataDAO.countAllRunningAppealsByOfficeId(officeId, monthDiff);
            grsStatisticDTO.appealTimeExpired = dashboardDataDAO.countAllTimeExpiredAppealsByOfficeId(officeId);
            grsStatisticDTO.appealResolved = dashboardDataDAO.countResolvedAppealsByOfficeIdV2(officeId, monthDiff);
            if(grsStatisticDTO.appealTotal > 0) {
                float value =  ((float)grsStatisticDTO.appealResolved / (float)grsStatisticDTO.appealTotal);
                grsStatisticDTO.appealResolveRate = Float.parseFloat(decimalFormat.format(value * 100)) ;
            }

            ComplaintsCountByTypeDTO aggregatedGrievanceData = getSubOfficesAggregatedDataForGrievances(officeId, monthDiff);
            grsStatisticDTO.subOfficesTotalGrievance = aggregatedGrievanceData.total;
            grsStatisticDTO.subOfficesTimeExpiredGrievance = aggregatedGrievanceData.expired;
            grsStatisticDTO.subOfficesResolvedGrievance = aggregatedGrievanceData.resolved;
            long exactDecidedSubOfficeGrievances = aggregatedGrievanceData.resolved + aggregatedGrievanceData.forwarded;
            if(aggregatedGrievanceData.total > 0) {
                float value =  ((float) exactDecidedSubOfficeGrievances / (float)aggregatedGrievanceData.total);
                grsStatisticDTO.subOfficesGrievanceResolveRate = Float.parseFloat(decimalFormat.format(value * 100)) ;
            }

            ComplaintsCountByTypeDTO aggregatedAppealData = getSubOfficesAggregatedDataForAppeals(officeId, monthDiff);
            grsStatisticDTO.subOfficesTotalAppeal = aggregatedAppealData.total;
            grsStatisticDTO.subOfficesTimeExpiredAppeal = aggregatedAppealData.expired;
            grsStatisticDTO.subOfficesResolvedAppeal = aggregatedAppealData.resolved;
            long exactDecidedSubOfficeAppeals = aggregatedAppealData.resolved + aggregatedAppealData.forwarded;
            if(aggregatedAppealData.total > 0) {
                float value =  ((float) exactDecidedSubOfficeAppeals / (float)aggregatedAppealData.total);
                grsStatisticDTO.subOfficesAppealResolveRate = Float.parseFloat(decimalFormat.format(value * 100)) ;
            }
        }
        return grsStatisticDTO;
    }

    private void aggregateAppealData(GRSStatisticDTO grsStatisticDTO, Long officeId, Long monthDiff) {
        ComplaintsCountByTypeDTO aggregatedGrievanceData = getSubOfficesAggregatedDataForGrievances(officeId, monthDiff);
        grsStatisticDTO.subOfficesTotalGrievance = aggregatedGrievanceData.total;
        grsStatisticDTO.subOfficesTimeExpiredGrievance = aggregatedGrievanceData.expired;
        grsStatisticDTO.subOfficesResolvedGrievance = aggregatedGrievanceData.resolved;
        long exactDecidedSubOfficeGrievances = aggregatedGrievanceData.resolved + aggregatedGrievanceData.forwarded;
        if(aggregatedGrievanceData.total > 0) {
            float value =  ((float) exactDecidedSubOfficeGrievances / (float)aggregatedGrievanceData.total);
            grsStatisticDTO.subOfficesGrievanceResolveRate = Float.parseFloat(decimalFormat.format(value * 100)) ;
        }

        ComplaintsCountByTypeDTO aggregatedAppealData = getSubOfficesAggregatedDataForAppeals(officeId, monthDiff);
        grsStatisticDTO.subOfficesTotalAppeal = aggregatedAppealData.total;
        grsStatisticDTO.subOfficesTimeExpiredAppeal = aggregatedAppealData.expired;
        grsStatisticDTO.subOfficesResolvedAppeal = aggregatedAppealData.resolved;
        long exactDecidedSubOfficeAppeals = aggregatedAppealData.resolved + aggregatedAppealData.forwarded;
        if(aggregatedAppealData.total > 0) {
            float value =  ((float) exactDecidedSubOfficeAppeals / (float)aggregatedAppealData.total);
            grsStatisticDTO.subOfficesAppealResolveRate = Float.parseFloat(decimalFormat.format(value * 100)) ;
        }
    }

    public GRSStatisticDTO getGRSStatisticsOfCabinetCell() {
        GRSStatisticDTO grsStatisticDTO = new GRSStatisticDTO();
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        long officeId = 0;
        long monthDiff = 0;

        grsStatisticDTO.totalSubmittedGrievance = dashboardDataDAO.countTotalComplaintsByOfficeIdV2(officeId, monthDiff);
        grsStatisticDTO.currentMonthAcceptance = dashboardDataDAO.countComplaintsByOfficeOfAnyMediumOfSubmissionV2(officeId, monthDiff);
        grsStatisticDTO.ascertainOfLastMonth = dashboardDataDAO.getGrievanceAscertainCountOfPreviousMonth(officeId, monthDiff);
        grsStatisticDTO.runningGrievances = dashboardDataDAO.countRunningGrievancesByOfficeId(officeId, monthDiff);
        grsStatisticDTO.forwardedGrievances = dashboardDataDAO.countDeclinedGrievancesByOfficeId(officeId, monthDiff);
        grsStatisticDTO.timeExpiredGrievances = dashboardDataDAO.countTimeExpiredComplaintsByOfficeId(officeId);
        grsStatisticDTO.resolvedGrievances = dashboardDataDAO.countResolvedComplaintsByOfficeId(officeId, monthDiff);
        long exactDecidedGrievances = grsStatisticDTO.resolvedGrievances + grsStatisticDTO.forwardedGrievances;
        if(grsStatisticDTO.totalSubmittedGrievance > 0) {
            float value =  ((float) exactDecidedGrievances / (float)grsStatisticDTO.totalSubmittedGrievance);
            grsStatisticDTO.resolveRate = Float.valueOf(decimalFormat.format(value * 100)) ;
        }
        DashboardRatingDTO ratingDTO = dashboardDataDAO.countAvgRatingOfComplaintsByOfficeId(officeId);
        grsStatisticDTO.totalRating = ratingDTO.getTotal();
        grsStatisticDTO.averageRating = ratingDTO.getAverage().floatValue();

        grsStatisticDTO.appealTotal = dashboardDataDAO.countTotalAppealsByOfficeIdV2(officeId, monthDiff);
        grsStatisticDTO.appealCurrentMonthAcceptance = dashboardDataDAO.countAppealsByOfficeAndMediumOfSubmissionV2(officeId, MediumOfSubmission.ONLINE, monthDiff);
        grsStatisticDTO.appealAscertain = dashboardDataDAO.getAppealsAscertainCountOfPreviousMonthV2(officeId, monthDiff);
        grsStatisticDTO.appealRunning = dashboardDataDAO.countAllRunningAppealsByOfficeId(officeId, monthDiff);
        grsStatisticDTO.appealTimeExpired = dashboardDataDAO.countAllTimeExpiredAppealsByOfficeId(officeId);
        grsStatisticDTO.appealResolved = dashboardDataDAO.countResolvedAppealsByOfficeIdV2(officeId, monthDiff);
        if(grsStatisticDTO.appealTotal > 0) {
            float value =  ((float) grsStatisticDTO.appealResolved / (float)grsStatisticDTO.appealTotal);
            grsStatisticDTO.appealResolveRate = Float.valueOf(decimalFormat.format(value * 100)) ;
        }
        return grsStatisticDTO;
    }

    public ComplaintsCountByTypeDTO getSubOfficesAggregatedDataForGrievances(Long officeId, Long monthDiff) {
        if(officeService.getChildCountByParentOfficeId(officeId) == 0 || officeService.isUpazilaLevelOffice(officeId)) {
            return new ComplaintsCountByTypeDTO();
        } else {
            ComplaintsCountByTypeDTO aggregatedData = new ComplaintsCountByTypeDTO();
            List<Office> offices = officeService.getOfficesByParentOfficeId(officeId);
            List<Long> officeIdList = offices.stream()
                    .map(Office::getId)
                    .collect(Collectors.toList());
            List<Long> grsEnabledChildOfficeIdList = officesGroService.getGRSEnabledOfficeIdFromOfficeIdList(officeIdList);
            for (Long id : grsEnabledChildOfficeIdList) {
                ComplaintsCountByTypeDTO subAggregatedData = getSubOfficesAggregatedDataForGrievances(id, monthDiff);
                aggregatedData.total += (dashboardDataDAO.countTotalComplaintsByOfficeIdV2(id, monthDiff) + subAggregatedData.total);
                aggregatedData.resolved += (dashboardDataDAO.countResolvedComplaintsByOfficeId(id, monthDiff) + subAggregatedData.resolved);
                aggregatedData.forwarded += (dashboardDataDAO.countDeclinedGrievancesByOfficeId(id, monthDiff) + subAggregatedData.forwarded);
                aggregatedData.running += (dashboardDataDAO.countRunningGrievancesByOfficeId(id, monthDiff) + subAggregatedData.running);
                aggregatedData.expired += (dashboardDataDAO.countTimeExpiredComplaintsByOfficeId(id) + subAggregatedData.expired);
            }
            return aggregatedData;
        }
    }

    public ComplaintsCountByTypeDTO getSubOfficesAggregatedDataForAppeals(Long officeId, Long monthDiff) {
        if(officeService.getChildCountByParentOfficeId(officeId) > 0 && !officeService.isZilaLevelOffice(officeId)) {
            ComplaintsCountByTypeDTO aggregatedData = new ComplaintsCountByTypeDTO();
            List<Office> offices = officeService.getOfficesByParentOfficeId(officeId);
            List<Long> officeIdList = offices.stream()
                    .map(Office::getId)
                    .collect(Collectors.toList());
            List<Long> grsEnabledChildOfficeIdList = officesGroService.getGRSEnabledOfficeIdFromOfficeIdList(officeIdList);
            for (Long id : grsEnabledChildOfficeIdList) {
                ComplaintsCountByTypeDTO subAggregatedData = getSubOfficesAggregatedDataForAppeals(id, monthDiff);
                aggregatedData.total += (dashboardDataDAO.countTotalAppealsByOfficeIdV2(id, monthDiff) + subAggregatedData.total);
                aggregatedData.resolved += (dashboardDataDAO.countResolvedAppealsByOfficeIdV2(id, monthDiff) + subAggregatedData.resolved);
                aggregatedData.forwarded += (dashboardDataDAO.countDeclinedAppealsByOfficeId(id) + subAggregatedData.forwarded);
                aggregatedData.running += (dashboardDataDAO.countAllRunningAppealsByOfficeId(id, monthDiff) + subAggregatedData.running);
                aggregatedData.expired += (dashboardDataDAO.countAllTimeExpiredAppealsByOfficeId(id) + subAggregatedData.expired);
            }
            return aggregatedData;
        } else {
            return new ComplaintsCountByTypeDTO();
        }
    }

    public GRSStatisticDTO getGRSStatisticsSampleTestData(long officeId) {
        GRSStatisticDTO grsStatisticDTO = new GRSStatisticDTO();
        grsStatisticDTO.officeId = 412;
        grsStatisticDTO.year = 2018;
        grsStatisticDTO.month = 11;
        grsStatisticDTO.totalSubmittedGrievance = 150;
        grsStatisticDTO.currentMonthAcceptance = 70;
        grsStatisticDTO.ascertainOfLastMonth = 80;
        grsStatisticDTO.runningGrievances = 60;
        grsStatisticDTO.forwardedGrievances = 30;
        grsStatisticDTO.timeExpiredGrievances = 30;
        grsStatisticDTO.resolvedGrievances = 30;
        grsStatisticDTO.resolveRate = 25.25f;
        grsStatisticDTO.rateOfAppealedGrievance = 40.00f;
        grsStatisticDTO.totalRating = 30;
        grsStatisticDTO.averageRating = 4.63f;

        grsStatisticDTO.appealTotal = 50;
        grsStatisticDTO.appealCurrentMonthAcceptance = 30;
        grsStatisticDTO.appealAscertain = 20;
        grsStatisticDTO.appealRunning = 20;
        grsStatisticDTO.appealTimeExpired = 10;
        grsStatisticDTO.appealResolved = 20;
        grsStatisticDTO.appealResolveRate = 50.55f;

        grsStatisticDTO.subOfficesTotalGrievance = 1200;
        grsStatisticDTO.subOfficesTimeExpiredGrievance = 300;
        grsStatisticDTO.subOfficesResolvedGrievance = 900;
        grsStatisticDTO.subOfficesTotalAppeal = 500;
        grsStatisticDTO.subOfficesTimeExpiredAppeal = 100;
        grsStatisticDTO.subOfficesResolvedAppeal = 400;
        grsStatisticDTO.subOfficesGrievanceResolveRate = 60.66f;
        grsStatisticDTO.subOfficesAppealResolveRate = 30.55f;

        return grsStatisticDTO;
    }

    public List<SubOfficesStatisticsDTO> getCurrentMonthChildOfficesStatistics(Long officeId) {
        if (officeService.getChildCountByParentOfficeId(officeId) == 0 || officeService.isUpazilaLevelOffice(officeId)) {
            return new ArrayList();
        }
        List<Office> offices = officeService.getOfficesByParentOfficeId(officeId);
        List<Long> officeIdList = offices.stream()
                .map(Office::getId)
                .collect(Collectors.toList());
        List<Long> grsEnabledOfficeIdList = officesGroService.getGRSEnabledOfficeIdFromOfficeIdList(officeIdList);
        List<SubOfficesStatisticsDTO> subOfficesStatisticsList = new ArrayList();
        if(offices.size() > 0) {
            Boolean isEnglish = messageService.isCurrentLanguageInEnglish();
            offices.stream().forEach(office -> {
                Long id = office.getId();
                if (grsEnabledOfficeIdList.contains(id)) {
                    SubOfficesStatisticsDTO subOfficesStatisticsDTO = new SubOfficesStatisticsDTO();
                    subOfficesStatisticsDTO.officeId = office.getId();
                    subOfficesStatisticsDTO.officeName = isEnglish ? office.getNameEnglish() : office.getNameBangla();
                    subOfficesStatisticsDTO.totalGrievances = dashboardDataDAO.countTotalComplaintsByOfficeIdV2(id, 0L);
                    subOfficesStatisticsDTO.forwardedGrievances = dashboardDataDAO.countDeclinedGrievancesByOfficeId(id, 0L);
                    subOfficesStatisticsDTO.resolvedGrievances = dashboardDataDAO.countResolvedComplaintsByOfficeId(id, 0L);
                    subOfficesStatisticsDTO.timeExpiredGrievances = dashboardDataDAO.countTimeExpiredComplaintsByOfficeId(id);
                    subOfficesStatisticsList.add(subOfficesStatisticsDTO);
                }
            });
        }
        return subOfficesStatisticsList;
    }

    public List<SubOfficesStatisticsDTO> getOfficesStatisticsForFieldCoordinator(Authentication authentication) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        OfficeInformation officeInformation = userInformation.getOfficeInformation();
        List<Office> offices = new ArrayList();
        if(Utility.isDivisionLevelFC(authentication)) {
            offices = officeService.getGrsEnabledDivisionOffices(officeInformation.getGeoDivisionId());
        } else if(Utility.isDistrictLevelFC(authentication)) {
            offices = officeService.getGrsEnabledDistrictOffices(officeInformation.getGeoDistrictId());
        }
        List<SubOfficesStatisticsDTO> fieldCoordinatorOfficesStatistics = new ArrayList();
        if(offices.size() > 0) {
            Boolean isEnglish = messageService.isCurrentLanguageInEnglish();
            offices.stream().forEach(office -> {
                Long id = office.getId();
                SubOfficesStatisticsDTO statisticsDTO = new SubOfficesStatisticsDTO();
                statisticsDTO.officeId = office.getId();
                statisticsDTO.officeName = isEnglish ? office.getNameEnglish() : office.getNameBangla();
                statisticsDTO.totalGrievances = dashboardDataDAO.countTotalComplaintsByOfficeIdV2(id, 0L);
                statisticsDTO.forwardedGrievances = dashboardDataDAO.countDeclinedGrievancesByOfficeId(id, 0L);
                statisticsDTO.resolvedGrievances = dashboardDataDAO.countResolvedComplaintsByOfficeId(id, 0L);
                statisticsDTO.timeExpiredGrievances = dashboardDataDAO.countTimeExpiredComplaintsByOfficeId(id);
                fieldCoordinatorOfficesStatistics.add(statisticsDTO);
            });
        }
        return fieldCoordinatorOfficesStatistics;
    }

    public ComplaintsCountByTypeDTO getDivisionAppealStatisticsForFieldCoordinator(UserInformation userInformation) {
        OfficeInformation officeInformation = userInformation.getOfficeInformation();
        Long officeId = officeInformation.getOfficeId();
        List<Office> offices = officeService.getGrsEnabledDivisionOffices(officeInformation.getGeoDivisionId());
        ComplaintsCountByTypeDTO aggregatedAppealStatistics = new ComplaintsCountByTypeDTO();
        if(offices.size() > 0) {
            offices.stream().forEach(office -> {
                aggregatedAppealStatistics.total += dashboardDataDAO.countTotalComplaintsByOfficeIdV2(officeId, 0L);
                aggregatedAppealStatistics.resolved += dashboardDataDAO.countResolvedComplaintsByOfficeId(officeId, 0L);
                aggregatedAppealStatistics.expired += dashboardDataDAO.countTimeExpiredComplaintsByOfficeId(officeId);
            });
        }
        return aggregatedAppealStatistics;
    }
}
