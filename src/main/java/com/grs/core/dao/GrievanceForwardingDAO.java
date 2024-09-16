package com.grs.core.dao;

import com.grs.api.model.GrievanceForwardingDTO;
import com.grs.api.model.OfficeInformationFullDetails;
import com.grs.api.model.UserInformation;
import com.grs.core.domain.GrievanceCurrentStatus;
import com.grs.core.domain.MediumOfSubmission;
import com.grs.core.domain.RoleType;
import com.grs.core.domain.grs.*;
import com.grs.core.model.ListViewType;
import com.grs.core.repo.grs.BaseEntityManager;
import com.grs.core.repo.grs.ComplainHistoryRepository;
import com.grs.core.repo.grs.GrievanceForwardingRepo;
import com.grs.core.service.ComplainantService;
import com.grs.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Acer on 05-Oct-17.
 */
@Slf4j
@Service
public class GrievanceForwardingDAO {
    @Autowired
    private GrievanceForwardingRepo grievanceForwardingRepo;
    @Autowired
    private ComplainantService complainantService;

    @Autowired
    private CellMemberDAO cellMemberDAO;

    @Autowired
    private BaseEntityManager baseEntityManager;

    @Autowired
    private ComplainHistoryRepository complainHistoryRepository;

    public GrievanceForwarding save(GrievanceForwarding grievanceForwarding) {
        return this.grievanceForwardingRepo.save(grievanceForwarding);
    }

    @Transactional("transactionManager")
    public GrievanceForwarding forwardGrievanceRemovingFromInbox(GrievanceForwarding grievanceForwarding) {
        GrievanceForwarding toBeUpdated = this.grievanceForwardingRepo.findByIsCurrentAndToOfficeIdAndToOfficeUnitOrganogramIdAndGrievance(true, grievanceForwarding.getFromOfficeId(), grievanceForwarding.getFromOfficeUnitOrganogramId(), grievanceForwarding.getGrievance());
        GrievanceForwarding existingToEntry = this.grievanceForwardingRepo.findByIsCurrentAndToOfficeIdAndToOfficeUnitOrganogramIdAndGrievance(true, grievanceForwarding.getToOfficeId(), grievanceForwarding.getToOfficeUnitOrganogramId(), grievanceForwarding.getGrievance());
        if (toBeUpdated != null) {
            toBeUpdated.setIsCurrent(false);
            this.grievanceForwardingRepo.save(toBeUpdated);
        }
        if (existingToEntry != null) {
            existingToEntry.setIsCurrent(false);
            this.grievanceForwardingRepo.save(existingToEntry);
        }
        if(grievanceForwarding.getIsCurrent() == null) {
            grievanceForwarding.setIsCurrent(true);
        }
        grievanceForwarding.setIsSeen(false);
        grievanceForwarding =  this.grievanceForwardingRepo.save(grievanceForwarding);
        boolean historyStatus = this.saveHistory(grievanceForwarding);
        log.info("====History status:{}", historyStatus);
        return grievanceForwarding;
    }

    @Transactional("transactionManager")
    public GrievanceForwarding forwardGrievanceRemovingFromInboxAndNotCurrent(GrievanceForwarding grievanceForwarding) {
        GrievanceForwarding toBeUpdated = this.grievanceForwardingRepo.findByIsCurrentAndToOfficeIdAndToOfficeUnitOrganogramIdAndGrievance(true, grievanceForwarding.getFromOfficeId(), grievanceForwarding.getFromOfficeUnitOrganogramId(), grievanceForwarding.getGrievance());
        if (toBeUpdated != null) {
            toBeUpdated.setIsCurrent(false);
            this.grievanceForwardingRepo.save(toBeUpdated);
        }

        grievanceForwarding.setIsCurrent(false);
        grievanceForwarding.setIsSeen(false);
        grievanceForwarding = this.grievanceForwardingRepo.save(grievanceForwarding);
        boolean historyStatus = this.saveHistory(grievanceForwarding);
        log.info("====History status:{}", historyStatus);
        return grievanceForwarding;
    }

    @Transactional("transactionManager")
    public GrievanceForwarding forwardGrievanceKeepingAtInbox(GrievanceForwarding grievanceForwarding) {
        if (grievanceForwarding.getIsCurrent() == null || grievanceForwarding.getIsCurrent()) {
            GrievanceForwarding existingToEntry = this.grievanceForwardingRepo.findByIsCurrentAndToOfficeIdAndToOfficeUnitOrganogramIdAndGrievance(true, grievanceForwarding.getToOfficeId(), grievanceForwarding.getToOfficeUnitOrganogramId(), grievanceForwarding.getGrievance());
            if (existingToEntry != null && !RoleType.INV_HEAD.equals(grievanceForwarding.getAssignedRole())) {
                existingToEntry.setIsCurrent(false);
                this.grievanceForwardingRepo.save(existingToEntry);
            }
            grievanceForwarding.setIsCurrent(true);
        }
        grievanceForwarding.setIsSeen(false);
        grievanceForwarding = this.grievanceForwardingRepo.save(grievanceForwarding);
        boolean historyStatus = this.saveHistory(grievanceForwarding);
        log.info("====History status:{}", historyStatus);
        return grievanceForwarding;
    }

    public List<GrievanceForwarding> getAllComplaintMovement(Grievance grievance) {
        return this.grievanceForwardingRepo.findByGrievance(grievance);
    }

    public List<GrievanceForwarding> getAllRelatedComplaintMovements(Long grievanceId, Long officeId, List<Long> officeUnitOrganogramId, String action) {
        return this.grievanceForwardingRepo.findByGrievanceIdAndOfficeAndOfficeUnitOrganogramInAndAction(grievanceId, officeId, officeUnitOrganogramId, action);
    }

    public GrievanceForwarding getLastForwadingForGivenGrievance(Grievance grievance) {
        return this.grievanceForwardingRepo.findTopByGrievanceOrderByIdDesc(grievance);
    }

    public GrievanceForwarding getLastForwadingForGivenGrievanceAndAction(Grievance grievance, String action) {
        return this.grievanceForwardingRepo.findTopByGrievanceAndActionLikeOrderByIdDesc(grievance, action);
    }

