package com.grs.core.dao;

import com.grs.api.model.GRSUserType;
import com.grs.api.model.UserInformation;
import com.grs.api.model.UserType;
import com.grs.api.model.request.FeedbackRequestDTO;
import com.grs.api.model.request.GrievanceRequestDTO;
import com.grs.api.model.response.GenericResponse;
import com.grs.api.model.response.GrievanceAdminDTO;
import com.grs.api.model.response.grievance.GrievanceDTO;
import com.grs.core.domain.*;
import com.grs.core.domain.grs.CitizenCharter;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.domain.grs.Grievance;
import com.grs.core.domain.grs.SafetyNetGrievance;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.model.ListViewType;
import com.grs.core.repo.grs.BaseEntityManager;
import com.grs.core.repo.grs.GrievanceForwardingRepo;
import com.grs.core.repo.grs.GrievanceRepo;
import com.grs.core.service.ComplainantService;
import com.grs.utils.*;
import com.grs.api.model.request.SafetyNetGrievanceSummaryRequest;
import javax.persistence.Query;
import javax.persistence.EntityManager;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Acer on 9/14/2017.
 */
@Slf4j
@Service
public class GrievanceDAO {
    @Autowired
    private GrievanceRepo grievanceRepo;
    @Autowired
    private CitizenCharterDAO citizenCharterDAO;
    @Autowired
    private ComplainantService complainantService;
    @Autowired
    private BaseEntityManager baseEntityManager;
    @Autowired
    private EntityManager entityManager;

    private SimpleDateFormat simpleDateFormat;

    @PostConstruct
    public void init() {
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }

    public Page<Grievance> findAll(Pageable pageable) {
        return this.grievanceRepo.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Grievance findOne(Long id) {
        return this.grievanceRepo.findOne(id);
    }

    public Grievance save(Grievance grievance) {
        return this.grievanceRepo.save(grievance);
    }

    public void save(List<Grievance> grievances) {
        this.grievanceRepo.save(grievances);
    }

    public Grievance addGrievance(UserInformation userInformation, GrievanceRequestDTO grievanceRequestDTO) {
        boolean isGrsUser = false;
        Boolean offlineGrievanceUploaded = false;
        Long uploaderGroOfficeUnitOrganogramId = null;
        Long userId;
        if (userInformation == null) {
            userId = 0L;
            isGrsUser = true;
        } else {
            isGrsUser = userInformation.getUserType().equals(UserType.COMPLAINANT);
            userId = isGrsUser ? userInformation.getUserId() : userInformation.getOfficeInformation().getEmployeeRecordId();
        }
        boolean isAnonymous = (grievanceRequestDTO.getIsAnonymous() != null && grievanceRequestDTO.getIsAnonymous());
        if(grievanceRequestDTO.getOfflineGrievanceUpload() != null && grievanceRequestDTO.getOfflineGrievanceUpload()){
            offlineGrievanceUploaded = true;
            uploaderGroOfficeUnitOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();
            if(!isAnonymous){
                Complainant complainant = this.complainantService.findComplainantByPhoneNumber(grievanceRequestDTO.getPhoneNumber());
                userId = complainant.getId();
            }
            isGrsUser = true;
        }
        Long officeId = Long.valueOf(grievanceRequestDTO.getOfficeId());
        CitizenCharter citizenCharter = this.citizenCharterDAO.findByOfficeIdAndServiceId(Long.valueOf(grievanceRequestDTO.getOfficeId()), Long.valueOf(grievanceRequestDTO.getServiceId()));
        GrievanceCurrentStatus currentStatus = (officeId == 0 ? GrievanceCurrentStatus.CELL_NEW : GrievanceCurrentStatus.NEW);
        Grievance grievance = Grievance.builder()
                .details(grievanceRequestDTO.getBody())
                .subject(grievanceRequestDTO.getSubject())
                .officeId(officeId)
                .officeLayers(grievanceRequestDTO.getOfficeLayers())
                .serviceOrigin(citizenCharter == null ? null : citizenCharter.getServiceOrigin())
                .serviceOriginBeforeForward(citizenCharter == null ? null : citizenCharter.getServiceOrigin())
                .grsUser(isGrsUser)
                .complainantId(isAnonymous ? 0L : userId)
                .grievanceType(grievanceRequestDTO.getServiceType())
                .grievanceCurrentStatus(currentStatus)
                .isAnonymous(isAnonymous)
                .trackingNumber(StringUtil.isValidString(grievanceRequestDTO.getServiceTrackingNumber()) ? grievanceRequestDTO.getServiceTrackingNumber() : this.getTrackingNumber())
                .otherService(grievanceRequestDTO.getServiceOthers())
                .otherServiceBeforeForward(grievanceRequestDTO.getServiceOthers())
                .serviceReceiver(grievanceRequestDTO.getServiceReceiver())
                .serviceReceiverRelation(grievanceRequestDTO.getRelation())
                .isOfflineGrievance(offlineGrievanceUploaded)
                .uploaderOfficeUnitOrganogramId(uploaderGroOfficeUnitOrganogramId)
                .isSelfMotivatedGrievance(grievanceRequestDTO.getIsSelfMotivated() != null && grievanceRequestDTO.getIsSelfMotivated())
                .sourceOfGrievance(grievanceRequestDTO.getSourceOfGrievance())
                .complaintCategory(grievanceRequestDTO.getGrievanceCategory())
                .spProgrammeId(StringUtil.isValidString(grievanceRequestDTO.getSpProgrammeId()) ? Integer.parseInt(grievanceRequestDTO.getSpProgrammeId()) : null)
                .geoDivisionId(StringUtil.isValidString(grievanceRequestDTO.getDivision()) ? Integer.parseInt(grievanceRequestDTO.getDivision()) : null)
                .geoDistrictId(StringUtil.isValidString(grievanceRequestDTO.getDistrict()) ? Integer.parseInt(grievanceRequestDTO.getDistrict()) : null)
                .geoUpazilaId(StringUtil.isValidString(grievanceRequestDTO.getUpazila()) ? Integer.parseInt(grievanceRequestDTO.getUpazila()) : null)
                .build();
//        grievance.setSafetyNet(grievanceRequestDTO.getSafetyNetId() >0 && grievanceRequestDTO.getDivisionId() >0 && grievanceRequestDTO.getDistrictId() >0 && grievanceRequestDTO.getUpazilaId() >0);
        grievance.setStatus(true);
        Grievance grievanceEO = this.save(grievance);
//        if (grievanceRequestDTO.getSafetyNetId() >0 && grievanceRequestDTO.getDivisionId() >0 && grievanceRequestDTO.getDistrictId() >0 && grievanceRequestDTO.getUpazilaId() >0) {
//            SafetyNetGrievance safetyNetGrievanceEO = new SafetyNetGrievance();
//            safetyNetGrievanceEO.setSafetyNetId(grievanceRequestDTO.getSafetyNetId());
//            safetyNetGrievanceEO.setGrievanceId(grievanceEO.getId());
//            safetyNetGrievanceEO.setDivisionId(grievanceRequestDTO.getDivisionId());
//            safetyNetGrievanceEO.setDistrictId(grievanceRequestDTO.getDistrictId());
//            safetyNetGrievanceEO.setUpazilaId(grievanceRequestDTO.getUpazilaId());
//            safetyNetGrievanceEO.setStatus(true);
//            baseEntityManager.save(safetyNetGrievanceEO);
//        }
        return grievanceEO;
    }

    public com.grs.api.model.response.grievance.SafetyNetGrievanceSummaryListDto getSafetyNetGrievanceSummary(SafetyNetGrievanceSummaryRequest request) {

        String sql = "";

        if (request.getFromDate() == null || request.getToDate() == null) {
            return new com.grs.api.model.response.grievance.SafetyNetGrievanceSummaryListDto();
        }

        if (request.getLanguageCode() != null && request.getLanguageCode().equalsIgnoreCase("en")) {
            sql = "select spp.name_en, division.division_name_eng, district.district_name_eng, upazila.upazila_name_eng,";
        } else {
            sql = "select spp.name_bn, division.division_name_bng, district.district_name_bng, upazila.upazila_name_bng,";
        }

        sql += " count(c.id), " +
                " sum(CASE WHEN (((d.complaint_status LIKE 'CLOSED_%' OR d.complaint_status LIKE '%REJECTED%')\n" +
                " AND d.closed_date IS NOT NULL)\n" +
                " OR (d.complaint_status LIKE 'FORWARDED_%' and d.closed_date IS NOT NULL)\n" +
                " OR (d.complaint_status LIKE 'FORWARDED_OUT' AND d.closed_date IS NOT NULL)) THEN 1 ELSE 0 END) as resolved " +
                " from sp_programme spp\n" +
                " left join complaints c on spp.id = c.sp_programme_id\n";

        if (request.getFromDate() != null) {
            SimpleDateFormat dmyFormat = new SimpleDateFormat("yyyy-MM-dd");
            String fromDateStr = dmyFormat.format(request.getFromDate());
            sql += " and (c.submission_date >= '" + fromDateStr+"' ";
        }
        if (request.getToDate() != null) {
            SimpleDateFormat dmyFormat = new SimpleDateFormat("yyyy-MM-dd");
            String toDateStr = dmyFormat.format(request.getToDate());
            sql += " and c.submission_date <= DATE_FORMAT('" + toDateStr+"', '%Y-%m-%d 23:59:59')) ";
        }

        sql += " left join dashboard_data d on c.id = d.complaint_id\n" +
                " left join grs_doptor.geo_divisions division on c.geo_division_id = division.id\n" +
                " left join grs_doptor.geo_districts district on c.geo_district_id = district.id\n" +
                " left join grs_doptor.geo_upazilas upazila on c.geo_upazila_id = upazila.id\n" +
                " where c.complaint_category=2\n";

        if (request.getSpProgrammeId() != null) {
            if (Utility.isNumber(request.getSpProgrammeId())) {
                if (Integer.parseInt(request.getSpProgrammeId()) > 0 ) {
                    sql += " and c.sp_programme_id = '" + request.getSpProgrammeId() + "'";
                } else {
                    sql += " OR c.id IS NULL ";
                }
            } else {
                sql += " OR c.id IS NULL ";
            }
        } else {
            sql += " OR c.id IS NULL ";
        }

        if (request.getDivision() != null) {
            if (Utility.isNumber(request.getDivision())) {
                if (Integer.parseInt(request.getDivision()) > 0 ) {
                    sql += " and c.geo_division_id = '" + request.getDivision() + "'";
                }
            }
        }
        if (request.getDistrict() != null) {
            if (Utility.isNumber(request.getDistrict())) {
                if (Integer.parseInt(request.getDistrict()) > 0 ) {
                    sql += " and c.geo_district_id = '" + request.getDistrict() + "'";
                }
            }
        }
        if (request.getUpazila() != null) {
            if (Utility.isNumber(request.getUpazila())) {
                if (Integer.parseInt(request.getUpazila()) > 0 ) {
                    sql += " and c.geo_upazila_id = '" + request.getUpazila() + "'";
                }
            }
        }

        String groupByQuery = " group by spp.id, division.id, district.id, upazila.id order by spp.id";
        sql += groupByQuery;

        List<com.grs.api.model.response.grievance.SafetyNetGrievanceSummaryDto> objectList =
                new ArrayList<>();

        javax.persistence.Query query = entityManager.createNativeQuery(sql);
        try {
            List<Object[]> list = query.getResultList();
            if (list != null) {
                for (Object[] row : list) {
                    com.grs.api.model.response.grievance.SafetyNetGrievanceSummaryDto dto =
                            new com.grs.api.model.response.grievance.SafetyNetGrievanceSummaryDto();

                    if (row[0] != null) {
                        dto.setSpProgramName(String.valueOf(row[0]));
                    }
                    if (row[1] != null) {
                        dto.setDivisionName(String.valueOf(row[1]));
                    }
                    if (row[2] != null) {
                        dto.setDistrictName(String.valueOf(row[2]));
                    }
                    if (row[3] != null) {
                        dto.setUpazilaName(String.valueOf(row[3]));
                    }
                    if (row[4] != null) {
                        dto.setTotalGrievances(Integer.parseInt(String.valueOf(row[4])));
                    }
                    if (row[5] != null) {
                        dto.setResolvedGrievances(Integer.parseInt(String.valueOf(row[5])));
                        dto.setOutstandingGrievances(dto.getTotalGrievances() - dto.getResolvedGrievances());
                    }

                    objectList.add(dto);
                }
            }

            com.grs.api.model.response.grievance.SafetyNetGrievanceSummaryListDto response =
                    new com.grs.api.model.response.grievance.SafetyNetGrievanceSummaryListDto();
            response.safetyNetGrievanceSummaryList = objectList;
            response.recordCount = objectList.size();
            return response;

        } catch (Exception ex) {
            System.out.println(ex.toString());
            return new com.grs.api.model.response.grievance.SafetyNetGrievanceSummaryListDto();
        }
    }

    public Grievance addGrievanceForOthers(UserInformation userInformation, GrievanceRequestDTO grievanceRequestDTO, long userIdFromToken, String sourceOfGrievance) {
        boolean isGrsUser = false;
        Long uploaderGroOfficeUnitOrganogramId = null;
        Long userId;
        if (userInformation == null) {
            userId = 0L;
            isGrsUser = true;
        } else {
            isGrsUser = userInformation.getUserType().equals(UserType.COMPLAINANT);
            userId = isGrsUser ? userInformation.getUserId() : userInformation.getOfficeInformation().getEmployeeRecordId();
        }
        boolean isAnonymous = (grievanceRequestDTO.getIsAnonymous() != null && grievanceRequestDTO.getIsAnonymous());
        if(grievanceRequestDTO.getOfflineGrievanceUpload() != null && grievanceRequestDTO.getOfflineGrievanceUpload() && userInformation != null){
            uploaderGroOfficeUnitOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();
            if(!isAnonymous){
            }
        }
        Long officeId = Long.valueOf(grievanceRequestDTO.getOfficeId());
        CitizenCharter citizenCharter = this.citizenCharterDAO.findByOfficeIdAndServiceId(Long.valueOf(grievanceRequestDTO.getOfficeId()), Long.valueOf(grievanceRequestDTO.getServiceId()));
        GrievanceCurrentStatus currentStatus = (officeId == 0 ? GrievanceCurrentStatus.CELL_NEW : GrievanceCurrentStatus.NEW);
        Grievance grievance = Grievance.builder()
                .details(grievanceRequestDTO.getBody())
                .subject(grievanceRequestDTO.getSubject())
                .officeId(officeId)
                .officeLayers(grievanceRequestDTO.getOfficeLayers())
                .serviceOrigin(citizenCharter == null ? null : citizenCharter.getServiceOrigin())
                .serviceOriginBeforeForward(citizenCharter == null ? null : citizenCharter.getServiceOrigin())
                .grsUser(isGrsUser)
                .complainantId(isAnonymous ? 0L : userId)
                .grievanceType(grievanceRequestDTO.getServiceType())
                .grievanceCurrentStatus(currentStatus)
                .isAnonymous(isAnonymous)
                .trackingNumber(StringUtil.isValidString(grievanceRequestDTO.getServiceTrackingNumber()) ? grievanceRequestDTO.getServiceTrackingNumber() : this.getTrackingNumber())
                .otherService(grievanceRequestDTO.getServiceOthers())
                .otherServiceBeforeForward(grievanceRequestDTO.getServiceOthers())
                .serviceReceiver(grievanceRequestDTO.getServiceReceiver())
                .serviceReceiverRelation(grievanceRequestDTO.getRelation())
                .isOfflineGrievance(grievanceRequestDTO.getOfflineGrievanceUpload())
                .uploaderOfficeUnitOrganogramId(uploaderGroOfficeUnitOrganogramId)
                .isSelfMotivatedGrievance(grievanceRequestDTO.getIsSelfMotivated() != null && grievanceRequestDTO.getIsSelfMotivated())
                .sourceOfGrievance(sourceOfGrievance)
                .complaintCategory(grievanceRequestDTO.getGrievanceCategory())
                .spProgrammeId(StringUtil.isValidString(grievanceRequestDTO.getSpProgrammeId()) ? Integer.parseInt(grievanceRequestDTO.getSpProgrammeId()) : null)
                .geoDivisionId(StringUtil.isValidString(grievanceRequestDTO.getDivision()) ? Integer.parseInt(grievanceRequestDTO.getDivision()) : null)
                .geoDistrictId(StringUtil.isValidString(grievanceRequestDTO.getDistrict()) ? Integer.parseInt(grievanceRequestDTO.getDistrict()) : null)
                .geoUpazilaId(StringUtil.isValidString(grievanceRequestDTO.getUpazila()) ? Integer.parseInt(grievanceRequestDTO.getUpazila()) : null)
                .build();

//        grievance.setSafetyNet(grievanceRequestDTO.getSafetyNetId() >0 && grievanceRequestDTO.getDivisionId() >0 && grievanceRequestDTO.getDistrictId() >0 && grievanceRequestDTO.getUpazilaId() >0);
        grievance.setStatus(true);
        grievance.setCreatedBy(userIdFromToken);
        Grievance grievanceEO = this.save(grievance);
//        if (grievanceRequestDTO.getSafetyNetId() >0 && grievanceRequestDTO.getDivisionId() >0 && grievanceRequestDTO.getDistrictId() >0 && grievanceRequestDTO.getUpazilaId() >0) {
//            SafetyNetGrievance safetyNetGrievanceEO = new SafetyNetGrievance();
//            safetyNetGrievanceEO.setSafetyNetId(grievanceRequestDTO.getSafetyNetId());
//            safetyNetGrievanceEO.setGrievanceId(grievanceEO.getId());
//            safetyNetGrievanceEO.setDivisionId(grievanceRequestDTO.getDivisionId());
//            safetyNetGrievanceEO.setDistrictId(grievanceRequestDTO.getDistrictId());
//            safetyNetGrievanceEO.setUpazilaId(grievanceRequestDTO.getUpazilaId());
//            safetyNetGrievanceEO.setStatus(true);
//            baseEntityManager.save(safetyNetGrievanceEO);
//        }
        return grievanceEO;
    }

    //add
    @Synchronized public String getTrackingNumber() {
        CacheUtil.updateTrackingNumber();
        Long count = CacheUtil.getTrackingNumber();
        return String.valueOf(count);
    }

    public String getCaseNumber(Long officeId) {
        DateFormat df = new SimpleDateFormat("yyyy");
        Long count = this.grievanceRepo.countByOfficeId(officeId) + 1;
        String caseNumber = df.format(new Date()) + String.valueOf(officeId) + String.format("%05d", count);
        return caseNumber;
    }

    public Page<Grievance> findByOfficeId(Pageable pageable, Long officeId) {
        GrievanceCurrentStatus grievanceCurrentStatus = GrievanceCurrentStatus.NEW;
        return this.grievanceRepo.findByOfficeIdAndGrievanceCurrentStatusNotOrderByCreatedAtAsc(officeId, grievanceCurrentStatus, pageable);
    }

    public Page<Grievance> findByComplainantId(Long userId, Boolean grsUser, Pageable pageable) {
        return this.grievanceRepo.findByComplainantIdAndGrsUserOrderByUpdatedAtDesc(userId, pageable, grsUser);
    }

    public Page<Grievance> findByCreatedByAndSourceOfGrievance(Long userId, Pageable pageable, String sourceOfGrievance) {
        return this.grievanceRepo.findByCreatedByAndSourceOfGrievanceOrderByUpdatedAtDesc(userId, pageable, sourceOfGrievance);
    }

    public List<Grievance> findByCreatedByAndSourceOfGrievance(Long userId, String sourceOfGrievance) {
        return this.grievanceRepo.findByCreatedByAndSourceOfGrievanceOrderByUpdatedAtDesc(userId, sourceOfGrievance);
    }

    public Long getResolvedGrievancesCountByOfficeId(Long officeId) {
        return grievanceRepo.getCountOfResolvedGrievancesByOfficeId(officeId);
    }

    public Long getCountOfUnresolvedGrievancesByOfficeId(Long officeId) {
        return grievanceRepo.getCountOfUnresolvedGrievancesByOfficeId(officeId);
    }

    public Long getCountOfRunningGrievancesByOfficeId(Long officeId) {
        return grievanceRepo.getCountOfRunningGrievancesByOfficeId(officeId);
    }

    public Long getSubmittedGrievancesCountByOffice(Long officeId) {
        return grievanceRepo.countAllByOfficeId(officeId);
    }

    public Page<Grievance> findByTrackingNumber(String trackingNumber, Pageable pageable) {
        return this.grievanceRepo.findByTrackingNumber(trackingNumber, pageable);
    }

    public Grievance findByTrackingNumber(String trackingNumber) {
        return this.grievanceRepo.findByTrackingNumber(trackingNumber);
    }

    @Transactional("transactionManager")
    public Grievance feedbackAgainstGrievance(Grievance grievance, FeedbackRequestDTO feedbackRequestDTO) {
        grievance.setIsRatingGiven(true);
        grievance.setRating(feedbackRequestDTO.getRating());
        grievance.setFeedbackComments(feedbackRequestDTO.getUserComments());
        Grievance gr = grievanceRepo.save(grievance);
        return gr;
    }

    public Grievance feedbackAgainstAppealGrievance(Grievance grievance, FeedbackRequestDTO feedbackRequestDTO) {
        grievance.setIsAppealRatingGiven(true);
        grievance.setAppealRating(feedbackRequestDTO.getRating());
        grievance.setAppealFeedbackComments(feedbackRequestDTO.getUserComments());
        grievance = this.grievanceRepo.save(grievance);
        return grievance;
    }

    public List<Grievance> findByIdIn(List<Long> grievanceIds) {
        return this.grievanceRepo.findByIdIn(grievanceIds);
    }

    public Long countByOfficeIdAndServiceOriginId(Long officeId, Long serviceOriginId) {
        return grievanceRepo.countByOfficeIdAndServiceOriginId(officeId, serviceOriginId);
    }

    public List<Grievance> findByOfficeIdAndStatus(Long officeId){
        return this.grievanceRepo.findByOfficeIdAndStatus(officeId);
    }

    public Grievance findByTrackingNumberAndComplaintId(String trackingNumber, Long complainantid) {
        return this.grievanceRepo.findByTrackingNumberAndComplainantId(trackingNumber, complainantid);
    }

    public List<Grievance> getAllGrievanceOfCell() {
        return this.grievanceRepo.findByCellOffice();

    }

    public Page<Grievance> getComplainantViewForReport(List<Office> childOffices, Integer fromYear, Integer fromMonth, Integer toYear, Integer toMonth, Pageable pageable) {

        Specification specification = this.getComplainantViewForReportSpecification(childOffices, fromYear, fromMonth, toYear, toMonth);
        return this.grievanceRepo.findAll(specification, pageable);
    }

    public Specification<Grievance> getComplainantViewForReportSpecification(List<Office> childOffices, Integer fromYear, Integer fromMonth, Integer toYear, Integer toMonth) {

        Specification<Grievance> specification = new Specification<Grievance>() {
            public Predicate toPredicate(Root<Grievance> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                String fromDateString = fromYear + "-" + String.format("%02d", fromMonth) + "-" + "01" + " 00:00:00.0";
                String toDateMonthFirstDateString = toYear + "-" + String.format("%02d", toMonth) + "-" + "01" + " 00:00:00.0";

                Date fromDate = null;
                Date toDate = null;
                try {
                    Date toDateMonthFirstDate = simpleDateFormat.parse(toDateMonthFirstDateString);

                    LocalDate toDateLocal = Instant.ofEpochMilli(toDateMonthFirstDate.getTime())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    int lastDayOfMonthDateGivenDate  = toDateLocal.getMonth().length(toDateLocal.isLeapYear());

                    String toDateString = toYear + "-" + String.format("%02d", toMonth) + "-" + String.format("%02d", lastDayOfMonthDateGivenDate) + " 23:59:59.9";

                    fromDate = simpleDateFormat.parse(fromDateString);
                    toDate = simpleDateFormat.parse(toDateString);
                } catch (ParseException e) {
                    e.printStackTrace();
                    fromDate = new Date();
                    toDate = new Date();
                }

                List<Predicate> predicates = new ArrayList<Predicate>();
                predicates.add(builder.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
                predicates.add(builder.lessThanOrEqualTo(root.get("createdAt"), toDate));

                if (!childOffices.isEmpty()) {

                    List<Long> officeIds = childOffices
                            .stream()
                            .map(childOffice -> childOffice.getId())
                            .collect(Collectors.toList());

                    predicates.add(builder.in(root.get("officeId")).value(officeIds));

                }

                query.orderBy(builder.desc(root.get("createdAt")));
                return builder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return specification;
    }

    public Page<GrievanceAdminDTO> getGrievanceAdminSearch(Long officeId, String referenceNumber, Pageable pageable) {
        return this.grievanceRepo.findAll(this.getGrievanceAdminSpecification(officeId, referenceNumber), pageable).map(this::convertToGrievanceAdminDTO);
    }

    private GrievanceAdminDTO convertToGrievanceAdminDTO(Grievance source) {
        return new GrievanceAdminDTO(source);
    }

    public Specification<Grievance> getGrievanceAdminSpecification(Long officeId, String referenceNumber) {

        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (officeId != null) {
                predicates.add(builder.equal(root.get("officeId"), officeId));
            }
            if (referenceNumber != null) {
                predicates.add(builder.like(root.get("trackingNumber"), "%"+referenceNumber.trim().toUpperCase()+"%"));
            }
            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
