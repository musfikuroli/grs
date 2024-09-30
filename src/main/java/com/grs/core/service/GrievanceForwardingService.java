package com.grs.core.service;

import com.grs.api.annotation.EventListener;
import com.grs.api.model.*;
import com.grs.api.model.request.*;
import com.grs.api.model.response.GenericResponse;
import com.grs.api.model.response.GrievanceForwardingEmployeeRecordsDTO;
import com.grs.api.model.response.OpinionReceiverDTO;
import com.grs.api.model.response.UnseenCountDTO;
import com.grs.api.model.response.file.ExistingFileDerivedDTO;
import com.grs.api.model.response.file.FileDerivedDTO;
import com.grs.api.model.response.grievance.ComplainantInfoDTO;
import com.grs.api.model.response.grievanceForwarding.GrievanceForwardingInvestigationDTO;
import com.grs.api.model.response.organogram.TreeNodeDTO;
import com.grs.api.model.response.organogram.TreeNodeOfficerDTO;
import com.grs.core.dao.*;
import com.grs.core.domain.GrievanceCurrentStatus;
import com.grs.core.domain.RoleType;
import com.grs.core.domain.grs.*;
import com.grs.core.domain.projapoti.EmployeeOffice;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.domain.projapoti.OfficeUnit;
import com.grs.core.domain.projapoti.OfficeUnitOrganogram;
import com.grs.core.model.EmployeeOrganogram;
import com.grs.core.model.ListViewType;
import com.grs.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Acer on 25-Oct-17.
 */
@Slf4j
@Service
public class GrievanceForwardingService {
    @Autowired
    private OfficeService officeService;
    @Autowired
    private OfficeOrganogramService officeOrganogramService;
    @Autowired
    private GrievanceService grievanceService;
    @Autowired
    private GrievanceForwardingDAO grievanceForwardingDAO;
    @Autowired
    private CitizenCharterService citizenCharterService;
    @Autowired
    private OfficesGroService officesGroService;
    @Autowired
    private AttachedFileService attachedFileService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ShortMessageService shortMessageService;
    @Autowired
    private ActionToRoleService actionToRoleService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private FcmService fcmService;
    @Autowired
    private TagidDAO tagidDAO;
    @Autowired
    private CellMemberDAO cellMemberDAO;
    @Autowired
    private ComplainantService complainantService;
    @Autowired
    private DashboardDataDAO dashboardDataDAO;

    public GrievanceForwarding getGrievanceForwarding(Grievance grievance,
                                                       String note,
                                                       String action,
                                                       Long toEmployeeRecordId,
                                                       Long fromEmployeeRecordId,
                                                       Long toOfficeId,
                                                       Long fromOfficeId,
                                                       Long toOfficeUnitOrganogramId,
                                                       Long fromOfficeUnitOrganogramId,
                                                       Boolean isCurrent,
                                                       Boolean isCC,
                                                       Boolean isCommitteeHead,
                                                       Boolean isCommitteeMember,
                                                       Date deadlineDate,
                                                       GrievanceCurrentStatus currentStatus,
                                                       RoleType roleType,
                                                       String fromUserName) {

        EmployeeOffice toEmployeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(toOfficeId, toOfficeUnitOrganogramId, true);

        EmployeeOffice fromEmployeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(fromOfficeId, fromOfficeUnitOrganogramId, true);
        OfficeUnitOrganogram fromOfficeUnit = this.officeService.getOfficeUnitOrganogramById(fromOfficeUnitOrganogramId);
        OfficeUnitOrganogram toOfficeUnit = this.officeService.getOfficeUnitOrganogramById(toOfficeUnitOrganogramId);

        return GrievanceForwarding.builder()
                .grievance(grievance)
                .comment(note)
                .action(action)
                .toEmployeeRecordId(toEmployeeRecordId)
                .fromEmployeeRecordId(fromEmployeeRecordId)
                .toOfficeId(toOfficeId)
                .fromOfficeId(fromOfficeId)
                .toOfficeUnitOrganogramId(toOfficeUnitOrganogramId)
                .fromOfficeUnitOrganogramId(fromOfficeUnitOrganogramId)
                .fromOfficeUnitId(fromOfficeUnit == null ? null : fromOfficeUnit.getOfficeUnitId())
                .toOfficeUnitId(toOfficeUnit == null ? null : toOfficeUnit.getOfficeUnitId())
                .isCurrent(isCurrent)
                .isCC(isCC)
                .isCommitteeHead(isCommitteeHead)
                .isCommitteeMember(isCommitteeMember)
                .deadlineDate(deadlineDate)
                .currentStatus(currentStatus)
                .toEmployeeNameBangla(toEmployeeOffice == null ? "" : toEmployeeOffice.getEmployeeRecord().getNameBangla())
                .fromEmployeeNameBangla(fromEmployeeOffice == null ? "" : fromEmployeeOffice.getEmployeeRecord().getNameBangla())
                .toEmployeeNameEnglish(toEmployeeOffice == null ? "" : toEmployeeOffice.getEmployeeRecord().getNameEnglish())
                .fromEmployeeNameEnglish(fromEmployeeOffice == null ? "" : fromEmployeeOffice.getEmployeeRecord().getNameEnglish())
                .toEmployeeDesignationBangla(toEmployeeOffice == null ? "" : toEmployeeOffice.getDesignation())
                .fromEmployeeDesignationBangla(fromEmployeeOffice == null ? "" : fromEmployeeOffice.getDesignation())
                .toOfficeNameBangla(toEmployeeOffice == null ? "" : toEmployeeOffice.getOffice().getNameBangla())
                .fromOfficeNameBangla(fromEmployeeOffice == null ? "" : fromEmployeeOffice.getOffice().getNameBangla())
                .toEmployeeUnitNameBangla(toEmployeeOffice == null ? "" : toEmployeeOffice.getOfficeUnit()==null ? "" : toEmployeeOffice.getOfficeUnit().getUnitNameBangla())
                .fromEmployeeUnitNameBangla(fromEmployeeOffice == null ? "" : fromEmployeeOffice.getOfficeUnit()==null ? "" : fromEmployeeOffice.getOfficeUnit().getUnitNameBangla())
                .fromEmployeeUsername(fromEmployeeOffice == null ? "" : fromUserName)
                .assignedRole(roleType)
                .build();
    }

    public GrievanceForwarding forwardRemovingFromInbox(GrievanceForwarding grievanceForwarding){
        return this.grievanceForwardingDAO.forwardGrievanceRemovingFromInbox(grievanceForwarding);
    }

    public void updateForwardSeenStatus(UserInformation userInformation, Long grievanceId) {
        if (userInformation.getUserType().equals(UserType.COMPLAINANT)) {
            return;
        }
        Grievance grievance = this.grievanceService.findGrievanceById(grievanceId);
        if (grievance.getGrievanceCurrentStatus().toString().contains("REJECTED") ||
                grievance.getGrievanceCurrentStatus().toString().contains("CLOSED")) {
            return;
        }

        Long userOfficeId = userInformation.getOfficeInformation().getOfficeId();
        Long userOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();

        GrievanceForwarding grievanceForwarding = this.grievanceForwardingDAO.getLastActiveGrievanceForwardingOfCurrentUser(grievance, userOfficeId, userOrganogramId);
        if (grievanceForwarding == null) {
            return;
        }
        if (grievanceForwarding.getIsSeen()) {
            return;
        }
        Long toOfficeId = grievanceForwarding.getToOfficeId();
        Long toOrganogramId = grievanceForwarding.getToOfficeUnitOrganogramId();

        if (toOfficeId.equals(userOfficeId) && toOrganogramId.equals(userOrganogramId)) {
            if (!grievanceForwarding.getIsSeen()) {
                grievanceForwarding.setIsSeen(true);
                this.grievanceForwardingDAO.save(grievanceForwarding);
                boolean historyStatus = grievanceForwardingDAO.saveHistory(grievanceForwarding);
                log.info("====History status:{}", historyStatus);
                return;
            }
        }
    }

