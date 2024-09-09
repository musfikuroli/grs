package com.grs.core.dao;

import com.grs.api.model.SafetyNetProgramReportResponse;
import com.grs.api.model.SafetyNetSummaryResponse;
import com.grs.api.model.response.dashboard.DashboardRatingDTO;
import com.grs.core.domain.GrievanceCountByService;
import com.grs.core.domain.GrievanceCurrentStatus;
import com.grs.core.domain.MediumOfSubmission;
import com.grs.core.domain.grs.DashboardData;
import com.grs.core.domain.grs.GrsStatistics;
import com.grs.core.domain.grs.YearlyDashboardStatistics;
import com.grs.core.repo.grs.BaseEntityManager;
import com.grs.core.repo.grs.DashboardDataRepo;
import com.grs.core.repo.grs.GrsStatisticsRepo;
import com.grs.core.repo.grs.YearlyDashboardStatisticsRepo;
import com.grs.utils.CalendarUtil;
import com.grs.utils.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DashboardDataDAO {
    @Autowired
    private DashboardDataRepo dashboardDataRepo;

    @Autowired
    private YearlyDashboardStatisticsRepo yearlyRepo;

    @Autowired
    private GrsStatisticsRepo grsStatisticsRepo;

    @Autowired
    private BaseEntityManager baseEntityManager;

    public DashboardData findOne(Long id) {
        return dashboardDataRepo.findOne(id);
    }

    public boolean delete(DashboardData dashboardData) {
        try {
            dashboardDataRepo.delete(dashboardData);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public DashboardData findByOfficeIdAndGrievanceId(Long officeId, Long grievanceId) {
        List<DashboardData> dashboardDataList = dashboardDataRepo.findNormalGrievanceByOfficeIdAndGrievanceId(officeId, grievanceId);
        if (!dashboardDataList.isEmpty()) return dashboardDataList.get(0);
        return null;
    }

    public DashboardData findAppealByOfficeIdAndGrievanceId(Long officeId, Long grievanceId) {
        return dashboardDataRepo.findAppealByOfficeIdAndGrievanceId(officeId, grievanceId);
    }

    public DashboardData findTopByOfficeIdAndGrievanceId(Long officeId, Long grievanceId) {
        return this.dashboardDataRepo.findTopByOfficeIdAndGrievanceId(officeId, grievanceId);
    }

    public DashboardData save(DashboardData dashboardData) {
        return dashboardDataRepo.save(dashboardData);
    }

    public Long countTotalComplaintsByOfficeIdV2(Long officeId, Long monthDiff) {
        return dashboardDataRepo.countTotalComplaintsByOfficeIdV2(officeId, monthDiff);
    }

    public Long countTotalComplaintsByOfficeIdsV2(List<Long> officeId, Long monthDiff) {
        return dashboardDataRepo.countTotalComplaintsByOfficeIdsV2(officeId, monthDiff);
    }

    public Long countResolvedComplaintsByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataRepo.countResolvedGrievancesByOfficeId(officeId, monthDiff);
    }

    public Long countResolvedComplaintsByOfficeIds(List<Long> officeId, Long monthDiff) {
        return dashboardDataRepo.countResolvedGrievancesByOfficeIds(officeId, monthDiff);
    }

    public Long countTotalComplaintsByOfficeIdListV2(List<Long> officeIds, Long monthDiff) {
        return dashboardDataRepo.countTotalComplaintsByOfficeIdV2(officeIds, monthDiff);
    }

    public Long countResolvedComplaintsByOfficeIdList(List<Long> officeIds, Long monthDiff) {
        return dashboardDataRepo.countResolvedGrievancesByOfficeId(officeIds, monthDiff);
    }

    public Long countResolvedComplaintsByOfficeIdV2(Long officeId, Long monthDiff) {
        return dashboardDataRepo.countResolvedGrievancesByOfficeIdV2(officeId, monthDiff);
    }

    public Long countTimeExpiredComplaintsByOfficeId(Long officeId) {
        return dashboardDataRepo.countTimeExpiredGrievancesByOfficeId(officeId, CalendarUtil.getWorkDaysCountBefore(new Date(), (int) Constant.GRIEVANCE_EXPIRATION_TIME));
    }

    public Long countTimeExpiredComplaintsByOfficeIds(List<Long> officeId) {
        return dashboardDataRepo.countTimeExpiredGrievancesByOfficeIds(officeId, CalendarUtil.getWorkDaysCountBefore(new Date(), (int) Constant.GRIEVANCE_EXPIRATION_TIME));
    }

    public Long countTimeExpiredComplaintsByOfficeIdList(List<Long> officeIds) {
        return dashboardDataRepo.countTimeExpiredGrievancesByOfficeId(officeIds, CalendarUtil.getWorkDaysCountBefore(new Date(), (int) Constant.GRIEVANCE_EXPIRATION_TIME));
    }

    public Long countTimeExpiredComplaintsByOfficeIdList(Date fromDate, List<Long> officeIds) {
        return dashboardDataRepo.countTimeExpiredGrievancesByOfficeId(officeIds, CalendarUtil.getWorkDaysCountBefore(fromDate, (int) Constant.GRIEVANCE_EXPIRATION_TIME), fromDate);
    }

    public Long countTimeExpiredComplaintsByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataRepo.countTimeExpiredGrievancesByOfficeId(officeId, CalendarUtil.getWorkDaysCountBefore(new Date(), (int) Constant.GRIEVANCE_EXPIRATION_TIME));
    }

    public Long countAllTimeExpiredComplaintsByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataRepo.countAllTimeExpiredComplaintsByOfficeId(officeId, CalendarUtil.getWorkDaysCountBefore(new Date(), (int) Constant.GRIEVANCE_EXPIRATION_TIME));
    }

    public Long countTimeExpiredComplaintsByOfficeIdV2(Long officeId, Long monthDiff, Long numberOfDays) {
        return dashboardDataRepo.countTimeExpiredGrievancesByOfficeIdV2(officeId, monthDiff, numberOfDays);
    }

    public Long countRunningGrievancesByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataRepo.countRunningGrievancesByOfficeId(officeId, monthDiff, -CalendarUtil.getWorkDaysCountBefore(new Date(), (int) Constant.GRIEVANCE_EXPIRATION_TIME));
    }

    public Long countRunningGrievancesByOfficeIds(List<Long> officeId, Long monthDiff) {
        return dashboardDataRepo.countRunningGrievancesByOfficeIds(officeId, monthDiff, -CalendarUtil.getWorkDaysCountBefore(new Date(), (int) Constant.GRIEVANCE_EXPIRATION_TIME));
    }

    public Long countRunningGrievancesByOfficeIdList(List<Long> officeIds, Long monthDiff) {
        return dashboardDataRepo.countRunningGrievancesByOfficeId(officeIds, monthDiff, -CalendarUtil.getWorkDaysCountBefore(new Date(), (int) Constant.GRIEVANCE_EXPIRATION_TIME));
    }

    public Long countAllRunningGrievancesByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataRepo.countAllRunningGrievancesByOfficeId(officeId, monthDiff, -CalendarUtil.getWorkDaysCountBefore(new Date(), (int) Constant.GRIEVANCE_EXPIRATION_TIME));
    }

    public Long countRunningGrievancesByOfficeIdV2(Long officeId, Long monthDiff) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, monthDiff.intValue());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return dashboardDataRepo.countRunningGrievancesByOfficeIdV2(officeId, monthDiff);
    }

    public Long countInheritedComplaintsByOfficeId(Long officeId, Long monthDiff) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, monthDiff.intValue());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return dashboardDataRepo.countInheritedComplaintsByOfficeId(officeId, monthDiff, monthDiff-1);
    }

    public Long countDeclinedGrievancesByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataRepo.countDeclinedGrievancesByOfficeId(officeId, monthDiff);
    }

    public Long countDeclinedGrievancesByOfficeIds(List<Long> officeId, Long monthDiff) {
        return dashboardDataRepo.countDeclinedGrievancesByOfficeIds(officeId, monthDiff);
    }

    public Long countDeclinedGrievancesByOfficeIdList(List<Long> officeIds, Long monthDiff) {
        return dashboardDataRepo.countDeclinedGrievancesByOfficeId(officeIds, monthDiff);
    }

    public Long countForwardedGrievancesByOfficeIdV2(Long officeId, Long monthDiff) {
        return dashboardDataRepo.countForwardedGrievancesByOfficeIdV2(officeId, monthDiff);
    }

    public List<GrievanceCountByService> getListOfGrievanceCountByServiceId(Long officeId) {
        return this.dashboardDataRepo.getListOfGrievanceCountByServiceId(officeId, 0L);
    }

    public DashboardRatingDTO convertToDashboardRatingDTO(Object ratingData) {
        Object[] objectArray = (Object[]) ratingData;
        Double average = 0.0;
        Long total = 0L;
        if (objectArray != null) {
            total = ((BigInteger) objectArray[1]).longValue();
            average = objectArray[0] != null ? ((Double) objectArray[0]) : 0.0;
        }
        return DashboardRatingDTO.builder()
                .average(average)
                .total(total)
                .build();
    }

    public DashboardRatingDTO countAvgRatingOfComplaintsByOfficeId(Long officeId) {
        return convertToDashboardRatingDTO(dashboardDataRepo.countAvgRatingOfComplaintsByOfficeId(officeId, 0L));
    }

    public List getListOfGrievanceCountByOfficeUnitId(Long officeId) {
        return this.dashboardDataRepo.getListOfGrievanceCountByOfficeUnitId(officeId, 0L);
    }

    public Long countAcceptedGrievancesByOfficeIdAndMonthDiff(Long officeId) {
        return dashboardDataRepo.countAcceptedGrievancesByOfficeIdAndMonthDiff(officeId, 0L);
    }

    public Long countResolvedGrievancesByOfficeIdAndIsReal(Long officeId) {
        return dashboardDataRepo.countResolvedGrievancesByOfficeIdAndIsReal(officeId, 0L);
    }

    public Long countResolvedGrievancesByOfficeIdAndIsNotReal(Long officeId) {
        return dashboardDataRepo.countResolvedGrievancesByOfficeIdAndIsNotReal(officeId, 0L);
    }

    public Long countComplaintsByOfficeIdAndStatus(Long officeId, GrievanceCurrentStatus grievanceCurrentStatus) {
        return dashboardDataRepo.countComplaintsByOfficeIdAndStatus(officeId, grievanceCurrentStatus.name());
    }

    public List<DashboardData> getResolvedGrievancesOfCurrentMonthByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataRepo.getResolvedGrievancesOfCurrentMonthByOfficeId(officeId, monthDiff);
    }

    public List<DashboardData> getTimeExpiredGrievancesByOfficeId(Long officeId) {
        return dashboardDataRepo.getTimeExpiredGrievancesByOfficeId(officeId, CalendarUtil.getWorkDaysCountBefore(new Date(), (int) Constant.GRIEVANCE_EXPIRATION_TIME));
    }

    public Long countComplaintsByOfficeAndMediumOfSubmission(Long officeId, MediumOfSubmission mediumOfSubmission, Long monthDiff) {
        return dashboardDataRepo.countComplaintsByOfficeAndMediumOfSubmission(officeId, mediumOfSubmission.name(), monthDiff);
    }

    public Long countComplaintsByOfficeOfAnyMediumOfSubmissionV2(Long officeId, Long monthDiff) {
        return dashboardDataRepo.countComplaintsByOfficeOfAnyMediumOfSubmissionV2(officeId, monthDiff);
    }

    public List getTotalAndResolvedGrievanceCountWithTypeByMonthAndYear(Long officeId, Integer monthDiff) {
        return dashboardDataRepo.getTotalAndResolvedGrievanceCountWithTypeByMonthAndYear(officeId, monthDiff);
    }

    public Long getGrievanceAscertainCountOfPreviousMonth(Long officeId, Long monthDiff) {
        return dashboardDataRepo.getGrievanceAscertainCountOfPreviousMonth(officeId, monthDiff);
    }

    public Long getGrievanceAscertainCountOfPreviousMonthV2(Long officeId, Long monthDiff) {
        return dashboardDataRepo.getGrievanceAscertainCountOfPreviousMonthV2(officeId, monthDiff );
    }

    public List getIdsOfGrievancesContainRatingInCurrentMonth(Long officeId) {
        return dashboardDataRepo.getIdsOfGrievancesContainRatingInCurrentMonth(officeId);
    }

    public Long countGrievancesAppealedFromThisOffice(Long officeId, Long monthDiff) {
        return dashboardDataRepo.countGrievancesAppealedFromThisOffice(officeId, monthDiff);
    }

    /************* APPEAL SECTION *******************/

    public Long countTotalAppealsByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataRepo.countTotalAppealsByOfficeId(officeId, monthDiff);
    }

    public Long countTotalAppealsByOfficeIds(List<Long> officeIds, Long monthDiff) {
        return dashboardDataRepo.countTotalAppealsByOfficeIds(officeIds, monthDiff);
    }

    public Long countTotalAppealsByOfficeIdV2(Long officeId, Long monthDiff) {
        return dashboardDataRepo.countTotalAppealsByOfficeIdV2(officeId, monthDiff);
    }

    public Long countResolvedAppealsByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataRepo.countResolvedAppealsByOfficeId(officeId, monthDiff);
    }

    public Long countResolvedAppealsByOfficeIds(List<Long> officeId, Long monthDiff) {
        return dashboardDataRepo.countResolvedAppealsByOfficeIds(officeId, monthDiff);
    }

    public Long countResolvedAppealsByOfficeIdV2(Long officeId, Long monthDiff) {
        return dashboardDataRepo.countResolvedAppealsByOfficeIdV2(officeId, monthDiff);
    }

    public Long countDeclinedAppealsByOfficeId(Long officeId) {
        return dashboardDataRepo.countDeclinedAppealsByOfficeId(officeId);
    }

    public Long countTimeExpiredAppealsByOfficeId(Long officeId) {
        return dashboardDataRepo.countTimeExpiredAppealsByOfficeId(officeId, Constant.APPEAL_EXPIRATION_TIME);
    }

    public Long countAllTimeExpiredAppealsByOfficeId(Long officeId) {
        return dashboardDataRepo.countAllTimeExpiredAppealsByOfficeId(officeId, Constant.APPEAL_EXPIRATION_TIME);
    }

    public Long countAllTimeExpiredAppealsByOfficeIds(List<Long> officeId) {
        return dashboardDataRepo.countAllTimeExpiredAppealsByOfficeIds(officeId, Constant.APPEAL_EXPIRATION_TIME);
    }

    public Long countTimeExpiredAppealsByOfficeIdV2(Long officeId, Long monthDiff) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, monthDiff.intValue());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Long days = CalendarUtil.getWorkDaysCountBefore(calendar.getTime(), (int) Constant.GRIEVANCE_EXPIRATION_TIME);

        return dashboardDataRepo.countTimeExpiredAppealsByOfficeIdV2(officeId, monthDiff, days);
    }

    public Long countRunningAppealsByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataRepo.countRunningAppealsByOfficeId(officeId, monthDiff, -CalendarUtil.getWorkDaysCountBefore(new Date(), (int) Constant.APPEAL_EXPIRATION_TIME));
    }

    public Long countAllRunningAppealsByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataRepo.countAllRunningAppealsByOfficeId(officeId, monthDiff, -CalendarUtil.getWorkDaysCountBefore(new Date(), (int) Constant.APPEAL_EXPIRATION_TIME));
    }

    public Long countAllRunningAppealsByOfficeIds(List<Long> officeId, Long monthDiff) {
        return dashboardDataRepo.countAllRunningAppealsByOfficeIds(officeId, monthDiff, -CalendarUtil.getWorkDaysCountBefore(new Date(), (int) Constant.APPEAL_EXPIRATION_TIME));
    }

    public Long countRunningAppealsByOfficeIdV2(Long officeId, Long monthDiff) {
        return dashboardDataRepo.countRunningAppealsByOfficeIdV2(officeId, monthDiff);
    }

    public Long countAppealsByOfficeAndMediumOfSubmission(Long officeId, MediumOfSubmission mediumOfSubmission, Long monthDiff) {
        return dashboardDataRepo.countAppealsByOfficeAndMediumOfSubmission(officeId, mediumOfSubmission.name(), monthDiff);
    }

    public Long countAppealsByOfficeAndMediumOfSubmissionV2(Long officeId, MediumOfSubmission mediumOfSubmission, Long monthDiff) {
        return dashboardDataRepo.countAppealsByOfficeAndMediumOfSubmissionV2(officeId, mediumOfSubmission.name(), monthDiff);
    }

    public Long countDailyAppealsByOfficeAndMediumOfSubmission(Long officeId, MediumOfSubmission mediumOfSubmission, Long dayDiff) {
        return dashboardDataRepo.countDailyAppealsByOfficeAndMediumOfSubmission(officeId, mediumOfSubmission.name(), dayDiff);
    }

    public List<DashboardData> getResolvedAppealsOfCurrentMonthByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataRepo.getResolvedAppealsOfCurrentMonthByOfficeId(officeId, monthDiff);
    }

    public List<DashboardData> getTimeExpiredAppealsByOfficeId(Long officeId) {
        return dashboardDataRepo.getTimeExpiredAppealsByOfficeId(officeId, CalendarUtil.getWorkDaysCountBefore(new Date(), (int) Constant.APPEAL_EXPIRATION_TIME));
    }

    public List getCountOfAppealsBySourceOffices(Long officeId) {
        return dashboardDataRepo.getCountOfAppealsBySourceOffices(officeId, 0L);
    }

    public List getTotalAndResolvedAppealCountWithTypeByMonthAndYear(Long officeId, Integer monthDiff) {
        return dashboardDataRepo.getTotalAndResolvedAppealCountWithTypeByMonthAndYear(officeId, monthDiff);
    }

    public Long getAppealsAscertainCountOfPreviousMonth(Long officeId, Long monthDiff) {
        return dashboardDataRepo.getAppealsAscertainCountOfPreviousMonth(officeId, monthDiff);
    }

    public Long getAppealsAscertainCountOfPreviousMonthV2(Long officeId, Long monthDiff) {
        return dashboardDataRepo.getAppealsAscertainCountOfPreviousMonthV2(officeId, monthDiff - 1);
    }

    public Long getAppealsAscertainCountOfPreviousDay(Long officeId, Long dayDiff) {
        return dashboardDataRepo.getAppealsAscertainCountOfPreviousDay(officeId, dayDiff - 1);
    }

    public DashboardRatingDTO countAvgRatingOfAppealsByOfficeId(Long officeId) {
        return convertToDashboardRatingDTO(dashboardDataRepo.countAvgRatingOfAppealsByOfficeId(officeId, 0L));
    }

    public List getIdsOfAppealsContainRatingInCurrentMonth(Long officeId) {
        return dashboardDataRepo.getIdsOfAppealsContainRatingInCurrentMonth(officeId);
    }

    /************* CENTRAL DASHBOARD SECTION *******************/

    public Long countTotalGrievances(Long monthDiff) {
        return dashboardDataRepo.countTotalGrievances(monthDiff);
    }

    public Long countGrievanceAscertainFromLastMonth(Long monthDiff) {
        return dashboardDataRepo.countGrievanceAscertainFromLastMonth(monthDiff);
    }

    public Long countGrievanceAscertainFromLastMonth(Date fromDate, Date toDate) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        calendar.add(Calendar.MONTH, -1);
        fromDate = calendar.getTime();

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.SECOND, -1);
        toDate = calendar.getTime();

        return dashboardDataRepo.countGrievanceAscertainFromLastMonth(fromDate, toDate);
    }

    public Long countTotalGrievancesYearly() {
        return dashboardDataRepo.countTotalGrievancesYearly();
    }

    public Long countTotalGrievances(Date fromDate, Date toDate) {
        return dashboardDataRepo.countTotalGrievances(fromDate, toDate);
    }

    public Long countForwardedGrievancesYearly() {
        return dashboardDataRepo.countForwardedGrievancesYearly();
    }

    public Long countForwardedGrievances(Date fromDate, Date toDate) {
        return dashboardDataRepo.countForwardedGrievances(fromDate, toDate);
    }

    public Long countResolvedGrievances(Long monthDiff) {
        return dashboardDataRepo.countResolvedGrievances(monthDiff);
    }

    public Long countResolvedGrievancesYearly() {
        return dashboardDataRepo.countResolvedGrievancesYearly();
    }

    public Long countResolvedGrievances(Date fromDate, Date toDate) {
        return dashboardDataRepo.countResolvedGrievances(fromDate, toDate);
    }

    public Object[] getCitizenCharterServicesByComplaintFrequency() {
        return dashboardDataRepo.getCitizenCharterServicesByComplaintFrequency(10);
    }

    public Object[] getCitizenCharterServicesByComplaintFrequency(Date fromDate, Date toDate) {
        return dashboardDataRepo.getCitizenCharterServicesByComplaintFrequency(fromDate,toDate, 10);
    }

    public Object[] getServiceCountWithOfficeNameByServiceId(Long monthDiff, Long serviceId) {
        return dashboardDataRepo.getServiceCountWithOfficeNameByServiceId(serviceId);
    }

    public Object[] getSubmittedCountByOfficeIdInList(List<Long> officeIds) {
        return dashboardDataRepo.getSubmittedCountByOfficeIdInList(0L, officeIds);
    }

    public Object[] getSubmittedCountByOfficeIdInList(Long monthDiff, List<Long> officeIds) {
        return dashboardDataRepo.getSubmittedCountByOfficeIdInList(monthDiff, officeIds);
    }

    public Object[] getResolvedCountByOfficeIdInList(List<Long> officeIds) {
        return dashboardDataRepo.getResolvedCountByOfficeIdInList(0L, officeIds);
    }

    public Object[] getResolvedCountByOfficeIdInList(Long monthDiff, List<Long> officeIds) {
        return dashboardDataRepo.getResolvedCountByOfficeIdInList(monthDiff, officeIds);
    }

    public Object[] getExpiredCountByOfficeIdInList(List<Long> officeIds) {
        return dashboardDataRepo.getExpiredCountByOfficeIdInList(officeIds, CalendarUtil.getWorkDaysCountBefore(new Date(), (int) Constant.GRIEVANCE_EXPIRATION_TIME));
    }

    public Object[] getExpiredCountByOfficeIdInList(Date fromDate, List<Long> officeIds) {
        return dashboardDataRepo.getExpiredCountByOfficeIdInList(officeIds, CalendarUtil.getWorkDaysCountBefore(fromDate, (int) Constant.GRIEVANCE_EXPIRATION_TIME), fromDate);
    }

    public List<DashboardData> getDashboardDataForCurrentMonthGrievanceRegister(Long monthDiff, Long officeId) {
        return dashboardDataRepo.getDashboardDataForCurrentMonthGrievanceRegister(monthDiff, officeId);
    }

    public List<DashboardData> getDashboardDataForCurrentMonthAppealRegister(Long monthDiff, Long officeId) {
        return dashboardDataRepo.getDashboardDataForCurrentMonthAppealRegister(monthDiff, officeId);
    }

    public DashboardData getDashboardDataForGrievancePhaseOfAppeal(Long grievanceId) {
        return dashboardDataRepo.findByGrievanceIdAndComplaintStatusNotAppeal(grievanceId);
    }

    public Page<DashboardData> getPageableDashboardDataForGrievanceRegister(Long officeId, Pageable pageable) {
        List<GrievanceCurrentStatus> nonAppealStatusList = new ArrayList();
        for (GrievanceCurrentStatus status : GrievanceCurrentStatus.values()) {
            if (!status.name().contains("APPEAL")) {
                nonAppealStatusList.add(status);
            }
        }
        return dashboardDataRepo.findByOfficeIdAndComplaintStatusInOrderByCreatedAtDesc(officeId, nonAppealStatusList, pageable);
    }

    public Page<DashboardData> getPageableDashboardDataForGrievanceRegisterByTrackingNumber(Long officeId, String trackingNumber, Pageable pageable) {
        // Filter out statuses containing "APPEAL"
        List<GrievanceCurrentStatus> nonAppealStatusList = new ArrayList<>();
        for (GrievanceCurrentStatus status : GrievanceCurrentStatus.values()) {
            if (!status.name().contains("APPEAL")) {
                nonAppealStatusList.add(status);
            }
        }

        // Call the repository method to find data by officeId, trackingNumber, and non-appeal statuses
        return dashboardDataRepo.findByOfficeIdAndTrackingNumberAndComplaintStatusInOrderByCreatedAtDesc(officeId, trackingNumber, nonAppealStatusList, pageable);
    }

    public Page<DashboardData> getPageableDashboardDataAppealRegister(Long officeId, Pageable pageable) {
        List<GrievanceCurrentStatus> nonAppealStatusList = new ArrayList();
        for (GrievanceCurrentStatus status : GrievanceCurrentStatus.values()) {
            if (status.name().contains("APPEAL")) {
                nonAppealStatusList.add(status);
            }
        }
        return dashboardDataRepo.findByOfficeIdAndComplaintStatusInOrderByCreatedAtDesc(officeId, nonAppealStatusList, pageable);
    }

    public Page<DashboardData> getPageableDashboardDataForAppealedComplaints(Long officeId, Pageable pageable) {
        List<DashboardData> dashboardDataList = dashboardDataRepo.findByAppealFromOfficeId(officeId);
        List<Long> grievanceIdList = dashboardDataList.stream().map(dashboardData -> {
            return dashboardData.getGrievanceId();
        }).collect(Collectors.toList());
        return dashboardDataRepo.findByOfficeIdAndGrievanceIdInAndAppealFromOfficeIdIsNullOrderByCreatedAtDesc(officeId, grievanceIdList, pageable);
    }

    public Long countTotalAppeals(Long monthDiff) {
        return dashboardDataRepo.countTotalAppeals(monthDiff);
    }

    public Long countTotalAppeals(Date fromDate, Date toDate) {
        return dashboardDataRepo.countTotalAppeals(fromDate, toDate);
    }

    public Long countResolvedAppeals(Long monthDIff) {
        return dashboardDataRepo.countResolvedAppeals(monthDIff);
    }

    public Long countResolvedAppeals(Date fromDate, Date toDate) {
        return dashboardDataRepo.countResolvedAppeals(fromDate, toDate);
    }

    public Long countTimeExpiredAppeals() {
        return dashboardDataRepo.countTimeExpiredAppeals(CalendarUtil.getWorkDaysCountBefore(new Date(), (int) Constant.APPEAL_EXPIRATION_TIME));
    }

    public Long countTimeExpiredAppeals(Date fromDate) {

        Long workDaysCountBefore = CalendarUtil.getWorkDaysCountBefore(fromDate, (int) Constant.APPEAL_EXPIRATION_TIME);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        calendar.add(Calendar.DAY_OF_MONTH, -workDaysCountBefore.intValue());
        fromDate = calendar.getTime();

        return dashboardDataRepo.countTimeExpiredAppeals(fromDate);
    }

    public Long countTImeExpiredComplaints() {
        return dashboardDataRepo.countTimeExpiredGrievances(CalendarUtil.getWorkDaysCountBefore(new Date(), (int) Constant.GRIEVANCE_EXPIRATION_TIME));
    }

    public Long countTImeExpiredComplaints(Date fromDate) {

        Long workDaysCountBefore = CalendarUtil.getWorkDaysCountBefore(fromDate, (int) Constant.GRIEVANCE_EXPIRATION_TIME);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        calendar.add(Calendar.DAY_OF_MONTH, -workDaysCountBefore.intValue());
        fromDate = calendar.getTime();

        return dashboardDataRepo.countTimeExpiredGrievances(fromDate);
    }

    public Long countTotalComplaintsByOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataRepo.countTotalComplaintsByOfficeIdAndYearAndMonth(officeId, year + "-" + month + "-01 00:00:00");
    }

    public Long countResolvedComplaintsByOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataRepo.countResolvedComplaintsByOfficeIdAndYearAndMonth(officeId, year + "-" + month + "-01 00:00:00");
    }

    public Long countTimeExpiredComplaintsByOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataRepo.countTimeExpiredComplaintsByOfficeIdAndYearAndMonth(officeId, year + "-" + month + "-01 00:00:00");
    }

    public Long countRunningGrievancesByOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataRepo.countRunningGrievancesByOfficeIdAndYearAndMonth(officeId, year + "-" + month + "-01 00:00:00");
    }

    public Long countDeclinedGrievancesByOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataRepo.countDeclinedGrievancesByOfficeIdAndYearAndMonth(officeId, year + "-" + month + "-01 00:00:00");
    }

    public Long getGrievanceAscertainCountbyOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataRepo.getGrievanceAscertainCountbyOfficeIdAndYearAndMonth(officeId, year + "-" + month + "-01 00:00:00");
    }

    public Long getMonthlyComplaintsCountByOfficeIdAndMediumOfSubmissionAndYearAndMonth(Long officeId, MediumOfSubmission mediumOfSubmission, String year, String month) {
        return this.dashboardDataRepo.getMonthlyComplaintsCountByOfficeIdAndMediumOfSubmissionAndYearAndMonth(officeId, mediumOfSubmission.name(), year + "-" + month + "-01 00:00:00");
    }

    public Long countResolvedAppealByOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataRepo.countResolvedAppealByOfficeIdAndYearAndMonth(officeId, year + "-" + month + "-01 00:00:00");
    }

    public Long countTimeExpiredAppealByOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataRepo.countTimeExpiredAppealByOfficeIdAndYearAndMonth(officeId, year + "-" + month + "-01 00:00:00");
    }

    public Long countRunningAppealByOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataRepo.countRunningAppealByOfficeIdAndYearAndMonth(officeId, year + "-" + month + "-01 00:00:00");
    }

    public Long countTotalAppealByOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataRepo.countTotalAppealByOfficeIdAndYearAndMonth(officeId, year + "-" + month + "-01 00:00:00");
    }

    public Long getAppealAscertainCountByOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataRepo.getAppealAscertainCountByOfficeIdAndYearAndMonth(officeId, year + "-" + month + "-01 00:00:00");
    }

    public Long getMonthlyAppealCountByOfficeIdAndMediumOfSubmissionAndYearAndMonth(Long officeId, MediumOfSubmission mediumOfSubmission, String year, String month) {
        return this.dashboardDataRepo.getMonthlyAppealCountByOfficeIdAndMediumOfSubmissionAndYearAndMonth(officeId, mediumOfSubmission.name(), year + "-" + month + "-01 00:00:00");
    }

    public YearlyDashboardStatistics findYearlyStatistics() {
        return yearlyRepo.findOne(1);
    }

    public YearlyDashboardStatistics saveYearlyStatistics(YearlyDashboardStatistics statistics) {
        return yearlyRepo.save(statistics);
    }

    public YearlyDashboardStatistics findYearlyStatistics(Integer year) {
        return yearlyRepo.findOne(year);
    }

    public GrsStatistics findGrsDashboardStatistics(Long officeId, Integer month, Integer year) {
        return grsStatisticsRepo.findByOfficeIdAndYearAndMonth(officeId, year, month);
    }

    public SafetyNetProgramReportResponse safetyNetProgramReport(Long officeId, String fromDate, String toDate) {
        return baseEntityManager.safetyNetProgramReport(officeId, fromDate, toDate);
    }

    public SafetyNetProgramReportResponse safetyNetProgramReportByProgramId(Long officeId, String fromDate, String toDate, Integer programId) {
        return baseEntityManager.safetyNetProgramReportByProgramId(officeId, fromDate, toDate, programId);
    }

    public SafetyNetSummaryResponse getSafetyNetSummary(Long officeId, Integer programId) {
        return this.baseEntityManager.getSafetyNetSummary(officeId, programId);
    }
}
