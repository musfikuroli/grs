package com.grs.core.service;

import com.grs.api.model.GrievanceForwardingDTO;
import com.grs.api.model.SafetyNetProgramReportResponse;
import com.grs.api.model.SafetyNetSummaryResponse;
import com.grs.api.model.UserInformation;
import com.grs.api.model.request.AddCentralDashboardRecipientDTO;
import com.grs.api.model.response.CentralDashboardRecipientDTO;
import com.grs.api.model.response.EmployeeOrganogramDTO;
import com.grs.api.model.response.NudgeableGrievanceDTO;
import com.grs.api.model.response.RegisterDTO;
import com.grs.api.model.response.dashboard.*;
import com.grs.api.model.response.dashboard.latest.TotalResolvedByMonth;
import com.grs.api.model.response.dashboard.latest.TotalResolvedByYear;
import com.grs.api.model.response.roles.SingleRoleDTO;
import com.grs.core.dao.CentralDashboardRecipientDAO;
import com.grs.core.dao.DashboardDataDAO;
import com.grs.core.dao.MonthlyReportDAO;
import com.grs.core.dao.TagidDAO;
import com.grs.core.domain.*;
import com.grs.core.domain.grs.*;
import com.grs.core.domain.projapoti.*;
import com.grs.core.repo.grs.DashboardTotalResolvedRepo;
import com.grs.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class DashboardService {
    @Autowired
    private OfficeService officeService;
    @Autowired
    private OfficesGroService officesGroService;
    @Autowired
    private GrievanceService grievanceService;
    @Autowired
    private GrievanceForwardingService grievanceForwardingService;
    @Autowired
    private CitizenCharterService citizenCharterService;
    @Autowired
    private DashboardDataDAO dashboardDataDAO;
    @Autowired
    private MessageService messageService;
    @Autowired
    private CentralDashboardRecipientDAO centralDashboardRecipientDAO;
    @Autowired
    private ComplainantService complainantService;
    @Autowired
    private MonthlyReportDAO monthlyReportDAO;
    @Autowired
    private TagidDAO tagidDAO;

    @Autowired
    private DashboardTotalResolvedRepo dashboardTotalResolvedRepo;

    @Transactional("transactionManager")
    public DashboardData putDashboardDataRecord(GrievanceForwarding grievanceForwarding) {
        List<GrievanceCurrentStatus> forwardingStatusList = getForwardedStatusList();
        Grievance grievance = grievanceForwarding.getGrievance();
        Long officeId;
        GrievanceCurrentStatus currentStatus = grievance.getGrievanceCurrentStatus();
        if (currentStatus.name().contains("APPEAL")) {
            officeId = grievance.getCurrentAppealOfficeId();
        } else if (grievance.getSendToAoOfficeId() != null) {
            officeId = grievance.getSendToAoOfficeId();
        } else {
            officeId = grievance.getOfficeId();
        }
        if (forwardingStatusList.contains(currentStatus)) {
            officeId = grievanceForwarding.getFromOfficeId();
            Boolean isForwardedToAo = (currentStatus == GrievanceCurrentStatus.FORWARDED_TO_AO);
            saveDashboardData(grievance, grievanceForwarding.getToOfficeId(), GrievanceCurrentStatus.NEW, isForwardedToAo, grievanceForwarding.getCurrentStatus(), grievanceForwarding.getAction());
        } else if (currentStatus == GrievanceCurrentStatus.APPEAL) {
            return saveDashboardData(grievance, grievanceForwarding.getToOfficeId(), currentStatus, false, grievanceForwarding.getCurrentStatus(), grievanceForwarding.getAction());
        }
        if (grievanceForwarding.getAction().equals("RETAKE")) {
            DashboardData dashboardData = dashboardDataDAO.findByOfficeIdAndGrievanceId(officeId, grievance.getId());
            dashboardDataDAO.delete(dashboardData);
            return null;
        }
        return saveDashboardData(grievance, officeId, currentStatus, false, grievanceForwarding.getCurrentStatus(), grievanceForwarding.getAction());
    }

    public DashboardData saveDashboardData(Grievance grievance, Long officeId, GrievanceCurrentStatus currentStatus, Boolean isForwardedToAo, GrievanceCurrentStatus complainantMovementStatus, String action) {
        DashboardData dashboardData = dashboardDataDAO.findByOfficeIdAndGrievanceId(officeId, grievance.getId());
        if (
                complainantMovementStatus.name().equals("NUDGE")
                        || complainantMovementStatus.name().equals("GRO_CHANGED")
        ) {
            return dashboardData;
        }
        if (isForwardedToAo || currentStatus == GrievanceCurrentStatus.APPEAL || dashboardData == null) {
            String subject = isForwardedToAo ? grievance.getSubject() : (messageService.isCurrentLanguageInEnglish() ? "Others" : "অন্যান্য");
            CitizenCharter citizenCharter = citizenCharterService.findByOfficeAndService(
                    grievance.getOfficeId(),
                    grievance.getServiceOrigin()
            );
            MediumOfSubmission medium = MediumOfSubmission.ONLINE;
            if (currentStatus == GrievanceCurrentStatus.NEW && grievance.getIsOfflineGrievance()) {
                medium = MediumOfSubmission.CONVENTIONAL_METHOD;
            } else if (currentStatus == GrievanceCurrentStatus.NEW && grievance.getIsSelfMotivatedGrievance()) {
                medium = MediumOfSubmission.SELF_MOTIVATED_ACCEPTANCE;
            }

            if (
                    complainantMovementStatus.name().contains("APPEAL")
            ) {
                dashboardData = dashboardDataDAO.findAppealByOfficeIdAndGrievanceId(officeId, grievance.getId());

                if (dashboardData != null) {
                    dashboardData.setOfficeId(officeId);
                    dashboardData.setCitizenCharter(citizenCharter);
                    dashboardData.setServiceId(citizenCharter != null ? citizenCharter.getServiceOrigin().getId() : null);
                    dashboardData.setSubmissionDate(grievance.getSubmissionDate());
                    dashboardData.setClosureDate(getMaximumDateToBeClosed(grievance));
                } else {
                    dashboardData = DashboardData.builder()
                            .officeId(officeId)
                            .layerLevel(grievance.getOfficeLayers() != null ? Long.parseLong(grievance.getOfficeLayers()) : null)
                            .grievanceId(grievance.getId())
                            .complainantId(grievance.getComplainantId())
                            .subject(subject)
                            .trackingNumber(grievance.getTrackingNumber())
                            .citizenCharter(citizenCharter)
                            .serviceId(citizenCharter != null ? citizenCharter.getServiceOrigin().getId() : null)
                            .submissionDate(grievance.getSubmissionDate())
                            .closureDate(getMaximumDateToBeClosed(grievance))
                            .grievanceType(grievance.getGrievanceType())
                            .forwarded(false)
                            .mediumOfSubmission(medium)
                            .build();
                }

            } else {
                dashboardData = DashboardData.builder()
                        .officeId(officeId)
                        .layerLevel(grievance.getOfficeLayers() != null ? Long.parseLong(grievance.getOfficeLayers()) : null)
                        .grievanceId(grievance.getId())
                        .complainantId(grievance.getComplainantId())
                        .subject(subject)
                        .trackingNumber(grievance.getTrackingNumber())
                        .citizenCharter(citizenCharter)
                        .serviceId(citizenCharter != null ? citizenCharter.getServiceOrigin().getId() : null)
                        .submissionDate(grievance.getSubmissionDate())
                        .closureDate(getMaximumDateToBeClosed(grievance))
                        .grievanceType(grievance.getGrievanceType())
                        .forwarded(false)
                        .mediumOfSubmission(medium)
                        .build();
            }
        }
        if (!StringUtil.isValidString(dashboardData.getCaseNumber())) {
            dashboardData.setCaseNumber(grievance.getCaseNumber());
        }
        if (currentStatus.equals(GrievanceCurrentStatus.APPEAL)) {
            dashboardData.setAppealFromOfficeId(grievance.getOfficeId());
            dashboardData.setAcceptedDate(new Date());
            dashboardData.setMediumOfSubmission(MediumOfSubmission.ONLINE);
        } else if (currentStatus == GrievanceCurrentStatus.ACCEPTED) {
            dashboardData.setAcceptedDate(new Date());
        } else if (getForwardedStatusList().contains(currentStatus)) {
            dashboardData.setForwarded(true);
        } else if (currentStatus.name().startsWith("CLOSED")) {
            dashboardData.setGroDecision(grievance.getGroDecision());
            dashboardData.setGroIdentifiedCause(grievance.getGroIdentifiedCause());
            dashboardData.setGroSuggestion(grievance.getGroSuggestion());
        } else if (currentStatus.name().startsWith("APPEAL_CLOSED")) {
            dashboardData.setAoDecision(grievance.getAppealOfficerDecision());
            dashboardData.setAoIdentifiedCause(grievance.getAppealOfficerIdentifiedCause());
            dashboardData.setAoSuggestion(grievance.getAppealOfficerSuggestion());
        }
        EmployeeOrganogramDTO employeeOrganogramDTO = grievanceService.getSODetail(grievance.getId());
        OfficeUnit officeUnit = null;
        Long officeUnitOrganogramId = employeeOrganogramDTO.getOfficeUnitOrganogramId();
        if (officeUnitOrganogramId != null) {
            officeUnit = officeService.getOfficeUnitOrganogramById(officeUnitOrganogramId).getOfficeUnit();
        }
        dashboardData.setOfficeUnitId(officeUnit != null ? officeUnit.getId() : null);
        dashboardData.setComplaintStatus(currentStatus);
        if (!(action != null && (action.equals("GRO_CHANGED") || action.equals("GRO_CHANGED"))))
            dashboardData.setClosedDate(getActualClosedDate(currentStatus));
        dashboardData.setUpdatedAt(new Date());
        return dashboardDataDAO.save(dashboardData);
    }

    public List<GrievanceCurrentStatus> getForwardedStatusList() {
        return new ArrayList() {{
            add(GrievanceCurrentStatus.FORWARDED_IN);
            add(GrievanceCurrentStatus.FORWARDED_OUT);
            add(GrievanceCurrentStatus.FORWARDED_TO_AO);
        }};
    }

    public Boolean hasAccessToAoAndSubOfficesDashboard(UserInformation userInformation, Long officeId) {
        return officeService.hasAccessToAoAndSubOfficesDashboard(userInformation, officeId);
    }

    public Date getMaximumDateToBeClosed(Grievance grievance) {
        GrievanceCurrentStatus status = grievance.getGrievanceCurrentStatus();
        if (status.equals(GrievanceCurrentStatus.NEW)) {
            LocalDate maxDateToResolve = LocalDate.now().plusDays(CalendarUtil.getWorkDaysCountAfter(new Date(), (int) Constant.GRIEVANCE_EXPIRATION_TIME));
            return Date.from(maxDateToResolve.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else if (status.equals(GrievanceCurrentStatus.APPEAL)) {
            LocalDate maxDateToResolve = LocalDate.now().plusDays(CalendarUtil.getWorkDaysCountAfter(new Date(), (int) Constant.APPEAL_EXPIRATION_TIME));
            return Date.from(maxDateToResolve.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else {
            return null;
        }
    }

    public GeneralDashboardDataDTO constructGeneralDashboardDataDTO(Long total, Long resolved, Long declined, Long running, Long unresolved, boolean isAppealType) {
        GeneralDashboardDataDTO generalDashboardDataDTO = GeneralDashboardDataDTO.builder()
                .total(NameValuePairDTO.builder()
                        .name("প্রাপ্ত অভিযোগ")
                        .value(total)
                        .build())
                .resolved(NameValuePairDTO.builder()
                        .name("নিষ্পন্ন")
                        .value(resolved)
                        .color("#008000")
                        .build())
                .declined(NameValuePairDTO.builder()
                        .name(isAppealType ? "নথিজাত" : "অন্য দপ্তরে প্রেরিত")
                        .value(declined)
                        .color("#8A2BE2")
                        .build())
                .running(NameValuePairDTO.builder()
                        .name("চলমান")
                        .value(running)
                        .color("#EED202")
                        .build())
                .unresolved(NameValuePairDTO.builder()
                        .name("সময় অতিক্রান্ত")
                        .value(unresolved)
                        .color("#ED2939")
                        .build())
                .build();
        return generalDashboardDataDTO;
    }

    public GeneralDashboardDataDTO getGrievanceDataForGRODashboard(Long officeId, boolean includeTotalAndRating) {
        Long monthDiff = 0L;
        Long totalComplaintsCount = null;
        if (includeTotalAndRating) {
            totalComplaintsCount = dashboardDataDAO.countTotalComplaintsByOfficeIdV2(officeId, monthDiff);
        }
        Long resolvedComplaintsCount = dashboardDataDAO.countResolvedComplaintsByOfficeId(officeId, monthDiff);
        Long unresolvedComplaintsCount = dashboardDataDAO.countTimeExpiredComplaintsByOfficeId(officeId);
        Long runningComplaintsCount = dashboardDataDAO.countRunningGrievancesByOfficeId(officeId, monthDiff);
        Long declinedComplaintsCount = dashboardDataDAO.countDeclinedGrievancesByOfficeId(officeId, monthDiff);

        GeneralDashboardDataDTO groDashboardData = constructGeneralDashboardDataDTO(totalComplaintsCount, resolvedComplaintsCount, declinedComplaintsCount, runningComplaintsCount, unresolvedComplaintsCount, false);
        if (includeTotalAndRating) {
            DashboardRatingDTO ratingDTO = dashboardDataDAO.countAvgRatingOfComplaintsByOfficeId(officeId);
            groDashboardData.setRating(ratingDTO);
        }
        return groDashboardData;
    }


    public GeneralDashboardDataDTO getGrievanceDataForGRODashboard(Integer layerLevel, Long officeOrigin, Long officeId, boolean includeTotalAndRating, boolean grsEnabled) {


        List<Long> officeIds = new ArrayList();

        if (layerLevel >= 0 && layerLevel <= 2) {

            officeIds = getOfficeIdsByOfficeLayerLevel(layerLevel, officeId, grsEnabled, officeIds);
        } else {

            officeIds = getOfficeIdsByOfficeOrigin(layerLevel, officeOrigin, officeId, grsEnabled, officeIds);

        }
        if (officeIds.size() == 0) return new GeneralDashboardDataDTO();

        Long monthDiff = 0L;
        Long totalComplaintsCount = null;
        if (includeTotalAndRating) {
            totalComplaintsCount = dashboardDataDAO.countTotalComplaintsByOfficeIdsV2(officeIds, monthDiff);
        }
        Long resolvedComplaintsCount = dashboardDataDAO.countResolvedComplaintsByOfficeIds(officeIds, monthDiff);
        Long unresolvedComplaintsCount = dashboardDataDAO.countTimeExpiredComplaintsByOfficeIds(officeIds);
        Long runningComplaintsCount = dashboardDataDAO.countRunningGrievancesByOfficeIds(officeIds, monthDiff);
        Long declinedComplaintsCount = dashboardDataDAO.countDeclinedGrievancesByOfficeIds(officeIds, monthDiff);

        GeneralDashboardDataDTO groDashboardData = constructGeneralDashboardDataDTO(totalComplaintsCount, resolvedComplaintsCount, declinedComplaintsCount, runningComplaintsCount, unresolvedComplaintsCount, false);
        if (includeTotalAndRating) {
            DashboardRatingDTO ratingDTO = dashboardDataDAO.countAvgRatingOfComplaintsByOfficeId(officeId);
            groDashboardData.setRating(ratingDTO);
        }
        return groDashboardData;
    }

    private List<Long> getOfficeIdsByOfficeOrigin(Integer layerLevel, Long officeOrigin, Long officeId, boolean grsEnabled, List<Long> officeIds) {
        if (officeOrigin.equals(CacheUtil.SELECT_ALL_OPTION_VALUE)) {
            List<OfficeOrigin> officeOrigins = officeService.getOfficeOriginsByLayerLevel(layerLevel, grsEnabled, false);
            List<Office> officesByOfficeOriginIds = officeService.findByOfficeOriginIds(officeOrigins.stream().map(e -> e.getId()).collect(Collectors.toList()), grsEnabled, false);
            officeIds = officesByOfficeOriginIds.stream().map(e -> e.getId()).collect(Collectors.toList());
        } else if (officeId.equals(CacheUtil.SELECT_ALL_OPTION_VALUE)) {
            List<Office> officesByOfficeOriginIds = officeService.findByOfficeOriginIds(Arrays.asList(officeOrigin), grsEnabled, false);
            officeIds = officesByOfficeOriginIds.stream().map(e -> e.getId()).collect(Collectors.toList());
        } else {
            officeIds.add(officeId);
        }
        return officeIds;
    }

    private List<Long> getOfficeIdsByOfficeLayerLevel(Integer layerLevel, Long officeId, boolean grsEnabled, List<Long> officeIds) {
        if (officeId.equals(CacheUtil.SELECT_ALL_OPTION_VALUE)) {
            List<Office> officesByLayerLevel = officeService.getOfficesByLayerLevel(layerLevel, grsEnabled, false);
            officeIds = officesByLayerLevel.stream().map(e -> e.getId()).collect(Collectors.toList());
        } else {
            officeIds.add(officeId);
        }
        return officeIds;
    }

    public GeneralDashboardDataDTO getGrievanceDataForAODashboard(Long officeId, boolean includeTotalAndRating) {
        Long monthDiff = 0L;
        Long totalComplaintsCount = null;
        if (includeTotalAndRating) {
            totalComplaintsCount = dashboardDataDAO.countTotalAppealsByOfficeId(officeId, monthDiff);
        }
        Long resolvedComplaintsCount = dashboardDataDAO.countResolvedAppealsByOfficeId(officeId, monthDiff);
        Long unresolvedComplaintsCount = dashboardDataDAO.countAllTimeExpiredAppealsByOfficeId(officeId);
        Long runningComplaintsCount = dashboardDataDAO.countAllRunningAppealsByOfficeId(officeId, monthDiff);

        GeneralDashboardDataDTO aoDashboardData = constructGeneralDashboardDataDTO(totalComplaintsCount, resolvedComplaintsCount, null, runningComplaintsCount, unresolvedComplaintsCount, false);
        if (includeTotalAndRating) {
            DashboardRatingDTO ratingDTO = dashboardDataDAO.countAvgRatingOfAppealsByOfficeId(officeId);
            aoDashboardData.setRating(ratingDTO);
        }
        return aoDashboardData;
    }

    public GeneralDashboardDataDTO getGrievanceDataForAODashboard(
            Integer layerLevel,
            Long officeOrigin,
            Long officeId,
            boolean includeTotalAndRating,
            boolean grsEnabled) {

        List<Long> officeIds = new ArrayList();

        if (layerLevel >= 0 && layerLevel <= 2) {

            officeIds = getOfficeIdsByOfficeLayerLevel(layerLevel, officeId, grsEnabled, officeIds);
        } else {

            officeIds = getOfficeIdsByOfficeOrigin(layerLevel, officeOrigin, officeId, grsEnabled, officeIds);

        }

        Long monthDiff = 0L;
        Long totalComplaintsCount = 0L, resolvedComplaintsCount = 0L, unresolvedComplaintsCount = 0L, runningComplaintsCount = 0L;

        if (officeIds.size() != 0) {

            if (includeTotalAndRating) {
                totalComplaintsCount = dashboardDataDAO.countTotalAppealsByOfficeIds(officeIds, monthDiff);
            }
            resolvedComplaintsCount = dashboardDataDAO.countResolvedAppealsByOfficeIds(officeIds, monthDiff);
            unresolvedComplaintsCount = dashboardDataDAO.countAllTimeExpiredAppealsByOfficeIds(officeIds);
            runningComplaintsCount = dashboardDataDAO.countAllRunningAppealsByOfficeIds(officeIds, monthDiff);

        }
        GeneralDashboardDataDTO aoDashboardData = constructGeneralDashboardDataDTO(totalComplaintsCount, resolvedComplaintsCount, null, runningComplaintsCount, unresolvedComplaintsCount, false);


        if (includeTotalAndRating) {
            DashboardRatingDTO ratingDTO = dashboardDataDAO.countAvgRatingOfAppealsByOfficeId(officeId);
            aoDashboardData.setRating(ratingDTO);
        }


        return aoDashboardData;
    }

    public List<NameValuePairDTO> getGrievanceCountByMediumOfSubmission(Long officeId, Long monthDiff) {
        List<NameValuePairDTO> countByMediumOfSubmissionList = new ArrayList();
        Long byConventionalMethodCount = dashboardDataDAO.countComplaintsByOfficeAndMediumOfSubmission(officeId, MediumOfSubmission.CONVENTIONAL_METHOD, monthDiff);
        Long byOnlineCount = dashboardDataDAO.countComplaintsByOfficeAndMediumOfSubmission(officeId, MediumOfSubmission.ONLINE, monthDiff);
        Long fromLastMonthCount = getGrievanceAscertainCountOfPreviousMonthV2(officeId, monthDiff);
        countByMediumOfSubmissionList.add(NameValuePairDTO.builder().name("প্রচলিত পদ্ধতিতে").value(byConventionalMethodCount).build());
        countByMediumOfSubmissionList.add(NameValuePairDTO.builder().name("অনলাইনে").value(byOnlineCount).build());
        countByMediumOfSubmissionList.add(NameValuePairDTO.builder().name("পূর্ববর্তী মাসের জের").value(fromLastMonthCount).build());
        return countByMediumOfSubmissionList;
    }

    public List<NameValuePairDTO> getAppealCountByMediumOfSubmission(Long officeId, Long monthDiff) {
        List<NameValuePairDTO> countByMediumOfSubmissionList = new ArrayList();
        Long byOnlineCount = dashboardDataDAO.countAppealsByOfficeAndMediumOfSubmission(officeId, MediumOfSubmission.ONLINE, monthDiff);
        Long fromLastMonthCount = getAppealAscertainCountOfPreviousMonth(officeId, monthDiff);
        countByMediumOfSubmissionList.add(NameValuePairDTO.builder().name("অনলাইনে").value(byOnlineCount).build());
        countByMediumOfSubmissionList.add(NameValuePairDTO.builder().name("পূর্ববর্তী মাসের জের").value(fromLastMonthCount).build());
        return countByMediumOfSubmissionList;
    }

    public List<GrievanceCountByItemDTO> countGrievanceOfAnOfficeByService(Long officeId) {
        List<GrievanceCountByItemDTO> servicesWithZeroCount = officeService.getGrievanceCountByCitizensCharter(officeId);
        List resultList = dashboardDataDAO.getListOfGrievanceCountByServiceId(officeId);
        List<GrievanceCountByService> grievanceCountByServiceList = new ArrayList();
        resultList.stream().forEach(item -> {
            Object[] objectArray = (Object[]) item;
            Long id = objectArray[0] != null ? ((BigInteger) objectArray[0]).longValue() : null;
            Long count = ((BigInteger) objectArray[1]).longValue();
            grievanceCountByServiceList.add(GrievanceCountByService.builder()
                    .citizenCharterId(id)
                    .count(count)
                    .build());
        });
        List<GrievanceCountByItemDTO> servicesWithGrievanceCount = grievanceCountByServiceList.stream()
                .map(source -> {
                    CitizenCharter citizenCharter = null;
                    if (source.getCitizenCharterId() != null) {
                        citizenCharter = citizenCharterService.findOne(source.getCitizenCharterId());
                    }
                    Long id;
                    String nameBangla, nameEnglish;
                    if (citizenCharter != null) {
                        id = citizenCharter.getId();
                        nameBangla = citizenCharter.getServiceNameBangla();
                        nameEnglish = citizenCharter.getServiceNameEnglish();
                    } else {
                        id = 0L;
                        nameBangla = "অন্যান্য";
                        nameEnglish = "others";
                    }
                    servicesWithZeroCount.removeIf(obj -> obj.getId().equals(id));
                    return GrievanceCountByItemDTO.builder()
                            .id(id)
                            .grievanceCount(source.getCount())
                            .nameBangla(nameBangla)
                            .nameEnglish(nameEnglish)
                            .build();
                }).collect(Collectors.toList());
        return new ArrayList<GrievanceCountByItemDTO>() {{
            addAll(servicesWithGrievanceCount);
            addAll(servicesWithZeroCount);
        }};
    }

    public List<GrievanceCountByItemDTO> countGrievanceOfAnOfficeByOfficeUnit(Long officeId) {
        List<GrievanceCountByItemDTO> officeUnitsWithZeroCount = officeService.getListOfOfficeUnitsByOfficeId(officeId);
        List resultList = dashboardDataDAO.getListOfGrievanceCountByOfficeUnitId(officeId);
        List<GrievanceCountByOfficeUnit> grievanceCountByOfficeUnitList = new ArrayList();
        resultList.stream().forEach(item -> {
            Object[] objectArray = (Object[]) item;
            Long id = objectArray[0] != null ? ((BigInteger) objectArray[0]).longValue() : null;
            Long count = ((BigInteger) objectArray[1]).longValue();
            grievanceCountByOfficeUnitList.add(GrievanceCountByOfficeUnit.builder()
                    .officeUnitId(id)
                    .count(count)
                    .build());
        });
        List<GrievanceCountByItemDTO> officeUnitsWithGrievanceCount = grievanceCountByOfficeUnitList.stream()
                .map(source -> {
                    Long id;
                    String nameBangla, nameEnglish;
                    if (source.getOfficeUnitId() != null) {
                        id = source.getOfficeUnitId();
                        GrievanceCountByItemDTO countByItemDTO = officeUnitsWithZeroCount.stream().filter(x -> Objects.equals(x.getId(), id)).findAny()
                                .orElse(null);
                        nameBangla = countByItemDTO.getNameBangla();
                        nameEnglish = countByItemDTO.getNameEnglish();
                    } else {
                        id = 0L;
                        nameBangla = "অন্যান্য";
                        nameEnglish = "Others";
                    }
                    officeUnitsWithZeroCount.removeIf(obj -> obj.getId().equals(id));
                    return GrievanceCountByItemDTO.builder()
                            .id(id)
                            .grievanceCount(source.getCount())
                            .nameBangla(nameBangla)
                            .nameEnglish(nameEnglish)
                            .build();
                })
                .collect(Collectors.toList());

        return new ArrayList<GrievanceCountByItemDTO>() {{
            addAll(officeUnitsWithGrievanceCount);
            addAll(officeUnitsWithZeroCount);
        }};
    }

    public Date getActualClosedDate(GrievanceCurrentStatus currentStatus) {
        List<GrievanceCurrentStatus> resolvableStatusList = new ArrayList<GrievanceCurrentStatus>() {{
            add(GrievanceCurrentStatus.CLOSED_ACCUSATION_INCORRECT);
            add(GrievanceCurrentStatus.CLOSED_ACCUSATION_PROVED);
            add(GrievanceCurrentStatus.CLOSED_ANSWER_OK);
            add(GrievanceCurrentStatus.CLOSED_INSTRUCTION_EXECUTED);
            add(GrievanceCurrentStatus.CLOSED_SERVICE_GIVEN);
            add(GrievanceCurrentStatus.CLOSED_OTHERS);
            add(GrievanceCurrentStatus.REJECTED);

            add(GrievanceCurrentStatus.APPEAL_CLOSED_ACCUSATION_INCORRECT);
            add(GrievanceCurrentStatus.APPEAL_CLOSED_OTHERS);
            add(GrievanceCurrentStatus.APPEAL_CLOSED_ACCUSATION_PROVED);
            add(GrievanceCurrentStatus.APPEAL_CLOSED_ANSWER_OK);
            add(GrievanceCurrentStatus.APPEAL_CLOSED_INSTRUCTION_EXECUTED);
            add(GrievanceCurrentStatus.APPEAL_CLOSED_SERVICE_GIVEN);
        }};
        if (resolvableStatusList.contains(currentStatus)) {
            return new Date();
        }
        return null;

    }

    public ResolutionTypeInfoDTO getResolutionTypeInfo(Long officeId) {
        return ResolutionTypeInfoDTO.builder()
                .acceptedGrievanceCount(dashboardDataDAO.countAcceptedGrievancesByOfficeIdAndMonthDiff(officeId))
                .trueGrievanceCount(dashboardDataDAO.countResolvedGrievancesByOfficeIdAndIsReal(officeId))
                .fakeGrievanceCount(dashboardDataDAO.countResolvedGrievancesByOfficeIdAndIsNotReal(officeId))
                .departmentalRecommendationCount(0L)
                .build();
    }

    public UnacceptedGrievancesCountDTO getUnacceptedGrievancesInfo(Long officeId) {
        return UnacceptedGrievancesCountDTO.builder()
                .sendToAOCount(dashboardDataDAO.countComplaintsByOfficeIdAndStatus(officeId, GrievanceCurrentStatus.FORWARDED_TO_AO))
                .sendToOtherOfficesCount(dashboardDataDAO.countComplaintsByOfficeIdAndStatus(officeId, GrievanceCurrentStatus.FORWARDED_OUT))
                .sendToChildOfficesCount(dashboardDataDAO.countComplaintsByOfficeIdAndStatus(officeId, GrievanceCurrentStatus.FORWARDED_IN))
                .rejectedGrievanceCount(dashboardDataDAO.countComplaintsByOfficeIdAndStatus(officeId, GrievanceCurrentStatus.REJECTED))
                .build();
    }

    public List<MonthlyGrievanceResolutionDTO> getCurrentMonthResolutionsAsList(List<DashboardData> dashboardDataList) {
        List<MonthlyGrievanceResolutionDTO> currentMonthResolutions = new ArrayList<>();
        Boolean isEnglish = messageService.getCurrentLanguageCode().equals("en");
        dashboardDataList.forEach(dashboardData -> {
            Grievance grievance = grievanceService.findGrievanceById(dashboardData.getGrievanceId());
            CitizenCharter citizenCharter = dashboardData.getCitizenCharter();
            String serviceName;
            if (citizenCharter != null) {
                serviceName = isEnglish ? citizenCharter.getServiceNameEnglish() : citizenCharter.getServiceNameBangla();
            } else {
                serviceName = isEnglish ? "others" : "অন্যান্য";
            }
            if (grievance != null && grievance.getServiceOrigin() == null) {
                serviceName = grievance.getOtherService();
            }
            MonthlyGrievanceResolutionDTO resolution = MonthlyGrievanceResolutionDTO.builder()
                    .id(dashboardData.getId())
                    .subject(dashboardData.getSubject())
                    .serviceName(serviceName)
                    .closedDate(dashboardData.getClosedDate())
                    .groIdentifiedCause(dashboardData.getGroIdentifiedCause())
                    .groDecision(dashboardData.getGroDecision())
                    .groSuggestion(dashboardData.getGroSuggestion())
                    .aoIdentifiedCause(dashboardData.getAoIdentifiedCause())
                    .aoDecision(dashboardData.getAoDecision())
                    .aoSuggestion(dashboardData.getAoSuggestion())
                    .build();
            currentMonthResolutions.add(resolution);
        });
        return currentMonthResolutions;
    }

    public List<MonthlyGrievanceResolutionDTO> getResolutionsInCurrentMonth(Long officeId, Long monthDiff) {
        List<DashboardData> dashboardDataList = dashboardDataDAO.getResolvedGrievancesOfCurrentMonthByOfficeId(officeId, monthDiff);
        return getCurrentMonthResolutionsAsList(dashboardDataList);
    }

    public List<MonthlyGrievanceResolutionDTO> getAppealResolutionsInCurrentMonth(Long officeId, Long monthDiff) {
        List<DashboardData> dashboardDataList = dashboardDataDAO.getResolvedAppealsOfCurrentMonthByOfficeId(officeId, monthDiff);
        return getCurrentMonthResolutionsAsList(dashboardDataList);
    }

    public List<ExpiredGrievanceInfoDTO> getExpiredGrievancesInformationAsList(List<DashboardData> dashboardDataList, Boolean isAppeal) {
        List<ExpiredGrievanceInfoDTO> expiredGrievanceInfoList = new ArrayList<>();
        Boolean isEnglish = messageService.getCurrentLanguageCode().equals("en");
        dashboardDataList.forEach(dashboardData -> {
            Grievance grievance = grievanceService.findGrievanceById(dashboardData.getGrievanceId());
            CitizenCharter citizenCharter = dashboardData.getCitizenCharter();
            String serviceName;
            if (citizenCharter != null) {
                serviceName = isEnglish ? citizenCharter.getServiceNameEnglish() : citizenCharter.getServiceNameBangla();
            } else {
                serviceName = isEnglish ? "others" : "অন্যান্য";
            }
            if (grievance != null && grievance.getServiceOrigin() == null) {
                serviceName = grievance.getOtherService();
            }

            ExpiredGrievanceInfoDTO resolution = ExpiredGrievanceInfoDTO.builder()
                    .id(dashboardData.getId())
                    .subject(dashboardData.getSubject())
                    .serviceName(serviceName)
                    .closureDate(getClosureDateFromCreatedDate(dashboardData.getCreatedAt(), isAppeal))
                    .currentLocationList(getGrievanceCurrentLocation(grievance))
                    .build();
            expiredGrievanceInfoList.add(resolution);
        });
        return expiredGrievanceInfoList;
    }

    public Date getClosureDateFromCreatedDate(Date createdDate, Boolean isAppeal) {
        Long additionalDays = isAppeal ? Constant.APPEAL_EXPIRATION_TIME : Constant.GRIEVANCE_EXPIRATION_TIME;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(createdDate);
        calendar.add(Calendar.DATE, Math.toIntExact(CalendarUtil.getWorkDaysCountAfter(createdDate, Math.toIntExact(additionalDays))));
        return calendar.getTime();
    }

    public List<GrievanceCurrentLocationDTO> getGrievanceCurrentLocation(Grievance grievance) {
        Boolean isEnglish = messageService.isCurrentLanguageInEnglish();
        List<GrievanceForwarding> grievanceForwardingList = grievanceForwardingService.findByGrievanceAndIsCurrent(grievance, true);
        List<GrievanceCurrentLocationDTO> grievanceCurrentLocationList = new ArrayList();
        grievanceForwardingList.forEach(grievanceForwarding -> {
            Office office = officeService.findOne(grievanceForwarding.getToOfficeId());
            OfficeUnit officeUnit = officeService.getOfficeUnitById(grievanceForwarding.getToOfficeUnitId());
            GrievanceCurrentLocationDTO currentLocationDTO = GrievanceCurrentLocationDTO.builder()
                    .officeName(isEnglish ? office.getNameEnglish() : office.getNameBangla())
                    .officeUnitName(officeUnit == null ? "" : (isEnglish ? officeUnit.getUnitNameEnglish() : officeUnit.getUnitNameBangla()))
                    .waitingFrom(grievanceForwarding.getCreatedAt())
                    .build();
            grievanceCurrentLocationList.add(currentLocationDTO);
        });
        return grievanceCurrentLocationList;
    }

    public List<NudgeableGrievanceDTO> getTimeExpiredGrievanceDTOList(Long officeId) {
        List<DashboardData> dataList = dashboardDataDAO.getTimeExpiredGrievancesByOfficeId(officeId);
        return convertToNudgeableGrievanceList(dataList);
    }

    public List<NudgeableGrievanceDTO> getTimeExpiredAppealDTOList(Long officeId) {
        List<DashboardData> dataList = dashboardDataDAO.getTimeExpiredAppealsByOfficeId(officeId);
        return convertToNudgeableGrievanceList(dataList);
    }

    public List<NudgeableGrievanceDTO> convertToNudgeableGrievanceList(List<DashboardData> dashboardDataList) {
        List<NudgeableGrievanceDTO> timeExpiredGrievances = dashboardDataList.stream()
                .map(dashboardData -> {
                    Grievance grievance = grievanceService.findGrievanceById(dashboardData.getGrievanceId());
                    return NudgeableGrievanceDTO.builder()
                            .grievance(grievanceService.convertToGrievanceDTO(grievance))
                            .grievanceCurrentLocationList(getGrievanceCurrentLocation(grievance))
                            .build();
                }).collect(Collectors.toList());
        return timeExpiredGrievances;
    }

    public List<ExpiredGrievanceInfoDTO> getTimeExpiredAppealsList(Long officeId) {
        List<DashboardData> dashboardDataList = dashboardDataDAO.getTimeExpiredAppealsByOfficeId(officeId);
        return getExpiredGrievancesInformationAsList(dashboardDataList, true);
    }

    public List<AppealCountByOfficeDTO> getCountOfAppealsBySourceOffices(Long officeId) {
        List<AppealCountByOfficeDTO> resultList = new ArrayList();
        List countObjectList = dashboardDataDAO.getCountOfAppealsBySourceOffices(officeId);
        List<Long> idList = new ArrayList();
        List<TotalAndResolvedCountDTO> grievanceCounts = new ArrayList();
        if (countObjectList.size() > 0) {
            for (int i = 0; i < countObjectList.size(); i++) {
                Object[] objectArray = (Object[]) countObjectList.get(i);
                Long id = ((BigInteger) objectArray[0]).longValue();
                idList.add(id);
                TotalAndResolvedCountDTO totalAndResolvedCountDTO = TotalAndResolvedCountDTO.builder()
                        .officeId(id)
                        .totalCount(((BigInteger) objectArray[1]).longValue())
                        .build();
                grievanceCounts.add(totalAndResolvedCountDTO);
            }
            List<Office> offices = officeService.findByOfficeIdInList(idList);
            offices.stream().forEach(office -> {
                TotalAndResolvedCountDTO grievanceCount = grievanceCounts.stream().filter(obj -> obj.getOfficeId().equals(office.getId())).findFirst().orElse(null);
                AppealCountByOfficeDTO countByOffice = AppealCountByOfficeDTO.builder()
                        .id(office.getId())
                        .nameBangla(office.getNameBangla())
                        .nameEnglish(office.getNameEnglish())
                        .grievanceCount(grievanceCount != null ? grievanceCount.getTotalCount() : 0L)
                        .build();
                resultList.add(countByOffice);
            });
        }
        return resultList;
    }

    public String getYearAndMonthWithMonthDiff(Integer monthDiff) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, monthDiff);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(calendar.getTime());
    }

    public MonthAndTypeWiseCountDTO formatMonthAndTypeWiseCountForGrievanceAndAppeal(List countObjectList, String year, String month) {
        MonthAndTypeWiseCountDTO monthAndTypeWiseCountDTO = MonthAndTypeWiseCountDTO.builder()
                .year(year)
                .month(month)
                .build();
        for (int i = 0; i < countObjectList.size(); i++) {
            Object[] objectArray = (Object[]) countObjectList.get(i);
            String type = (String) objectArray[0];
            TotalAndResolvedCountDTO totalAndResolvedCountDTO = TotalAndResolvedCountDTO.builder()
                    .totalCount(((BigInteger) objectArray[1]).longValue())
                    .resolvedCount(((BigInteger) objectArray[2]).longValue())
                    .build();
            if (type.equals("STAFF")) {
                monthAndTypeWiseCountDTO.setStaffCounts(totalAndResolvedCountDTO);
            } else if (type.equals("DAPTORIK")) {
                monthAndTypeWiseCountDTO.setDaptorikCounts(totalAndResolvedCountDTO);
            } else {
                monthAndTypeWiseCountDTO.setNagorikCounts(totalAndResolvedCountDTO);
            }
        }
        return monthAndTypeWiseCountDTO;
    }

    public MonthAndTypeWiseCountDTO getMonthAndTypeWiseCountOfGrievanceByOfficeIdWithMonthDiff(Long officeId, Integer monthDiff) {
        String yearAndMonth = getYearAndMonthWithMonthDiff(monthDiff);
        String year = yearAndMonth.substring(0, 4);
        String month = yearAndMonth.substring(5, 7);
        List countObjectList = dashboardDataDAO.getTotalAndResolvedGrievanceCountWithTypeByMonthAndYear(officeId, monthDiff);
        return formatMonthAndTypeWiseCountForGrievanceAndAppeal(countObjectList, year, month);
    }

    public MonthAndTypeWiseCountDTO getMonthAndTypeWiseCountOfAppealByOfficeIdWithMonthDiff(Long officeId, Integer monthDiff) {
        String yearAndMonth = getYearAndMonthWithMonthDiff(monthDiff);
        String year = yearAndMonth.substring(0, 4);
        String month = yearAndMonth.substring(5, 7);
        List countObjectList = dashboardDataDAO.getTotalAndResolvedAppealCountWithTypeByMonthAndYear(officeId, monthDiff);
        return formatMonthAndTypeWiseCountForGrievanceAndAppeal(countObjectList, year, month);
    }

    public Long getGrievanceAscertainCountOfPreviousMonthV2(Long officeId, Long monthDiff) {
        return dashboardDataDAO.getGrievanceAscertainCountOfPreviousMonthV2(officeId, monthDiff);
    }
    public List<Long> getComplaintIdsContainRatingInCurrentMonth(Long officeId, Boolean isAppeal) {
        List<BigInteger> complaintsIdList;
        if (isAppeal) {
            complaintsIdList = dashboardDataDAO.getIdsOfAppealsContainRatingInCurrentMonth(officeId);
        } else {
            complaintsIdList = dashboardDataDAO.getIdsOfGrievancesContainRatingInCurrentMonth(officeId);
        }
        return complaintsIdList.stream()
                .map(id -> id.longValue())
                .collect(Collectors.toList());
    }

    public Long getMonthlyComplaintsCountByOfficeIdAndMediumOfSubmission(Long officeId, MediumOfSubmission mediumOfSubmission, Long monthDiff) {
        return dashboardDataDAO.countComplaintsByOfficeAndMediumOfSubmission(officeId, mediumOfSubmission, monthDiff);
    }

    public Long getMonthlyAppealsCountByOfficeIdAndMediumOfSubmission(Long officeId, MediumOfSubmission mediumOfSubmission, Long monthDiff) {
        return dashboardDataDAO.countAppealsByOfficeAndMediumOfSubmission(officeId, mediumOfSubmission, monthDiff);
    }

    public Long getAppealAscertainCountOfPreviousMonth(Long officeId, Long monthDiff) {
        return dashboardDataDAO.getAppealsAscertainCountOfPreviousMonth(officeId, monthDiff);
    }

    public Long getMonthlyAppealsCountByOfficeIdAndMediumOfSubmissionV2(Long officeId, MediumOfSubmission mediumOfSubmission, Long monthDiff) {
        return dashboardDataDAO.countAppealsByOfficeAndMediumOfSubmissionV2(officeId, mediumOfSubmission, monthDiff);
    }

    public Long getDailyAppealsCountByOfficeIdAndMediumOfSubmission(Long officeId, MediumOfSubmission mediumOfSubmission, Long dayDiff) {
        return dashboardDataDAO.countDailyAppealsByOfficeAndMediumOfSubmission(officeId, mediumOfSubmission, dayDiff);
    }

    public Long getAppealAscertainCountOfPreviousMonthV2(Long officeId, Long monthDiff) {
        return dashboardDataDAO.getAppealsAscertainCountOfPreviousMonthV2(officeId, monthDiff);
    }

    public Long getAppealAscertainCountOfPreviousDay(Long officeId, Long dayDiff) {
        return dashboardDataDAO.getAppealsAscertainCountOfPreviousDay(officeId, dayDiff);
    }

    public List<ChildOfficesDashboardNavigatorDTO> getGrsEnabledOfficesFromOfficeList(List<Office> offices) {
        List<Long> officeIdList = offices.stream()
                .map(Office::getId)
                .collect(Collectors.toList());
        List<Long> grsEnabledOfficeIdList = officesGroService.getGRSEnabledOfficeIdFromOfficeIdList(officeIdList);
        List<ChildOfficesDashboardNavigatorDTO> childOfficesNameIdList = new ArrayList();
        if (offices.size() > 0) {
            Boolean isEnglish = messageService.isCurrentLanguageInEnglish();
            offices.stream().forEach(office -> {
                Long id = office.getId();
                if (grsEnabledOfficeIdList.contains(id)) {
                    ChildOfficesDashboardNavigatorDTO nameIdDTO = ChildOfficesDashboardNavigatorDTO.builder()
                            .id(id)
                            .name(isEnglish ? office.getNameEnglish() : office.getNameBangla())
                            .enabled(true)
                            .build();
                    childOfficesNameIdList.add(nameIdDTO);
                }
            });
        }
        return childOfficesNameIdList;
    }

    public List<ChildOfficesDashboardNavigatorDTO> getListOfChildOffices(Long officeId) {
        if (officeService.getChildCountByParentOfficeId(officeId) == 0 || officeService.isUpazilaLevelOffice(officeId)) {
            return null;
        }
        List<Office> offices = officeService.getOfficesByParentOfficeId(officeId);
        return getGrsEnabledOfficesFromOfficeList(offices);
    }

    public List<Long> getSubOfficesAggregatedIdListForGrievances(Long officeId) {
        if (officeService.getChildCountByParentOfficeId(officeId) == 0 || officeService.isUpazilaLevelOffice(officeId)) {
            return new ArrayList<Long>() {{
                add(officeId);
            }};
        } else {
            List<Office> offices = officeService.getOfficesByParentOfficeId(officeId);
            List<Long> officeIdList = offices.stream()
                    .map(Office::getId)
                    .collect(Collectors.toList());
            List<Long> grsEnabledChildOfficeIdList = officesGroService.getGRSEnabledOfficeIdFromOfficeIdList(officeIdList);
            List<Long> subOfficesAggregatedIdListForGrievances = new ArrayList<>(1);
            for (Long id : grsEnabledChildOfficeIdList) {
                subOfficesAggregatedIdListForGrievances.addAll(getSubOfficesAggregatedIdListForGrievances(id));
            }
            return subOfficesAggregatedIdListForGrievances;
        }
    }

    public GeneralDashboardDataDTO getSubOfficesAggregatedDataForGrievances(Long officeId) {
        if (officeService.getChildCountByParentOfficeId(officeId) == 0 || officeService.isUpazilaLevelOffice(officeId)) {
            return constructGeneralDashboardDataDTO(0L, 0L, 0L, 0L, 0L, false);
        } else {
            Long monthDiff = 0L;
            Long totalCount = 0L;
            Long resolvedCount = 0L;
            Long declinedCount = 0L;
            Long runningCount = 0L;
            Long unresolvedCount = 0L;
            List<Office> offices = officeService.getOfficesByParentOfficeId(officeId);
            List<Long> officeIdList = offices.stream()
                    .map(Office::getId)
                    .collect(Collectors.toList());
            List<Long> grsEnabledChildOfficeIdList = officesGroService.getGRSEnabledOfficeIdFromOfficeIdList(officeIdList);
            List<Long> subOfficesAggregatedIdListForGrievances = new ArrayList<>(1);
            for (Long id : grsEnabledChildOfficeIdList) {
                subOfficesAggregatedIdListForGrievances.addAll(getSubOfficesAggregatedIdListForGrievances(id));
            }

            if (subOfficesAggregatedIdListForGrievances.isEmpty()) {
                return constructGeneralDashboardDataDTO(0L, 0L, 0L, 0L, 0L, false);
            }

            totalCount += (dashboardDataDAO.countTotalComplaintsByOfficeIdListV2(subOfficesAggregatedIdListForGrievances, monthDiff));
            resolvedCount += (dashboardDataDAO.countResolvedComplaintsByOfficeIdList(subOfficesAggregatedIdListForGrievances, monthDiff));
            declinedCount += (dashboardDataDAO.countDeclinedGrievancesByOfficeIdList(subOfficesAggregatedIdListForGrievances, monthDiff));
            runningCount += (dashboardDataDAO.countRunningGrievancesByOfficeIdList(subOfficesAggregatedIdListForGrievances, monthDiff));
            unresolvedCount += (dashboardDataDAO.countTimeExpiredComplaintsByOfficeIdList(subOfficesAggregatedIdListForGrievances));

            return constructGeneralDashboardDataDTO(totalCount, resolvedCount, declinedCount, runningCount, unresolvedCount, false);
        }
    }

    public GeneralDashboardDataDTO getSubOfficesAggregatedDataForGrievances(Integer year, Integer month, Long officeId) {


        if (officeService.getChildCountByParentOfficeId(officeId) == 0 || officeService.isUpazilaLevelOffice(officeId)) {
            return constructGeneralDashboardDataDTO(0L, 0L, 0L, 0L, 0L, false);
        } else {
            Calendar c = Calendar.getInstance();
            Long diff = (long) (year * 12 + month) - (c.get(Calendar.YEAR) * 12 + c.get(Calendar.MONTH) + 1);
            c.set(year, month-1, 1, 0, 0, 0);
            Date fromDate = c.getTime();

            Long monthDiff = diff;
            Long totalCount = 0L;
            Long resolvedCount = 0L;
            Long declinedCount = 0L;
            Long runningCount = 0L;
            Long unresolvedCount = 0L;
            List<Office> offices = officeService.getOfficesByParentOfficeId(officeId);
            List<Long> officeIdList = offices.stream()
                    .map(Office::getId)
                    .collect(Collectors.toList());
            List<Long> grsEnabledChildOfficeIdList = officesGroService.getGRSEnabledOfficeIdFromOfficeIdList(officeIdList);
            List<Long> subOfficesAggregatedIdListForGrievances = new ArrayList<>(1);
            for (Long id : grsEnabledChildOfficeIdList) {
                subOfficesAggregatedIdListForGrievances.addAll(getSubOfficesAggregatedIdListForGrievances(id));
            }

            if (subOfficesAggregatedIdListForGrievances.isEmpty()) {
                return constructGeneralDashboardDataDTO(0L, 0L, 0L, 0L, 0L, false);
            }

            totalCount += (dashboardDataDAO.countTotalComplaintsByOfficeIdListV2(subOfficesAggregatedIdListForGrievances, monthDiff));
            resolvedCount += (dashboardDataDAO.countResolvedComplaintsByOfficeIdList(subOfficesAggregatedIdListForGrievances, monthDiff));
            declinedCount += (dashboardDataDAO.countDeclinedGrievancesByOfficeIdList(subOfficesAggregatedIdListForGrievances, monthDiff));
            runningCount += (dashboardDataDAO.countRunningGrievancesByOfficeIdList(subOfficesAggregatedIdListForGrievances, monthDiff));
            unresolvedCount += (dashboardDataDAO.countTimeExpiredComplaintsByOfficeIdList(fromDate, subOfficesAggregatedIdListForGrievances));

            return constructGeneralDashboardDataDTO(totalCount, resolvedCount, declinedCount, runningCount, unresolvedCount, false);
        }
    }

    public GeneralDashboardDataDTO getSubOfficesAggregatedDataForAppeals(Long officeId) {
        if (officeService.getChildCountByParentOfficeId(officeId) > 0 && !officeService.isZilaLevelOffice(officeId)) {
            Long monthDiff = 0L;
            Long totalCount = 0L;
            Long resolvedCount = 0L;
            Long declinedCount = 0L;
            Long runningCount = 0L;
            Long unresolvedCount = 0L;
            List<Office> offices = officeService.getOfficesByParentOfficeId(officeId);
            List<Long> officeIdList = offices.stream()
                    .map(Office::getId)
                    .collect(Collectors.toList());
            List<Long> grsEnabledChildOfficeIdList = officesGroService.getGRSEnabledOfficeIdFromOfficeIdList(officeIdList);
            for (Long id : grsEnabledChildOfficeIdList) {
                GeneralDashboardDataDTO subAggregatedData = getSubOfficesAggregatedDataForAppeals(id);
                totalCount += (dashboardDataDAO.countTotalAppealsByOfficeId(id, monthDiff) + subAggregatedData.getTotal().getValue());
                resolvedCount += (dashboardDataDAO.countResolvedAppealsByOfficeId(id, monthDiff) + subAggregatedData.getResolved().getValue());
                declinedCount += (dashboardDataDAO.countDeclinedAppealsByOfficeId(id) + subAggregatedData.getDeclined().getValue());
                runningCount += (dashboardDataDAO.countAllRunningAppealsByOfficeId(id, monthDiff) + subAggregatedData.getRunning().getValue());
                unresolvedCount += (dashboardDataDAO.countAllTimeExpiredAppealsByOfficeId(id) + subAggregatedData.getUnresolved().getValue());
            }
            return constructGeneralDashboardDataDTO(totalCount, resolvedCount, declinedCount, runningCount, unresolvedCount, true);
        } else {
            return constructGeneralDashboardDataDTO(0L, 0L, 0L, 0L, 0L, true);
        }
    }

    public void getFeedbackForDashboardData(Grievance grievance) {
        DashboardData dashboardData = dashboardDataDAO.findByOfficeIdAndGrievanceId(grievance.getOfficeId(), grievance.getId());
        dashboardData.setRating(grievance.getRating());
        this.dashboardDataDAO.save(dashboardData);
    }

    public void getAppealFeedbackForDashboardData(Grievance grievance) {
        GrievanceForwardingDTO grievanceForwardingDTO = this.grievanceForwardingService.getLatestForwardingEntry(grievance.getId());
        DashboardData dashboardData = dashboardDataDAO.findTopByOfficeIdAndGrievanceId(grievanceForwardingDTO.getToOfficeId(), grievance.getId());
        dashboardData.setRating(grievance.getAppealRating());
        this.dashboardDataDAO.save(dashboardData);
    }

    public List<ItemIdNameCountDTO> getCitizenCharterServicesByComplaintFrequency() {
        Object[] objects = dashboardDataDAO.getCitizenCharterServicesByComplaintFrequency();
        List<ItemIdNameCountDTO> services = new ArrayList();
        for (Object o : objects) {
            Object[] objectArray = (Object[]) o;
            if (objectArray != null) {
                Long serviceId = ((BigInteger) objectArray[0]).longValue();
                String serviceName = (String) objectArray[1];
                Long count = objectArray[2] != null ? ((BigInteger) objectArray[2]).longValue() : 0L;
                services.add(ItemIdNameCountDTO.builder()
                        .id(serviceId)
                        .name(serviceName)
                        .grievanceCount(count)
                        .build());
            }
        }
        return services;
    }

    public List<ItemIdNameCountDTO> getCitizenCharterServicesByComplaintFrequency(Date fromDate, Date toDate) {
        Object[] objects = dashboardDataDAO.getCitizenCharterServicesByComplaintFrequency(fromDate, toDate);
        List<ItemIdNameCountDTO> services = new ArrayList();
        for (Object o : objects) {
            Object[] objectArray = (Object[]) o;
            if (objectArray != null) {
                Long serviceId = ((BigInteger) objectArray[0]).longValue();
                String serviceName = (String) objectArray[1];
                Long count = objectArray[2] != null ? ((BigInteger) objectArray[2]).longValue() : 0L;
                services.add(ItemIdNameCountDTO.builder()
                        .id(serviceId)
                        .name(serviceName)
                        .grievanceCount(count)
                        .build());
            }
        }
        return services;
    }

    public List<ItemIdNameCountDTO> getServicesCountWithOfficeNameByServiceId(Long serviceId) {
        Object[] objects = dashboardDataDAO.getServiceCountWithOfficeNameByServiceId(0L, serviceId);
        List<ItemIdNameCountDTO> serviceCountByOffices = new ArrayList();
        for (Object o : objects) {
            Object[] objectArray = (Object[]) o;
            if (objectArray != null) {
                Long officeId = ((BigInteger) objectArray[0]).longValue();
                Long count = objectArray[1] != null ? ((BigInteger) objectArray[1]).longValue() : 0L;
                serviceCountByOffices.add(ItemIdNameCountDTO.builder()
                        .id(officeId)
                        .grievanceCount(count)
                        .build());
            }
        }
        List<Long> officeIds = serviceCountByOffices.stream()
                .map(ItemIdNameCountDTO::getId)
                .collect(Collectors.toList());
        List<Office> offices = officeService.findByOfficeIdInList(officeIds);
        for (ItemIdNameCountDTO item : serviceCountByOffices) {
            Office office = offices.stream()
                    .filter(o -> o.getId().equals(item.getId()))
                    .findFirst()
                    .orElse(null);
            item.setName(office.getNameBangla());
        }
        return serviceCountByOffices;
    }

    public List<TotalAndResolvedCountDTO> getTotalSubmittedAndResolvedCountsOfMinistries() {
        List<ChildOfficesDashboardNavigatorDTO> ministryLevelOffices = getGrsEnabledMinistryLevelOffices();
        List<Long> officeIds = ministryLevelOffices.stream()
                .map(ChildOfficesDashboardNavigatorDTO::getId)
                .collect(Collectors.toList());
//        Object[] objects = dashboardDataDAO.getTotalSubmittedAndResolvedCountByOfficeIdInList(officeIds);
        Object[] submittedObjects = dashboardDataDAO.getSubmittedCountByOfficeIdInList(officeIds);
        Object[] resolvedObjects = dashboardDataDAO.getResolvedCountByOfficeIdInList(officeIds);
        Object[] expiredObjects = dashboardDataDAO.getExpiredCountByOfficeIdInList(officeIds);
        List<TotalAndResolvedCountDTO> listOfOfficesWithCounts = new ArrayList();
        ministryLevelOffices.forEach(ministry -> {
            listOfOfficesWithCounts.add(
                    TotalAndResolvedCountDTO.builder()
                            .officeId(ministry.getId())
                            .officeName(ministry.getName())
                            .resolvedCount(0L)
                            .totalCount(0L)
                            .rate(0D)
                            .build()
            );
        });
//        for(Object o: objects) {
//            Object[] objectArray = (Object[]) o;
//            if(objectArray != null) {
//                Long officeId = ((BigInteger) objectArray[0]).longValue();
//                Long total = objectArray[1] != null ? ((BigInteger) objectArray[1]).longValue() : 0L;
//                Long resolved = objectArray[2] != null ? ((BigInteger) objectArray[2]).longValue() : 0L;
//                Long expired = objectArray[3] != null ? ((BigInteger) objectArray[3]).longValue() : 0L;
//                TotalAndResolvedCountDTO officeCountDTO = listOfOfficesWithCounts.stream()
//                        .filter(m -> m.getOfficeId().equals(officeId))
//                        .findFirst()
//                        .orElse(null);
//                officeCountDTO.setResolvedCount(resolved);
//                officeCountDTO.setTotalCount(total);
//                officeCountDTO.setExpiredCount(expired);
//                if(total > 0) {
//                    Double rate = (resolved * 100.00D) / total;
//                    officeCountDTO.setRate((Double) (Math.round(rate * 100.0) / 100.0));
//                }
//            }
//        }


        HashMap<Long, Long> officeIdToSubmitted = new HashMap<>();
        HashMap<Long, Long> officeIdToResolved = new HashMap<>();
        HashMap<Long, Long> officeIdToExpired = new HashMap<>();

        for (Object o : submittedObjects) {
            Object[] objectArray = (Object[]) o;
            if (objectArray != null) {
                Long officeId = ((BigInteger) objectArray[0]).longValue();
                Long total = objectArray[1] != null ? ((BigInteger) objectArray[1]).longValue() : 0L;
                officeIdToSubmitted.put(officeId, total);
            }
        }

        for (Object o : resolvedObjects) {
            Object[] objectArray = (Object[]) o;
            if (objectArray != null) {
                Long officeId = ((BigInteger) objectArray[0]).longValue();
                Long total = objectArray[1] != null ? ((BigInteger) objectArray[1]).longValue() : 0L;
                officeIdToResolved.put(officeId, total);
            }
        }

        for (Object o : expiredObjects) {
            Object[] objectArray = (Object[]) o;
            if (objectArray != null) {
                Long officeId = ((BigInteger) objectArray[0]).longValue();
                Long total = objectArray[1] != null ? ((BigInteger) objectArray[1]).longValue() : 0L;
                officeIdToExpired.put(officeId, total);
            }
        }


        for (Object o : submittedObjects) {
            Object[] objectArray = (Object[]) o;
            if (objectArray != null) {
                Long officeId = ((BigInteger) objectArray[0]).longValue();
                Long total = officeIdToSubmitted.getOrDefault(officeId, 0L);
                Long resolved = officeIdToResolved.getOrDefault(officeId, 0L);
                Long expired = officeIdToExpired.getOrDefault(officeId, 0L);
                TotalAndResolvedCountDTO officeCountDTO = listOfOfficesWithCounts.stream()
                        .filter(m -> m.getOfficeId().equals(officeId))
                        .findFirst()
                        .orElse(null);
                officeCountDTO.setResolvedCount(resolved);
                officeCountDTO.setTotalCount(total);
                officeCountDTO.setExpiredCount(expired);
                if (total > 0) {
                    Double rate = (resolved * 100.00D) / (total + resolved);
                    officeCountDTO.setRate((Double) (Math.round(rate * 100.0) / 100.0));
                }
            }
        }


        Collections.sort(listOfOfficesWithCounts, (o1, o2) -> {
            int rateComparisonValue = o2.getRate().compareTo(o1.getRate());
            if (rateComparisonValue == 0) {
                return o2.getTotalCount().compareTo(o1.getTotalCount());
            }
            return rateComparisonValue;
        });
        return listOfOfficesWithCounts;
    }

    public List<TotalAndResolvedCountDTO> getTotalSubmittedAndResolvedCountsOfMinistries(Integer year, Integer month) {
        Calendar c = Calendar.getInstance();
        Long diff = (long) (year * 12 + month) - (c.get(Calendar.YEAR) * 12 + c.get(Calendar.MONTH) + 1);

        c.set(year, month-1, 1, 0, 0, 0);
        Date fromDate = c.getTime();


        List<ChildOfficesDashboardNavigatorDTO> ministryLevelOffices = getGrsEnabledMinistryLevelOffices();
        List<Long> officeIds = ministryLevelOffices.stream()
                .map(ChildOfficesDashboardNavigatorDTO::getId)
                .collect(Collectors.toList());
        Object[] submittedObjects = dashboardDataDAO.getSubmittedCountByOfficeIdInList(diff, officeIds);
        Object[] resolvedObjects = dashboardDataDAO.getResolvedCountByOfficeIdInList(diff, officeIds);
        Object[] expiredObjects = dashboardDataDAO.getExpiredCountByOfficeIdInList(fromDate, officeIds);
        List<TotalAndResolvedCountDTO> listOfOfficesWithCounts = new ArrayList();
        ministryLevelOffices.forEach(ministry -> {
            listOfOfficesWithCounts.add(
                    TotalAndResolvedCountDTO.builder()
                            .officeId(ministry.getId())
                            .officeName(ministry.getName())
                            .resolvedCount(0L)
                            .totalCount(0L)
                            .rate(0D)
                            .build()
            );
        });


        HashMap<Long, Long> officeIdToSubmitted = new HashMap<>();
        HashMap<Long, Long> officeIdToResolved = new HashMap<>();
        HashMap<Long, Long> officeIdToExpired = new HashMap<>();

        for (Object o : submittedObjects) {
            Object[] objectArray = (Object[]) o;
            if (objectArray != null) {
                Long officeId = ((BigInteger) objectArray[0]).longValue();
                Long total = objectArray[1] != null ? ((BigInteger) objectArray[1]).longValue() : 0L;
                officeIdToSubmitted.put(officeId, total);
            }
        }

        for (Object o : resolvedObjects) {
            Object[] objectArray = (Object[]) o;
            if (objectArray != null) {
                Long officeId = ((BigInteger) objectArray[0]).longValue();
                Long total = objectArray[1] != null ? ((BigInteger) objectArray[1]).longValue() : 0L;
                officeIdToResolved.put(officeId, total);
            }
        }

        for (Object o : expiredObjects) {
            Object[] objectArray = (Object[]) o;
            if (objectArray != null) {
                Long officeId = ((BigInteger) objectArray[0]).longValue();
                Long total = objectArray[1] != null ? ((BigInteger) objectArray[1]).longValue() : 0L;
                officeIdToExpired.put(officeId, total);
            }
        }


        for (Object o : submittedObjects) {
            Object[] objectArray = (Object[]) o;
            if (objectArray != null) {
                Long officeId = ((BigInteger) objectArray[0]).longValue();
                Long total = officeIdToSubmitted.getOrDefault(officeId, 0L);
                Long resolved = officeIdToResolved.getOrDefault(officeId, 0L);
                Long expired = officeIdToExpired.getOrDefault(officeId, 0L);
                TotalAndResolvedCountDTO officeCountDTO = listOfOfficesWithCounts.stream()
                        .filter(m -> m.getOfficeId().equals(officeId))
                        .findFirst()
                        .orElse(null);
                officeCountDTO.setResolvedCount(resolved);
                officeCountDTO.setTotalCount(total);
                officeCountDTO.setExpiredCount(expired);
                if (total > 0) {
                    Double rate = (resolved * 100.00D) / (total + resolved);
                    officeCountDTO.setRate((Double) (Math.round(rate * 100.0) / 100.0));
                }
            }
        }


        Collections.sort(listOfOfficesWithCounts, (o1, o2) -> {
            int rateComparisonValue = o2.getRate().compareTo(o1.getRate());
            if (rateComparisonValue == 0) {
                return o2.getTotalCount().compareTo(o1.getTotalCount());
            }
            return rateComparisonValue;
        });
        return listOfOfficesWithCounts;
    }

    public List<ChildOfficesDashboardNavigatorDTO> getGrsEnabledMinistryLevelOffices() {
        List<OfficeLayer> officeLayerList = officeService.getOfficeLayersByLayerLevel(1);
        List<Office> offices = officeService.getOfficesByOfficeLayer(officeLayerList, false);
        return getGrsEnabledOfficesFromOfficeList(offices);
    }

    public CentralDashboardDataDTO getCentralDashboardData() {
        return CentralDashboardDataDTO.builder()
                .total(dashboardDataDAO.countTotalGrievances(0L))
                .resolved(dashboardDataDAO.countResolvedGrievances(0L))
                .ascertain(dashboardDataDAO.countGrievanceAscertainFromLastMonth(0L))
                .timeExpiredComplaints(dashboardDataDAO.countTImeExpiredComplaints())
                .totalAppeal(dashboardDataDAO.countTotalAppeals(0L))
                .resolvedAppeal(dashboardDataDAO.countResolvedAppeals(0L))
                .timeExpiredAppeal(dashboardDataDAO.countTimeExpiredAppeals())
                .grievanceListDTO(getCitizenCharterServicesByComplaintFrequency())
                .build();
    }

    public CentralDashboardDataDTO getCentralDashboardData(Integer year, Integer month) {


        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1, 0, 0, 0);
        Date fromDate = calendar.getTime();

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.SECOND, -1);
        Date toDate = calendar.getTime();

        calendar.set(year, 0, 1, 0, 0, 0);
        Date fromDateYear = calendar.getTime();
        calendar.set(year, 11, 31, 23, 59, 59);
        Date toDatYear = calendar.getTime();


        CentralDashboardDataDTO centralDashboardDataDTO = CentralDashboardDataDTO.builder()
                .total(dashboardDataDAO.countTotalGrievances(fromDate, toDate))
                .resolved(dashboardDataDAO.countResolvedGrievances(fromDate, toDate))
                .ascertain(dashboardDataDAO.countGrievanceAscertainFromLastMonth(fromDate, toDate))
                .timeExpiredComplaints(dashboardDataDAO.countTImeExpiredComplaints(fromDate ))
                .totalAppeal(dashboardDataDAO.countTotalAppeals(fromDate, toDate))
                .resolvedAppeal(dashboardDataDAO.countResolvedAppeals(fromDate, toDate))
                .timeExpiredAppeal(dashboardDataDAO.countTimeExpiredAppeals(fromDate ))
                .grievanceListDTO(getCitizenCharterServicesByComplaintFrequency(fromDateYear, toDatYear))
                .build();

        return centralDashboardDataDTO;
    }


    public List<CentralDashboardRecipientDTO> getAllCentralDashboardRecipients() {
        List<CentralDashboardRecipient> recipientList = centralDashboardRecipientDAO.findAll();
        return recipientList.stream()
                .map(this::convertToCentralDashboardRecipientDTO)
                .collect(Collectors.toList());
    }

    public CentralDashboardRecipientDTO convertToCentralDashboardRecipientDTO(CentralDashboardRecipient recipient) {
        Long officeId = recipient.getOfficeId();
        Long officeUnitOrganogramId = recipient.getOfficeUnitOrganogramId();
        EmployeeOffice employeeOffice = officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(officeId, officeUnitOrganogramId, true);
        if (employeeOffice == null) {
            return CentralDashboardRecipientDTO.builder()
                    .id(recipient.getId())
                    .nameBangla("তালিকাভুক্ত ব্যক্তিকে পাওয়া যাচ্ছেনা")
                    .nameEnglish("")
                    .designation("")
                    .officeId(0L)
                    .officeNameBangla("")
                    .officeNameEnglish("")
                    .officeUnitId(0L)
                    .officeUnitNameBangla("")
                    .officeUnitNameEnglish("")
                    .phoneNumber("")
                    .email("")
                    .status(recipient.getStatus())
                    .build();
        }
        SingleRoleDTO role = officeService.findSingleRole(officeId, officeUnitOrganogramId);
        EmployeeRecord employeeRecord = employeeOffice.getEmployeeRecord();
        return CentralDashboardRecipientDTO.builder()
                .id(recipient.getId())
                .nameBangla(employeeRecord.getNameBangla())
                .nameEnglish(employeeRecord.getNameEnglish())
                .designation(role.getDesignation())
                .officeId(role.getOfficeId())
                .officeNameBangla(role.getOfficeNameBangla())
                .officeNameEnglish(role.getOfficeNameEnglish())
                .officeUnitId(role.getOfficeUnitId())
                .officeUnitNameBangla(role.getOfficeUnitNameBangla())
                .officeUnitNameEnglish(role.getOfficeUnitNameEnglish())
                .phoneNumber(role.getPhone())
                .email(role.getEmail())
                .status(recipient.getStatus())
                .build();
    }

    public CentralDashboardRecipientDTO addNewCentralDashboardRecipients(AddCentralDashboardRecipientDTO dashboardRecipientDTO) {
        Long officeId = dashboardRecipientDTO.getOfficeId();
        Long officeUnitOrganogramId = dashboardRecipientDTO.getOfficeUnitOrganogramId();
        CentralDashboardRecipient recipient = centralDashboardRecipientDAO.findByOfficeIdAndOfficeUnitOrganogramId(officeId, officeUnitOrganogramId);
        if (recipient != null) {
            return null;
        }
        recipient = CentralDashboardRecipient.builder()
                .officeId(officeId)
                .officeUnitOrganogramId(officeUnitOrganogramId)
                .status(true)
                .build();
        centralDashboardRecipientDAO.save(recipient);
        CentralDashboardRecipientDTO recipientDTO = convertToCentralDashboardRecipientDTO(recipient);
        return recipientDTO;
    }

    public Boolean changeCentralDashboardRecipientStatus(Long id, Boolean status) {
        try {
            CentralDashboardRecipient recipient = centralDashboardRecipientDAO.findOne(id);
            recipient.setStatus(status);
            centralDashboardRecipientDAO.save(recipient);
            return true;
        } catch (NullPointerException npe) {
            return false;
        }
    }

    public Boolean deleteCentralDashboardRecipient(Long id) {
        try {
            centralDashboardRecipientDAO.delete(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public WeakHashMap<String, String> getComplainantInfoAndServiceName(DashboardData dashboardData) {
        String email = "", phoneNumber = "", name = "", serviceName = "";
        WeakHashMap<String, String> map = new WeakHashMap();
        if (dashboardData.getGrievanceType().equals(ServiceType.NAGORIK)) {
            if (dashboardData.getComplainantId() > 0L) {
                Complainant complainant = complainantService.findOne(dashboardData.getComplainantId());
                if (complainant != null) {
                    email = complainant.getEmail();
                    phoneNumber = complainant.getPhoneNumber();
                    name = complainant.getName();
                }
            }
        } else {
            EmployeeRecord employeeRecord = this.officeService.findEmployeeRecordById(dashboardData.getComplainantId());
            email = employeeRecord.getPersonalEmail();
            phoneNumber = employeeRecord.getPersonalMobile();
            name = employeeRecord.getNameBangla();
        }
        if (dashboardData.getServiceId() != null) {
            ServiceOrigin serviceOrigin = officeService.getServiceOrigin(dashboardData.getServiceId());
            serviceName = (serviceOrigin != null ? serviceOrigin.getServiceNameBangla() : "");
        } else {
            Grievance grievance = grievanceService.findGrievanceById(dashboardData.getGrievanceId());
            serviceName = (grievance != null ? grievance.getOtherService() : "");
        }
        map.put("name", name);
        map.put("email", email);
        map.put("phoneNumber", phoneNumber);
        map.put("serviceName", serviceName);
        return map;
    }

    public List<RegisterDTO> getDashboardDataForGrievanceRegister(Long officeId) {
        List<DashboardData> dashboardDataList = dashboardDataDAO.getDashboardDataForCurrentMonthGrievanceRegister(0L, officeId);
        List<RegisterDTO> registerEntries = new ArrayList();
        long index = 0;
        for (DashboardData dashboardData : dashboardDataList) {
            WeakHashMap<String, String> complainantAndServiceInfo = getComplainantInfoAndServiceName(dashboardData);
            RegisterDTO registerDTO = RegisterDTO.builder()
                    .id(++index)
                    .dateBng(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(dashboardData.getCreatedAt())))
                    .subject(dashboardData.getSubject())
                    .grievanceId(dashboardData.getGrievanceId())
                    .complainantEmail(complainantAndServiceInfo.get("email"))
                    .complainantMobile(complainantAndServiceInfo.get("phoneNumber"))
                    .complainantName(complainantAndServiceInfo.get("name"))
                    .caseNumber(dashboardData.getCaseNumber())
                    .service(complainantAndServiceInfo.get("serviceName"))
                    .serviceType(dashboardData.getGrievanceType())
                    .medium(dashboardData.getMediumOfSubmission())
                    .closingOrRejectingDateBng(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(dashboardData.getClosedDate())))
                    .closingOrRejectingDateEng(DateTimeConverter.convertDateToString(dashboardData.getClosedDate()))
                    .rootCause(dashboardData.getGroIdentifiedCause())
                    .remedyMeasures(dashboardData.getGroDecision())
                    .preventionMeasures(dashboardData.getGroSuggestion())
                    .build();
            registerEntries.add(registerDTO);
        }
        return registerEntries;
    }

    public RegisterDTO convertDashboardDataToRegisterDTO(DashboardData dashboardData) {
        WeakHashMap<String, String> complainantAndServiceInfo = getComplainantInfoAndServiceName(dashboardData);
        Date closedDate = dashboardData.getClosedDate();
        if (dashboardData.getComplaintStatus().toString().contains("FORWARDED")) {
            closedDate = dashboardData.getUpdatedAt();
        }
        return RegisterDTO.builder()
                .id(dashboardData.getGrievanceId())
                .dateBng(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(dashboardData.getCreatedAt())))
                .subject(dashboardData.getSubject())
                .grievanceId(dashboardData.getGrievanceId())
                .complainantEmail(complainantAndServiceInfo.get("email"))
                .complainantMobile(complainantAndServiceInfo.get("phoneNumber"))
                .complainantName(complainantAndServiceInfo.get("name"))
                .caseNumber(dashboardData.getCaseNumber())
                .trackingNumber(dashboardData.getTrackingNumber())
                .service(complainantAndServiceInfo.get("serviceName"))
                .serviceType(dashboardData.getGrievanceType())
                .medium(dashboardData.getMediumOfSubmission())
                .currentStatus(dashboardData.getComplaintStatus())
                .closingOrRejectingDateBng(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(closedDate)))
                .closingOrRejectingDateEng(DateTimeConverter.convertDateToString(closedDate))
                .rootCause(dashboardData.getGroIdentifiedCause())
                .remedyMeasures(dashboardData.getGroDecision())
                .preventionMeasures(dashboardData.getGroSuggestion())
                .build();
    }

    public RegisterDTO convertDashboardDataToAppealRegisterDTO(DashboardData dashboardData) {
        RegisterDTO registerDTO = convertDashboardDataToRegisterDTO(dashboardData);
        DashboardData dashboardDataInGrievancePhase = dashboardDataDAO.getDashboardDataForGrievancePhaseOfAppeal(dashboardData.getGrievanceId());
        Date grievanceClosingDate = dashboardDataInGrievancePhase != null ? dashboardDataInGrievancePhase.getClosedDate() : null;
        registerDTO.setClosingDateInGrievancePhase(grievanceClosingDate != null ? BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(grievanceClosingDate)) : null);
        return registerDTO;
    }

    public Page<RegisterDTO> getPageableDashboardDataForGrievanceRegister(Long officeId, String trackingNumber, Pageable pageable) {
        Page<DashboardData> dashboardDataList;

        if(trackingNumber != null) {
            // If tracking number is provided, search by officeId and tracking number
            dashboardDataList = dashboardDataDAO.getPageableDashboardDataForGrievanceRegisterByTrackingNumber(officeId, trackingNumber, pageable);
        } else {
            // If no tracking number is provided, return the default paginated data
            dashboardDataList = dashboardDataDAO.getPageableDashboardDataForGrievanceRegister(officeId, pageable);
        }

        return dashboardDataList.map(this::convertDashboardDataToRegisterDTO);
    }

    public Page<RegisterDTO> getPageableDashboardDataForAppealRegister(Long officeId, Pageable pageable) {
        Page<DashboardData> dashboardDataList = dashboardDataDAO.getPageableDashboardDataAppealRegister(officeId, pageable);
        return dashboardDataList.map(this::convertDashboardDataToAppealRegisterDTO);
    }

    public Page<RegisterDTO> getPageableDashboardDataForAppealedComplaints(Long officeId, Pageable pageable) {
        Page<DashboardData> dashboardDataList = dashboardDataDAO.getPageableDashboardDataForAppealedComplaints(officeId, pageable);
        return dashboardDataList.map(this::convertDashboardDataToRegisterDTO);
    }

    public Page<NudgeDTO> getPageableDashboardDataForTagidList(UserInformation userInformation, Pageable pageable) {
        Long officeId = userInformation.getOfficeInformation().getOfficeId();
        Long officeUnitOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();
        Page<Tagid> dashboardDataList = tagidDAO.findByOfficeIdAndOfficeUnitOrganogramId(officeId, officeUnitOrganogramId, pageable);
        return dashboardDataList.map(this::convertToNudgeDTO);
    }

    private NudgeDTO convertToNudgeDTO(Tagid tagid) {
        Grievance grievance = this.grievanceService.findGrievanceById(tagid.getComplaintId());
        return NudgeDTO.builder()
                .currentStatus(BanglaConverter.convertGrievanceStatusToBangla(grievance.getGrievanceCurrentStatus()))
                .dateOfNudge(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(tagid.getGivingDate())))
                .grievanceSubmissionDate(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(grievance.getSubmissionDate())))
                .officeName(tagid.getOfficeName())
                .subject(grievance.getSubject())
                .trackingNumber(grievance.getTrackingNumber())
                .id(grievance.getId())
                .build();
    }

    public List<RegisterDTO> getDashboardDataForAppealRegister(Long officeId) {
        List<DashboardData> dashboardDataList = dashboardDataDAO.getDashboardDataForCurrentMonthAppealRegister(0L, officeId);
        List<RegisterDTO> registerEntries = new ArrayList();
        long index = 0;
        for (DashboardData dashboardData : dashboardDataList) {
            WeakHashMap<String, String> complainantAndServiceInfo = getComplainantInfoAndServiceName(dashboardData);
            DashboardData dashboardDataInGrievancePhase = dashboardDataDAO.getDashboardDataForGrievancePhaseOfAppeal(dashboardData.getGrievanceId());
            Date grievanceClosingDate = dashboardDataInGrievancePhase != null ? dashboardDataInGrievancePhase.getClosedDate() : null;
            RegisterDTO registerDTO = RegisterDTO.builder()
                    .id(++index)
                    .dateBng(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(dashboardData.getCreatedAt())))
                    .subject(dashboardData.getSubject())
                    .grievanceId(dashboardData.getGrievanceId())
                    .complainantEmail(complainantAndServiceInfo.get("email"))
                    .complainantMobile(complainantAndServiceInfo.get("phoneNumber"))
                    .complainantName(complainantAndServiceInfo.get("name"))
                    .caseNumber(dashboardData.getCaseNumber())
                    .service(complainantAndServiceInfo.get("serviceName"))
                    .serviceType(dashboardData.getGrievanceType())
                    .medium(dashboardData.getMediumOfSubmission())
                    .closingDateInGrievancePhase(grievanceClosingDate != null ? BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(grievanceClosingDate)) : null)
                    .closingOrRejectingDateBng(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(dashboardData.getClosedDate())))
                    .rootCause(dashboardData.getAoIdentifiedCause())
                    .remedyMeasures(dashboardData.getAoDecision())
                    .preventionMeasures(dashboardData.getAoSuggestion())
                    .build();
            registerEntries.add(registerDTO);
        }
        return registerEntries;
    }

    public Long countTotalComplaintsByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataDAO.countTotalComplaintsByOfficeIdV2(officeId, monthDiff);
    }

    public Long countResolvedComplaintsByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataDAO.countResolvedComplaintsByOfficeId(officeId, monthDiff);
    }

    public Long countTimeExpiredComplaintsByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataDAO.countTimeExpiredComplaintsByOfficeId(officeId, monthDiff);
    }

    public Long countAllTimeExpiredComplaintsByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataDAO.countAllTimeExpiredComplaintsByOfficeId(officeId, monthDiff);
    }

    public Long countRunningGrievancesByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataDAO.countRunningGrievancesByOfficeId(officeId, monthDiff);
    }

    public Long countAllRunningGrievancesByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataDAO.countAllRunningGrievancesByOfficeId(officeId, monthDiff);
    }

    public Long countDeclinedGrievancesByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataDAO.countDeclinedGrievancesByOfficeId(officeId, monthDiff);
    }

    public Long countTotalAppealsByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataDAO.countTotalAppealsByOfficeId(officeId, monthDiff);
    }

    public Long countResolvedAppealsByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataDAO.countResolvedAppealsByOfficeId(officeId, monthDiff);
    }

    public Long countRunningAppealsByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataDAO.countRunningAppealsByOfficeId(officeId, monthDiff);
    }

    public Long countTimeExpiredAppealsByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataDAO.countTimeExpiredAppealsByOfficeId(officeId);
    }

    public Long countTotalComplaintsByOfficeIdV2(Long officeId, Long monthDiff) {
        return dashboardDataDAO.countTotalComplaintsByOfficeIdV2(officeId, monthDiff);
    }


    public Long countResolvedComplaintsByOfficeIdV2(Long officeId, Long monthDiff) {
        return dashboardDataDAO.countResolvedComplaintsByOfficeIdV2(officeId, monthDiff);
    }

    public Long countTimeExpiredComplaintsByOfficeIdV3(Long officeId, Long monthDiff) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, monthDiff.intValue());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Long days = CalendarUtil.getWorkDaysCountBefore(calendar.getTime(), (int) Constant.GRIEVANCE_EXPIRATION_TIME);
        try {
            return dashboardDataDAO.countTimeExpiredComplaintsByOfficeIdV2(officeId, monthDiff, days);
        } catch (Throwable t) {
            t.printStackTrace();
            return 0L;
        }
    }
    public Long countRunningGrievancesByOfficeIdV2(Long officeId, Long monthDiff) {
        return dashboardDataDAO.countRunningGrievancesByOfficeIdV2(officeId, monthDiff);
    }
    public Long countInheritedComplaintsByOfficeId(Long officeId, Long monthDiff) {
        return dashboardDataDAO.countInheritedComplaintsByOfficeId(officeId, monthDiff);
    }


    public Long countForwardedGrievancesByOfficeIdV2(Long officeId, Long monthDiff) {
        return dashboardDataDAO.countForwardedGrievancesByOfficeIdV2(officeId, monthDiff);
    }

    public Long countTotalAppealsByOfficeIdV2(Long officeId, Long monthDiff) {
        return dashboardDataDAO.countTotalAppealsByOfficeIdV2(officeId, monthDiff);
    }

    public Long countResolvedAppealsByOfficeIdV2(Long officeId, Long monthDiff) {
        return dashboardDataDAO.countResolvedAppealsByOfficeIdV2(officeId, monthDiff);
    }

    public Long countTimeExpiredAppealsByOfficeIdV2(Long officeId, Long monthDiff) {
        return dashboardDataDAO.countTimeExpiredAppealsByOfficeIdV2(officeId, monthDiff);
    }

    public Long countRunningAppealsByOfficeIdV2(Long officeId, Long monthDiff) {
        return dashboardDataDAO.countRunningAppealsByOfficeIdV2(officeId, monthDiff);
    }

    public List<TotalResolvedByMonth> getTotalResolvedGrievancesByMonthOfCurrentYear(Long officeId) {
        List<TotalResolvedByMonth> totalResolvedByMonthList = new ArrayList();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        List<MonthlyReport> reportList = monthlyReportDAO.findByOfficeIdAndYear(officeId, currentYear);
        for (int i = 0; i < 12; i++) {
            TotalResolvedByMonth totalResolvedByMonth = new TotalResolvedByMonth();
            totalResolvedByMonth.month = i;
            Integer monthNumber = i + 1;
            MonthlyReport monthlyReport = reportList.stream().filter(r -> r.getMonth().equals(monthNumber)).findFirst().orElse(null);
            if (monthlyReport != null) {
                totalResolvedByMonth.total = monthlyReport.getTotalCount();
                totalResolvedByMonth.resolved = monthlyReport.getResolvedCount();
            }
            if (i == currentMonth) {
                totalResolvedByMonth.total = dashboardDataDAO.countTotalComplaintsByOfficeIdV2(officeId, 0L);
                totalResolvedByMonth.resolved = dashboardDataDAO.countResolvedComplaintsByOfficeId(officeId, 0L);
            }
            totalResolvedByMonthList.add(totalResolvedByMonth);
        }
        return totalResolvedByMonthList;
    }

    public List<TotalResolvedByMonth> getTotalResolvedGrievancesByMonthOfCurrentYear(Long officeId, Integer year, Integer month) {
        List<TotalResolvedByMonth> totalResolvedByMonthList = new ArrayList();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        List<MonthlyReport> reportList = month == null ?
                monthlyReportDAO.findByOfficeIdAndYear(officeId, year != null ? year : currentYear) :
                monthlyReportDAO.findByOfficeIdAndMonthAndYear(officeId, month, (year != null ? year : currentYear));
        for (int i = 0; i < 12; i++) {
            TotalResolvedByMonth totalResolvedByMonth = new TotalResolvedByMonth();
            totalResolvedByMonth.month = i;
            Integer monthNumber = i + 1;
            MonthlyReport monthlyReport = reportList.stream().filter(r -> r.getMonth().equals(monthNumber)).findFirst().orElse(null);
            if (monthlyReport != null) {
                totalResolvedByMonth.year = monthlyReport.getYear();
                totalResolvedByMonth.total = monthlyReport.getTotalCount();
                totalResolvedByMonth.resolved = monthlyReport.getResolvedCount();
            }
            if ((year == null || year == currentYear)
                    && (month == null || month == currentMonth + 1)
                    && i == currentMonth) {
                totalResolvedByMonth.total = dashboardDataDAO.countTotalComplaintsByOfficeIdV2(officeId, 0L);
                totalResolvedByMonth.resolved = dashboardDataDAO.countResolvedComplaintsByOfficeId(officeId, 0L);
            }
            totalResolvedByMonthList.add(totalResolvedByMonth);
        }
        return totalResolvedByMonthList;
    }

    public List<TotalResolvedByMonth> getTotalResolvedGrievancesByMonthOfCurrentYear(Integer layerLevel, Long officeOrigin, Long officeId, Integer year, Integer month, Boolean grsEnabled) {

        List<Long> officeIds = new ArrayList();
        if (layerLevel >= 0 && layerLevel <= 2) {

            officeIds = getOfficeIdsByOfficeLayerLevel(layerLevel, officeId, grsEnabled, officeIds);
        } else {

            officeIds = getOfficeIdsByOfficeOrigin(layerLevel, officeOrigin, officeId, grsEnabled, officeIds);

        }
        if (officeIds.size() == 0)
            return IntStream.range(0, 12).mapToObj(Integer::new).map(e -> new TotalResolvedByMonth())
                    .collect(Collectors.toList());


        List<TotalResolvedByMonth> totalResolvedByMonthList = new ArrayList();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        List<MonthlyReport> reportList = month == null ?
                monthlyReportDAO.findByOfficeIdsAndYearGroupByYearAndMonthOrderByYearAndMonth(officeIds, year != null ? year : currentYear) :
                monthlyReportDAO.findByOfficeIdsAndYearAndMonthGroupByYearAndMonthOrderByYearAndMonth(officeIds, (year != null ? year : currentYear), month);


        for (int i = 0; i < 12; i++) {
            TotalResolvedByMonth totalResolvedByMonth = new TotalResolvedByMonth();
            totalResolvedByMonth.month = i;
            Integer monthNumber = i + 1;
            MonthlyReport monthlyReport = reportList.stream().filter(r -> r.getMonth().equals(monthNumber)).findFirst().orElse(null);
            if (monthlyReport != null) {
                totalResolvedByMonth.year = monthlyReport.getYear();
                totalResolvedByMonth.total += monthlyReport.getTotalCount();
                totalResolvedByMonth.resolved += monthlyReport.getResolvedCount();
            }
            if ((year == null || year == currentYear)
                    && (month == null || month == currentMonth + 1)
                    && i == currentMonth) {
                totalResolvedByMonth.total += dashboardDataDAO.countTotalComplaintsByOfficeIdsV2(officeIds, 0L);
                totalResolvedByMonth.resolved += dashboardDataDAO.countResolvedComplaintsByOfficeIds(officeIds, 0L);
            }
            totalResolvedByMonthList.add(totalResolvedByMonth);
        }
        return totalResolvedByMonthList;
    }


    public List<TotalResolvedByMonth> getTotalResolvedAppealByMonthOfCurrentYear(Integer layerLevel, Long officeOrigin, Long officeId, Integer year, Integer month, Boolean grsEnabled) {

        List<Long> officeIds = new ArrayList();
        if (layerLevel >= 0 && layerLevel <= 2) {

            officeIds = getOfficeIdsByOfficeLayerLevel(layerLevel, officeId, grsEnabled, officeIds);
        } else {

            officeIds = getOfficeIdsByOfficeOrigin(layerLevel, officeOrigin, officeId, grsEnabled, officeIds);

        }

        if (officeIds.size() == 0)
            return IntStream.range(0, 12).mapToObj(Integer::new).map(e -> new TotalResolvedByMonth())
                    .collect(Collectors.toList());

        List<TotalResolvedByMonth> totalResolvedByMonthList = new ArrayList();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        List<MonthlyReport> reportList = month == null ?
                monthlyReportDAO.findByOfficeIdsAndYearGroupByYearAndMonthOrderByYearAndMonth(officeIds, year != null ? year : currentYear) :
                monthlyReportDAO.findByOfficeIdsAndYearAndMonthGroupByYearAndMonthOrderByYearAndMonth(officeIds, (year != null ? year : currentYear), month);


        for (int i = 0; i < 12; i++) {
            TotalResolvedByMonth totalResolvedByMonth = new TotalResolvedByMonth();
            totalResolvedByMonth.month = i;
            Integer monthNumber = i + 1;
            MonthlyReport monthlyReport = reportList.stream().filter(r -> r.getMonth().equals(monthNumber)).findFirst().orElse(null);
            if (monthlyReport != null) {
                totalResolvedByMonth.year = monthlyReport.getYear();
                totalResolvedByMonth.total += monthlyReport.getAppealTotalCount();
                totalResolvedByMonth.resolved += monthlyReport.getAppealResolvedCount();
            }
            if ((year == null || year == currentYear)
                    && (month == null || month == currentMonth + 1)
                    && i == currentMonth) {

                totalResolvedByMonth.total = dashboardDataDAO.countTotalAppealsByOfficeIds(officeIds, 0L);
                totalResolvedByMonth.resolved = dashboardDataDAO.countResolvedAppealsByOfficeIds(officeIds, 0L);
            }
            totalResolvedByMonthList.add(totalResolvedByMonth);
        }
        return totalResolvedByMonthList;
    }


    public GeneralDashboardDataDTO getTotalSummaryGrievancesByOfficeAndYearAndMonth(Integer layerLevel, Long officeOrigin, Long officeId, Integer year, Integer month, Boolean grsEnabled) {
        List<Long> officeIds = new ArrayList();


        if (layerLevel >= 0 && layerLevel <= 2) {

            officeIds = getOfficeIdsByOfficeLayerLevel(layerLevel, officeId, grsEnabled, officeIds);
        } else {

            officeIds = getOfficeIdsByOfficeOrigin(layerLevel, officeOrigin, officeId, grsEnabled, officeIds);

        }


        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);

        Long totalComplaintsCount = 0L;
        Long resolvedComplaintsCount = 0L;
        Long unresolvedComplaintsCount = 0L;
        Long runningComplaintsCount = 0L;
        Long declinedComplaintsCount = 0L;


        List<MonthlyReport> reportList = month == null ?
                monthlyReportDAO.findByOfficeIdInAndYear(officeIds, year != null ? year : currentYear) :
                monthlyReportDAO.findByOfficeIdInAndMonthAndYear(officeIds, month, (year != null ? year : currentYear));
        for (MonthlyReport monthlyReport : reportList) {

            totalComplaintsCount += monthlyReport.getTotalCount();
            resolvedComplaintsCount += monthlyReport.getResolvedCount();
            runningComplaintsCount += monthlyReport.getRunningCount();
            unresolvedComplaintsCount += monthlyReport.getSentToOtherCount();
            declinedComplaintsCount += monthlyReport.getTimeExpiredCount();

            if ((year != null) && (month != null) && (year == currentYear) && month == currentMonth) {
                totalComplaintsCount += dashboardDataDAO.countTotalComplaintsByOfficeIdsV2(officeIds, 0L);
                resolvedComplaintsCount += dashboardDataDAO.countResolvedComplaintsByOfficeIds(officeIds, 0L);
                runningComplaintsCount += dashboardDataDAO.countRunningGrievancesByOfficeIds(officeIds, 0L);
//                unresolvedComplaintsCount += dashboardDataDAO.count(officeIds, 0L);
                declinedComplaintsCount += dashboardDataDAO.countTimeExpiredComplaintsByOfficeIds(officeIds);
            }

        }
        GeneralDashboardDataDTO groDashboardData = constructGeneralDashboardDataDTO(totalComplaintsCount, resolvedComplaintsCount, declinedComplaintsCount, runningComplaintsCount, unresolvedComplaintsCount, false);

        return groDashboardData;
    }

    public GeneralDashboardDataDTO getTotalSummaryAppealByOfficeAndYearAndMonth(Integer layerLevel, Long officeOrigin, Long officeId, Integer year, Integer month, Boolean grsEnabled) {
        List<Long> officeIds = new ArrayList();
        if (layerLevel >= 0 && layerLevel <= 2) {

            officeIds = getOfficeIdsByOfficeLayerLevel(layerLevel, officeId, grsEnabled, officeIds);
        } else {

            officeIds = getOfficeIdsByOfficeOrigin(layerLevel, officeOrigin, officeId, grsEnabled, officeIds);

        }
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);

        Long totalComplaintsCount = 0L;
        Long resolvedComplaintsCount = 0L;
        Long unresolvedComplaintsCount = 0L;
        Long runningComplaintsCount = 0L;
        Long declinedComplaintsCount = 0L;


        List<MonthlyReport> reportList = month == null ?
                monthlyReportDAO.findByOfficeIdInAndYear(officeIds, year != null ? year : currentYear) :
                monthlyReportDAO.findByOfficeIdInAndMonthAndYear(officeIds, month, (year != null ? year : currentYear));
        for (MonthlyReport monthlyReport : reportList) {

            totalComplaintsCount += monthlyReport.getAppealTotalCount();
            resolvedComplaintsCount += monthlyReport.getAppealResolvedCount();
            runningComplaintsCount += monthlyReport.getAppealRunningCount();
            declinedComplaintsCount += monthlyReport.getAppealTimeExpiredCount();

            if ((year != null) && (month != null) && (year == currentYear) && month == currentMonth) {
                totalComplaintsCount += dashboardDataDAO.countTotalAppealsByOfficeIds(officeIds, 0L);
                resolvedComplaintsCount += dashboardDataDAO.countResolvedAppealsByOfficeIds(officeIds, 0L);
                runningComplaintsCount += dashboardDataDAO.countAllRunningAppealsByOfficeIds(officeIds, 0L);
                declinedComplaintsCount += dashboardDataDAO.countAllTimeExpiredAppealsByOfficeIds(officeIds);
            }

        }
        GeneralDashboardDataDTO groDashboardData = constructGeneralDashboardDataDTO(totalComplaintsCount, resolvedComplaintsCount, declinedComplaintsCount, runningComplaintsCount, unresolvedComplaintsCount, false);

        return groDashboardData;
    }


    public List<TotalResolvedByYear> getTotalResolvedGrievancesByYearOfAllYear(Long officeId, Integer year) {
        List<TotalResolvedByYear> totalResolvedByMonthList = new ArrayList();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        List<MonthlyReport> reportList = monthlyReportDAO.findByOfficeIdGroupByYear(officeId, year);
        for (Integer i = (year != null ? year : 2018); i <= (year != null ? year : currentYear); i++) {
            TotalResolvedByYear totalResolvedByYear = new TotalResolvedByYear();
            totalResolvedByYear.year = i;
            Integer yearNumber = i;
            MonthlyReport monthlyReport = reportList.stream().filter(r -> r.getYear().equals(yearNumber)).findFirst().orElse(null);
            if (monthlyReport != null) {
                totalResolvedByYear.total = monthlyReport.getTotalCount();
                totalResolvedByYear.resolved = monthlyReport.getResolvedCount();
            }
            if ((year == null || year == currentYear) && i == currentMonth) {
                totalResolvedByYear.total += dashboardDataDAO.countTotalComplaintsByOfficeIdV2(officeId, 0L);
                totalResolvedByYear.resolved += dashboardDataDAO.countResolvedComplaintsByOfficeId(officeId, 0L);
            }
            totalResolvedByMonthList.add(totalResolvedByYear);
        }
        return totalResolvedByMonthList;
    }

    public List<TotalResolvedByMonth> getTotalResolvedAppealsByMonthOfCurrentYear(Long officeId) {
        List<TotalResolvedByMonth> totalResolvedByMonthList = new ArrayList();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        List<MonthlyReport> reportList = monthlyReportDAO.findByOfficeIdAndYear(officeId, currentYear);
        for (int i = 0; i < 12; i++) {
            TotalResolvedByMonth totalResolvedByMonth = new TotalResolvedByMonth();
            totalResolvedByMonth.month = i;
            Integer monthNumber = i + 1;
            MonthlyReport monthlyReport = reportList.stream().filter(r -> r.getMonth().equals(monthNumber)).findFirst().orElse(null);
            if (monthlyReport != null) {
                totalResolvedByMonth.total = monthlyReport.getAppealTotalCount();
                totalResolvedByMonth.resolved = monthlyReport.getAppealResolvedCount();
            }
            if (i == currentMonth) {
                totalResolvedByMonth.total = dashboardDataDAO.countTotalAppealsByOfficeId(officeId, 0L);
                totalResolvedByMonth.resolved = dashboardDataDAO.countResolvedAppealsByOfficeId(officeId, 0L);
            }
            totalResolvedByMonthList.add(totalResolvedByMonth);
        }
        return totalResolvedByMonthList;
    }


    public YearlyCounts getYearlyCounts() {
        YearlyDashboardStatistics yearlyDashboardStatistics = dashboardDataDAO.findYearlyStatistics();
        if (yearlyDashboardStatistics != null && yearlyDashboardStatistics.getId() != null) {
            return YearlyCounts.builder()
                    .totalComplaint(yearlyDashboardStatistics.getTotalComplaint())
                    .totalForwarded(yearlyDashboardStatistics.getTotalForwarded())
                    .totalResolved(yearlyDashboardStatistics.getTotalResolved()).build();
        } else {
            return YearlyCounts.builder()
                    .totalComplaint(dashboardDataDAO.countTotalGrievancesYearly())
                    .totalForwarded(dashboardDataDAO.countForwardedGrievancesYearly())
                    .totalResolved(dashboardDataDAO.countResolvedGrievancesYearly())
                    .build();
        }
    }

    public YearlyCounts getYearlyCounts(Integer year) {
        Calendar calendar = Calendar.getInstance();
        if (year < 2018 || year > calendar.getTime().getYear() + 1900) return YearlyCounts.builder().build();

        YearlyDashboardStatistics yearlyDashboardStatistics = dashboardDataDAO.findYearlyStatistics(year);
        if (yearlyDashboardStatistics != null && yearlyDashboardStatistics.getId() != null) {
            return YearlyCounts.builder()
                    .totalComplaint(yearlyDashboardStatistics.getTotalComplaint())
                    .totalForwarded(yearlyDashboardStatistics.getTotalForwarded())
                    .totalResolved(yearlyDashboardStatistics.getTotalResolved()).build();
        } else {

            calendar.set(year, 0, 1, 0, 0, 0);
            Date fromDate = calendar.getTime();

            calendar.set(year, 11, 31, 23, 59, 59);
            Date toDate = calendar.getTime();


            YearlyCounts yearlyCounts = YearlyCounts.builder()
                    .totalComplaint(dashboardDataDAO.countTotalGrievances(fromDate, toDate))
                    .totalForwarded(dashboardDataDAO.countForwardedGrievances(fromDate, toDate))
                    .totalResolved(dashboardDataDAO.countResolvedGrievances(fromDate, toDate))
                    .build();

            YearlyDashboardStatistics saveYearlyStatistics = new YearlyDashboardStatistics();
            saveYearlyStatistics.setId(year);
            saveYearlyStatistics.setTotalComplaint(yearlyCounts.getTotalComplaint());
            saveYearlyStatistics.setTotalForwarded(yearlyCounts.getTotalForwarded());
            saveYearlyStatistics.setTotalResolved(yearlyCounts.getTotalResolved());
            dashboardDataDAO.saveYearlyStatistics(saveYearlyStatistics);

            return yearlyCounts;

        }
    }

    public List<TotalAndResolvedCountDTO> getSubordinateTotalSubmittedAndResolvedCountsOfMinistries(boolean fromController) {
        List<TotalAndResolvedCountDTO> totalAndResolvedCountDTOS = new ArrayList<>();
        List<DashboardTotalResolved> list = fromController ? dashboardTotalResolvedRepo.findAll() : null;
        if (list != null && list.size() > 0) {
            for (DashboardTotalResolved eo : list) {
                totalAndResolvedCountDTOS.add(
                        TotalAndResolvedCountDTO.builder()
                                .officeId(eo.getOfficeId())
                                .totalCount(eo.getTotalCount())
                                .resolvedCount(eo.getResolvedCount())
                                .expiredCount(eo.getExpiredCount())
                                .rate(eo.getRate())
                                .build());
            }
        } else {
            List<ChildOfficesDashboardNavigatorDTO> ministryLevelOffices = getGrsEnabledMinistryLevelOffices();
            List<Long> officeIds = ministryLevelOffices.stream()
                    .map(ChildOfficesDashboardNavigatorDTO::getId)
                    .collect(Collectors.toList());
            officeIds.forEach(officeId -> {
                GeneralDashboardDataDTO dataDTO = getSubOfficesAggregatedDataForGrievances(officeId);
                Double rate = 0D;
                Long total = dataDTO.getTotal().getValue();
                Long resolved = dataDTO.getResolved().getValue();
                Long forwarded = dataDTO.getDeclined().getValue();
                if (total > 0) {
                    Double tempRate = ((resolved + forwarded) * 100.00D) / total;
                    rate = Math.round(tempRate * 100.0) / 100.0;
                }
                totalAndResolvedCountDTOS.add(
                        TotalAndResolvedCountDTO.builder()
                                .officeId(officeId)
                                .totalCount(dataDTO.getTotal().getValue())
                                .resolvedCount(dataDTO.getResolved().getValue())
                                .expiredCount(dataDTO.getUnresolved().getValue())
                                .rate(rate)
                                .build()
                );
            });
        }
        return totalAndResolvedCountDTOS;
    }

    public List<TotalAndResolvedCountDTO> getSubordinateTotalSubmittedAndResolvedCountsOfMinistries(Integer year, Integer month, boolean fromController) {
        List<TotalAndResolvedCountDTO> totalAndResolvedCountDTOS = new ArrayList<>();
        List<DashboardTotalResolved> list = fromController ? dashboardTotalResolvedRepo.findAll() : null;
        if (list != null && list.size() > 0) {
            for (DashboardTotalResolved eo : list) {
                totalAndResolvedCountDTOS.add(
                        TotalAndResolvedCountDTO.builder()
                                .officeId(eo.getOfficeId())
                                .totalCount(eo.getTotalCount())
                                .resolvedCount(eo.getResolvedCount())
                                .expiredCount(eo.getExpiredCount())
                                .rate(eo.getRate())
                                .build());
            }
        } else {
            List<ChildOfficesDashboardNavigatorDTO> ministryLevelOffices = getGrsEnabledMinistryLevelOffices();
            List<Long> officeIds = ministryLevelOffices.stream()
                    .map(ChildOfficesDashboardNavigatorDTO::getId)
                    .collect(Collectors.toList());
            officeIds.forEach(officeId -> {
                GeneralDashboardDataDTO dataDTO = getSubOfficesAggregatedDataForGrievances(year, month, officeId);
                Double rate = 0D;
                Long total = dataDTO.getTotal().getValue();
                Long resolved = dataDTO.getResolved().getValue();
                Long forwarded = dataDTO.getDeclined().getValue();
                if (total > 0) {
                    Double tempRate = ((resolved + forwarded) * 100.00D) / total;
                    rate = Math.round(tempRate * 100.0) / 100.0;
                }
                totalAndResolvedCountDTOS.add(
                        TotalAndResolvedCountDTO.builder()
                                .officeId(officeId)
                                .totalCount(dataDTO.getTotal().getValue())
                                .resolvedCount(dataDTO.getResolved().getValue())
                                .expiredCount(dataDTO.getUnresolved().getValue())
                                .rate(rate)
                                .build()
                );
            });
        }
        return totalAndResolvedCountDTOS;
    }

    public Long countResolvedComplaintsByOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataDAO.countResolvedComplaintsByOfficeIdAndYearAndMonth(officeId, year, month);
    }

    public Long countTimeExpiredComplaintsByOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataDAO.countTimeExpiredComplaintsByOfficeIdAndYearAndMonth(officeId, year, month);
    }

    public Long countRunningGrievancesByOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataDAO.countRunningGrievancesByOfficeIdAndYearAndMonth(officeId, year, month);
    }

    public Long countTotalComplaintsByOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataDAO.countTotalComplaintsByOfficeIdAndYearAndMonth(officeId, year, month);
    }

    public Long countDeclinedGrievancesByOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataDAO.countDeclinedGrievancesByOfficeIdAndYearAndMonth(officeId, year, month);
    }

    public Long getGrievanceAscertainCountbyOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataDAO.getGrievanceAscertainCountbyOfficeIdAndYearAndMonth(officeId, year, month);
    }

    public Long getMonthlyComplaintsCountByOfficeIdAndMediumOfSubmissionAndYearAndMonth(Long officeId, MediumOfSubmission mediumOfSubmission, String year, String month) {
        return this.dashboardDataDAO.getMonthlyComplaintsCountByOfficeIdAndMediumOfSubmissionAndYearAndMonth(officeId, mediumOfSubmission, year, month);
    }

    public Long countResolvedAppealByOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataDAO.countResolvedAppealByOfficeIdAndYearAndMonth(officeId, year, month);
    }

    public Long countTimeExpiredAppealByOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataDAO.countTimeExpiredAppealByOfficeIdAndYearAndMonth(officeId, year, month);
    }

    public Long countRunningAppealByOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataDAO.countRunningAppealByOfficeIdAndYearAndMonth(officeId, year, month);
    }

    public Long countTotalAppealByOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataDAO.countTotalAppealByOfficeIdAndYearAndMonth(officeId, year, month);
    }

    public Long getAppealAscertainCountByOfficeIdAndYearAndMonth(Long officeId, String year, String month) {
        return this.dashboardDataDAO.getAppealAscertainCountByOfficeIdAndYearAndMonth(officeId, year, month);
    }

    public Long getMonthlyAppealCountByOfficeIdAndMediumOfSubmissionAndYearAndMonth(Long officeId, MediumOfSubmission selfMotivatedAcceptance, String year, String month) {

        return this.dashboardDataDAO.getMonthlyAppealCountByOfficeIdAndMediumOfSubmissionAndYearAndMonth(officeId, selfMotivatedAcceptance, year, month);
    }

    public SafetyNetProgramReportResponse safetyNetProgramReport(UserInformation userInformation, String fromDate, String toDate) {
        Long officeId = null;
        if (userInformation.getOfficeInformation() != null && userInformation.getOfficeInformation().getOfficeId().intValue() != 28) {
            officeId = userInformation.getOfficeInformation().getOfficeId();
        }
        return this.dashboardDataDAO.safetyNetProgramReport(officeId, fromDate, toDate);
    }

    public SafetyNetProgramReportResponse safetyNetProgramReportByProgramId(UserInformation userInformation, String fromDate, String toDate, Integer programId) {
        Long officeId = null;
        if (userInformation.getOfficeInformation() != null && userInformation.getOfficeInformation().getOfficeId().intValue() != 28) {
            officeId = userInformation.getOfficeInformation().getOfficeId();
        }
        return this.dashboardDataDAO.safetyNetProgramReportByProgramId(officeId, fromDate, toDate, programId);
    }

    public SafetyNetSummaryResponse getSafetyNetSummary(UserInformation userInformation, Integer programId) {
        Long officeId = null;
        if (userInformation.getOfficeInformation() != null && userInformation.getOfficeInformation().getOfficeId().intValue() != 28) {
            officeId = userInformation.getOfficeInformation().getOfficeId();
        }

        return this.dashboardDataDAO.getSafetyNetSummary(officeId, programId);
    }
}