    @Transactional(value = "transactionManager", rollbackFor = RuntimeException.class)
    public GenericResponse closeGrievance(Authentication authentication, Long grievanceId, GrievanceForwardingCloseDTO grievanceForwardingCloseDTO) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Grievance grievance = this.grievanceService.findGrievanceById(grievanceId);
        if (grievanceForwardingCloseDTO.getTakeAction()) {
            String departmentalActionNote = grievanceForwardingCloseDTO.getDepartmentalActionNote();
            String employeeList = "<p>যাদের বিরুদ্ধে বিভাগীয় ব্যবস্থা গ্রহণের সুপারিশ করা হচ্ছে : </p>";
            for (String employee : grievanceForwardingCloseDTO.getEmployeeList()) {
                String[] splittedNode = employee.split("_");
                Long officeId = Long.parseLong(splittedNode[2]);
                Long officeUnitOrganogramId = Long.parseLong(splittedNode[1]);
                EmployeeOffice employeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(officeId, officeUnitOrganogramId, true);
                employeeList = employeeList + "<p>" + employeeOffice.getEmployeeRecord().getNameBangla() + ", " +
                        employeeOffice.getOfficeUnit().getUnitNameBangla() + ", " +
                        employeeOffice.getOffice().getNameBangla() + "</p>";
            }
            GenericResponse response = this.giveRecommendDepartmentalAction(authentication, grievanceId, departmentalActionNote + employeeList);
            if (!response.isSuccess()) {
                return response;
            }
        }
        if (grievance.getGrievanceCurrentStatus().toString().contains("APPEAL")) {
            grievance.setAppealOfficerDecision(grievanceForwardingCloseDTO.getGroDecision());
            grievance.setAppealOfficerIdentifiedCause(grievanceForwardingCloseDTO.getMainReason());
            grievance.setAppealOfficerSuggestion(grievanceForwardingCloseDTO.getGroSuggestion());
        } else {
            grievance.setGroDecision(grievanceForwardingCloseDTO.getGroDecision());
            grievance.setGroIdentifiedCause(grievanceForwardingCloseDTO.getMainReason());
            grievance.setGroSuggestion(grievanceForwardingCloseDTO.getGroSuggestion());
        }
        try {
            this.grievanceService.saveGrievance(grievance, false);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Grievance update error. Please contact with admin");
        }
        String message = "আপনার " + grievance.getTrackingNumber() + " ট্র্যাকিং নম্বরের  অভিযোগটি নিষ্পত্তি হয়েছে।";
        return this.rejectOrCloseGrievance(userInformation, grievanceId, grievanceForwardingCloseDTO.getGroDecision(), grievanceForwardingCloseDTO.getStatus(), message, grievanceForwardingCloseDTO.getFiles(), grievanceForwardingCloseDTO.getReferredFiles());
    }

    public GenericResponse rejectGrievance(UserInformation userInformation, GrievanceForwardingNoteDTO comment) {
        Long grievanceId = comment.getGrievanceId();
        Grievance grievance = this.grievanceService.findGrievanceById(grievanceId);
        String email = this.grievanceService.getComplainantInfo(grievance).getEmail();
        String mobile = this.grievanceService.getComplainantInfo(grievance).getMobileNumber();
        ActionToRole actionToRole = mapTemplateWithActionToRole(userInformation, "NEW", 1L);
        EmailTemplate emailTemplate = this.emailService.findEmailTemplate(actionToRole);
        SmsTemplate smsTemplate = this.shortMessageService.findSmsTemplate(actionToRole);
        emailService.sendEmailUsingDB(email, emailTemplate, grievance);
        shortMessageService.sendSMSUsingDB(mobile, smsTemplate, grievance);
        String message = "Your Grievance with tracking number " + grievance.getTrackingNumber() + " is rejected.";
        return this.rejectOrCloseGrievance(userInformation, grievanceId, comment.getNote(), GrievanceCurrentStatus.REJECTED, message, comment.getFiles(), null);
    }

    @Transactional(value = "transactionManager", rollbackFor = RuntimeException.class)
    public GenericResponse rejectOrCloseGrievance(UserInformation userInformation,
                                                  Long grievanceId,
                                                  String comment,
                                                  GrievanceCurrentStatus grievanceCurrentStatus,
                                                  String message,
                                                  List<FileDTO> files,
                                                  List<Long> referredFiles) {
        Grievance grievance = this.grievanceService.findGrievanceById(grievanceId);
        GrievanceCurrentStatus previousStatus = grievance.getGrievanceCurrentStatus();

        grievance.setGrievanceCurrentStatus(grievanceCurrentStatus);

        GrievanceForwarding grievanceForwarding = this.getGrievanceForwarding(
                grievance,
                comment,
                grievanceCurrentStatus.toString(),
                userInformation.getOfficeInformation().getEmployeeRecordId(),
                userInformation.getOfficeInformation().getEmployeeRecordId(),
                userInformation.getOfficeInformation().getOfficeId(),
                userInformation.getOfficeInformation().getOfficeId(),
                userInformation.getOfficeInformation().getOfficeUnitOrganogramId(),
                userInformation.getOfficeInformation().getOfficeUnitOrganogramId(),
                null,
                false,
                false,
                false,
                null,
                previousStatus,
                RoleType.GRO,
                userInformation.getUsername()
        );
        try {
            grievanceForwarding = this.grievanceForwardingDAO.forwardGrievanceRemovingFromInbox(grievanceForwarding);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Grievance movement insertion error. Please contact with admin");
        }
        try {
            this.grievanceService.saveGrievance(grievance);
        } catch (Throwable t) {
            throw new RuntimeException("Grievance update error. Please contact with admin");
        }
        String body = message.replace("grievance", "Your grievance (tracking id : " + grievance.getTrackingNumber() + ") ");
        this.notifyComplainantAboutStatusChange(this.grievanceService.findGrievanceById(grievanceId), message, body);
        if (files != null) {
            this.attachedFileService.addMovementAttachedFiles(grievanceForwarding, files);
        }
        if (referredFiles != null && referredFiles.size() > 0) {
            List<MovementAttachedFile> attachedFiles = this.attachedFileService.getAttachedFilesByIdList(referredFiles);
            this.attachedFileService.addMovementAttachedFilesRef(grievanceForwarding, attachedFiles);
        }
        //

        message = grievanceCurrentStatus.equals(GrievanceCurrentStatus.REJECTED) ? "অভিযোগ প্রত্যাখ্যান করা হয়েছে" : "অভিযোগ নিষ্পত্তি করা হয়েছে";
        return new GenericResponse(true, message);
    }

    public void notifyComplainantAboutStatusChange(Grievance grievance, String header, String body) {
        ComplainantInfoDTO complainantDTO = this.grievanceService.getComplainantInfo(grievance);
        if (grievance.isGrsUser() && complainantDTO.getEmail() != null) {
            emailService.sendEmail(complainantDTO.getEmail(), header, body);
        }
        if (grievance.isGrsUser() && complainantDTO.getMobileNumber() != null) {
            shortMessageService.sendSMS(complainantDTO.getMobileNumber(), body);
        }
    }

    private List<Long> getCurrentInvestigationCommitteeMembersOrganograms(List<GrievanceForwarding> grievanceForwardings, GrievanceCurrentStatus currentStatus) {
        List<Long> organogramIds = new ArrayList<>();
        List<GrievanceForwarding> refinedForwardings = grievanceForwardings.stream().filter(grievanceForwarding -> grievanceForwarding.getCurrentStatus().equals(currentStatus)).collect(Collectors.toList());
        Long currentId = refinedForwardings.get(0).getId();
        organogramIds.add(refinedForwardings.get(0).getToOfficeUnitOrganogramId());
        refinedForwardings.remove(0);
        for (GrievanceForwarding grievanceForwarding : refinedForwardings) {
            if (Math.abs(grievanceForwarding.getId() - currentId) > 1) {
                break;
            }
            currentId = grievanceForwarding.getId();
            organogramIds.add(grievanceForwarding.getToOfficeUnitOrganogramId());
        }
        return organogramIds;
    }

    public List<GrievanceForwardingEmployeeRecordsDTO> getAllComplainantComplaintMovementHistoryByGrievance(Long grievanceId, Authentication authentication) {
        List<GrievanceForwarding> grievanceForwardings = this.grievanceForwardingDAO.findByGrievanceIdAndAssignedRoleWithForwarded(grievanceId, RoleType.COMPLAINANT.name());
        return grievanceForwardings.stream()
                .map(grievanceForwarding -> GrievanceForwardingEmployeeRecordsDTO.builder()
                        .toGroNameBangla(grievanceForwarding.getToEmployeeNameBangla())
                        .fromGroNameBangla(grievanceForwarding.getFromEmployeeNameBangla())
                        .toGroNameEnglish(grievanceForwarding.getToEmployeeNameEnglish())
                        .fromGroNameEnglish(grievanceForwarding.getFromEmployeeNameEnglish())
                        .comment(grievanceForwarding.getComment())
                        .action(grievanceForwarding.getAction())
                        .createdAtEng(DateTimeConverter.convertDateToStringForTimeline(grievanceForwarding.getCreatedAt()))
                        .createdAtBng(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToStringForTimeline(grievanceForwarding.getCreatedAt())))
                        .createdAtFullEng(DateTimeConverter.convertDateToStringForTimelineFull(grievanceForwarding.getCreatedAt()))
                        .createdAtFullBng(BanglaConverter.getDateBanglaFromEnglishFull(DateTimeConverter.convertDateToStringForTimelineFull(grievanceForwarding.getCreatedAt())))
                        .files(getFiles(grievanceForwarding))
                        .toDesignationNameBangla(grievanceForwarding.getToEmployeeDesignationBangla())
                        .fromDesignationNameBangla(grievanceForwarding.getFromEmployeeDesignationBangla())
                        .toOfficeNameBangla(grievanceForwarding.getToOfficeNameBangla())
                        .fromOfficeNameBangla(grievanceForwarding.getFromOfficeNameBangla())
                        .toOfficeUnitNameBangla(grievanceForwarding.getToEmployeeUnitNameBangla())
                        .fromOfficeUnitNameBangla(grievanceForwarding.getFromEmployeeUnitNameBangla())
                        .fromGroUsername(grievanceForwarding.getFromEmployeeUsername())
                        .isCC(grievanceForwarding.getIsCC())
                        .isCommitteeHead(grievanceForwarding.getIsCommitteeHead())
                        .isCommitteeMember(grievanceForwarding.getIsCommitteeMember())
                        .assignedRole(grievanceForwarding.getAssignedRole())
                        .build())
                .collect(Collectors.toList());
    }

    public List<GrievanceForwardingEmployeeRecordsDTO> getAllComplainantComplaintMovementHistoryByTrackingNumber(String trackingNumber) {
        Grievance grievance = this.grievanceService.getSingleGrievanceByTrackingNumber(trackingNumber);
        if (grievance == null)return new ArrayList<>();
        List<GrievanceForwarding> grievanceForwardings = this.grievanceForwardingDAO.findByGrievanceId(grievance.getId());
        return grievanceForwardings.stream()
                .map(grievanceForwarding -> GrievanceForwardingEmployeeRecordsDTO.builder()
                        .toGroNameBangla(grievanceForwarding.getToEmployeeNameBangla())
                        .fromGroNameBangla(grievanceForwarding.getFromEmployeeNameBangla())
                        .toGroNameEnglish(grievanceForwarding.getToEmployeeNameEnglish())
                        .fromGroNameEnglish(grievanceForwarding.getFromEmployeeNameEnglish())
                        .comment(grievanceForwarding.getComment())
                        .action(grievanceForwarding.getAction())
                        .createdAtEng(DateTimeConverter.convertDateToStringForTimeline(grievanceForwarding.getCreatedAt()))
                        .createdAtBng(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToStringForTimeline(grievanceForwarding.getCreatedAt())))
                        .createdAtFullEng(DateTimeConverter.convertDateToStringForTimelineFull(grievanceForwarding.getCreatedAt()))
                        .createdAtFullBng(BanglaConverter.getDateBanglaFromEnglishFull(DateTimeConverter.convertDateToStringForTimelineFull(grievanceForwarding.getCreatedAt())))
                        .files(getFiles(grievanceForwarding))
                        .toDesignationNameBangla(grievanceForwarding.getToEmployeeDesignationBangla())
                        .fromDesignationNameBangla(grievanceForwarding.getFromEmployeeDesignationBangla())
                        .toOfficeNameBangla(grievanceForwarding.getToOfficeNameBangla())
                        .fromOfficeNameBangla(grievanceForwarding.getFromOfficeNameBangla())
                        .toOfficeUnitNameBangla(grievanceForwarding.getToEmployeeUnitNameBangla())
                        .fromOfficeUnitNameBangla(grievanceForwarding.getFromEmployeeUnitNameBangla())
                        .fromGroUsername(grievanceForwarding.getFromEmployeeUsername())
                        .isCC(grievanceForwarding.getIsCC())
                        .isCommitteeHead(grievanceForwarding.getIsCommitteeHead())
                        .isCommitteeMember(grievanceForwarding.getIsCommitteeMember())
                        .assignedRole(grievanceForwarding.getAssignedRole())
                        .build())
                .collect(Collectors.toList());
    }

    public List<FileDerivedDTO> getAllComplaintMovementAttachedFiles(Authentication authentication, Long grievanceId){
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        List<GrievanceForwarding> grievanceForwardings;
        List<FileDerivedDTO> files = new ArrayList<>();
        if(userInformation.getUserType().equals(UserType.COMPLAINANT)){
            grievanceForwardings = this.grievanceForwardingDAO.findByGrievanceIdAndAssignedRole(grievanceId, RoleType.COMPLAINANT.name());
        } else {
            Grievance grievance = this.grievanceService.findGrievanceById(grievanceId);
            grievanceForwardings = this.getAllUserRelatedForwardings(grievance, userInformation);
        }
        grievanceForwardings.forEach(
                grievanceForwarding -> {
                    List<FileDerivedDTO> fileDerivedDTOS = getFiles(grievanceForwarding);
                    if(fileDerivedDTOS != null){
                        files.addAll(fileDerivedDTOS);
                    }
                }
        );
        return files;
    }

    public int getCountOfComplaintMovementAttachedFiles(Authentication authentication, Long grievanceId){
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        List<GrievanceForwarding> grievanceForwardings;
        List<FileDerivedDTO> files = new ArrayList<>();
        if(userInformation.getUserType().equals(UserType.COMPLAINANT)){
            grievanceForwardings = this.grievanceForwardingDAO.findByGrievanceIdAndAssignedRole(grievanceId, RoleType.COMPLAINANT.name());
        } else {
            Grievance grievance = this.grievanceService.findGrievanceById(grievanceId);
            grievanceForwardings = this.getAllUserRelatedForwardings(grievance, userInformation);
        }
        grievanceForwardings.forEach(
                grievanceForwarding -> {
                    List<FileDerivedDTO> fileDerivedDTOS = getFiles(grievanceForwarding);
                    if(fileDerivedDTOS != null){
                        files.addAll(fileDerivedDTOS);
                    }
                }
        );
        return files.size();
    }

    //TODO:: FIX Issue
    public List<GrievanceForwarding> getAllUserRelatedForwardings(Grievance grievance, UserInformation userInformation){
        if (userInformation.getOfficeInformation() == null) {
            return new ArrayList<>();
        }
        List<GrievanceForwarding> grievanceForwardings;
        Long officeId = userInformation.getOfficeInformation().getOfficeId();
        Long officeUnitOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();
        GrievanceForwarding committeeEntry = this.grievanceForwardingDAO.getByActionAndToOfficeIdAndToOfficeUnitOrganogramIdAndGrievance(grievance, officeId, officeUnitOrganogramId, "INVESTIGATION");
        if (committeeEntry != null && (committeeEntry.getIsCommitteeMember() || committeeEntry.getIsCommitteeHead())) {
            List<GrievanceForwarding> committeeForwardings = this.grievanceForwardingDAO.findByGrievanceAndActionLikeOrderByIdDesc(grievance, "%INVESTIGATION%");
            List<Long> committeeOrganograms = getCurrentInvestigationCommitteeMembersOrganograms(committeeForwardings, committeeEntry.getCurrentStatus());
            GrievanceForwarding reportEntry = committeeEntry.getCurrentStatus().toString().contains("APPEAL") ?
                    this.grievanceForwardingDAO.findByGrievanceAndActionLikeAndCurrentStatusLike(grievance, "%REPORT%", "%APPEAL%")
                    : this.grievanceForwardingDAO.findByGrievanceAndActionLikeAndCurrentStatusNotLike(grievance, "%REPORT%", "%APPEAL%");
            grievanceForwardings = this.grievanceForwardingDAO.getAllRelatedComplaintMovementsBetweenDates(
                    grievance.getId(), userInformation.getOfficeInformation().getOfficeId(),
                    new ArrayList<Long>() {{
                        addAll(committeeOrganograms);
                    }},
                    "",
                    committeeEntry.getCreatedAt(),
                    reportEntry == null ? new Date() : reportEntry.getCreatedAt()
            );
            grievanceForwardings.addAll(this.grievanceForwardingDAO.getAllRelatedComplaintMovements(
                    grievance.getId(), officeId, new ArrayList<Long>() {{
                        add(officeUnitOrganogramId);
                    }}, ""
            ));
        } else if (userInformation.getOisfUserType().equals(OISFUserType.HEAD_OF_OFFICE)) {
            OfficesGRO officesGRO = this.officesGroService.findOfficesGroByOfficeId(userInformation.getOfficeInformation().getOfficeId());
            List<Long> orgUnitList = new ArrayList<>();
            if (officesGRO.getGroOfficeUnitOrganogramId() != null) {
                orgUnitList.add(officesGRO.getGroOfficeUnitOrganogramId());
            }
            if (userInformation.getOfficeInformation() != null && userInformation.getOfficeInformation().getOfficeUnitOrganogramId() != null) {
                orgUnitList.add(userInformation.getOfficeInformation().getOfficeUnitOrganogramId());
            }
            grievanceForwardings = this.grievanceForwardingDAO.getAllRelatedComplaintMovements(grievance.getId(),
                    officesGRO.getOfficeId(),orgUnitList,
                    "");
        } else {
            grievanceForwardings = this.grievanceForwardingDAO.getAllRelatedComplaintMovements(
                    grievance.getId(), officeId, new ArrayList<Long>() {{
                        add(officeUnitOrganogramId);
                    }}, ""
            );
        }
        return grievanceForwardings.stream().distinct().collect(Collectors.toList());
    }
    public List<GrievanceForwardingEmployeeRecordsDTO> searchAllComplaintMovementHistoryByGrievance(Long grievanceId) {
        Grievance grievance = this.grievanceService.findGrievanceById(grievanceId);
        List<GrievanceForwarding> grievanceForwardings = grievanceForwardingDAO.getAllComplaintMovement(grievance);

        return grievanceForwardings.stream()
                .map(grievanceForwarding -> GrievanceForwardingEmployeeRecordsDTO.builder()
                        .toGroNameBangla(grievanceForwarding.getToEmployeeNameBangla())
                        .fromGroNameBangla(grievanceForwarding.getFromEmployeeNameBangla())
                        .toGroNameEnglish(grievanceForwarding.getToEmployeeNameEnglish())
                        .fromGroNameEnglish(grievanceForwarding.getFromEmployeeNameEnglish())
                        .comment(grievanceForwarding.getComment())
                        .action(grievanceForwarding.getAction())
                        .createdAtEng(DateTimeConverter.convertDateToStringForTimeline(grievanceForwarding.getCreatedAt()))
                        .createdAtBng(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToStringForTimeline(grievanceForwarding.getCreatedAt())))
                        .createdAtFullEng(DateTimeConverter.convertDateToStringForTimelineFull(grievanceForwarding.getCreatedAt()))
                        .createdAtFullBng(BanglaConverter.getDateBanglaFromEnglishFull(DateTimeConverter.convertDateToStringForTimelineFull(grievanceForwarding.getCreatedAt())))
                        .files(getFiles(grievanceForwarding))
                        .toDesignationNameBangla(grievanceForwarding.getToEmployeeDesignationBangla())
                        .fromDesignationNameBangla(grievanceForwarding.getFromEmployeeDesignationBangla())
                        .toOfficeNameBangla(grievanceForwarding.getToOfficeNameBangla())
                        .fromOfficeNameBangla(grievanceForwarding.getFromOfficeNameBangla())
                        .toOfficeUnitNameBangla(grievanceForwarding.getToEmployeeUnitNameBangla())
                        .fromOfficeUnitNameBangla(grievanceForwarding.getFromEmployeeUnitNameBangla())
                        .fromGroUsername(grievanceForwarding.getFromEmployeeUsername())
                        .isCC(grievanceForwarding.getIsCC())
                        .isCommitteeHead(grievanceForwarding.getIsCommitteeHead())
                        .isCommitteeMember(grievanceForwarding.getIsCommitteeMember())
                        .assignedRole(grievanceForwarding.getAssignedRole())
                        .build())
                .collect(Collectors.toList());
    }

    public List<GrievanceForwardingEmployeeRecordsDTO> getAllComplaintMovementHistoryByGrievance(Long grievanceId, Authentication authentication) {
        Grievance grievance = this.grievanceService.findGrievanceById(grievanceId);
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        List<GrievanceForwarding> grievanceForwardings = getAllUserRelatedForwardings(grievance, userInformation);

        return grievanceForwardings.stream()
                .map(grievanceForwarding -> GrievanceForwardingEmployeeRecordsDTO.builder()
                        .toGroNameBangla(grievanceForwarding.getToEmployeeNameBangla())
                        .fromGroNameBangla(grievanceForwarding.getFromEmployeeNameBangla())
                        .toGroNameEnglish(grievanceForwarding.getToEmployeeNameEnglish())
                        .fromGroNameEnglish(grievanceForwarding.getFromEmployeeNameEnglish())
                        .comment(grievanceForwarding.getComment())
                        .action(grievanceForwarding.getAction())
                        .createdAtEng(DateTimeConverter.convertDateToStringForTimeline(grievanceForwarding.getCreatedAt()))
                        .createdAtBng(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToStringForTimeline(grievanceForwarding.getCreatedAt())))
                        .createdAtFullEng(DateTimeConverter.convertDateToStringForTimelineFull(grievanceForwarding.getCreatedAt()))
                        .createdAtFullBng(BanglaConverter.getDateBanglaFromEnglishFull(DateTimeConverter.convertDateToStringForTimelineFull(grievanceForwarding.getCreatedAt())))
                        .files(getFiles(grievanceForwarding))
                        .toDesignationNameBangla(grievanceForwarding.getToEmployeeDesignationBangla())
                        .fromDesignationNameBangla(grievanceForwarding.getFromEmployeeDesignationBangla())
                        .toOfficeNameBangla(grievanceForwarding.getToOfficeNameBangla())
                        .fromOfficeNameBangla(grievanceForwarding.getFromOfficeNameBangla())
                        .toOfficeUnitNameBangla(grievanceForwarding.getToEmployeeUnitNameBangla())
                        .fromOfficeUnitNameBangla(grievanceForwarding.getFromEmployeeUnitNameBangla())
                        .fromGroUsername(grievanceForwarding.getFromEmployeeUsername())
                        .isCC(grievanceForwarding.getIsCC())
                        .isCommitteeHead(grievanceForwarding.getIsCommitteeHead())
                        .isCommitteeMember(grievanceForwarding.getIsCommitteeMember())
                        .assignedRole(grievanceForwarding.getAssignedRole())
                        .build())
                .collect(Collectors.toList());
    }

    public List<FileDerivedDTO> getFiles(GrievanceForwarding grievanceForwarding) {
        List<MovementAttachedFile> attachedFiles = grievanceForwarding.getAttachedFiles();
        List<FileDerivedDTO> files = null;

        if (attachedFiles.size() > 0) {
            files = attachedFiles.stream().map(attachedFile -> {
                StringBuilder stringBuilderFirst = StringUtil.replaceAll(
                        new StringBuilder(attachedFile.getFilePath().substring(1)),
                        "uploadedFiles", "api/file/upload");
                String link = StringUtil.replaceAll(stringBuilderFirst, "\\", "/").append("/").toString();
                return FileDerivedDTO.builder()
                        .url(link)
                        .name(attachedFile.getFileName())
                        .build();
            }).collect(Collectors.toList());
        }
        return files;
    }

    public List<ExistingFileDerivedDTO> getFiles(GrievanceForwarding grievanceForwarding, int type) {
        List<MovementAttachedFile> attachedFiles = grievanceForwarding.getAttachedFiles();
        List<ExistingFileDerivedDTO> files = null;

        if (attachedFiles.size() > 0) {
            files = attachedFiles.stream().map(attachedFile -> {
                StringBuilder stringBuilderFirst = StringUtil.replaceAll(
                        new StringBuilder(attachedFile.getFilePath().substring(1)),
                        "uploadedFiles", "api/file/upload");
                String link = StringUtil.replaceAll(stringBuilderFirst, "\\", "/").append("/").toString();
                return ExistingFileDerivedDTO.builder()
                        .id(attachedFile.getId())
                        .url(link)
                        .name(attachedFile.getFileName())
                        .build();
            }).collect(Collectors.toList());
        }
        return files;
    }

    public GrievanceForwarding convertToGrievanceForwardingFromDTO(GrievanceForwardingDTO grievanceForwardingDTO, String action, RoleType roleType, String username) {
        return this.getGrievanceForwarding(this.grievanceService.findGrievanceById(grievanceForwardingDTO.getGrievanceId()),
                grievanceForwardingDTO.getNote(),
                action,
                grievanceForwardingDTO.getToEmployeeRecordId(),
                grievanceForwardingDTO.getFromEmployeeRecordId(),
                grievanceForwardingDTO.getToOfficeId(),
                grievanceForwardingDTO.getFromOfficeId(),
                grievanceForwardingDTO.getToOfficeOrganogramId(),
                grievanceForwardingDTO.getFromOfficeOrganogramId(),
                null,
                grievanceForwardingDTO.getIsCC(),
                grievanceForwardingDTO.getIsCommitteeHead(),
                grievanceForwardingDTO.getIsCommitteeMember(),
                grievanceForwardingDTO.getDeadlineDate(),
                grievanceForwardingDTO.getCurrentStatus(),
                roleType,
                username
        );
    }


    public GrievanceForwardingDTO convertFromOpinionReceiverDTO(OpinionReceiverDTO opinionReceiverDTO) {
        return GrievanceForwardingDTO.builder()
                .toEmployeeRecordId(opinionReceiverDTO.getToEmployeeRecordId())
                .fromEmployeeRecordId(opinionReceiverDTO.getFromEmployeeRecordId())
                .toOfficeId(opinionReceiverDTO.getToOfficeId())
                .fromOfficeId(opinionReceiverDTO.getFromOfficeId())
                .toOfficeOrganogramId(opinionReceiverDTO.getToOfficeOrganogramId())
                .fromOfficeOrganogramId(opinionReceiverDTO.getFromOfficeOrganogramId())
                .currentStatus(opinionReceiverDTO.getCurrentStatus())
                .build();
    }

    @Transactional(value = "transactionManager", rollbackFor = RuntimeException.class)
    public GenericResponse sendForOpinion(Authentication authentication, OpinionRequestDTO opinionRequestDTO) {
        Long grievanceId = opinionRequestDTO.getGrievanceId();
        List<String> postNodeList = opinionRequestDTO.getPostNode();
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        List<OpinionReceiverDTO> opinionReceiverDTOS = new ArrayList<>();
        List<OpinionReceiverDTO> opinionReceiverCcDTOS = new ArrayList<>();

        for (String postNode : postNodeList) {
            opinionReceiverDTOS.add(this.getOpinionFieldsByGrievance(grievanceId, authentication, postNode));
        }
        List<GrievanceForwardingDTO> forwardingDTOS = new ArrayList<>();
        for (OpinionReceiverDTO opinionReceiverDTO : opinionReceiverDTOS) {
            GrievanceForwardingDTO grievanceForwardingDTO = convertFromOpinionReceiverDTO(opinionReceiverDTO);
            grievanceForwardingDTO.setGrievanceId(grievanceId);
            grievanceForwardingDTO.setNote(opinionRequestDTO.getComment());
            grievanceForwardingDTO.setDeadlineDate(opinionRequestDTO.getDeadline());
            grievanceForwardingDTO.setIsCC(false);
            grievanceForwardingDTO.setIsCommitteeHead(false);
            grievanceForwardingDTO.setIsCommitteeMember(false);
            forwardingDTOS.add(grievanceForwardingDTO);
        }

        if (opinionRequestDTO.getCcNode() != null) {
            List<String> ccNodeList = opinionRequestDTO.getCcNode();
            for (String ccNode : ccNodeList) {
                opinionReceiverCcDTOS.add(this.getOpinionFieldsByGrievance(grievanceId, authentication, ccNode));
            }

            for (OpinionReceiverDTO opinionReceiverDTO : opinionReceiverCcDTOS) {
                GrievanceForwardingDTO grievanceForwardingDTO = convertFromOpinionReceiverDTO(opinionReceiverDTO);
                grievanceForwardingDTO.setGrievanceId(grievanceId);
                grievanceForwardingDTO.setNote(opinionRequestDTO.getComment());
                grievanceForwardingDTO.setDeadlineDate(opinionRequestDTO.getDeadline());
                grievanceForwardingDTO.setIsCC(true);
                grievanceForwardingDTO.setIsCommitteeHead(false);
                grievanceForwardingDTO.setIsCommitteeMember(false);
                forwardingDTOS.add(grievanceForwardingDTO);
            }
        }
        GrievanceCurrentStatus prevStatus;
        GrievanceForwarding newForwarding = this.grievanceForwardingDAO.getLastForwadingForGivenGrievance(this.grievanceService.findGrievanceById(grievanceId));
        Grievance grievance = newForwarding.getGrievance();
        String action = "SEND_FOR_OPINION";
        String successMessage = "সফলভাবে মতামতের জন্য পাঠানো হয়েছে";
        if (grievance.getGrievanceCurrentStatus().equals(GrievanceCurrentStatus.NEW)) {
            grievance.setGrievanceCurrentStatus(GrievanceCurrentStatus.ACCEPTED);
            grievance.setCaseNumber(this.grievanceService.getCaseNumber(grievance.getOfficeId()));
            try {
                this.grievanceService.saveGrievance(grievance);
            } catch (Throwable t) {
                throw new RuntimeException("Internal error. Please contact with admin");
            }

            String message = "<p>অভিযোগটি গৃহীত হয়েছে</p>";
            GrievanceForwarding acceptedForwarding = this.getGrievanceForwarding(
                    grievance,
                    message,
                    "ACCEPTED",
                    newForwarding.getToEmployeeRecordId(),
                    newForwarding.getFromEmployeeRecordId(),
                    newForwarding.getToOfficeId(),
                    newForwarding.getToOfficeId(),
                    newForwarding.getToOfficeUnitOrganogramId(),
                    newForwarding.getToOfficeUnitOrganogramId(),
                    null,
                    false,
                    false,
                    false,
                    newForwarding.getDeadlineDate(),
                    GrievanceCurrentStatus.NEW,
                    RoleType.GRO,
                    userInformation.getUsername()
            );
            try {
                this.grievanceForwardingDAO.forwardGrievanceRemovingFromInbox(acceptedForwarding);
            } catch (Throwable t) {
                throw new RuntimeException("Internal error. Please contact with admin");
            }
            prevStatus = grievance.getGrievanceCurrentStatus();
            grievance.setGrievanceCurrentStatus(GrievanceCurrentStatus.STATEMENT_ASKED);
            try {
                this.grievanceService.saveGrievance(grievance);
            } catch (Throwable t) {
                throw new RuntimeException("Internal error. Please contact with admin");
            }

        } else if (grievance.getGrievanceCurrentStatus().toString().startsWith("APPEAL")) {
            if (grievance.getCaseNumber() == null) {
                grievance.setCaseNumber(this.grievanceService.getCaseNumber(grievance.getOfficeId()));
            }
            prevStatus = grievance.getGrievanceCurrentStatus();
            grievance.setGrievanceCurrentStatus(GrievanceCurrentStatus.APPEAL_STATEMENT_ASKED);
            try {
                this.grievanceService.saveGrievance(grievance);
            } catch (Throwable t) {
                throw new RuntimeException("Internal error. Please contact with admin");
            }
            action = "APPEAL_STATEMENT_ASKED";

        } else {
            if (grievance.getCaseNumber() == null) {
                grievance.setCaseNumber(this.grievanceService.getCaseNumber(grievance.getOfficeId()));
            }
            prevStatus = grievance.getGrievanceCurrentStatus();
            grievance.setGrievanceCurrentStatus(GrievanceCurrentStatus.STATEMENT_ASKED);
            try {
                this.grievanceService.saveGrievance(grievance);
            } catch (Throwable t) {
                throw new RuntimeException("Internal error. Please contact with admin");
            }
        }

        for (GrievanceForwardingDTO grievanceForwardingDTO : forwardingDTOS) {
            grievanceForwardingDTO.setCurrentStatus(prevStatus);
            GrievanceForwarding grievanceForwardingForSentForOpinion = this.convertToGrievanceForwardingFromDTO(grievanceForwardingDTO, action, RoleType.SERVICE_OFFICER, userInformation.getUsername());
            GrievanceForwarding grievanceForwarding = this.grievanceForwardingDAO.forwardGrievanceRemovingFromInbox(grievanceForwardingForSentForOpinion);
            if (opinionRequestDTO.getReferredFiles() != null && opinionRequestDTO.getReferredFiles().size() > 0) {
                List<MovementAttachedFile> attachedFiles = this.attachedFileService.getAttachedFilesByIdList(opinionRequestDTO.getReferredFiles());
                this.attachedFileService.addMovementAttachedFilesRef(grievanceForwarding, attachedFiles);
            }
            if (opinionRequestDTO.getFiles() != null && opinionRequestDTO.getFiles().size() > 0) {
                this.attachedFileService.addMovementAttachedFiles(grievanceForwarding, opinionRequestDTO.getFiles());
            }
        }
        return new GenericResponse(true, successMessage);
    }

    private EmployeeOrganogram getOfficeOrgDetail(String post) {
        String[] splittedNodeId = post.split("_");
        Long officeMinistryId = Long.parseLong(splittedNodeId[1]);
        Long officeId = Long.parseLong(splittedNodeId[2]);
        Long officeUnitOrganogramId = Long.parseLong(splittedNodeId[3]);
        return new EmployeeOrganogram(officeId, officeMinistryId, officeUnitOrganogramId);
    }

    @Transactional(value = "transactionManager", rollbackFor = RuntimeException.class)
    public GenericResponse initiateInvestigation(GrievanceForwardingInvestigationDTO grievanceForwardingInvestigationDTO, Authentication authentication) {
        EmployeeOrganogram headOfInvestigation = getOfficeOrgDetail(grievanceForwardingInvestigationDTO.getHead());
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Long officeId = userInformation.getOfficeInformation().getOfficeId();
        Long toOfficeUnitOrganogramId = headOfInvestigation.getOfficeUnitOrganogramId();
        Long fromOfficeUnitOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();
        EmployeeOffice toEmployeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(headOfInvestigation.getOfficeId(), toOfficeUnitOrganogramId, true);
        EmployeeOffice fromEmployeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(officeId, fromOfficeUnitOrganogramId, true);
        List<String> committeeMembers = grievanceForwardingInvestigationDTO.getCommittee();

        GrievanceCurrentStatus currentStatus;
        Grievance grievance = this.grievanceService.findGrievanceById(grievanceForwardingInvestigationDTO.getGrievanceId());
        if (grievance.getGrievanceCurrentStatus().toString().startsWith("APPEAL")) {
            currentStatus = GrievanceCurrentStatus.INVESTIGATION_APPEAL;
        } else {
            currentStatus = GrievanceCurrentStatus.INVESTIGATION;
        }
        List<EmployeeOrganogram> members = new ArrayList<>();
        for (String member : committeeMembers) {
            EmployeeOrganogram employeeOrganogram = getOfficeOrgDetail(member);
            members.add(employeeOrganogram);
        }

        GrievanceForwarding grievanceForwarding = this.getGrievanceForwarding(
                grievance,
                grievanceForwardingInvestigationDTO.getNote(),
                "INVESTIGATION",
                toEmployeeOffice.getEmployeeRecord().getId(),
                fromEmployeeOffice.getEmployeeRecord().getId(),
                headOfInvestigation.getOfficeId(),
                officeId,
                headOfInvestigation.getOfficeUnitOrganogramId(),
                fromOfficeUnitOrganogramId,
                null,
                false,
                true,
                false,
                null,
                currentStatus,
                RoleType.INV_HEAD,
                userInformation.getUsername()
        );
        try {
            this.grievanceForwardingDAO.forwardGrievanceRemovingFromInbox(grievanceForwarding);
        } catch (Throwable t) {
            throw new RuntimeException("Internal error. Please contact with admin");
        }
        if (toEmployeeOffice.getEmployeeRecord().getPersonalMobile() != null) {
            this.shortMessageService.sendSMS(toEmployeeOffice.getEmployeeRecord().getPersonalMobile(), "আপনাকে তদন্ত কমিটির প্রধান নির্বাচিত করা হয়েছে। grs.gov.bd এ লগইন করুন এবং আপনার ইনবক্স দেখুন");
        }

        for (EmployeeOrganogram member : members) {
            EmployeeOffice toEmployeeOfficeMember = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(member.getOfficeId(), member.getOfficeUnitOrganogramId(), true);
            GrievanceForwarding committeeMemberGrievanceForward = this.getGrievanceForwarding(
                    grievance,
                    grievanceForwardingInvestigationDTO.getNote(),
                    "INVESTIGATION",
                    toEmployeeOfficeMember.getEmployeeRecord().getId(),
                    fromEmployeeOffice.getEmployeeRecord().getId(),
                    member.getOfficeId(),
                    officeId,
                    member.getOfficeUnitOrganogramId(),
                    fromOfficeUnitOrganogramId,
                    null,
                    false,
                    false,
                    true,
                    null,
                    currentStatus,
                    RoleType.INV_MEMBER,
                    userInformation.getUsername()
            );
            try {
                this.grievanceForwardingDAO.forwardGrievanceKeepingAtInbox(committeeMemberGrievanceForward);
            } catch (Throwable t) {
                throw new RuntimeException("Internal error. Please contact with admin");
            }
            if (toEmployeeOffice.getEmployeeRecord().getPersonalMobile() != null) {
                this.shortMessageService.sendSMS(toEmployeeOffice.getEmployeeRecord().getPersonalMobile(), "আপনাকে তদন্ত কমিটির সদস্য নির্বাচিত করা হয়েছে। grs.gov.bd এ লগইন করুন এবং আপনার ইনবক্স দেখুন");
            }
        }
        grievance.setGrievanceCurrentStatus(currentStatus);
        try {
            this.grievanceService.saveGrievance(grievance);
        } catch (Throwable t){
            throw new RuntimeException("Internal error. Please contact with admin");
        }
        String successMessage = "তদন্ত কমিটি সফলভাবে তৈরি করা হয়েছে";
        return new GenericResponse(true, successMessage);
    }

    public GenericResponse requestEvidences(InvestigationMaterialHearingDTO investigationMaterialHearingDTO, Authentication authentication) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Grievance grievance = this.grievanceService.findGrievanceById(investigationMaterialHearingDTO.getGrievanceId());
        String action = "REQUEST_EVIDENCES";
        GrievanceCurrentStatus currentStatus;
        Long fromOfficeId;
        Long fromOfficeUnitOrganogramId;
        Long fromEmployeeRecordId;
        if (grievance.getGrievanceCurrentStatus().toString().startsWith("INV")) {
            GrievanceForwarding grievanceForwardingHead = this.grievanceForwardingDAO.getActiveInvestigationHeadEntry(grievance);
            fromOfficeId = grievanceForwardingHead.getToOfficeId();
            fromEmployeeRecordId = grievanceForwardingHead.getToEmployeeRecordId();
            fromOfficeUnitOrganogramId = grievanceForwardingHead.getToOfficeUnitOrganogramId();
            currentStatus = grievance.getGrievanceCurrentStatus().toString().contains("APPEAL") ? GrievanceCurrentStatus.INV_NOTICE_FILE_APPEAL :
                    GrievanceCurrentStatus.INV_NOTICE_FILE;
            action = grievance.getGrievanceCurrentStatus().toString().contains("APPEAL") ? "REQUEST_EVIDENCES_APPEAL" : action;

        } else {
            EmployeeOrganogram gro = grievance.getGrievanceCurrentStatus().toString().contains("APPEAL") ?
                    this.grievanceService.getAppealOfficer(grievance.getId()): this.grievanceService.getGRO(grievance.getId());
            fromOfficeId = gro.getOfficeId();
            fromOfficeUnitOrganogramId = gro.getOfficeUnitOrganogramId();
            EmployeeOffice employeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(fromOfficeId, fromOfficeUnitOrganogramId, true);
            fromEmployeeRecordId = employeeOffice.getEmployeeRecord().getId();
            currentStatus = grievance.getGrievanceCurrentStatus().toString().contains("APPEAL") ?
                    GrievanceCurrentStatus.APPEAL_REQUEST_TESTIMONY : GrievanceCurrentStatus.REQUEST_TESTIMONY;
        }
        for (String person : investigationMaterialHearingDTO.getPersons()) {
            switch (person) {
                case "SO":
                    EmployeeOrganogram soOrganogram = this.grievanceService.getServiceOfficer(investigationMaterialHearingDTO.getGrievanceId());
                    Long soOfficeId = soOrganogram.getOfficeId();
                    Long soOfficeUnitOrganogramId = soOrganogram.getOfficeUnitOrganogramId();
                    EmployeeOffice soEmployeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(soOfficeId, soOfficeUnitOrganogramId, true);
                    GrievanceForwarding grievanceForwarding = this.getGrievanceForwarding(
                            grievance,
                            investigationMaterialHearingDTO.getNote(),
                            action,
                            soEmployeeOffice.getEmployeeRecord().getId(),
                            fromEmployeeRecordId,
                            soOfficeId,
                            fromOfficeId,
                            soOfficeUnitOrganogramId,
                            fromOfficeUnitOrganogramId,
                            null,
                            false,
                            false,
                            false,
                            null,
                            grievance.getGrievanceCurrentStatus(),
                            RoleType.SERVICE_OFFICER,
                            userInformation.getUsername()
                    );
                    this.grievanceForwardingDAO.forwardGrievanceKeepingAtInbox(grievanceForwarding);
                    break;
                case "GRO":
                    EmployeeOrganogram groOrganogram = this.grievanceService.getGRO(investigationMaterialHearingDTO.getGrievanceId());
                    Long groOfficeId = groOrganogram.getOfficeId();
                    Long groOfficeUnitOrganogramId = groOrganogram.getOfficeUnitOrganogramId();
                    EmployeeOffice groEmployeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(groOfficeId, groOfficeUnitOrganogramId, true);
                    GrievanceForwarding grievanceForwardingGRO = this.getGrievanceForwarding(
                            grievance,
                            investigationMaterialHearingDTO.getNote(),
                            action,
                            groEmployeeOffice.getEmployeeRecord().getId(),
                            fromEmployeeRecordId,
                            groOfficeId,
                            fromOfficeId,
                            groOfficeUnitOrganogramId,
                            fromOfficeUnitOrganogramId,
                            null,
                            false,
                            false,
                            false,
                            null,
                            grievance.getGrievanceCurrentStatus(),
                            RoleType.GRO,
                            userInformation.getUsername()
                    );
                    this.grievanceForwardingDAO.forwardGrievanceKeepingAtInbox(grievanceForwardingGRO);
                    break;
                case "COMPLAINANT":
                    GrievanceForwarding newForwarding = this.grievanceForwardingDAO.getLastForwadingForGivenGrievanceAndAction(grievance, "NEW");
                    GrievanceForwarding grievanceForwardingComp = this.getGrievanceForwarding(
                            grievance,
                            investigationMaterialHearingDTO.getNote(),
                            action,
                            !grievance.isGrsUser() ? newForwarding.getFromEmployeeRecordId() : fromEmployeeRecordId,
                            fromEmployeeRecordId,
                            !grievance.isGrsUser() ? newForwarding.getFromOfficeId() : fromOfficeId,
                            fromOfficeId,
                            !grievance.isGrsUser() ? newForwarding.getFromOfficeUnitOrganogramId() : fromOfficeUnitOrganogramId,
                            fromOfficeUnitOrganogramId,
                            !grievance.isGrsUser(),
                            false,
                            false,
                            false,
                            null,
                            grievance.getGrievanceCurrentStatus(),
                            RoleType.COMPLAINANT,
                            userInformation.getUsername()
                    );
                    this.grievanceForwardingDAO.forwardGrievanceKeepingAtInbox(grievanceForwardingComp);
                    break;
            }
        }
        grievance.setGrievanceCurrentStatus(currentStatus);
        this.grievanceService.saveGrievance(grievance);
        String successMessage = "প্রমাণ সফলভাবে অনুরোধ করা হয়েছে";
        return new GenericResponse(true, successMessage);
    }

    public GenericResponse takeHearing(GrievanceForwardingNoteDTO grievanceForwardingNoteDTO, Authentication authentication) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Grievance grievance = this.grievanceService.findGrievanceById(grievanceForwardingNoteDTO.getGrievanceId());
        GrievanceForwarding grievanceForwardingHead = this.grievanceForwardingDAO.getActiveInvestigationHeadEntry(grievance);
        if (grievanceForwardingHead == null) {
            String successMessage = "Committee head not found. Please contact with admin";
            return new GenericResponse(false, successMessage);
        }
        String action = "TAKE_HEARING";
        GrievanceCurrentStatus currentStatus;
        if (grievance.getGrievanceCurrentStatus().toString().endsWith("APPEAL")) {
            currentStatus = GrievanceCurrentStatus.INV_HEARING_APPEAL;
            action = "TAKE_HEARING_APPEAL";
        } else {
            currentStatus = GrievanceCurrentStatus.INV_HEARING;
        }
        GrievanceForwarding grievanceForwarding = this.getGrievanceForwarding(
                grievance,
                grievanceForwardingNoteDTO.getNote(),
                action,
                grievanceForwardingHead.getToEmployeeRecordId(), //before last step its becoming 0
                grievanceForwardingHead.getToEmployeeRecordId(),
                grievanceForwardingHead.getToOfficeId(),
                grievanceForwardingHead.getToOfficeId(),
                grievanceForwardingHead.getToOfficeUnitOrganogramId(),
                grievanceForwardingHead.getToOfficeUnitOrganogramId(),
                false,
                false,
                false,
                false,
                null,
                grievance.getGrievanceCurrentStatus(),
                RoleType.INV_HEAD,
                userInformation.getUsername()
        );
        grievanceForwarding = this.grievanceForwardingDAO.forwardGrievanceKeepingAtInbox(grievanceForwarding);

        grievance.setGrievanceCurrentStatus(currentStatus);
        this.grievanceService.saveGrievance(grievance);
        if (grievanceForwardingNoteDTO.getFiles() != null && grievanceForwardingNoteDTO.getFiles().size() > 0) {
            this.attachedFileService.addMovementAttachedFiles(grievanceForwarding, grievanceForwardingNoteDTO.getFiles());
        }
        if (grievanceForwardingNoteDTO.getReferredFiles() != null && grievanceForwardingNoteDTO.getReferredFiles().size() > 0) {
            List<MovementAttachedFile> attachedFiles = this.attachedFileService.getAttachedFilesByIdList(grievanceForwardingNoteDTO.getReferredFiles());
            this.attachedFileService.addMovementAttachedFilesRef(grievanceForwarding, attachedFiles);
        }
        String successMessage = "তদন্ত শুনানি সফলভাবে সংরক্ষণ করা হয়েছে";
        return new GenericResponse(true, successMessage);
    }

    public GenericResponse askForHearing(InvestigationMaterialHearingDTO investigationMaterialHearingDTO, Authentication authentication) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Grievance grievance = this.grievanceService.findGrievanceById(investigationMaterialHearingDTO.getGrievanceId());
        GrievanceForwarding grievanceForwardingHead = this.grievanceForwardingDAO.getActiveInvestigationHeadEntry(grievance);
        String action = "REQUEST_FOR_HEARING";
        GrievanceCurrentStatus currentStatus;
        String HearingDateBangla = "\n শুনানির তারিখ: " + BanglaConverter.getDateBanglaFromEnglishFull24HourFormat(investigationMaterialHearingDTO.getHearingDate().toString()).replace("BDT", "বাংলাদেশ স্ট্যান্ডার্ড সময়");
        if (grievance.getGrievanceCurrentStatus().toString().endsWith("APPEAL")) {
            currentStatus = GrievanceCurrentStatus.INV_NOTICE_HEARING_APPEAL;
            action = "REQUEST_FOR_HEARING_APPEAL";
        } else {
            currentStatus = GrievanceCurrentStatus.INV_NOTICE_HEARING;
        }
        for (String person : investigationMaterialHearingDTO.getPersons()) {
            switch (person) {
                case "SO":
                    EmployeeOrganogram soOrganogram = this.grievanceService.getServiceOfficer(investigationMaterialHearingDTO.getGrievanceId());
                    Long soOfficeId = soOrganogram.getOfficeId();
                    Long soOfficeUnitOrganogramId = soOrganogram.getOfficeUnitOrganogramId();
                    EmployeeOffice soEmployeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(soOfficeId, soOfficeUnitOrganogramId, true);
                    GrievanceForwarding grievanceForwarding = this.getGrievanceForwarding(
                            grievance,
                            investigationMaterialHearingDTO.getNote() + HearingDateBangla,
                            action,
                            soEmployeeOffice.getEmployeeRecord().getId(),
                            grievanceForwardingHead.getToEmployeeRecordId(),
                            soOfficeId,
                            grievanceForwardingHead.getToOfficeId(),
                            soOfficeUnitOrganogramId,
                            grievanceForwardingHead.getToOfficeUnitOrganogramId(),
                            null,
                            false,
                            false,
                            false,
                            investigationMaterialHearingDTO.getHearingDate(),
                            grievance.getGrievanceCurrentStatus(),
                            RoleType.SERVICE_OFFICER,
                            userInformation.getUsername()
                    );
                    this.grievanceForwardingDAO.forwardGrievanceKeepingAtInbox(grievanceForwarding);
                    break;
                case "GRO":
                    EmployeeOrganogram groOrganogram = this.grievanceService.getGRO(investigationMaterialHearingDTO.getGrievanceId());
                    Long groOfficeId = groOrganogram.getOfficeId();
                    Long groOfficeUnitOrganogramId = groOrganogram.getOfficeUnitOrganogramId();
                    EmployeeOffice groEmployeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(groOfficeId, groOfficeUnitOrganogramId, true);
                    GrievanceForwarding grievanceForwardingGRO = this.getGrievanceForwarding(
                            grievance,
                            investigationMaterialHearingDTO.getNote() + HearingDateBangla,
                            action,
                            groEmployeeOffice.getEmployeeRecord().getId(),
                            grievanceForwardingHead.getToEmployeeRecordId(),
                            groOfficeId,
                            grievanceForwardingHead.getToOfficeId(),
                            groOfficeUnitOrganogramId,
                            grievanceForwardingHead.getToOfficeUnitOrganogramId(),
                            null,
                            false,
                            false,
                            false,
                            investigationMaterialHearingDTO.getHearingDate(),
                            grievance.getGrievanceCurrentStatus(),
                            RoleType.GRO,
                            userInformation.getUsername()
                    );
                    this.grievanceForwardingDAO.forwardGrievanceKeepingAtInbox(grievanceForwardingGRO);
                    break;
                case "COMPLAINANT":
                    GrievanceForwarding newForwarding = this.grievanceForwardingDAO.getLastForwadingForGivenGrievanceAndAction(grievance, "NEW");
                    GrievanceForwarding grievanceForwardingComp = this.getGrievanceForwarding(
                            grievance,
                            investigationMaterialHearingDTO.getNote() + HearingDateBangla,
                            action,
                            !grievance.isGrsUser() ? newForwarding.getFromEmployeeRecordId() : grievanceForwardingHead.getToEmployeeRecordId(),
                            grievanceForwardingHead.getToEmployeeRecordId(),
                            !grievance.isGrsUser() ? newForwarding.getFromOfficeId() : grievanceForwardingHead.getToOfficeId(),
                            grievanceForwardingHead.getToOfficeId(),
                            !grievance.isGrsUser() ? newForwarding.getFromOfficeUnitOrganogramId() : grievanceForwardingHead.getToOfficeUnitOrganogramId(),
                            grievanceForwardingHead.getToOfficeUnitOrganogramId(),
                            !grievance.isGrsUser(),
                            false,
                            false,
                            false,
                            investigationMaterialHearingDTO.getHearingDate(),
                            grievance.getGrievanceCurrentStatus(),
                            RoleType.COMPLAINANT,
                            userInformation.getUsername()
                    );
                    this.grievanceForwardingDAO.forwardGrievanceKeepingAtInbox(grievanceForwardingComp);
                    Complainant complainant =  complainantService.findOne(grievance.getComplainantId());
                    if (complainant != null){
                        if(complainant.getEmail() != null) {
                            emailService.sendEmail(complainant.getEmail(), "Request For Hearing",
                                    investigationMaterialHearingDTO.getNote() + HearingDateBangla);
                        }

                        shortMessageService.sendSMS(complainant.getPhoneNumber(), "শুনানির জন্য অনুরোধ। " +
                                investigationMaterialHearingDTO.getNote() + " " + HearingDateBangla);
                    }
                    break;
            }
        }
        grievance.setGrievanceCurrentStatus(currentStatus);
        this.grievanceService.saveGrievance(grievance);
        String successMessage = "শুনানিতে যোগ দিতে একটি নোটিশ সফলভাবে পাঠানো হয়েছে।";
        return new GenericResponse(true, successMessage);
    }

    public GenericResponse joinHearing(GrievanceForwardingNoteDTO grievanceForwardingNoteDTO, Authentication authentication) {
        String action = "COMPLAINANT_JOIN_HEARING";
        Grievance grievance = this.grievanceService.findGrievanceById(grievanceForwardingNoteDTO.getGrievanceId());
        GrievanceCurrentStatus currentStatus;
        if (grievance.getGrievanceCurrentStatus().toString().endsWith("APPEAL")) {
            currentStatus = GrievanceCurrentStatus.INV_NOTICE_HEARING_APPEAL;
            action = "COMPLAINANT_JOIN_HEARING_APPEAL";
        } else {
            currentStatus = GrievanceCurrentStatus.INV_NOTICE_HEARING;
        }
        GrievanceForwarding grievanceForwardingHead = this.grievanceForwardingDAO.getActiveInvestigationHeadEntry(grievance);
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Long fromOfficeId;
        Long fromOfficeUnitOrganogramId;
        Long fromEmployeeRecordId;
        if (userInformation.getOfficeInformation() == null) {
            fromEmployeeRecordId = grievanceForwardingHead.getToEmployeeRecordId();
            fromOfficeId = grievanceForwardingHead.getToOfficeId();
            fromOfficeUnitOrganogramId = grievanceForwardingHead.getToOfficeUnitOrganogramId();
        } else {
            fromOfficeId = userInformation.getOfficeInformation().getOfficeId();
            fromOfficeUnitOrganogramId = (userInformation.getOfficeInformation().getOfficeUnitOrganogramId());
            fromEmployeeRecordId = userInformation.getOfficeInformation().getEmployeeRecordId();
        }
        GrievanceForwarding grievanceForwarding = this.getGrievanceForwarding(
                grievance,
                grievanceForwardingNoteDTO.getNote(),
                action,
                grievanceForwardingHead.getToEmployeeRecordId(),
                fromEmployeeRecordId,
                grievanceForwardingHead.getToOfficeId(),
                fromOfficeId,
                grievanceForwardingHead.getToOfficeUnitOrganogramId(),
                fromOfficeUnitOrganogramId,
                false,
                false,
                false,
                false,
                null,
                grievance.getGrievanceCurrentStatus(),
                RoleType.INV_HEAD,
                userInformation.getUsername()
        );
        if (userInformation.getOfficeInformation() == null) {
            grievanceForwarding = this.grievanceForwardingDAO.forwardGrievanceKeepingAtInbox(grievanceForwarding);
        } else {
            grievanceForwarding = this.grievanceForwardingDAO.forwardGrievanceRemovingFromInboxAndNotCurrent(grievanceForwarding);
        }
        grievance.setGrievanceCurrentStatus(currentStatus);
        this.grievanceService.saveGrievance(grievance);
        if (grievanceForwardingNoteDTO.getFiles() != null && grievanceForwardingNoteDTO.getFiles().size() > 0) {
            this.attachedFileService.addMovementAttachedFiles(grievanceForwarding, grievanceForwardingNoteDTO.getFiles());
        }
        if (grievanceForwardingNoteDTO.getReferredFiles() != null && grievanceForwardingNoteDTO.getReferredFiles().size() > 0) {
            List<MovementAttachedFile> attachedFiles = this.attachedFileService.getAttachedFilesByIdList(grievanceForwardingNoteDTO.getReferredFiles());
            this.attachedFileService.addMovementAttachedFilesRef(grievanceForwarding, attachedFiles);
        }

        String successMessage = "শুনানিতে উপস্থিতির নির্দেশ স্বীকার করে তদন্ত কমিটির জ্ঞাতার্থে প্রেরণ সফল হয়েছে।";
        return new GenericResponse(true, successMessage);
    }

    public GenericResponse addFileTransitToHearing(GrievanceForwardingNoteDTO grievanceForwardingNoteDTO, Authentication authentication) {
        String action = "SUBMIT_NEW_EVIDENCE";
        Grievance grievance = this.grievanceService.findGrievanceById(grievanceForwardingNoteDTO.getGrievanceId());
        GrievanceCurrentStatus currentStatus;
        GrievanceForwarding grievanceForwarding;
        Long fromOfficeId;
        Long fromOfficeUnitOrganogramId;
        Long fromEmployeeRecordId;
        if (grievance.getGrievanceCurrentStatus().toString().contains(GrievanceCurrentStatus.REQUEST_TESTIMONY.toString()) ||
                grievance.getGrievanceCurrentStatus().toString().contains(GrievanceCurrentStatus.TESTIMONY_GIVEN.toString())) {
            currentStatus = grievance.getGrievanceCurrentStatus().toString().contains("APPEAL") ?
                    GrievanceCurrentStatus.APPEAL_REQUEST_TESTIMONY : GrievanceCurrentStatus.TESTIMONY_GIVEN;
            UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
            Long toOfficeId;
            Long toOfficeUnitOrganogramId;
            Long toEmployeeRecordId;
            if (userInformation.getOfficeInformation() == null) {
                EmployeeOrganogram groOrg = grievance.getGrievanceCurrentStatus().toString().contains("APPEAL") ?
                        this.grievanceService.getAppealOfficer(grievance.getId()) : this.grievanceService.getGRO(grievance.getId());
                Long groEmployeeOfficeId = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(groOrg.getOfficeId(), groOrg.getOfficeUnitOrganogramId(), true)
                        .getEmployeeRecord().getId();
                fromEmployeeRecordId = groEmployeeOfficeId;
                toEmployeeRecordId = groEmployeeOfficeId;
                fromOfficeId = groOrg.getOfficeId();
                toOfficeId = groOrg.getOfficeId();
                fromOfficeUnitOrganogramId = groOrg.getOfficeUnitOrganogramId();
                toOfficeUnitOrganogramId = groOrg.getOfficeUnitOrganogramId();
            } else {
                fromOfficeId = userInformation.getOfficeInformation().getOfficeId();
                fromOfficeUnitOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();
                GrievanceForwarding toLatestForwarding = grievanceForwardingDAO.getLastActiveGrievanceForwardingOfCurrentUser(grievance, fromOfficeId, fromOfficeUnitOrganogramId);
                fromEmployeeRecordId = toLatestForwarding.getToEmployeeRecordId();
                toEmployeeRecordId = toLatestForwarding.getFromEmployeeRecordId();
                toOfficeId = toLatestForwarding.getFromOfficeId();
                toOfficeUnitOrganogramId = toLatestForwarding.getFromOfficeUnitOrganogramId();
            }
            GrievanceForwarding opinionGivenForwarding = this.getGrievanceForwarding(
                    grievance,
                    grievanceForwardingNoteDTO.getNote(),
                    "TESTIMONY_GIVEN",
                    toEmployeeRecordId,
                    fromEmployeeRecordId,
                    toOfficeId,
                    fromOfficeId,
                    toOfficeUnitOrganogramId,
                    fromOfficeUnitOrganogramId,
                    null,
                    false,
                    false,
                    false,
                    null,
                    grievance.getGrievanceCurrentStatus(),
                    grievance.getGrievanceCurrentStatus().toString().contains("APPEAL") ? RoleType.AO : RoleType.GRO,
                    userInformation.getUsername()
            );
            grievanceForwarding = this.grievanceForwardingDAO.forwardGrievanceRemovingFromInbox(opinionGivenForwarding);

        } else {

            if (grievance.getGrievanceCurrentStatus().toString().contains("APPEAL")) {
                currentStatus = GrievanceCurrentStatus.INV_NOTICE_FILE_APPEAL;
                action = "SUBMIT_NEW_EVIDENCE_APPEAL";
            } else {
                currentStatus = GrievanceCurrentStatus.INV_NOTICE_FILE;
            }

            GrievanceForwarding grievanceForwardingHead = this.grievanceForwardingDAO.getActiveInvestigationHeadEntry(grievance);
            UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
            if (userInformation.getOfficeInformation() == null) {
                fromEmployeeRecordId = grievanceForwardingHead.getToEmployeeRecordId();
                fromOfficeId = grievanceForwardingHead.getToOfficeId();
                fromOfficeUnitOrganogramId = grievanceForwardingHead.getToOfficeUnitOrganogramId();
            } else {
                fromOfficeId = userInformation.getOfficeInformation().getOfficeId();
                fromOfficeUnitOrganogramId = (userInformation.getOfficeInformation().getOfficeUnitOrganogramId());
                fromEmployeeRecordId = userInformation.getOfficeInformation().getEmployeeRecordId();
            }
            grievanceForwarding = this.getGrievanceForwarding(
                    grievance,
                    grievanceForwardingNoteDTO.getNote(),
                    action,
                    grievanceForwardingHead.getToEmployeeRecordId(),
                    fromEmployeeRecordId,
                    grievanceForwardingHead.getToOfficeId(),
                    fromOfficeId,
                    grievanceForwardingHead.getToOfficeUnitOrganogramId(),
                    fromOfficeUnitOrganogramId,
                    null,
                    false,
                    false,
                    false,
                    null,
                    grievance.getGrievanceCurrentStatus(),
                    RoleType.INV_HEAD,
                    userInformation.getUsername()
            );
            if (userInformation.getOfficeInformation() == null) {
                grievanceForwarding.setIsCurrent(false);
                grievanceForwarding = this.grievanceForwardingDAO.forwardGrievanceKeepingAtInbox(grievanceForwarding);
            } else {
                grievanceForwarding = this.grievanceForwardingDAO.forwardGrievanceRemovingFromInboxAndNotCurrent(grievanceForwarding);
            }
        }
        grievance.setGrievanceCurrentStatus(currentStatus);
        this.grievanceService.saveGrievance(grievance);
        if (grievanceForwardingNoteDTO.getFiles() != null && grievanceForwardingNoteDTO.getFiles().size() > 0) {
            this.attachedFileService.addMovementAttachedFiles(grievanceForwarding, grievanceForwardingNoteDTO.getFiles());
        }
        if (grievanceForwardingNoteDTO.getReferredFiles() != null && grievanceForwardingNoteDTO.getReferredFiles().size() > 0) {
            List<MovementAttachedFile> attachedFiles = this.attachedFileService.getAttachedFilesByIdList(grievanceForwardingNoteDTO.getReferredFiles());
            this.attachedFileService.addMovementAttachedFilesRef(grievanceForwarding, attachedFiles);
        }

        String successMessage = "নতুন প্রমাণ সফলভাবে জমা দেওয়া হয়েছে।";
        return new GenericResponse(true, successMessage);
    }

    @Transactional("transactionManager")
    public GenericResponse investigationReportSubmission(GrievanceForwardingNoteDTO grievanceForwardingNoteDTO, Authentication authentication) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Grievance grievance = this.grievanceService.findGrievanceById(grievanceForwardingNoteDTO.getGrievanceId());
        GrievanceForwarding grievanceForwardingHead = this.grievanceForwardingDAO.getActiveInvestigationHeadEntry(grievance);
        EmployeeOffice headRecord = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(grievanceForwardingHead.getToOfficeId(), grievanceForwardingHead.getToOfficeUnitOrganogramId(), true);
        GrievanceCurrentStatus currentStatus;
        GrievanceCurrentStatus prevStatus = grievance.getGrievanceCurrentStatus();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String signature = "<div class='col-md-4'> <img src=\" " + grievanceForwardingNoteDTO.getFiles().get(0).getUrl() + "\" alt=\"signature\" style=\"width:150px;height:80px;\"><br> <p>["
                + headRecord.getEmployeeRecord().getNameBangla()
                + ", " + headRecord.getDesignation() + "' " +  headRecord.getOfficeUnit().getUnitNameBangla() + ", "
                + userInformation.getOfficeInformation().getOfficeNameBangla() + "]</p>" +
                "<p>" + formatter.format(date) + "</p></div>";
        int count = this.grievanceForwardingDAO.countByIsCurrentAndGrievanceAndIsCommitteeMember(true, grievance, true);
        grievanceForwardingNoteDTO.getFiles().remove(0);
        List<FileDTO> files = grievanceForwardingNoteDTO.getFiles();
        GrievanceForwarding grievanceForwarding = this.getGrievanceForwarding(
                grievance,
                grievanceForwardingNoteDTO.getNote() + signature,
                "SUBMIT_INVESTIGATION_REPORT",
                grievanceForwardingHead.getFromEmployeeRecordId(),
                grievanceForwardingHead.getToEmployeeRecordId(),
                grievanceForwardingHead.getFromOfficeId(),
                grievanceForwardingHead.getToOfficeId(),
                grievanceForwardingHead.getFromOfficeUnitOrganogramId(),
                grievanceForwardingHead.getToOfficeUnitOrganogramId(),
                count == 0,
                false,
                false,
                false,
                null,
                prevStatus,
                grievance.getGrievanceCurrentStatus().toString().endsWith("APPEAL") ? RoleType.AO : RoleType.GRO,
                userInformation.getUsername()
        );

        if (count == 0) {
            if (grievance.getGrievanceCurrentStatus().toString().endsWith("APPEAL")) {
                currentStatus = GrievanceCurrentStatus.APPEAL_IN_REVIEW;
            } else {
                currentStatus = GrievanceCurrentStatus.IN_REVIEW;
            }
            grievanceForwarding = this.grievanceForwardingDAO.forwardGrievanceRemovingFromInbox(grievanceForwarding);
        } else {
            if (grievance.getGrievanceCurrentStatus().toString().endsWith("APPEAL")) {
                currentStatus = GrievanceCurrentStatus.INV_REPORT_APPEAL;
            } else {
                currentStatus = GrievanceCurrentStatus.INV_REPORT;
            }
            grievanceForwarding = this.grievanceForwardingDAO.forwardGrievanceRemovingFromInboxAndNotCurrent(grievanceForwarding);
        }

        if (files != null && files.size() > 0) {
            this.attachedFileService.addMovementAttachedFiles(grievanceForwarding, files);
        }
        if (grievanceForwardingNoteDTO.getReferredFiles() != null && grievanceForwardingNoteDTO.getReferredFiles().size() > 0) {
            List<MovementAttachedFile> attachedFiles = this.attachedFileService.getAttachedFilesByIdList(grievanceForwardingNoteDTO.getReferredFiles());
            this.attachedFileService.addMovementAttachedFilesRef(grievanceForwarding, attachedFiles);
        }
        grievance.setGrievanceCurrentStatus(currentStatus);
        this.grievanceService.saveGrievance(grievance);

        String successMessage = "তদন্ত প্রতিবেদন সফলভাবে জমা দেওয়া হয়েছে।";
        return new GenericResponse(true, successMessage);
    }

    public OpinionReceiverDTO getOpinionFieldsByGrievance(Long grievanceId, Authentication authentication, String postNodeId) {
        Grievance grievance = this.grievanceService.findGrievanceById(grievanceId);
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        if (userInformation.getUserType().equals(UserType.COMPLAINANT)) {
            return OpinionReceiverDTO.builder().build();
        }
        String[] splittedNodeId = postNodeId.split("_");
        if (splittedNodeId.length == 1) {
            return null;
        }
        Long toOfficeId = Long.parseLong(splittedNodeId[2]);
        Long toOfficeUnitOrganogramId = Long.parseLong(splittedNodeId[3]);
        EmployeeOffice toEmployeeOffice = this.officeService
                .findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(toOfficeId, toOfficeUnitOrganogramId, true);

        return OpinionReceiverDTO.builder()
                .toEmployeeRecordId(toEmployeeOffice.getEmployeeRecord().getId())
                .toOfficeId(toOfficeId)
                .toOfficeOrganogramId(toOfficeUnitOrganogramId)
                .employeeName(toEmployeeOffice.getEmployeeRecord().getNameBangla())
                .employeeDesignation(toEmployeeOffice.getDesignation())
                .fromOfficeId(userInformation.getOfficeInformation().getOfficeId())
                .fromEmployeeRecordId(userInformation.getOfficeInformation().getEmployeeRecordId())
                .fromOfficeOrganogramId(userInformation.getOfficeInformation().getOfficeUnitOrganogramId())
                .currentStatus(grievance.getGrievanceCurrentStatus())
                .build();
    }

    @Transactional(value = "transactionManager", rollbackFor = RuntimeException.class)
    public GenericResponse forwardGrievanceToAnotherOffice(ForwardToAnotherOfficeDTO forwardToOfficeDTO, UserInformation userInformation) {
        Grievance grievance = this.grievanceService.findGrievanceById(forwardToOfficeDTO.getGrievanceId());
        Long toOfficeId = forwardToOfficeDTO.getOfficeId();

        CitizenCharter citizenCharter = null;
        if (forwardToOfficeDTO.getCitizenCharterId() != null) {
            citizenCharter = this.citizenCharterService.findOne(forwardToOfficeDTO.getCitizenCharterId());
        }

        Long fromEmployeeRecordId = userInformation.getOfficeInformation().getEmployeeRecordId();
        Long fromOfficeId = userInformation.getOfficeInformation().getOfficeId();
        Long fromOfficeUnitOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();

        OfficesGRO toOfficesGRO = this.officesGroService.findOfficesGroByOfficeId(toOfficeId);
        Long toOfficeUnitOrganogramId = toOfficesGRO.getGroOfficeUnitOrganogramId();
        toOfficeId = toOfficesGRO.getGroOfficeId();
        if (toOfficeUnitOrganogramId == null || toOfficeId == null) {
            return new GenericResponse(false, "আপনার বাছাইকৃত দপ্তরটি সেটআপ করা নেই।");
        }
        EmployeeOffice employeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(toOfficeId, toOfficeUnitOrganogramId, true);
        if (employeeOffice == null) {
            return new GenericResponse(false, "আপনার বাছাইকৃত দপ্তরটি সেটআপ করা নেই।");
        }
        Long toEmployeeRecordId = employeeOffice.getEmployeeRecord().getId();

        GrievanceForwarding grievanceForwarding = this.getGrievanceForwarding(
                grievance,
                forwardToOfficeDTO.getNote(),
                "FORWARD_TO_ANOTHER_OFFICE",
                toEmployeeRecordId,
                fromEmployeeRecordId,
                toOfficeId,
                fromOfficeId,
                toOfficeUnitOrganogramId,
                fromOfficeUnitOrganogramId,
                null,
                false,
                false,
                false,
                null,
                grievance.getGrievanceCurrentStatus(),
                RoleType.GRO,
                userInformation.getUsername()
        );

        grievance.setGrievanceCurrentStatus(forwardToOfficeDTO.getCurrentStatus());
        grievance.setOfficeId(toOfficeId);
        grievance.setServiceOriginBeforeForward(grievance.getServiceOrigin());
        grievance.setServiceOrigin(citizenCharter == null ? null : citizenCharter.getServiceOrigin());
        grievance.setOtherServiceBeforeForward(grievance.getOtherService());
        grievance.setOtherService(citizenCharter == null ? forwardToOfficeDTO.getOtherServiceName() : null);
        try {
            this.grievanceForwardingDAO.forwardGrievanceRemovingFromInbox(grievanceForwarding);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Grievance movement error. Please contact with admin");
        }

        try {
            this.grievanceService.saveGrievance(grievance);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Grievance update error. Please contact with admin");
        }

        String trackingNumber = (StringUtil.isValidString(grievance.getTrackingNumber())
                && (grievance.getTrackingNumber().startsWith("01"))) ?
                grievance.getTrackingNumber().substring(11) :
                grievance.getTrackingNumber();
        String sentToOtherOfficeMessage = "অভিযোগ অন্য দপ্তরে প্রেরিত " + grievanceForwarding.getFromEmployeeNameBangla() + ", " + grievanceForwarding.getFromEmployeeDesignationBangla() + ", " + grievanceForwarding.getFromEmployeeUnitNameBangla() + ", " + grievanceForwarding.getFromOfficeNameBangla() + " কর্তৃক " + trackingNumber + " অভিযোগটি " + grievanceForwarding.getToEmployeeNameBangla() + ", " + grievanceForwarding.getToEmployeeDesignationBangla() + ", " + grievanceForwarding.getToEmployeeUnitNameBangla() + ", " + grievanceForwarding.getToOfficeNameBangla() + "-এর দপ্তরে প্রেরিত হয়েছে।";

        if (employeeOffice.getEmployeeRecord().getPersonalMobile() != null) {
            this.shortMessageService.sendSMS(employeeOffice.getEmployeeRecord().getPersonalMobile(), sentToOtherOfficeMessage);
        }
        if (employeeOffice.getEmployeeRecord().getPersonalEmail() != null) {
            this.emailService.sendEmail(employeeOffice.getEmployeeRecord().getPersonalEmail(),"অভিযোগ অন্য দপ্তরে প্রেরিত", sentToOtherOfficeMessage);
        }

        return new GenericResponse(true, "অভিযোগ অন্য অফিসে পাঠানো হয়েছে।");
    }

    @Transactional(value = "transactionManager", rollbackFor = RuntimeException.class)
    public GenericResponse appealToOfficer(GrievanceForwardingNoteDTO grievanceForwardingNoteDTO, Authentication authentication) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Boolean isServiceOfficer = Utility.isServiceOfficer(authentication);
        OfficeInformation serviceOfficerInfo = userInformation.getOfficeInformation();
        Grievance grievance = grievanceService.findGrievanceById(grievanceForwardingNoteDTO.getGrievanceId());

        OfficesGRO officesGRO = this.officesGroService.findOfficesGroByOfficeId(grievance.getOfficeId());
        EmployeeOffice groEmployeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(officesGRO.getOfficeId(), officesGRO.getGroOfficeUnitOrganogramId(), true);
        EmployeeOffice aoEmployeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(officesGRO.getAppealOfficeId(), officesGRO.getAppealOfficerOfficeUnitOrganogramId(), true);

        CellMember cellGro = cellMemberDAO.findByIsGro();
        boolean sendToCell = StringUtil.isValidString(grievance.getAppealOfficerDecision()) && grievance.getCurrentAppealOfficeId() != null && cellGro != null;

        String sql = "select count(id) from complaint_movements where complaint_id=:complaint_id and to_office_id=:paramZero and from_office_id=:paramZero and to_office_unit_id=:paramZero and from_office_unit_id=:paramZero ";

        Map<String, Object> params = new HashMap<>();
        params.put("complaint_id", grievanceForwardingNoteDTO.getGrievanceId());
        params.put("paramZero", 0);

        Long count = this.grievanceService.findCount(sql, params);
        if (count != null && count >0) {
            return new GenericResponse(false, "আপনি একাধিকবার সেল এ আপীল করতে পারবেননা। আপনার অভিযোগ আগে একবার সেল থেকে সমাধান করা হয়েছে। ");
        }

        if (!sendToCell && aoEmployeeOffice == null) {
            return new GenericResponse(false, "আপনার বাছাইকৃত দপ্তরটি সেটআপ করা নেই।");
        }

        if (isServiceOfficer) {
            if (serviceOfficerInfo == null) {
                return new GenericResponse(false, "আপনার বাছাইকৃত দপ্তরটি সেটআপ করা নেই।");
            }
            else {
                if (sendToCell) {
                    if (aoEmployeeOffice == null || aoEmployeeOffice.getEmployeeRecord() == null) {
                        return new GenericResponse(false, "আপনার বাছাইকৃত দপ্তরটি সেটআপ করা নেই।");
                    }
                }
                else {
                    if (groEmployeeOffice == null || groEmployeeOffice.getEmployeeRecord() == null) {
                        return new GenericResponse(false, "আপনার বাছাইকৃত দপ্তরটি সেটআপ করা নেই।");
                    }
                }
            }
        }
        GrievanceForwarding grievanceForwarding = this.getGrievanceForwarding(
                grievance,
                grievanceForwardingNoteDTO.getNote(),
                "APPEAL",
                sendToCell ? cellGro.getEmployeeRecordId() : aoEmployeeOffice.getEmployeeRecord().getId(),
                isServiceOfficer ?
                        serviceOfficerInfo.getEmployeeRecordId(): ( sendToCell ?
                        aoEmployeeOffice.getEmployeeRecord().getId() :
                        groEmployeeOffice.getEmployeeRecord().getId()),
                sendToCell ? 0 : officesGRO.getAppealOfficeId(),
                isServiceOfficer ? serviceOfficerInfo.getOfficeId(): (sendToCell ? aoEmployeeOffice.getOffice().getId() : officesGRO.getOfficeId()),
                sendToCell ? cellGro.getCellOfficeUnitOrganogramId() : officesGRO.getAppealOfficerOfficeUnitOrganogramId(),
                isServiceOfficer ? serviceOfficerInfo.getOfficeUnitOrganogramId(): (sendToCell ? officesGRO.getAppealOfficerOfficeUnitOrganogramId() : officesGRO.getGroOfficeUnitOrganogramId()),
                null,
                false,
                false,
                false,
                null,
                grievance.getGrievanceCurrentStatus(),
                RoleType.AO,
                userInformation.getUsername()
        );
        grievance.setGrievanceCurrentStatus(GrievanceCurrentStatus.APPEAL);
        grievance.setCurrentAppealOfficeId(sendToCell ? 0 : officesGRO.getAppealOfficeId());
        grievance.setCurrentAppealOfficerOfficeUnitOrganogramId(sendToCell ? cellGro.getCellOfficeUnitOrganogramId() : officesGRO.getAppealOfficerOfficeUnitOrganogramId());
        try {
            this.grievanceService.saveGrievance(grievance);
            this.grievanceForwardingDAO.forwardGrievanceRemovingFromInbox(grievanceForwarding);
        } catch (Throwable t) {
            throw new RuntimeException("Internal error. Please contact with admin");
        }

        if(sendToCell){
            aoEmployeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(0L, cellGro.getCellOfficeUnitOrganogramId(), true);
        }
        OfficeUnit aoOfficeUnit = aoEmployeeOffice.getOfficeUnit();
        Office aoOffice = aoEmployeeOffice.getOffice();

        String officeInfo = aoEmployeeOffice.getDesignation() + (aoOfficeUnit == null ? "" : (", " + aoOfficeUnit.getUnitNameBangla())) + ", " + aoOffice.getNameBangla();

        String phoneNumber = StringUtil.isValidString(aoOfficeUnit == null ? null : aoOfficeUnit.getPhoneNumber()) ? BanglaConverter.convertToBanglaDigit(aoOfficeUnit.getPhoneNumber()) : Constant.NO_INFO_FOUND;
        String email = StringUtil.isValidString(aoOfficeUnit == null ? null : aoOfficeUnit.getEmail()) ? aoOfficeUnit.getEmail() : Constant.NO_INFO_FOUND;

        String successMessage = "আপিল অফিসারের নিকট সফলভাবে আপীল সম্পন্ন হয়েছে\n" +
                "আপিল অফিসারের নাম: " + aoEmployeeOffice.getEmployeeRecord().getNameBangla() +
                "\nপদবী: " + officeInfo +
                "\nফোন নম্বর: " + phoneNumber +
                "\nই-মেইল: " + email;

        if (aoEmployeeOffice.getEmployeeRecord().getPersonalEmail() != null) {
            String message = "আপনার কাছে একটি আপীল করা হয়েছে। আপিলের ট্র্যাকিং নম্বর :"+grievance.getTrackingNumber()+" এবং আপিলের বিষয়:"+grievance.getSubject();
            this.emailService.sendEmail(aoEmployeeOffice.getEmployeeRecord().getPersonalEmail(), "আপনার কাছে একটি আপীল করা হয়েছে", message);
            if (!email.equalsIgnoreCase(Constant.NO_INFO_FOUND)) {
                this.emailService.sendEmail(email, "আপনার কাছে একটি আপীল করা হয়েছে", message);
            }
        }

        return new GenericResponse(true, successMessage);
    }

    public GrievanceForwardingDTO getLatestForwardingEntry(Long id) {
        GrievanceForwarding grievanceForwarding = this.grievanceForwardingDAO.getLastForwadingForGivenGrievance(this.grievanceService.findGrievanceById(id));
        return grievanceForwardingDAO.convertToGrievanceForwardingDTO(grievanceForwarding);
    }

    public Object getInvestigationHeadEntry(Long id) {
        GrievanceForwarding grievanceForwarding = this.grievanceForwardingDAO.getActiveInvestigationHeadEntry(this.grievanceService.findGrievanceById(id));
        return grievanceForwardingDAO.convertToGrievanceForwardingDTO(grievanceForwarding);
    }

    @Transactional(value = "transactionManager", rollbackFor = RuntimeException.class)
    public GenericResponse sendToAppealOfficer(Authentication authentication, GrievanceForwardingNoteDTO grievanceForwardingNoteDTO) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        OfficesGRO officesGRO = this.officesGroService.findOfficesGroByOfficeId(userInformation.getOfficeInformation().getOfficeId());
        if (officesGRO == null || officesGRO.getAppealOfficeId() == 0 || officesGRO.getAppealOfficeId() == null) {
            String successMessage = "দুঃখিত সামান্য ত্রুটির জন্য আপিল অফিসারের কাছে অভিযোগটি পাঠানো যাচ্ছে না!";
            return new GenericResponse(false, successMessage);
        }
        EmployeeOffice groEmployeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(officesGRO.getOfficeId(), officesGRO.getGroOfficeUnitOrganogramId(), true);
        EmployeeOffice aoEmployeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(officesGRO.getAppealOfficeId(), officesGRO.getAppealOfficerOfficeUnitOrganogramId(), true);

        if (aoEmployeeOffice == null) {
            return new GenericResponse(false, "আপনার বাছাইকৃত দপ্তরটি সেটআপ করা নেই।");
        }
        Grievance grievance = grievanceService.findGrievanceById(grievanceForwardingNoteDTO.getGrievanceId());
        GrievanceCurrentStatus prevStatus = grievance.getGrievanceCurrentStatus();
        Long toOfficeId = officesGRO.getAppealOfficeId();
        grievance.setGrievanceCurrentStatus(GrievanceCurrentStatus.FORWARDED_TO_AO);
        grievance.setOtherService(grievance.getServiceOrigin() == null ? grievance.getOtherService() : grievance.getServiceOrigin().getServiceNameBangla());
        grievance.setServiceOrigin(null);
        grievance.setSendToAoOfficeId(toOfficeId);
        GrievanceForwarding grievanceForwarding = this.getGrievanceForwarding(
                grievance,
                grievanceForwardingNoteDTO.getNote(),
                "FORWARDED_TO_AO",
                aoEmployeeOffice.getEmployeeRecord().getId(),
                groEmployeeOffice.getEmployeeRecord().getId(),
                toOfficeId,
                officesGRO.getGroOfficeId(),
                officesGRO.getAppealOfficerOfficeUnitOrganogramId(),
                officesGRO.getGroOfficeUnitOrganogramId(),
                null,
                false,
                false,
                false,
                null,
                prevStatus,
                RoleType.GRO,
                userInformation.getUsername()
        );
        try {
            this.grievanceService.saveGrievance(grievance);
            this.grievanceForwardingDAO.forwardGrievanceRemovingFromInbox(grievanceForwarding);
        } catch (Throwable t) {
            throw new RuntimeException("Internal error. Please contact with admin");
        }

        String successMessage = "অভিযোগটি আপিল অফিসারকে সফলভাবে পাঠানো হয়েছে";
        ActionToRole actionToRole = mapTemplateWithActionToRole(userInformation, "FORWARDED_TO_APPEAL_OFFICER", 3L);
        EmailTemplate emailTemplate = this.emailService.findEmailTemplate(actionToRole);
        SmsTemplate smsTemplate = this.shortMessageService.findSmsTemplate(actionToRole);
        String email = aoEmployeeOffice.getOfficeUnit().getEmail();
        String mobile = aoEmployeeOffice.getOfficeUnit().getPhoneNumber();
        if (email != null) {
            emailService.sendEmailUsingDB(email, emailTemplate, grievance);
        }
        if (mobile != null) {
            shortMessageService.sendSMSUsingDB(mobile, smsTemplate, grievance);
        }
        return new GenericResponse(true, successMessage);
    }

    public ActionToRole mapTemplateWithActionToRole(UserInformation userInformation, String grievance, Long actionId) {
        GrievanceStatus grievanceStatus = this.actionToRoleService.findByName(grievance);
        Action action = this.actionToRoleService.findByActionId(actionId);
        UserType userType = userInformation.getUserType();
        GrsRole role;
        if (userType.equals(UserType.OISF_USER)) {
            OISFUserType oisfUserType = userInformation.getOisfUserType();
            RoleType roleType = RoleType.valueOf(oisfUserType.toString());
            role = this.actionToRoleService.getRolebyRoleName(roleType.toString());
        } else {
            role = this.actionToRoleService.getRolebyRoleName("COMPLAINANT");
        }
        return this.actionToRoleService.findByGrievanceStatusAndRoleAndAction(grievanceStatus, role, action);
    }


    public List<TreeNodeOfficerDTO> getRootOfAOSubOffice(Long grievanceId, Authentication authentication) {
        Grievance grievance = grievanceService.findGrievanceById(grievanceId);
        Office office = officeService.getOffice(grievance.getOfficeId());
        String nodeId = "units_" + office.getOfficeMinistry().getId() + "_" + office.getId() + "_" + 0 + "_root";
        return this.officeOrganogramService.getSOOrganogram(nodeId, authentication);
    }

    public List<TreeNodeDTO> getRootOfSubOffice(Long grievanceId, Authentication authentication) {
        Grievance grievance = grievanceService.findGrievanceById(grievanceId);
        GrievanceForwarding lastGrievanceForwarding = grievanceForwardingDAO.getLastForwadingForGivenGrievance(grievance);
        if (lastGrievanceForwarding == null) {
            return new ArrayList<>();
        }
        String nodeId = "#";
        return this.officeOrganogramService.getSubOfficesWithOrganograms(nodeId, authentication);
    }

    @Transactional(value = "transactionManager", rollbackFor = RuntimeException.class)
    public GenericResponse giveOpinion(Authentication authentication, OpinionRequestDTO opinionRequestDTO) {
        Grievance grievance = grievanceService.findGrievanceById(opinionRequestDTO.getGrievanceId());
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Long officeId = userInformation.getOfficeInformation().getOfficeId();
        Long officeUnitOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();
        GrievanceForwarding latestForwarding = grievanceForwardingDAO.getLastActiveGrievanceForwardingOfCurrentUser(grievance, officeId, officeUnitOrganogramId);
        GrievanceForwarding grievanceForwarding = null;
        if (grievance.getGrievanceCurrentStatus().toString().startsWith("INV") && latestForwarding.getIsCommitteeMember()) {
            GrievanceForwarding grievanceForwardingHead = this.grievanceForwardingDAO.getActiveInvestigationHeadEntry(grievance);
            GrievanceForwarding grievanceForwardingComp = this.getGrievanceForwarding(
                    grievance,
                    opinionRequestDTO.getComment(),
                    "GIVE_OPINION",
                    grievanceForwardingHead.getToEmployeeRecordId(),
                    latestForwarding.getToEmployeeRecordId(),
                    grievanceForwardingHead.getToOfficeId(),
                    latestForwarding.getToOfficeId(),
                    grievanceForwardingHead.getToOfficeUnitOrganogramId(),
                    latestForwarding.getToOfficeUnitOrganogramId(),
                    false,
                    false,
                    false,
                    true,
                    null,
                    grievance.getGrievanceCurrentStatus(),
                    RoleType.INV_HEAD,
                    userInformation.getUsername()
            );
            try {
                grievanceForwarding = this.grievanceForwardingDAO.forwardGrievanceKeepingAtInbox(grievanceForwardingComp);
            } catch (Throwable t) {
                t.printStackTrace();
                throw new RuntimeException("Internal error. Please contact with admin");
            }
        } else {
            GrievanceForwarding opinionGivenForwarding = this.getGrievanceForwarding(
                    grievance,
                    opinionRequestDTO.getComment(),
                    "GIVE_OPINION",
                    latestForwarding.getFromEmployeeRecordId(),
                    latestForwarding.getToEmployeeRecordId(),
                    latestForwarding.getFromOfficeId(),
                    latestForwarding.getToOfficeId(),
                    latestForwarding.getFromOfficeUnitOrganogramId(),
                    latestForwarding.getToOfficeUnitOrganogramId(),
                    null,
                    false,
                    false,
                    false,
                    null,
                    grievance.getGrievanceCurrentStatus(),
                    grievance.getGrievanceCurrentStatus().toString().startsWith("APPEAL") ? RoleType.AO : RoleType.GRO,
                    userInformation.getUsername()
            );
            if (grievance.getGrievanceCurrentStatus().toString().startsWith("APPEAL")) {
                opinionGivenForwarding.setAction("APPEAL_STATEMENT_ANSWERED");
                if (!grievance.getGrievanceCurrentStatus().equals(GrievanceCurrentStatus.APPEAL_GIVE_GUIDANCE)) {
                    grievance.setGrievanceCurrentStatus(GrievanceCurrentStatus.APPEAL_STATEMENT_ANSWERED);
                    try {
                        this.grievanceService.saveGrievance(grievance);
                    } catch (Throwable t) {
                        throw new RuntimeException("Internal error. Please contact with admin");
                    }
                }
            } else {
                opinionGivenForwarding.setAction("STATEMENT_ANSWERED");
                if (!grievance.getGrievanceCurrentStatus().equals(GrievanceCurrentStatus.GIVE_GUIDANCE)) {
                    grievance.setGrievanceCurrentStatus(GrievanceCurrentStatus.STATEMENT_ANSWERED);
                    try {
                        this.grievanceService.saveGrievance(grievance);
                    } catch (Throwable t) {
                        throw new RuntimeException("Internal error. Please contact with admin");
                    }
                }
            }
            try {
                grievanceForwarding = this.grievanceForwardingDAO.forwardGrievanceRemovingFromInbox(opinionGivenForwarding);
            } catch (Throwable t) {
                throw new RuntimeException("Internal error. Please contact with admin");
            }
        }
        if (opinionRequestDTO.getReferredFiles() != null && opinionRequestDTO.getReferredFiles().size() > 0) {
            List<MovementAttachedFile> attachedFiles = this.attachedFileService.getAttachedFilesByIdList(opinionRequestDTO.getReferredFiles());
            this.attachedFileService.addMovementAttachedFilesRef(grievanceForwarding, attachedFiles);
        }
        if (opinionRequestDTO.getFiles() != null && opinionRequestDTO.getFiles().size() > 0) {
            this.attachedFileService.addMovementAttachedFiles(grievanceForwarding, opinionRequestDTO.getFiles());
        }
        String successMessage = "মতামত পাঠানো সফল হয়েছে।";
        return new GenericResponse(true, successMessage);
    }

    @Transactional(value = "transactionManager", rollbackFor = RuntimeException.class)
    public GenericResponse givePermission(Authentication authentication, GrievanceForwardingNoteDTO noteDTO) {
        Grievance grievance = grievanceService.findGrievanceById(noteDTO.getGrievanceId());
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Long officeId = userInformation.getOfficeInformation().getOfficeId();
        Long officeUnitOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();
        GrievanceForwarding latestForwarding = grievanceForwardingDAO.getLastActiveGrievanceForwardingOfCurrentUser(grievance, officeId, officeUnitOrganogramId);

        GrievanceForwarding opinionGivenForwarding = this.getGrievanceForwarding(
                grievance,
                noteDTO.getNote(),
                "PERMISSION_REPLIED",
                latestForwarding.getFromEmployeeRecordId(),
                latestForwarding.getToEmployeeRecordId(),
                latestForwarding.getFromOfficeId(),
                latestForwarding.getToOfficeId(),
                latestForwarding.getFromOfficeUnitOrganogramId(),
                latestForwarding.getToOfficeUnitOrganogramId(),
                null,
                false,
                false,
                false,
                null,
                grievance.getGrievanceCurrentStatus(),
                RoleType.GRO,
                userInformation.getUsername()
        );
        grievance.setGrievanceCurrentStatus(GrievanceCurrentStatus.PERMISSION_REPLIED);
        try {
            this.grievanceService.saveGrievance(grievance);
            this.grievanceForwardingDAO.forwardGrievanceRemovingFromInbox(opinionGivenForwarding);
        } catch (Throwable t) {
            throw new RuntimeException("Internal error. Please contact with admin");
        }

        String successMessage = "অনুমতির জন্য উত্তরটি সফলভাবে পাঠানো হয়েছে";
        return new GenericResponse(true, successMessage);
    }

    public EmployeeOrganogram getGroOfRecentmostGrievanceForwarding(Long grievanceId) {
        Grievance grievance = this.grievanceService.findGrievanceById(grievanceId);
        GrievanceForwarding lastClosedOrRejectedForwarding = this.grievanceForwardingDAO.getLastClosedOrRejectedForwarding(grievance);
        if (lastClosedOrRejectedForwarding == null) {
            return new EmployeeOrganogram();
        }
        EmployeeOrganogram gro = new EmployeeOrganogram();
        gro.setMinistryId(this.officeService.getOffice(lastClosedOrRejectedForwarding.getFromOfficeId()).getOfficeMinistry().getId());
        gro.setOfficeId(lastClosedOrRejectedForwarding.getFromOfficeId());
        gro.setOfficeUnitOrganogramId(lastClosedOrRejectedForwarding.getFromOfficeUnitOrganogramId());
        return gro;
    }

    @Transactional(value = "transactionManager", rollbackFor = RuntimeException.class)
    public GenericResponse askPermission(Authentication authentication, GrievanceForwardingNoteDTO grievanceForwardingNoteDTO) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Long fromOfficeId = userInformation.getOfficeInformation().getOfficeId();
        EmployeeOffice employeeOffice = this.officeService.findEmployeeOfficeByOfficeAndIsOfficeHead(fromOfficeId);
        if (employeeOffice == null) {
            return GenericResponse.builder().success(false).message("অনুমতিপ্রাপ্তির জন্য পাঠানো সম্ভব হচ্ছে না").build();
        }
        Grievance grievance = this.grievanceService.findGrievanceById(grievanceForwardingNoteDTO.getGrievanceId());
        Long fromOfficeUnitOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();
        EmployeeOffice fromEmployeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(fromOfficeId, fromOfficeUnitOrganogramId, true);
        GrievanceForwarding grievanceForwarding = this.getGrievanceForwarding(
                grievance,
                grievanceForwardingNoteDTO.getNote(),
                "PERMISSION_ASKED",
                employeeOffice.getEmployeeRecord().getId(),
                fromEmployeeOffice.getEmployeeRecord().getId(),
                employeeOffice.getOffice().getId(),
                fromOfficeId,
                employeeOffice.getOfficeUnitOrganogram().getId(),
                fromOfficeUnitOrganogramId,
                null,
                false,
                false,
                false,
                null,
                grievance.getGrievanceCurrentStatus(),
                RoleType.HEAD_OF_OFFICE,
                userInformation.getUsername()
        );
        try {
            this.grievanceForwardingDAO.forwardGrievanceRemovingFromInbox(grievanceForwarding);
            grievance.setGrievanceCurrentStatus(GrievanceCurrentStatus.PERMISSION_ASKED);
            this.grievanceService.saveGrievance(grievance);
        } catch (Throwable t) {
            throw new RuntimeException("Internal error. Please contact with admin");
        }
        String successMessage = "অনুমতিপ্রাপ্তির জন্য কার্যালয়ের প্রধানের কাছে পাঠানো হয়েছে";
        return new GenericResponse(true, successMessage);
    }

    @Transactional(value = "transactionManager", rollbackFor = RuntimeException.class)
    public GenericResponse giveGuidanceToGiveService(Authentication authentication, GrievanceForwardingGuidanceForServiceDTO guidanceForServiceDTO) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Long fromOfficeId = userInformation.getOfficeInformation().getOfficeId();
        Long fromOfficeOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();
        Grievance grievance = this.grievanceService.findGrievanceById(guidanceForServiceDTO.getGrievanceId());
        EmployeeOffice fromEmployeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(fromOfficeId, fromOfficeOrganogramId, true);
        EmployeeOrganogram employeeOrganogram;
        employeeOrganogram = (grievance.getServiceOrigin() == null) ?
                this.getOfficeOrgDetail(guidanceForServiceDTO.getGuidanceReceiver()) : this.grievanceService.getServiceOfficer(guidanceForServiceDTO.getGrievanceId());
        Long toOfficeId = employeeOrganogram.getOfficeId();
        Long toOfficeOrganogramId = employeeOrganogram.getOfficeUnitOrganogramId();
        EmployeeOffice toEmployeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(toOfficeId, toOfficeOrganogramId, true);

        GrievanceForwarding grievanceForwarding = this.getGrievanceForwarding(
                grievance,
                guidanceForServiceDTO.getNote(),
                grievance.getGrievanceCurrentStatus().toString().contains("APPEAL") ? "APPEAL_GIVE_GUIDANCE" : "GIVE_GUIDANCE",
                toEmployeeOffice.getEmployeeRecord().getId(),
                fromEmployeeOffice.getEmployeeRecord().getId(),
                toOfficeId,
                fromOfficeId,
                toOfficeOrganogramId,
                fromOfficeOrganogramId,
                null,
                false,
                false,
                false,
                null,
                grievance.getGrievanceCurrentStatus(),
                RoleType.SERVICE_OFFICER,
                userInformation.getUsername()
        );
        try {
            this.grievanceForwardingDAO.forwardGrievanceKeepingAtInbox(grievanceForwarding);
        } catch (Throwable t) {
            throw new RuntimeException("Internal error. Please contact with admin");
        }
        if(grievance.getGrievanceCurrentStatus().toString().contains("IN_REVIEW")){
            grievance.setGrievanceCurrentStatus(
                grievance.getGrievanceCurrentStatus().toString().contains("APPEAL") ?
                        GrievanceCurrentStatus.APPEAL_GIVE_GUIDANCE_POST_INVESTIGATION : GrievanceCurrentStatus.GIVE_GUIDANCE_POST_INVESTIGATION
            );
        } else {
            grievance.setGrievanceCurrentStatus(
                    grievance.getGrievanceCurrentStatus().toString().contains("APPEAL") ?
                            GrievanceCurrentStatus.APPEAL_GIVE_GUIDANCE : GrievanceCurrentStatus.GIVE_GUIDANCE
            );
        }
        try {
            this.grievanceService.saveGrievance(grievance);
        } catch (Throwable t) {
            throw new RuntimeException("Internal error. Please contact with admin");
        }
        String successMessage = "সেবা প্রদানকারী কর্মকর্তাকে সেবা প্রদানের জন্য নির্দেশ দেয়া হয়েছে ";
        return new GenericResponse(true, successMessage);
    }

    @Transactional(value = "transactionManager", rollbackFor = RuntimeException.class)
    public GenericResponse giveRecommendDepartmentalAction(Authentication authentication, Long grievanceId, String departmentalActionNote) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Grievance grievance = this.grievanceService.findGrievanceById(grievanceId);
        EmployeeOffice employeeOffice = this.officeService.findEmployeeOfficeByOfficeAndIsOfficeHead(grievance.getOfficeId());
        if (employeeOffice == null) {
            String successMessage = "আপনার দপ্তরের দপ্তর প্রধান এর পদ বর্তমানে খালি আছে";
            return new GenericResponse(false, successMessage);
        }

        GrievanceForwarding grievanceForwarding = this.getGrievanceForwarding(
                grievance,
                departmentalActionNote,
                grievance.getGrievanceCurrentStatus().toString().contains("APPEAL") ? "RECOMMEND_DEPARTMENTAL_ACTION_APPEAL" : "RECOMMEND_DEPARTMENTAL_ACTION",
                employeeOffice.getEmployeeRecord().getId(),
                userInformation.getOfficeInformation().getEmployeeRecordId(),
                grievance.getOfficeId(),
                userInformation.getOfficeInformation().getOfficeId(),
                employeeOffice.getOfficeUnitOrganogram().getId(),
                userInformation.getOfficeInformation().getOfficeUnitOrganogramId(),
                null,
                true,
                false,
                false,
                null,
                grievance.getGrievanceCurrentStatus(),
                RoleType.HEAD_OF_OFFICE,
                userInformation.getUsername()
        );
        try {
            this.grievanceForwardingDAO.forwardGrievanceKeepingAtInbox(grievanceForwarding);
            this.notificationService.saveNotification(grievanceForwarding, "notification.departmental.action", "/viewGrievances.do?id=" + grievance.getId());
        } catch (Throwable t) {
            throw new RuntimeException("Internal error. Please contact with admin");
        }
        grievance.setGrievanceCurrentStatus(
                grievance.getGrievanceCurrentStatus().toString().contains("APPEAL") ?
                        GrievanceCurrentStatus.APPEAL_RECOMMMEND_DETARTMENTAL_ACTION : GrievanceCurrentStatus.RECOMMEND_DEPARTMENTAL_ACTION
        );
        try {
            this.grievanceService.saveGrievance(grievance);
        } catch (Throwable t) {
            throw new RuntimeException("Internal error. Please contact with admin");
        }
        String successMessage = "বিভাগীয় ব্যবস্থা গ্রহণের সুপারিশ সফলভাবে করা হয়েছে ";
        return new GenericResponse(true, successMessage);
    }

    public List<GrievanceForwardingEmployeeRecordsDTO> getGroHistory(Long grievanceId) {
        Grievance grievance = this.grievanceService.findGrievanceById(grievanceId);
        OfficesGRO officesGRO = this.officesGroService.findOfficesGroByOfficeId(grievance.getOfficeId());
        List<GrievanceForwarding> grievanceForwardings = this.grievanceForwardingDAO.getAllRelatedComplaintMovements(grievanceId,
                officesGRO.getOfficeId(),
                new ArrayList<Long>() {{
                    add(officesGRO.getGroOfficeUnitOrganogramId());
                }},
                "%APPEAL%");
        List<GrievanceForwardingEmployeeRecordsDTO> complaintMovements = grievanceForwardings.stream()
                .map(grievanceForwarding -> GrievanceForwardingEmployeeRecordsDTO.builder()
                        .toGroNameBangla(grievanceForwarding.getToEmployeeNameBangla())
                        .fromGroNameBangla(grievanceForwarding.getFromEmployeeNameBangla())
                        .toGroNameEnglish(grievanceForwarding.getToEmployeeNameEnglish())
                        .fromGroNameEnglish(grievanceForwarding.getFromEmployeeNameEnglish())
                        .comment(grievanceForwarding.getComment())
                        .action(grievanceForwarding.getAction())
                        .createdAtEng(DateTimeConverter.convertDateToStringForTimeline(grievanceForwarding.getCreatedAt()))
                        .createdAtBng(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToStringForTimeline(grievanceForwarding.getCreatedAt())))
                        .createdAtFullEng(DateTimeConverter.convertDateToStringForTimelineFull(grievanceForwarding.getCreatedAt()))
                        .createdAtFullBng(BanglaConverter.getDateBanglaFromEnglishFull(DateTimeConverter.convertDateToStringForTimelineFull(grievanceForwarding.getCreatedAt())))
                        .files(getFiles(grievanceForwarding))
                        .toDesignationNameBangla(grievanceForwarding.getToEmployeeDesignationBangla())
                        .fromDesignationNameBangla(grievanceForwarding.getFromEmployeeDesignationBangla())
                        .toOfficeNameBangla(grievanceForwarding.getToOfficeNameBangla())
                        .fromOfficeNameBangla(grievanceForwarding.getFromOfficeNameBangla())
                        .toOfficeUnitNameBangla(grievanceForwarding.getToEmployeeUnitNameBangla())
                        .fromOfficeUnitNameBangla(grievanceForwarding.getFromEmployeeUnitNameBangla())
                        .fromGroUsername(grievanceForwarding.getFromEmployeeUsername())
                        .isCC(grievanceForwarding.getIsCC())
                        .isCommitteeHead(grievanceForwarding.getIsCommitteeHead())
                        .isCommitteeMember(grievanceForwarding.getIsCommitteeMember())
                        .assignedRole(grievanceForwarding.getAssignedRole())
                        .build())
                .collect(Collectors.toList());
        return complaintMovements;
    }

    @Transactional(value = "transactionManager", rollbackFor = RuntimeException.class)
    public GenericResponse confirmReport(Authentication authentication, GrievanceForwardingInvestigationComment messageDTO) {
        Grievance grievance = this.grievanceService.findGrievanceById(messageDTO.getGrievanceId());
        GrievanceForwarding reportForwarding = this.grievanceForwardingDAO.getLastForwadingForGivenGrievanceAndAction(grievance, "%SUBMIT_INVESTIGATION_REPORT%");
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        Long fromOfficeId = userInformation.getOfficeInformation().getOfficeId();
        Long fromOfficeUnitOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();
        EmployeeOffice employeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(fromOfficeId, fromOfficeUnitOrganogramId, true);
        String report = reportForwarding.getComment();
        String picture = "<img src=\" " + messageDTO.getSignature().get(0).getUrl() + "\" alt=\"signature\" style=\"width:150px;height:80px;\"><br> <p>["
                + employeeOffice.getEmployeeRecord().getNameBangla()
                + ", " + employeeOffice.getDesignation()+ ", " + employeeOffice.getOfficeUnit().getUnitNameBangla() + ", "
                + userInformation.getOfficeInformation().getOfficeNameBangla() + "]</p></div>";
        if (messageDTO.getDecision().toUpperCase().contains("DISAGREED")) {
            String signature = "<div class='col-md-4'> <p>দ্বিমত প্রকাশ করেছেন </p> " + picture;
            report = report + signature;
        } else {
            String signature = "<div class='col-md-4'> <p>সহমত প্রকাশ করেছেন </p> " + picture;
            report = report + signature;
        }
        try {
            this.grievanceForwardingDAO.updateGrievanceForwardingRemovingFromInbox(
                    userInformation.getOfficeInformation().getOfficeId(),
                    userInformation.getOfficeInformation().getOfficeUnitOrganogramId(),
                    grievance,
                    reportForwarding
            );
        } catch (Throwable t) {
            throw new RuntimeException("Internal service error. Please contact with admin");
        }

        int count = this.grievanceForwardingDAO.countByIsCurrentAndGrievanceAndIsCommitteeMember(true, grievance, true);
        reportForwarding.setComment(report);
        messageDTO.getSignature().remove(0);
        List<FileDTO> files = messageDTO.getSignature();
        if (files != null && files.size() > 0) {
            this.attachedFileService.addMovementAttachedFiles(reportForwarding, files);
        }

        if (count == 0) {
            if (grievance.getGrievanceCurrentStatus().toString().contains("APPEAL")) {
                grievance.setGrievanceCurrentStatus(GrievanceCurrentStatus.APPEAL_IN_REVIEW);
            } else {
                grievance.setGrievanceCurrentStatus(GrievanceCurrentStatus.IN_REVIEW);
            }

            GrievanceForwarding existingToEntry = this.grievanceForwardingDAO.findByIsCurrentAndToOfficeAndToGROPostAndGrievance(true, reportForwarding.getToOfficeId(), reportForwarding.getToOfficeUnitOrganogramId(), reportForwarding.getGrievance());
            try {
                if (existingToEntry != null) {
                    existingToEntry.setIsCurrent(false);
                    this.grievanceForwardingDAO.save(existingToEntry);
                }
                this.grievanceService.saveGrievance(grievance);
            } catch (Throwable t) {
                throw new RuntimeException("Internal service error. Please contact with admin");
            }
            reportForwarding.setIsCurrent(true);
        }
        try {
            this.grievanceForwardingDAO.save(reportForwarding);
        } catch (Throwable t) {
            throw new RuntimeException("Internal service error. Please contact with admin");
        }
        return new GenericResponse(true, "সফলভাবে সহমত /দ্বিমত প্রকাশ করা হয়েছে");
    }

    public List<ExistingFileDerivedDTO> getAllFilesList(Long grievanceId, Authentication authentication) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        List<ExistingFileDerivedDTO> files = new ArrayList<>();
        List<GrievanceForwarding> grievanceForwardings = this.grievanceForwardingDAO.getAllRelatedComplaintMovements(grievanceId, userInformation.getOfficeInformation().getOfficeId(),
                new ArrayList<Long>() {{
                    add(userInformation.getOfficeInformation().getOfficeUnitOrganogramId());
                }}, "");
        for (GrievanceForwarding source : grievanceForwardings) {
            if (source.getAttachedFiles().size() > 0) {
                List<ExistingFileDerivedDTO> singleMovementFile = this.getFiles(source, 0);
                files.addAll(singleMovementFile);
            }
        }
        return files;
    }


    public ExistingFileDerivedDTO getDTOFromAttachedFIle(AttachedFile attachedFile) {
        StringBuilder stringBuilderFirst = StringUtil.replaceAll(
                new StringBuilder(attachedFile.getFilePath().substring(1)),
                "uploadedFiles", "api/file/upload");
        String link = StringUtil.replaceAll(stringBuilderFirst, "\\", "/").append("/").toString();
        return ExistingFileDerivedDTO.builder()
                .id(attachedFile.getId())
                .url(link)
                .name(attachedFile.getFileName())
                .build();
    }

    public List<GrievanceForwarding> findByGrievanceAndIsCurrent(Grievance grievance, Boolean isCurrent) {
        return this.grievanceForwardingDAO.findByGrievanceAndIsCurrent(grievance, isCurrent);
    }

    @Transactional(value = "transactionManager", rollbackFor = RuntimeException.class)
    public GenericResponse provideNudgeAgainstGrievance(GrievanceForwardingMessageDTO forwardingMessageDTO, Authentication authentication) {
        Grievance grievance = grievanceService.findGrievanceById(forwardingMessageDTO.getGrievanceId());
        List<GrievanceForwarding> currentForwardingList = findByGrievanceAndIsCurrent(grievance, true);
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        boolean success = true;
        for (GrievanceForwarding currentForwarding : currentForwardingList) {
            GrievanceCurrentStatus currentStatus = grievance.getGrievanceCurrentStatus();
            OfficeInformation officeInformation = userInformation.getOfficeInformation();
            GrievanceForwarding grievanceForwarding = getGrievanceForwarding(
                    grievance,
                    forwardingMessageDTO.getDecision(),
                    "NUDGE",
                    currentForwarding.getToEmployeeRecordId(),
                    officeInformation.getEmployeeRecordId(),
                    currentForwarding.getToOfficeId(),
                    officeInformation.getOfficeId(),
                    currentForwarding.getToOfficeUnitOrganogramId(),
                    officeInformation.getOfficeUnitOrganogramId(),
                    false,
                    false,
                    false,
                    false,
                    null,
                    currentStatus,
                    RoleType.GRO,
                    userInformation.getUsername()
            );
            try {
                grievanceForwarding = grievanceForwardingDAO.forwardGrievanceKeepingAtInbox(grievanceForwarding);
                if (grievanceForwarding == null) {
                    success = false;
                    break;
                }
            } catch (Throwable t) {
                throw new RuntimeException("Internal service error. Please contact with admin");
            }
            Tagid tagid = Tagid.builder()
                    .complaintId(grievance.getId())
                    .complaintOfficeId(grievance.getOfficeId())
                    .givingDate(new Date())
                    .officeId(userInformation.getOfficeInformation().getOfficeId())
                    .officeUnitOrganogramId(userInformation.getOfficeInformation().getOfficeUnitOrganogramId())
                    .note(forwardingMessageDTO.getDecision())
                    .officeName(userInformation.getOfficeInformation().getOfficeNameBangla())
                    .build();
            this.tagidDAO.save(tagid);
            this.notificationService.saveNotification(grievanceForwarding, "notification.nudge", "/viewGrievances.do?id=" + grievance.getId());
        }
        return GenericResponse.builder()
                .success(success)
                .message(success ? "তাগিদ প্রদান করা হয়েছে" : "দুঃখিত! তাগিদ প্রদান করা সম্ভব হচ্ছেনা")
                .build();
    }

    public void addEntryForCellMeetingStart(UserInformation userInformation, List<Grievance> grievances) {
        grievances.forEach(grievance -> {
            GrievanceForwarding grievanceForwarding = this.getGrievanceForwarding(grievance,
                    "অভিযোগ ব্যবস্থাপনা সেল মিটিং এর জন্য নির্বাচন করা হয়েছে ",
                    "CELL_NEW",
                    userInformation.getOfficeInformation().getEmployeeRecordId(),
                    userInformation.getOfficeInformation().getEmployeeRecordId(),
                    userInformation.getOfficeInformation().getOfficeId(),
                    userInformation.getOfficeInformation().getOfficeId(),
                    userInformation.getOfficeInformation().getOfficeUnitOrganogramId(),
                    userInformation.getOfficeInformation().getOfficeUnitOrganogramId(),
                    true,
                    false,
                    false,
                    false,
                    null,
                    GrievanceCurrentStatus.CELL_MEETING_PRESENTED,
                    RoleType.GRO,
                    userInformation.getUsername());
            this.grievanceForwardingDAO.forwardGrievanceRemovingFromInbox(grievanceForwarding);
        });
    }

    public void addEntryForCellMeetingClose(UserInformation userInformation, List<Grievance> grievances, CellMeetingCloseDTO cellMeetingCloseDTO) {
        grievances.forEach(grievance -> {
            GrievanceForwarding grievanceForwarding = this.getGrievanceForwarding(grievance,
                    cellMeetingCloseDTO.getNote(),
                    "CELL_MEETING_PRESENTED",
                    userInformation.getOfficeInformation().getEmployeeRecordId(),
                    userInformation.getOfficeInformation().getEmployeeRecordId(),
                    userInformation.getOfficeInformation().getOfficeId(),
                    userInformation.getOfficeInformation().getOfficeId(),
                    userInformation.getOfficeInformation().getOfficeUnitOrganogramId(),
                    userInformation.getOfficeInformation().getOfficeUnitOrganogramId(),
                    true,
                    false,
                    false,
                    false,
                    null,
                    GrievanceCurrentStatus.CELL_MEETING_ACCEPTED,
                    RoleType.GRO,
                    userInformation.getUsername()
            );

            this.grievanceForwardingDAO.forwardGrievanceRemovingFromInbox(grievanceForwarding);
            this.attachedFileService.addMovementAttachedFiles(grievanceForwarding, cellMeetingCloseDTO.getFiles());
        });
    }

    @EventListener("send-push-notification-on-grievance-forwarding")
    public void sendPushNotificationOnGrievanceForwarding(GrievanceForwarding grievanceForwarding) {
        Grievance grievance = grievanceForwarding.getGrievance();
        List<String> complainantPushNotificationActionList = new ArrayList(){{
            add("NEW");
            add("ACCEPTED");
            add("REJECTED");
            add("CLOSED_ACCUSATION_PROVED");
            add("CLOSED_ACCUSATION_INCORRECT");
        }};
        String action = grievanceForwarding.getAction();
        Map actionMap = MessageUtils.getNotificationMessagesByGrievanceForwardingAction(action, grievance.getId(), grievance.getTrackingNumber());
        String message = actionMap.get("toText").toString();
        String clickAction = actionMap.get("clickAction").toString();


        if (grievanceForwarding.getToEmployeeRecordId() != null) {
            fcmService.sendPushNotification(grievanceForwarding.getToEmployeeRecordId().toString(), message, clickAction);
        }

        if(!grievance.isAnonymous()) {
            ComplainantInfoDTO complainantInfo = grievanceService.getComplainantInfo(grievance);
            if (complainantPushNotificationActionList.contains(action)) {
                fcmService.sendPushNotification(complainantInfo.getMobileNumber(), message, clickAction);
            }
        }
    }

    @Transactional(value = "transactionManager", rollbackFor = RuntimeException.class)
    public Boolean retrieveRejectedComplaint(Long grievanceId) {
        Grievance grievance = this.grievanceService.findGrievanceById(grievanceId);
        Long officeId = grievance.getOfficeId();
        Long groOfficeUnitOrganogramId = this.officesGroService.findOfficesGroByOfficeId(officeId).getGroOfficeUnitOrganogramId();
        EmployeeOffice toEmployeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(officeId, groOfficeUnitOrganogramId, true);
        GrievanceForwarding grievanceForwarding = this.grievanceForwardingDAO.getLastForwadingForGivenGrievance(grievance);
        GrievanceForwarding revivalMovement = GrievanceForwarding.builder()
                .action("RETRIEVE")
                .assignedRole(RoleType.GRO)
                .attachedFiles(null)
                .comment("")
                .currentStatus(GrievanceCurrentStatus.NEW)
                .deadlineDate(null)
                .fromEmployeeDesignationBangla(grievanceForwarding.getFromEmployeeDesignationBangla())
                .fromEmployeeNameBangla(grievanceForwarding.getFromEmployeeNameBangla())
                .fromEmployeeNameEnglish(grievanceForwarding.getFromEmployeeNameEnglish())
                .fromEmployeeRecordId(grievanceForwarding.getFromEmployeeRecordId())
                .fromEmployeeUnitNameBangla(grievanceForwarding.getFromEmployeeUnitNameBangla())
                .fromEmployeeUsername(grievanceForwarding.getFromEmployeeUsername())
                .fromOfficeId(grievanceForwarding.getFromOfficeId())
                .fromOfficeNameBangla(grievanceForwarding.getFromOfficeNameBangla())
                .fromOfficeUnitId(grievanceForwarding.getFromOfficeUnitId())
                .fromOfficeUnitOrganogramId(grievanceForwarding.getFromOfficeUnitOrganogramId())
                .grievance(grievance)
                .isCC(false)
                .id(null)
                .isCommitteeHead(false)
                .isCommitteeMember(false)
                .isCurrent(true)
                .isSeen(false)
                .toEmployeeDesignationBangla(toEmployeeOffice.getDesignation())
                .toEmployeeNameBangla(toEmployeeOffice.getEmployeeRecord().getNameBangla())
                .toEmployeeNameEnglish(toEmployeeOffice.getEmployeeRecord().getNameEnglish())
                .toEmployeeRecordId(toEmployeeOffice.getEmployeeRecord().getId())
                .toEmployeeUnitNameBangla(toEmployeeOffice.getOfficeUnit()==null ? "" : toEmployeeOffice.getOfficeUnit().getUnitNameBangla())
                .toOfficeId(officeId)
                .toOfficeNameBangla(grievanceForwarding.getToOfficeNameBangla())
                .toOfficeUnitId(this.officeService.getOfficeUnitOrganogramById(groOfficeUnitOrganogramId).getOfficeUnitId())
                .toOfficeUnitOrganogramId(groOfficeUnitOrganogramId)
                .build();
        try {
            GrievanceForwarding ret = this.grievanceForwardingDAO.forwardGrievanceRemovingFromInbox(revivalMovement);
            if (ret != null) {
                grievance.setGrievanceCurrentStatus(GrievanceCurrentStatus.NEW);
                this.grievanceService.saveGrievance(grievance);
                return true;
            } else {
                return false;
            }
        } catch (Throwable t) {
            throw new RuntimeException("Internal service error. Please contact with admin");
        }
    }

    public List<GrievanceForwarding> getAllMovementsOfPreviousGRO(OfficesGRO officesGRO) {
        return this.grievanceForwardingDAO.getAllMovementsOfPreviousGRO(officesGRO);
    }

    public Boolean getComplaintRetakeFlag(Long complaintId, Authentication authentication) {
        Grievance grievance = this.grievanceService.findGrievanceById(complaintId);
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        OfficesGRO gro = this.officesGroService.findOfficesGroByOfficeId(grievance.getOfficeId());
        boolean flag = false;
        if(grievance.getGrievanceCurrentStatus().toString().contains("CLOSED") || grievance.getGrievanceCurrentStatus().toString().contains("REJECTED")
                || grievance.getGrievanceCurrentStatus().toString().contains("INV") || grievance.getGrievanceCurrentStatus().toString().contains("REVIEW")
                || grievance.getGrievanceCurrentStatus().toString().contains("CELL") ){
            return false;
        }
        if(gro.getGroOfficeId().equals(userInformation.getOfficeInformation().getOfficeId())
                && gro.getGroOfficeUnitOrganogramId().equals(userInformation.getOfficeInformation().getOfficeUnitOrganogramId())
                && grievance.getCurrentAppealOfficeId() == null){
            List<GrievanceForwarding> grievanceForwardings = this.grievanceForwardingDAO.findByGrievanceAndIsCurrent(grievance, true);
            for (GrievanceForwarding grievanceForwarding : grievanceForwardings) {
                if (grievanceForwarding.getFromOfficeId().equals(gro.getGroOfficeId())
                        && grievanceForwarding.getFromOfficeUnitOrganogramId().equals(gro.getGroOfficeUnitOrganogramId())
                        && grievanceForwarding.getDeadlineDate() != null
                        && grievanceForwarding.getDeadlineDate().before(new Date())
                        && !grievanceForwarding.getIsCC()) {
                    flag = true;
                    break;
                } else if (!grievanceForwarding.getIsSeen() && grievanceForwarding.getIsCurrent()
                        && grievanceForwarding.getFromOfficeId().equals(gro.getGroOfficeId())
                        && grievanceForwarding.getFromOfficeUnitOrganogramId().equals(gro.getGroOfficeUnitOrganogramId())
                        && !grievanceForwarding.getIsCC()
                        && !grievance.getGrievanceCurrentStatus().equals(GrievanceCurrentStatus.NEW)) {
                    flag = true;
                    break;
                }
            }
        } else if(grievance.getCurrentAppealOfficeId() != null){
            if(grievance.getCurrentAppealOfficeId().equals(userInformation.getOfficeInformation().getOfficeId())
                    && grievance.getCurrentAppealOfficerOfficeUnitOrganogramId().equals(userInformation.getOfficeInformation().getOfficeUnitOrganogramId())
                    ){
                List<GrievanceForwarding> grievanceForwardings = this.grievanceForwardingDAO.findByGrievanceAndIsCurrent(grievance, true);
                for (GrievanceForwarding grievanceForwarding : grievanceForwardings) {
                    if (grievanceForwarding.getFromOfficeId().equals(userInformation.getOfficeInformation().getOfficeId())
                            && grievanceForwarding.getFromOfficeUnitOrganogramId().equals(userInformation.getOfficeInformation().getOfficeUnitOrganogramId())
                            && grievanceForwarding.getDeadlineDate() != null
                            && grievanceForwarding.getDeadlineDate().before(new Date())) {
                        flag = true;
                        break;
                    }
                }
            }
        } else if(grievance.getGrievanceCurrentStatus().name().contains("FORWARDED")){
            GrievanceForwarding grievanceForwarding = this.grievanceForwardingDAO.getLastForwadingForGivenGrievance(grievance);
            if(!grievanceForwarding.getIsSeen() && grievanceForwarding.getIsCurrent() &&
                    grievanceForwarding.getAction().contains("FORWARD") &&
                    grievanceForwarding.getFromOfficeUnitOrganogramId().equals(userInformation.getOfficeInformation().getOfficeUnitOrganogramId()) &&
                    grievanceForwarding.getFromOfficeId().equals(userInformation.getOfficeInformation().getOfficeId())){
                flag = true;
            }
        }
        return flag;
    }

    @Transactional(value = "transactionManager", rollbackFor = RuntimeException.class)
    public Boolean retakeTimeExpiredComplaint(Long grievanceId, Authentication authentication) {
        Grievance grievance = this.grievanceService.findGrievanceById(grievanceId);
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Long officeId = userInformation.getOfficeInformation().getOfficeId();
        Long officeUnitOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();
        GrievanceForwarding grievanceForwarding = this.grievanceForwardingDAO.findByGrievanceAndIsCurrent(grievance, true).get(0);
        if(grievanceForwarding.getAction().contains("FORWARD")){
            grievance.setOtherService(grievance.getOtherServiceBeforeForward());
            grievance.setServiceOrigin(grievance.getServiceOriginBeforeForward());
            grievance.setOfficeId(officeId);
        }
        EmployeeOffice employeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(officeId, officeUnitOrganogramId, true);
        GrievanceForwarding movement = getGrievanceForwarding(
                grievance,
                "",
                "RETAKE",
                employeeOffice.getEmployeeRecord().getId(),
                grievanceForwarding.getToEmployeeRecordId(),
                officeId,
                grievanceForwarding.getToOfficeId(),
                officeUnitOrganogramId,
                grievanceForwarding.getToOfficeUnitOrganogramId(),
                null,
                false,
                false,
                false,
                null,
                grievanceForwarding.getCurrentStatus(),
                RoleType.GRO,
                userInformation.getUsername()
        );
        try {
            GrievanceForwarding ret = this.grievanceForwardingDAO.forwardGrievanceRemovingFromInbox(movement);
            if (ret != null) {
                grievance.setGrievanceCurrentStatus(grievanceForwarding.getCurrentStatus());
                this.grievanceService.saveGrievance(grievance);
                return true;
            } else {
                return false;
            }
        } catch (Throwable t) {
            throw new RuntimeException("Internal service error. Please contact with admin");
        }
    }

    public UnseenCountDTO getUnseenCountForUser(Authentication authentication, String inboxType) {
        boolean isAppealInbox = Objects.equals(inboxType, "appeal");
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Long officeId = userInformation.getOfficeInformation().getOfficeId();
        Long officeUnitOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();
        int inboxUnseenCount = this.grievanceForwardingDAO.getUnseenCountForUser(officeId, officeUnitOrganogramId, false).size();
        int ccUnseenCount = this.grievanceForwardingDAO.getUnseenCountForUser(officeId, officeUnitOrganogramId, true).size();
        Long expiredCount = this.dashboardDataDAO.countTimeExpiredComplaintsByOfficeId(officeId);
        int appealInboxCount = this.grievanceForwardingDAO.getUnseesAppealCount(officeId, officeUnitOrganogramId).intValue();
        return UnseenCountDTO.builder()
                .inboxCount(isAppealInbox ? appealInboxCount : inboxUnseenCount)
                .ccCount(isAppealInbox ? 0 : ccUnseenCount)
                .expiredCount(isAppealInbox ? 0 : expiredCount.intValue())
                .build();

    }

    public UnseenCountDTO getTotalCountForUser(Authentication authentication, String inboxType) {
        boolean isAppealInbox = Objects.equals(inboxType, "appeal");
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        ListViewConditionOnCurrentStatusGenerator viewConditionOnCurrentStatusGenerator = new ListViewConditionOnCurrentStatusGenerator();

        if (!isAppealInbox) {

            ListViewType listViewTypeInbox = viewConditionOnCurrentStatusGenerator.getNormalListTypeByString("inbox");
            ListViewType listViewTypeCc = viewConditionOnCurrentStatusGenerator.getNormalListTypeByString("cc");
            ListViewType listViewTypeExpired = viewConditionOnCurrentStatusGenerator.getNormalListTypeByString("expired");
            Integer grievanceDTOInboxCount = this.grievanceService.getInboxCount(userInformation, listViewTypeInbox).intValue();
            Integer grievanceDTOCcCount = this.grievanceService.getInboxCount(userInformation, listViewTypeCc).intValue();
            Integer grievanceDTOExpiredCount = this.grievanceService.getInboxCount(userInformation, listViewTypeExpired).intValue();

            return UnseenCountDTO.builder()
                    .inboxCount(grievanceDTOInboxCount)
                    .ccCount(grievanceDTOCcCount)
                    .expiredCount(grievanceDTOExpiredCount)
                    .build();

        }
        else {

            ListViewType listViewTypeAppealInbox = viewConditionOnCurrentStatusGenerator.getAppealListTypeByString("inbox");
            Integer grievanceDTOAppealInboxCount = this.grievanceService.getInboxCount(userInformation, listViewTypeAppealInbox).intValue();

            return UnseenCountDTO.builder()
                    .inboxCount(grievanceDTOAppealInboxCount)
                    .ccCount(0)
                    .expiredCount(0)
                    .build();

        }

    }
}