    public GrievanceForwarding getLastClosedOrRejectedForwarding(Grievance grievance) {
        return this.grievanceForwardingRepo.findRecentlyClosedOrRejectedOne(grievance.getId());
    }

    public GrievanceForwarding findRecentlyClosedOrRejectedOne(Long grievanceId) {
        return this.grievanceForwardingRepo.findRecentlyClosedOrRejectedOne(grievanceId);
    }

    public GrievanceForwarding getCurrentForwardingForGivenGrievanceAndUser(Grievance grievance, Long officeId, Long officeUnitOrganogramId) {
        return this.grievanceForwardingRepo.findByIsCurrentAndToOfficeIdAndToOfficeUnitOrganogramIdAndGrievance(true, officeId, officeUnitOrganogramId, grievance);
    }


    public GrievanceForwarding getByActionAndToOfficeIdAndToOfficeUnitOrganogramIdAndGrievance(Grievance grievance, Long officeId, Long officeUnitOrganogramId, String action) {
        return this.grievanceForwardingRepo.findByActionAndToOfficeIdAndToOfficeUnitOrganogramIdAndGrievance(action, officeId, officeUnitOrganogramId, grievance);
    }

    public GrievanceForwarding getLastActiveGrievanceForwardingOfCurrentUser(Grievance grievance, Long officeId, Long officeUnitOrganogramId) {
        return this.grievanceForwardingRepo.findByGrievanceAndToOfficeIdAndToOfficeUnitOrganogramIdAndIsCurrent(grievance, officeId, officeUnitOrganogramId, true);
    }

    public GrievanceForwarding addNewHistory(Grievance grievance,
                                             OfficeInformationFullDetails fromInfo,
                                             OfficeInformationFullDetails toInfo) {
        GrievanceForwarding grievanceForwarding = GrievanceForwarding.builder()
                .fromOfficeUnitOrganogramId(fromInfo.getOfficeUnitOrganogramId())
                .toOfficeUnitOrganogramId(toInfo.getOfficeUnitOrganogramId())
                .fromOfficeId(fromInfo.getOfficeId())
                .toOfficeId(toInfo.getOfficeId())
                .fromOfficeUnitId(fromInfo.getOfficeUnitId())
                .toOfficeUnitId(toInfo.getOfficeUnitId())
                .grievance(grievance)
                .officeLayers(grievance.getOfficeLayers())
                .toEmployeeRecordId(toInfo.getEmployeeRecordId())
                .fromEmployeeRecordId(fromInfo.getEmployeeRecordId())
                .comment("<p>অভিযোগকারী একটি নতুন অভিযোগ জমা দিয়েছেন</p>")
                .currentStatus(GrievanceCurrentStatus.NEW)
                .action("NEW")
                .isCurrent(true)
                .isCC(false)
                .isCommitteeHead(false)
                .isCommitteeMember(false)
                .isSeen(false)
                .toEmployeeNameBangla(toInfo.getEmployeeNameBangla())
                .fromEmployeeNameBangla(fromInfo.getEmployeeNameBangla())
                .toEmployeeNameEnglish(toInfo.getEmployeeNameEnglish())
                .fromEmployeeNameEnglish(fromInfo.getEmployeeNameEnglish())
                .toEmployeeDesignationBangla(toInfo.getEmployeeDesignation())
                .fromEmployeeDesignationBangla(fromInfo.getEmployeeDesignation())
                .toOfficeNameBangla(toInfo.getOfficeNameBangla())
                .fromOfficeNameBangla(fromInfo.getOfficeNameBangla())
                .toEmployeeUnitNameBangla(toInfo.getOfficeUnitNameBangla())
                .fromEmployeeUnitNameBangla(fromInfo.getOfficeUnitNameBangla())
                .fromEmployeeUsername(fromInfo.getUsername())
                .assignedRole(RoleType.GRO)
                .build();
        grievanceForwarding =  this.grievanceForwardingRepo.save(grievanceForwarding);
        boolean historyStatus = this.saveHistory(grievanceForwarding);
        log.info("====History status:{} For Grievance Id:{} Tracking:{}", historyStatus, grievance.getId(), grievance.getTrackingNumber());
        return grievanceForwarding;
    }
    public GrievanceForwardingDTO convertToGrievanceForwardingDTO(GrievanceForwarding grievanceForwarding) {
        return GrievanceForwardingDTO.builder()
                .currentStatus(grievanceForwarding.getCurrentStatus())
                .deadlineDate(grievanceForwarding.getDeadlineDate())
                .fromEmployeeRecordId(grievanceForwarding.getFromEmployeeRecordId())
                .toEmployeeRecordId(grievanceForwarding.getToEmployeeRecordId())
                .fromOfficeId(grievanceForwarding.getFromOfficeId())
                .toOfficeId(grievanceForwarding.getToOfficeId())
                .fromOfficeOrganogramId(grievanceForwarding.getFromOfficeUnitOrganogramId())
                .toOfficeOrganogramId(grievanceForwarding.getToOfficeUnitOrganogramId())
                .grievanceId(grievanceForwarding.getGrievance().getId())
                .note(grievanceForwarding.getComment())
                .action(grievanceForwarding.getAction())
                .build();
    }

    public GrievanceForwarding getActiveInvestigationHeadEntry(Grievance grievance) {
        return this.grievanceForwardingRepo.findByGrievanceAndIsCurrentAndIsCommitteeHead(grievance, true, true);
    }


    public Page<GrievanceForwarding> getListViewDTOPage(UserInformation userInformation,
                                                        Pageable pageable,
                                                        ListViewType listViewType) {

        Long officeId = userInformation.getOfficeInformation().getOfficeId();
        Long officeUnitOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();
        Specification specification = this.getListViewSpecification(officeUnitOrganogramId, officeId, listViewType, userInformation.getUserId());
        return this.grievanceForwardingRepo.findAll(specification, pageable);
    }

    public Page<GrievanceForwarding> getListViewDTOPageWithSearching(Long officeUnitOrganogramId, Long officeId, Long userId,
                                                                     ListViewType listViewType,
                                                                     String seachCriteria, Pageable pageable) {

        Specification specification = this.getListViewSpecificationWithSearch(officeUnitOrganogramId, officeId, userId, listViewType, seachCriteria);
        return this.grievanceForwardingRepo.findAll(specification, pageable);
    }


    public Page<GrievanceForwarding> getListViewDTOPageWithSearching(UserInformation userInformation,
                                                                     Pageable pageable,
                                                                     ListViewType listViewType,
                                                                     String seachCriteria) {

        Long officeId = userInformation.getOfficeInformation().getOfficeId();
        Long officeUnitOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();
        Long userId = userInformation.getUserId();
        Specification<GrievanceForwarding> specification = this.getListViewSpecificationWithSearch(officeUnitOrganogramId, officeId, userId, listViewType, seachCriteria);
        return this.grievanceForwardingRepo.findAll(specification, pageable);
    }

    public Long getInboxCount(UserInformation userInformation,
                              ListViewType listViewType) {

        Long officeId = userInformation.getOfficeInformation().getOfficeId();
        Long officeUnitOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();
        Long userId = userInformation.getUserId();
        Specification specification = this.getListViewSpecificationWithSearch(officeUnitOrganogramId, officeId, userId, listViewType, "");
        return Long.valueOf(this.grievanceForwardingRepo.findAll(specification).size());
    }

    public List<GrievanceForwarding> getListViewDTOPageWithOutSearching(long officeId, long userId, long officeOrganogramId) {

        Specification specification = this.getListViewSpecificationWithOutSearch(officeId, userId, officeOrganogramId);
        return this.grievanceForwardingRepo.findAll(specification);
    }

    public Specification<GrievanceForwarding> getListViewSpecification(Long officeOrganogramId,
                                                                       Long officeId,
                                                                       ListViewType listType,
                                                                       Long userId) {

        Specification<GrievanceForwarding> specification = new Specification<GrievanceForwarding>() {
            public Predicate toPredicate(Root<GrievanceForwarding> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                predicates.add(builder.notEqual(root.get("grievance").get("complainantId"), userId));
                if (listType.toString().contains("APPEAL")) {
                    predicates.add(builder.equal(root.get("grievance").<Long>get("currentAppealOfficeId"), officeId));
                    predicates.add(builder.equal(root.get("grievance").<Long>get("currentAppealOfficerOfficeUnitOrganogramId"), officeOrganogramId));
                } else {
                    predicates.add(
                            builder.or(
                                    builder.notEqual(root.get("grievance").<Long>get("currentAppealOfficerOfficeUnitOrganogramId"), officeOrganogramId),
                                    builder.isNull(root.get("grievance").<Long>get("currentAppealOfficerOfficeUnitOrganogramId"))
                            )
                    );
                }

                if (listType.toString().contains("OUTBOX")) {

                    predicates.add(builder.and(
                            builder.or(
                                    builder.and(
                                            builder.equal(root.get("fromOfficeUnitOrganogramId"), officeOrganogramId),
                                            builder.equal(root.get("fromOfficeId"), officeId),
                                            builder.notEqual(root.get("toOfficeUnitOrganogramId"), officeOrganogramId)
                                    ),
                                    builder.and(
                                            builder.equal(root.get("toOfficeUnitOrganogramId"), officeOrganogramId),
                                            builder.equal(root.get("toOfficeId"), officeId),
                                            builder.notEqual(root.get("fromOfficeUnitOrganogramId"), officeOrganogramId)
                                    )
                            ),
                            builder.not(builder.in(root.get("action")).value(new ArrayList<>(Arrays.asList("REJECTED", "FORWARDED_TO_AO", "FORWARD_TO_ANOTHER_OFFICE"))))
                    ));

                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<GrievanceForwarding> subqueryRoot = subquery.from(GrievanceForwarding.class);

                    subquery.select(builder.count(subqueryRoot.get("grievance").get("id")));
                    subquery.where(
                            builder.and(
                                    builder.equal(subqueryRoot.get("toOfficeUnitOrganogramId"), officeOrganogramId),
                                    builder.equal(subqueryRoot.get("toOfficeId"), officeId),
                                    builder.equal(subqueryRoot.get("isCurrent"), true),
                                    builder.equal(subqueryRoot.get("grievance").get("id"), root.get("grievance").get("id"))
                            )

                    );
                    predicates.add(builder.equal(subquery.getSelection(), 0L));

                } else if (listType.toString().contains("INBOX")) {
                    predicates.add(builder.equal(root.get("isCurrent"), true));
                    predicates.add(builder.equal(root.get("isCC"), false));
                    predicates.add(builder.equal(root.get("toOfficeUnitOrganogramId"), officeOrganogramId));
                    predicates.add(builder.equal(root.get("toOfficeId"), officeId));
                } else if (listType.toString().contains("CC")) {
                    predicates.add(builder.equal(root.get("isCurrent"), true));
                    predicates.add(builder.equal(root.get("isCC"), true));
                    predicates.add(builder.equal(root.get("toOfficeUnitOrganogramId"), officeOrganogramId));
                    predicates.add(builder.equal(root.get("toOfficeId"), officeId));
                } else if(listType.toString().contains("FORWARDED")){
                    predicates.add(builder.and(
                            builder.and(
                                    builder.notEqual(root.get("grievance").get("grievanceCurrentStatus"), GrievanceCurrentStatus.NEW),
                                    builder.in(root.get("action")).value(new ArrayList<String>(Arrays.asList("FORWARDED_TO_AO", "FORWARD_TO_ANOTHER_OFFICE", "GRO_CHANGED")))
                            ),
                            builder.and(
                                    builder.equal(root.get("fromOfficeUnitOrganogramId"), officeOrganogramId),
                                    builder.equal(root.get("fromOfficeId"), officeId)
                            )

                    ));
                } else if(listType.toString().contains("CLOSED")) {
                    predicates.add(builder.and(
                            builder.or(
                                    builder.like(root.get("action"), "%CLOSED%"),
                                    builder.like(root.get("action"), "%REJECTED%"),
                                    builder.and(
                                            builder.like(root.get("action"), "%GRO_CHANGED%"),
                                            builder.in(root.get("currentStatus")).value(ListViewConditionOnCurrentStatusGenerator.getListOfCLosedOrRejectedStatus())
                                    )
                            ),
                            builder.or(
                                    builder.and(
                                            builder.equal(root.get("toOfficeUnitOrganogramId"), officeOrganogramId),
                                            builder.equal(root.get("toOfficeId"), officeId)),
                                    builder.and(
                                            builder.equal(root.get("fromOfficeUnitOrganogramId"), officeOrganogramId),
                                            builder.equal(root.get("fromOfficeId"), officeId))
                            )
                    ));
                } else {
                    predicates.add(builder.or(
                            builder.and(
                                    builder.equal(root.get("toOfficeUnitOrganogramId"), officeOrganogramId),
                                    builder.equal(root.get("toOfficeId"), officeId)),
                            builder.and(
                                    builder.equal(root.get("fromOfficeUnitOrganogramId"), officeOrganogramId),
                                    builder.equal(root.get("fromOfficeId"), officeId))
                            )
                    );
                }

                if (listType.toString().contains("EXPIRED")) {
                    Date date = new Date();
                    Long expTime = CalendarUtil.getWorkDaysCountBefore(date, (int) Constant.GRIEVANCE_EXPIRATION_TIME);
                    date.setTime(date.getTime() - expTime * 24 * 60 * 60 * 1000);
                    predicates.add(
                            builder.and(
                                    builder.lessThan(root.get("grievance").get("createdAt"), date),
                                    builder.equal(root.get("grievance").get("officeId"), officeId)
                            )
                    );
                }

                ListViewConditionOnCurrentStatusGenerator statusGenerator = new ListViewConditionOnCurrentStatusGenerator();
                List<GrievanceCurrentStatus> grievanceCurrentStatusList = statusGenerator.getCurrentStatusListBasedOnListViewType(listType);
                predicates.add(builder.in(root.get("grievance").get("grievanceCurrentStatus")).value(grievanceCurrentStatusList));

                query.orderBy(builder.desc(root.get("updatedAt")));
                query.groupBy(root.get("grievance"));

                return builder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return specification;
    }


    public Specification<GrievanceForwarding> getListViewSpecificationWithSearch(Long officeOrganogramId,
                                                                                 Long officeId,
                                                                                 Long userId,
                                                                                 ListViewType listType,
                                                                                 String searchCriteria) {

        Specification<GrievanceForwarding> specification = new Specification<GrievanceForwarding>() {
            public Predicate toPredicate(Root<GrievanceForwarding> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                predicates.add(builder.notEqual(root.get("grievance").get("complainantId"), userId));
                if (listType.toString().contains("APPEAL")) {
                    predicates.add(builder.equal(root.get("grievance").<Long>get("currentAppealOfficeId"), officeId));
                    predicates.add(builder.equal(root.get("grievance").<Long>get("currentAppealOfficerOfficeUnitOrganogramId"), officeOrganogramId));
                } else {
                    predicates.add(
                            builder.or(
                                    builder.notEqual(root.get("grievance").<Long>get("currentAppealOfficerOfficeUnitOrganogramId"), officeOrganogramId),
                                    builder.isNull(root.get("grievance").<Long>get("currentAppealOfficerOfficeUnitOrganogramId"))
                            )
                    );
                }

                if (listType.toString().contains("OUTBOX")) {

                    predicates.add(builder.and(
                            builder.or(
                                    builder.and(
                                            builder.equal(root.get("fromOfficeUnitOrganogramId"), officeOrganogramId),
                                            builder.equal(root.get("fromOfficeId"), officeId),
                                            builder.notEqual(root.get("toOfficeUnitOrganogramId"), officeOrganogramId)
                                    ),
                                    builder.and(
                                            builder.equal(root.get("toOfficeUnitOrganogramId"), officeOrganogramId),
                                            builder.equal(root.get("toOfficeId"), officeId),
                                            builder.notEqual(root.get("fromOfficeUnitOrganogramId"), officeOrganogramId)
                                    )
                            ),
                            builder.not(builder.in(root.get("action")).value(new ArrayList<>(Arrays.asList("REJECTED", "FORWARDED_TO_AO", "FORWARD_TO_ANOTHER_OFFICE"))))
                    ));

                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<GrievanceForwarding> subqueryRoot = subquery.from(GrievanceForwarding.class);

                    subquery.select(builder.count(subqueryRoot.get("grievance").get("id")));
                    subquery.where(
                            builder.and(
                                    builder.equal(subqueryRoot.get("toOfficeUnitOrganogramId"), officeOrganogramId),
                                    builder.equal(subqueryRoot.get("toOfficeId"), officeId),
                                    builder.equal(subqueryRoot.get("isCurrent"), true),
                                    builder.equal(subqueryRoot.get("grievance").get("id"), root.get("grievance").get("id"))
                            )

                    );
                    predicates.add(builder.equal(subquery.getSelection(), 0L));

                } else if (listType.toString().contains("INBOX")) {
                    predicates.add(builder.equal(root.get("isCurrent"), true));
                    predicates.add(builder.equal(root.get("isCC"), false));
                    predicates.add(builder.equal(root.get("toOfficeUnitOrganogramId"), officeOrganogramId));
                    predicates.add(builder.equal(root.get("toOfficeId"), officeId));
                } else if (listType.toString().contains("CC")) {
                    predicates.add(builder.equal(root.get("isCurrent"), true));
                    predicates.add(builder.equal(root.get("isCC"), true));
                    predicates.add(builder.equal(root.get("toOfficeUnitOrganogramId"), officeOrganogramId));
                    predicates.add(builder.equal(root.get("toOfficeId"), officeId));
                } else if(listType.toString().contains("FORWARDED")){
                    predicates.add(builder.and(
                            builder.and(
                                    builder.notEqual(root.get("grievance").get("grievanceCurrentStatus"), GrievanceCurrentStatus.NEW),
                                    builder.in(root.get("action")).value(new ArrayList<>(Arrays.asList("FORWARDED_TO_AO", "FORWARD_TO_ANOTHER_OFFICE", "GRO_CHANGED")))
                            ),
                            builder.and(
                                    builder.equal(root.get("fromOfficeUnitOrganogramId"), officeOrganogramId),
                                    builder.equal(root.get("fromOfficeId"), officeId)
                            )

                    ));
                } else if(listType.toString().contains("CLOSED")) {
                    predicates.add(builder.and(
                            builder.or(
                                    builder.like(root.get("action"), "%CLOSED%"),
                                    builder.like(root.get("action"), "%REJECTED%"),
                                    builder.and(
                                            builder.like(root.get("action"), "%GRO_CHANGED%"),
                                            builder.in(root.get("currentStatus")).value(ListViewConditionOnCurrentStatusGenerator.getListOfCLosedOrRejectedStatus())
                                    )
                            ),
                            builder.or(
                                    builder.and(
                                            builder.equal(root.get("toOfficeUnitOrganogramId"), officeOrganogramId),
                                            builder.equal(root.get("toOfficeId"), officeId)),
                                    builder.and(
                                            builder.equal(root.get("fromOfficeUnitOrganogramId"), officeOrganogramId),
                                            builder.equal(root.get("fromOfficeId"), officeId))
                            )
                    ));
                } else {
                    predicates.add(builder.or(
                            builder.and(
                                    builder.equal(root.get("toOfficeUnitOrganogramId"), officeOrganogramId),
                                    builder.equal(root.get("toOfficeId"), officeId)),
                            builder.and(
                                    builder.equal(root.get("fromOfficeUnitOrganogramId"), officeOrganogramId),
                                    builder.equal(root.get("fromOfficeId"), officeId))
                            )
                    );
                }

                if (listType.toString().contains("EXPIRED")) {
                    Date date = new Date();
                    Long expTime = CalendarUtil.getWorkDaysCountBefore(date, (int) Constant.GRIEVANCE_EXPIRATION_TIME);
                    date.setTime(date.getTime() - expTime * 24 * 60 * 60 * 1000);
                    predicates.add(
                            builder.and(
                                    builder.lessThan(root.get("grievance").<Date>get("createdAt"), date),
                                    builder.equal(root.get("grievance").get("officeId"), officeId)
                            )
                    );
                    //TODO:: Added by Md Alauddin Hossain to ensure it's active
                    predicates.add(builder.and(builder.equal(root.get("isCurrent"), true)));
                }

                ListViewConditionOnCurrentStatusGenerator statusGenerator = new ListViewConditionOnCurrentStatusGenerator();
                List<GrievanceCurrentStatus> grievanceCurrentStatusList = statusGenerator.getCurrentStatusListBasedOnListViewType(listType);
                predicates.add(builder.in(root.get("grievance").get("grievanceCurrentStatus")).value(grievanceCurrentStatusList));

                if (!searchCriteria.isEmpty()) {

                    String englisNumber = "";
                    if(BanglaConverter.isABanglaDigit(searchCriteria)){
                        englisNumber = BanglaConverter.convertToEnglish(searchCriteria);
                    } else {
                        englisNumber = searchCriteria;
                    }

                    List<Long> complainantIds = Optional.of(Optional.ofNullable(complainantService.findComplainantLikePhoneNumber(searchCriteria))
                            .map(Collection::stream)
                            .orElseGet(Stream::empty)
                            .map(complainant -> complainant.getId())
                            .collect(Collectors.toList()))
                            .filter(l->!l.isEmpty())
                            .orElse(Arrays.asList( Long.MAX_VALUE));

                    predicates.add(
                            builder.or(
                                    builder.like(root.get("grievance").get("subject"), "%" + searchCriteria + "%"),
                                    builder.like(root.get("grievance").get("trackingNumber"), "%" + englisNumber + "%"),
                                    root.get("grievance").get("id").in(complainantIds)
                            )
                    );

                }

                query.groupBy(root.get("grievance"));
                query.orderBy(builder.desc(root.get("grievance").get("updatedAt")));
                return builder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return specification;
    }

    public Specification<GrievanceForwarding> getListViewSpecificationWithOutSearch(Long officeId, Long userId, Long officeOrganogramId) {

        Specification<GrievanceForwarding> specification = new Specification<GrievanceForwarding>() {
            public Predicate toPredicate(Root<GrievanceForwarding> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                predicates.add(
                        builder.or(
                                builder.notEqual(root.get("grievance").<Long>get("currentAppealOfficerOfficeUnitOrganogramId"), officeOrganogramId),
                                builder.isNull(root.get("grievance").<Long>get("currentAppealOfficerOfficeUnitOrganogramId"))
                        )
                );

                predicates.add(builder.equal(root.get("isCurrent"), true));
                predicates.add(builder.equal(root.get("isCC"), false));
                predicates.add(builder.equal(root.get("toOfficeUnitOrganogramId"), officeOrganogramId));
                predicates.add(builder.equal(root.get("toOfficeId"), officeId));

                ListViewConditionOnCurrentStatusGenerator statusGenerator = new ListViewConditionOnCurrentStatusGenerator();
                List<GrievanceCurrentStatus> grievanceCurrentStatusList = statusGenerator.getCurrentStatusListBasedOnListViewType(ListViewType.NORMAL_INBOX);
                predicates.add(builder.in(root.get("grievance").get("grievanceCurrentStatus")).value(grievanceCurrentStatusList));

                query.groupBy(root.get("grievance"));
                query.orderBy(builder.desc(root.get("grievance").get("updatedAt")));
                return builder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return specification;
    }

    public int countByIsCurrentAndGrievanceAndIsCommitteeMember(boolean isCurrent, Grievance grievance, boolean isCommitteeMember) {
        List<GrievanceForwarding> gf = this.grievanceForwardingRepo.findByIsCurrentAndGrievanceAndIsCommitteeMember(isCurrent, grievance, isCommitteeMember);
        return gf.size();
    }

    public void updateGrievanceForwardingRemovingFromInbox(Long officeId, Long officeUnitOrganogramId, Grievance grievance, GrievanceForwarding grievanceForwarding) {
        GrievanceForwarding toBeUpdated = this.grievanceForwardingRepo.findByIsCurrentAndToOfficeIdAndToOfficeUnitOrganogramIdAndGrievance(true, officeId, officeUnitOrganogramId, grievance);
        toBeUpdated.setIsCurrent(false);
        this.save(toBeUpdated);
        this.save(grievanceForwarding);
        boolean historyStatus = this.saveHistory(grievanceForwarding);
        log.info("====History status:{}", historyStatus);
    }

    public GrievanceForwarding findByIsCurrentAndToOfficeAndToGROPostAndGrievance(boolean isCurrent, Long toOfficeId, Long toOfficeUnitOrganogramId, Grievance grievance) {
        return this.grievanceForwardingRepo.findByIsCurrentAndToOfficeIdAndToOfficeUnitOrganogramIdAndGrievance(isCurrent, toOfficeId, toOfficeUnitOrganogramId, grievance);
    }

    public List<GrievanceForwarding> findByGrievanceAndActionLikeOrderByIdDesc(Grievance grievance, String action){
        return this.grievanceForwardingRepo.findByGrievanceAndActionLikeOrderByIdDesc(grievance, action);
    }

    public GrievanceForwarding findByGrievanceAndActionLikeAndCurrentStatusLike(Grievance grievance, String action, String currentStatus){
        return this.grievanceForwardingRepo.findByGrievanceAndActionLikeAndCurrentStatusLike(grievance.getId(), action, currentStatus);
    }

    public GrievanceForwarding findByGrievanceAndActionLikeAndCurrentStatusNotLike(Grievance grievance, String action, String currentStatus){
        return this.grievanceForwardingRepo.findByGrievanceAndActionLikeAndCurrentStatusNotLike(grievance.getId(), action, currentStatus);
    }

    public List<GrievanceForwarding> getAllRelatedComplaintMovementsBetweenDates(Long grievanceId, Long officeId, List<Long> officeUnitOrganogramId, String action, Date start, Date finish) {
        return this.grievanceForwardingRepo.findByGrievanceIdAndOfficeAndOfficeUnitOrganogramInAndActionAndCreatedAt(grievanceId, officeId, officeUnitOrganogramId, action, start, finish);
    }

    public List<GrievanceForwarding> findByGrievanceAndIsCurrent(Grievance grievance, Boolean isCurrent) {
        return this.grievanceForwardingRepo.findByGrievanceAndIsCurrent(grievance, isCurrent);
    }

    public List<GrievanceForwarding> getdistinctemployeRecordIds(Long grievanceId) {
        return this.grievanceForwardingRepo.getDistinctToEmployeeRecordIdByGrievance(grievanceId);
    }

    public GrievanceForwarding getLatestComplainantMovement(Long complaintId){
        return this.grievanceForwardingRepo.getLatestComplainantMovement(complaintId);
    }

    public List<GrievanceForwarding> findByGrievanceIdAndAssignedRole(Long grievanceId, String roleName){
        return this.grievanceForwardingRepo.findByGrievanceIdAndAssignedRole(grievanceId, roleName);
    }

    public List<GrievanceForwarding> findByGrievanceIdAndAssignedRoleWithForwarded(Long grievanceId, String roleName){
        return this.grievanceForwardingRepo.findByGrievanceIdAndAssignedRoleAndAction(grievanceId, roleName, "FORWARD_TO_ANOTHER_OFFICE");
    }

    public List<GrievanceForwarding> findByGrievanceId(Long grievanceId){
        return this.grievanceForwardingRepo.findByGrievanceId(grievanceId);
    }

    public List<GrievanceForwarding> getAllMovementsOfPreviousGRO(OfficesGRO officesGRO) {
        return this.grievanceForwardingRepo.getAllMovementsOfPreviousGRO(officesGRO.getGroOfficeId(), officesGRO.getGroOfficeUnitOrganogramId());
    }

    public GrievanceForwarding getByActionAndFromOffice(Grievance complaint, String action, Long officeId) {
        List<GrievanceForwarding> forwardings = this.grievanceForwardingRepo.findByGrievanceAndActionLikeAndFromOfficeIdOrderByIdDesc(complaint, action, officeId);
        return forwardings.size() == 0 ? null : forwardings.get(0);
    }

    public List<GrievanceForwarding> getUnseenCountForUser(Long officeId, Long officeUnitOrganogramId, Boolean isCC) {
        return this.grievanceForwardingRepo.getUnseenInboxOrCCCount(officeId, officeUnitOrganogramId, true, isCC, false);
    }

    public Long getUnseesAppealCount(Long officeId, Long officeUnitOrganogramId) {
        return this.grievanceForwardingRepo.getUnseenAppealCount(officeId, officeUnitOrganogramId, true, false);
    }

    public List<GrievanceForwarding> getTotalCountForUser(Long officeId, Long officeUnitOrganogramId, Boolean isCC) {
        return this.grievanceForwardingRepo.getTotalInboxOrCCCount(officeId, officeUnitOrganogramId, true, isCC);
    }

    public Long getTotalAppealCount(Long officeId, Long officeUnitOrganogramId) {
        return this.grievanceForwardingRepo.getTotalAppealCount(officeId, officeUnitOrganogramId, true);
    }

    public boolean saveHistory(GrievanceForwarding movement) {
        log.info("===Writing history for Tacking:{} Grievance:{} Status:{} and Action:{}", movement.getGrievance().getTrackingNumber(), movement.getGrievance().getId(), movement.getGrievance().getGrievanceCurrentStatus(), movement.getAction());
        if (!Utility.isInList(movement.getAction(),
                "NEW",
                "FORWARD_TO_ANOTHER_OFFICE",
                "REJECTED",
                "APPEAL",
                "CLOSED_ACCUSATION_INCORRECT",
                "CLOSED_ACCUSATION_PROVED",
                "CLOSED_ANSWER_OK",
                "CLOSED_OTHERS",
                "APPEAL_CLOSED_ACCUSATION_INCORRECT",
                "APPEAL_CLOSED_ACCUSATION_PROVED",
                "APPEAL_CLOSED_OTHERS",
                "CELL_NEW",
                "RETAKE"
        )) {
            log.info("===Current Action:{} Need no log:{}", movement.getAction(), movement.getGrievance().getTrackingNumber());
            return false;
        }

        if (Utility.isInList(movement.getAction(),"REJECTED",
                "CLOSED_ACCUSATION_INCORRECT",
                "CLOSED_ACCUSATION_PROVED",
                "CLOSED_ANSWER_OK",
                "CLOSED_OTHERS",
                "APPEAL_CLOSED_ACCUSATION_INCORRECT",
                "APPEAL_CLOSED_ACCUSATION_PROVED",
                "APPEAL_CLOSED_OTHERS"
        )) {

            String sql = "update complain_history set closed_at=:closedAt where complain_id=:complain_id and closed_at is null ";
            Map<String, Object> params = new HashMap<>();
            // Set the modified date with time set to 00:00:00
            params.put("closedAt", CalendarUtil.truncateDate(new Date()));

            params.put("complain_id", movement.getGrievance().getId());

            baseEntityManager.updateByQuery(sql, params);

            String status;

            if (Utility.isInList(movement.getAction(),"APPEAL_CLOSED_ACCUSATION_INCORRECT",
                    "APPEAL_CLOSED_ACCUSATION_PROVED",
                    "APPEAL_CLOSED_OTHERS")) {
                status = "APPEAL_CLOSED";
            } else {
                status = "CLOSED";
            }

            ComplainHistory historyEO = getHistory(movement.getGrievance(), status, movement.getToOfficeId());
            try {
                if (historyEO != null) {
                    this.complainHistoryRepository.save(historyEO);
                }
            } catch (Throwable t) {
                log.error("===ERROR:{}", t.getMessage());
            }
        }

        if (movement.getAction().equalsIgnoreCase("FORWARD_TO_ANOTHER_OFFICE")) {
            String sql = "update complain_history set closed_at=:closedAt where complain_id=:complain_id and current_status in(:current_status1,:current_status2) and closed_at is null ";
            Map<String, Object> params = new HashMap<>();

            // Assign the modified date (with time 00:00:00) to closedAt
            params.put("closedAt", CalendarUtil.truncateDate(new Date()));
            params.put("complain_id", movement.getGrievance().getId());
            params.put("current_status1", "NEW");
            params.put("current_status2", "RETAKE");

            baseEntityManager.updateByQuery(sql, params);

            ComplainHistory historyEO = prepareHistory(movement.getGrievance(), "FORWARDED_OUT", movement.getFromOfficeId());

            // Setting the time part to 00:00:00 for movement.getUpdatedAt() as well
            historyEO.setClosedAt(CalendarUtil.truncateDate(movement.getUpdatedAt()));
            this.complainHistoryRepository.save(historyEO);

            ComplainHistory historyNew = prepareHistory(movement.getGrievance(), "NEW", movement.getToOfficeId());
            this.complainHistoryRepository.save(historyNew);



        }

        if (movement.getAction().equalsIgnoreCase("RETAKE")) {
            ComplainHistory historyEO = prepareHistory(movement.getGrievance(), "RETAKE", movement.getToOfficeId());
            this.complainHistoryRepository.save(historyEO);

            String sql = "update complain_history set closed_at=:closedAt where complain_id=:complain_id and current_status=:current_status and closed_at is null ";
            Map<String, Object> params = new HashMap<>();
            params.put("closedAt", CalendarUtil.truncateDate(new Date()));
            params.put("complain_id", movement.getGrievance().getId());
            params.put("current_status", "NEW");

            baseEntityManager.updateByQuery(sql, params);
        }

        if (movement.getAction().equalsIgnoreCase("APPEAL")) {
            String sql = "update complain_history set closed_at=:closedAt where complain_id=:complain_id and closed_at is null ";
            Map<String, Object> params = new HashMap<>();
            params.put("closedAt", CalendarUtil.truncateDate(new Date()));
            params.put("complain_id", movement.getGrievance().getId());

            baseEntityManager.updateByQuery(sql, params);


            CellMember cellGro = cellMemberDAO.findByIsGro();
            boolean sendToCell = StringUtil.isValidString(movement.getGrievance().getAppealOfficerDecision()) && movement.getGrievance().getCurrentAppealOfficeId() != null && cellGro != null;
            if (sendToCell) {
                ComplainHistory historyEO = getHistory(movement.getGrievance(), "CELL_APPEAL", movement.getToOfficeId());
                try {
                    if (historyEO != null) {
                        this.complainHistoryRepository.save(historyEO);
                    }
                } catch (Throwable t) {
                    log.error("===ERROR:{}", t.getMessage());
                }
            } else {
                ComplainHistory historyEO = getHistory(movement.getGrievance(), "APPEAL", movement.getToOfficeId());
                try {
                    if (historyEO != null) {
                        this.complainHistoryRepository.save(historyEO);
                    }
                } catch (Throwable t) {
                    log.error("===ERROR:{}", t.getMessage());
                }
            }

        }

        if (Utility.isInList(movement.getAction(), "NEW", "CELL_NEW")) {
            ComplainHistory historyEO = getHistory(movement.getGrievance(), movement.getAction(), movement.getToOfficeId());
            try {
                if (historyEO != null) {
                    this.complainHistoryRepository.save(historyEO);
                }
            } catch (Throwable t) {
                log.error("==ERROR:{}", t.getMessage());
            }
        }

        return true;

    }

    public ComplainHistory prepareHistory(Grievance grievanceEO, String currentStatus, Long officeId) {
        ComplainHistory historyEO = new ComplainHistory();
        historyEO.setComplainId(grievanceEO.getId());
        historyEO.setTrackingNumber(grievanceEO.getTrackingNumber());
        historyEO.setCurrentStatus(currentStatus);
        historyEO.setOfficeId(officeId);
        Map<String, Object> params = new HashMap<>();
        String sql = "select o.id,ol.layer_level as layer_level, ol.custom_layer_id as custom_layer, o.office_origin_id as office_origin " +
                "from grs_doptor.offices o " +
                "left join grs_doptor.office_layers ol on o.office_layer_id = ol.id " +
                "where o.id =:officeId order by o.id desc ";
        params.put("officeId", officeId);
        try {
            Object[] officeInfo = baseEntityManager.findSingleByQuery(sql, params);
            if (officeInfo != null && officeInfo.length >0) {
                if (Utility.valueExists(officeInfo, 1)) {
                    historyEO.setLayerLevel(Utility.getLongValue(officeInfo[1]));
                }
                if (Utility.valueExists(officeInfo, 2)) {
                    historyEO.setCustomLayer(Utility.getLongValue(officeInfo[2]));
                }
                if (Utility.valueExists(officeInfo, 3)) {
                    historyEO.setOfficeOrigin(Utility.getLongValue(officeInfo[3]));
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        MediumOfSubmission medium = MediumOfSubmission.ONLINE;
        if (grievanceEO.getGrievanceCurrentStatus().equals(GrievanceCurrentStatus.NEW) && grievanceEO.getIsOfflineGrievance() != null && grievanceEO.getIsOfflineGrievance()) {
            medium = MediumOfSubmission.CONVENTIONAL_METHOD;
        } else if (grievanceEO.getGrievanceCurrentStatus().equals(GrievanceCurrentStatus.NEW) && grievanceEO.getIsSelfMotivatedGrievance() != null && grievanceEO.getIsSelfMotivatedGrievance()) {
            medium = MediumOfSubmission.SELF_MOTIVATED_ACCEPTANCE;
        }

        historyEO.setMediumOfSubmission(medium.name());
        historyEO.setGrievanceType(grievanceEO.getGrievanceType().name());
        historyEO.setSelfMotivated(grievanceEO.getIsSelfMotivatedGrievance() != null && grievanceEO.getIsSelfMotivatedGrievance() ? 1L : 0L);
        // historyEO.setCreatedAt(new Date());
        // if (currentStatus.contains("CLOSED")) {
        //     historyEO.setClosedAt(new Date());
        // }

        // Set the modified Date (with time set to 00:00:00)
        historyEO.setCreatedAt(CalendarUtil.truncateDate(new Date()));

        // Set the closedAt field with time 00:00:00 if current status contains "CLOSED"
        if (currentStatus.contains("CLOSED")) {
            historyEO.setClosedAt(CalendarUtil.truncateDate(new Date()));
        }

        historyEO.setCreatedYearMonthDay(new SimpleDateFormat("yyyy-MM-dd").format(historyEO.getCreatedAt()));

        return historyEO;
    }

    public ComplainHistory getHistory(Grievance grievanceEO, String currentStatus, Long officeId) {
        Map<String, Object> params = new HashMap<>();
        String select = "select * from complain_history where complain_id=:complain_id and current_status=:current_status and office_id=:office_id ";
        params.put("complain_id", grievanceEO.getId());
        params.put("current_status", currentStatus);
        params.put("office_id", officeId);

        try {
            ComplainHistory his = baseEntityManager.findSingleByQuery(select, ComplainHistory.class, params);
            if (his != null) {
                return null;
            }
        } catch (Throwable t) {

        }

        ComplainHistory historyEO = new ComplainHistory();
        historyEO.setComplainId(grievanceEO.getId());
        historyEO.setTrackingNumber(grievanceEO.getTrackingNumber());
        historyEO.setCurrentStatus(currentStatus);
        historyEO.setOfficeId(officeId);

        String sql = "select o.id,ol.layer_level as layer_level, ol.custom_layer_id as custom_layer, o.office_origin_id as office_origin " +
                "from grs_doptor.offices o " +
                "left join grs_doptor.office_layers ol on o.office_layer_id = ol.id " +
                "where o.id =:officeId order by o.id desc ";
        params.clear();
        params.put("officeId", officeId);
        try {
            Object[] officeInfo = baseEntityManager.findSingleByQuery(sql, params);
            if (officeInfo != null && officeInfo.length >0) {
                if (Utility.valueExists(officeInfo, 1)) {
                    historyEO.setLayerLevel(Utility.getLongValue(officeInfo[1]));
                }
                if (Utility.valueExists(officeInfo, 2)) {
                    historyEO.setCustomLayer(Utility.getLongValue(officeInfo[2]));
                }
                if (Utility.valueExists(officeInfo, 3)) {
                    historyEO.setOfficeOrigin(Utility.getLongValue(officeInfo[3]));
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        MediumOfSubmission medium = MediumOfSubmission.ONLINE;
        if (grievanceEO.getGrievanceCurrentStatus().equals(GrievanceCurrentStatus.NEW) && grievanceEO.getIsOfflineGrievance() != null && grievanceEO.getIsOfflineGrievance()) {
            medium = MediumOfSubmission.CONVENTIONAL_METHOD;
        } else if (grievanceEO.getGrievanceCurrentStatus().equals(GrievanceCurrentStatus.NEW) && grievanceEO.getIsSelfMotivatedGrievance() != null && grievanceEO.getIsSelfMotivatedGrievance()) {
            medium = MediumOfSubmission.SELF_MOTIVATED_ACCEPTANCE;
        }

        historyEO.setMediumOfSubmission(medium.name());
        historyEO.setGrievanceType(grievanceEO.getGrievanceType().name());
        historyEO.setSelfMotivated(grievanceEO.getIsSelfMotivatedGrievance() != null && grievanceEO.getIsSelfMotivatedGrievance() ? 1L : 0L);

        // Set the createdAt field with time 00:00:00
        historyEO.setCreatedAt(CalendarUtil.truncateDate(new Date()));

        // Set the closedAt field with time 00:00:00 if current status contains "CLOSED"
        if (currentStatus.contains("CLOSED")) {
            historyEO.setClosedAt(CalendarUtil.truncateDate(new Date()));
        }

        historyEO.setCreatedYearMonthDay(new SimpleDateFormat("yyyy-MM-dd").format(historyEO.getCreatedAt()));

        return historyEO;
    }
}