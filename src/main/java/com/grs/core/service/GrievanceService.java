package com.grs.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.grs.api.exception.DuplicateEmailException;
import com.grs.api.model.*;
import com.grs.api.model.request.*;
import com.grs.api.model.response.*;
import com.grs.api.model.response.dashboard.NameValuePairDTO;
import com.grs.api.model.response.file.FileDerivedDTO;
import com.grs.api.model.response.grievance.*;
import com.grs.api.myGov.MyGovComplaintResponseDTO;
import com.grs.api.sso.GeneralInboxDataDTO;
import com.grs.api.sso.SSOPropertyReader;
import com.grs.core.dao.GeoDAO;
import com.grs.core.dao.GrievanceDAO;
import com.grs.core.dao.GrievanceForwardingDAO;
import com.grs.core.domain.GrievanceCurrentStatus;
import com.grs.core.domain.IdentificationType;
import com.grs.core.domain.MediumOfSubmission;
import com.grs.core.domain.ServiceType;
import com.grs.core.domain.grs.*;
import com.grs.core.domain.projapoti.*;
import com.grs.core.model.EmployeeOrganogram;
import com.grs.core.model.EmptyJsonResponse;
import com.grs.core.model.ListViewType;
import com.grs.core.repo.grs.BaseEntityManager;
import com.grs.core.repo.grs.GrievanceForwardingRepo;
import com.grs.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.reflections.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * Created by Acer on 9/14/2017.
 */
@Slf4j
@Service
public class GrievanceService {
    @Autowired
    private GrievanceDAO grievanceDAO;
    @Autowired
    private OfficeService officeService;
    @Autowired
    private OfficesGroService officesGroService;
    @Autowired
    private OfficeOrganogramService officeOrganogramService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private AttachedFileService attachedFileService;
    @Autowired
    private ComplainantService complainantService;
    @Autowired
    private GrievanceForwardingDAO grievanceForwardingDAO;
    @Autowired
    private CitizenCharterService citizenCharterService;
    @Autowired
    private ActionToRoleService actionToRoleService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ShortMessageService shortMessageService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private CellService cellService;
    @Autowired
    private GeneralSettingsService generalSettingsService;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GrievanceForwardingRepo grievanceForwardingRepo;
    @Autowired
    private Gson gson;

    @Autowired
    private StorageService storageService;

    @Autowired
    private BaseEntityManager entityManager;

    @Autowired
    private com.grs.core.repo.grs.SpProgrammeRepo spProgrammeRepo;

    @Autowired
    private GeoDAO geoDAO;

    @Autowired
    private SafetyNetProgramService safetyNetProgramService;

    public Grievance findGrievanceById(Long grievanceId) {
        return this.grievanceDAO.findOne(grievanceId);
    }

    public Long findCount(String sql, Map<String, Object> params) {
        return this.entityManager.findMaxId(sql, params);
    }


    public Grievance saveGrievance(Grievance grievance, boolean callHistory) {
        grievance = this.grievanceDAO.save(grievance);
        return grievance;
    }

    public Grievance saveGrievance(Grievance grievance) {
        return saveGrievance(grievance, true);
    }

    public void SaveGrievancesList(List<Grievance> grievances) {
        this.grievanceDAO.save(grievances);
    }

    public EmployeeRecord getEmployeeRecordById(Long id) {
        return this.officeService.findEmployeeRecordById(id);
    }

    public EmployeeRecordDTO getEmployeeRecord(Long id) {
        EmployeeRecord employeeRecord = this.getEmployeeRecordById(id);
        String designation = employeeRecord.getEmployeeOffices()
                .stream()
                .filter(employeeOffice -> employeeOffice.getStatus())
                .map(employeeOffice -> employeeOffice.getDesignation() + "," + employeeOffice.getOfficeUnit().getUnitNameBangla())
                .collect(Collectors.joining("\n"));

        return EmployeeRecordDTO.builder()
                .id(id.toString())
                .designation(designation)
                .email(employeeRecord.getPersonalEmail())
                .name(employeeRecord.getNameBangla())
                .phoneNumber(employeeRecord.getPersonalMobile())
                .build();
    }

    public Office getOfficeById(Long id) {
        return this.officeService.getOffice(id);
    }

    public List<GrievanceForwarding> getAllComplaintMovementByGrievance(Grievance grievance) {
        return this.grievanceForwardingDAO.getAllComplaintMovement(grievance);
    }

    public Page<RegisterDTO> getGrievanceByOfficeID(Pageable pageable, Authentication authentication, Long officeId) {
        if (officeId == null) {
            UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
            officeId = userInformation.getOfficeInformation().getOfficeId();
        }
        Page<Grievance> grievances = this.grievanceDAO.findByOfficeId(pageable, officeId);
        return grievances.map(this::convertToRegisterDTO);
    }

    public RegisterDTO convertToRegisterDTO(Grievance grievance) {
        Boolean isGRSuser = grievance.isGrsUser();
        String email = "", phoneNumber = "", name = "";
        if (isGRSuser == true) {
            Complainant complainant = this.complainantService.findOne(grievance.getComplainantId());
            if (complainant != null) {
                email = complainant.getEmail();
                phoneNumber = complainant.getPhoneNumber();
                name = complainant.getName();
            }
        } else {
            EmployeeRecord employeeRecord = this.officeService.findEmployeeRecordById(grievance.getComplainantId());
            email = employeeRecord.getPersonalEmail();
            phoneNumber = employeeRecord.getPersonalMobile();
            name = employeeRecord.getFatherNameBangla();
        }
        GrievanceForwarding grievanceForwarding = this.grievanceForwardingDAO.findRecentlyClosedOrRejectedOne(grievance.getId());
        Date closingOrRejectingDate = grievanceForwarding == null ? null : grievanceForwarding.getCreatedAt();
        ServiceOrigin serviceOrigin = grievance.getServiceOrigin();
        String serviceName = null;
        if (serviceOrigin != null) {
            serviceName = messageService.isCurrentLanguageInEnglish() ? serviceOrigin.getServiceNameEnglish() : serviceOrigin.getServiceNameBangla();
        } else {
            serviceName = grievance.getOtherService();
        }
        return RegisterDTO.builder()
                .id(grievance.getId())
                .dateBng(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(grievance.getCreatedAt())))
                .dateEng(DateTimeConverter.convertDateToString(grievance.getCreatedAt()))
                .subject(grievance.getSubject())
                .complainantEmail(email)
                .complainantMobile(phoneNumber)
                .complainantName(name)
                .service(serviceName)
                .serviceTypeEng(grievance.getGrievanceType().toString())
                .serviceTypeBng(BanglaConverter.convertServiceTypeToBangla(grievance.getGrievanceType()))
                .closingOrRejectingDateBng(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(closingOrRejectingDate)))
                .closingOrRejectingDateEng(DateTimeConverter.convertDateToString(closingOrRejectingDate))
                .rootCause(grievance.getGroIdentifiedCause() == null ? "" : grievance.getGroIdentifiedCause())
                .remedyMeasures(grievance.getGroDecision() == null ? "" : grievance.getGroDecision())
                .preventionMeasures(grievance.getGroSuggestion() == null ? "" : grievance.getGroSuggestion())
                .build();
    }

    public GrievanceDTO convertToGrievanceDTO(Grievance grievance) {
        if (grievance == null) {
            return GrievanceDTO.builder().build();
        }
        ServiceOriginDTO serviceOriginDTO;
        CitizenCharter citizenCharter;
        ServiceOrigin serviceOrigin = grievance.getServiceOrigin();

        String serviceNameEnglish = "", serviceNameBangla = "",
                serviceOfficerPostEnglish = null, serviceOfficerPostBangla = null,
                officeUnitNameEnglish = null, officeUnitNameBangla = null;

        if (serviceOrigin != null) {
            citizenCharter = citizenCharterService.findByOfficeAndService(grievance.getOfficeId(), serviceOrigin);
            if (citizenCharter != null) {
                OfficeUnitOrganogram officeUnitOrganogram = this.officeService.getOfficeUnitOrganogramById(citizenCharter.getSoOfficeUnitOrganogramId());
                OfficeUnit officeUnit = officeService.getOfficeUnitById(citizenCharter.getSoOfficeUnitId());

                serviceNameEnglish = citizenCharter.getServiceNameEnglish() == null ? "Empty Name Found" : citizenCharter.getServiceNameEnglish();
                serviceNameBangla = citizenCharter.getServiceNameBangla();
                serviceOfficerPostEnglish = officeUnitOrganogram == null ? "" : officeUnitOrganogram.getDesignationEnglish();
                serviceOfficerPostBangla = officeUnitOrganogram == null ? "" : officeUnitOrganogram.getDesignationBangla();
                officeUnitNameEnglish = officeUnit == null ? "" : officeUnit.getUnitNameEnglish();
                officeUnitNameBangla = officeUnit == null ? "" : officeUnit.getUnitNameBangla();
            }
            else {
                serviceNameEnglish = grievance.getOtherService();
                serviceNameBangla = grievance.getOtherService();
            }
        } else {
            serviceNameEnglish = grievance.getOtherService();
            serviceNameBangla = grievance.getOtherService();
        }

        List<GrievanceForwarding> grievanceForwardings = this.grievanceForwardingDAO.getAllComplaintMovement(grievance);
        Optional<Boolean> isInvestigated = grievanceForwardings.stream().map(grievanceForwarding -> {
            if (grievanceForwarding.getAction().contains("INVESTIGATION")){
                return true;
            } else {
                return false;
            }
        }).reduce((a, b) -> a || b);

        String complaintCategoryDetails = "", spProgrammeName = "", geoLocation = "",
                geoDivisionName = "", geoDistrictName = "", geoUpazilaName = "";

        if (grievance.getComplaintCategory() != null) {
            if (grievance.getComplaintCategory() == 2) {
                complaintCategoryDetails = "সামাজিক সুরক্ষা ভাতা";
                if (grievance.getSpProgrammeId() != null) {
                    SpProgramme spProgrammeObj = spProgrammeRepo.findById(grievance.getSpProgrammeId()).get();
                    complaintCategoryDetails += " (" + spProgrammeObj.getNameBn() + ")";
                    if (grievance.getGeoDivisionId() != null) {
                        geoDivisionName = geoDAO.getDivisionById(grievance.getGeoDivisionId()).getNameBangla();
                        if (grievance.getGeoDistrictId() != null) {
                            geoDistrictName = geoDAO.getDistrictById(grievance.getGeoDistrictId()).getNameBangla();
                            if (grievance.getGeoUpazilaId() != null) {
                                geoUpazilaName = geoDAO.getUpazilaById(grievance.getGeoUpazilaId()).getNameBangla();
                            }
                        }
                        geoLocation = geoDivisionName + ", " + geoDistrictName + ", " + geoUpazilaName;
                    }
                }
            } else {
                complaintCategoryDetails = "সাধারণ";
            }
        } else {
            complaintCategoryDetails = "সাধারণ";
        }

        return GrievanceDTO.builder()
                .id(String.valueOf(grievance.getId()))
                .dateEnglish(DateTimeConverter.convertDateToString(grievance.getCreatedAt()))
                .dateBangla(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(grievance.getCreatedAt())))
                .submissionDateEnglish(DateTimeConverter.convertDateToString(grievance.getSubmissionDate()))
                .submissionDateBangla(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(grievance.getSubmissionDate())))
                .subject(grievance.getSubject())
                .trackingNumberEnglish(grievance.getTrackingNumber())
                .trackingNumberBangla(BanglaConverter.convertToBanglaDigit(Long.parseLong(grievance.getTrackingNumber())))
                .typeEnglish(grievance.getGrievanceType().toString())
                .typeBangla(BanglaConverter.convertServiceTypeToBangla(grievance.getGrievanceType()))
                .statusBangla(BanglaConverter.convertGrievanceStatusToBangla(grievance.getGrievanceCurrentStatus()))
                .statusEnglish(grievance.getGrievanceCurrentStatus().toString())
                .caseNumberEnglish(grievance.getCaseNumber() == null ? "" : grievance.getCaseNumber())
                .caseNumberBangla(BanglaConverter.convertToBanglaDigit(Long.parseLong(grievance.getCaseNumber() == null ? "-1" : grievance.getCaseNumber())))
                .expectedDateOfClosingEnglish(isInvestigated.isPresent() ?
                        DateTimeConverter.makeExpectedDateOfClosing(grievance.getCreatedAt(), isInvestigated.get())
                        : DateTimeConverter.makeExpectedDateOfClosing(grievance.getCreatedAt(), false))
                .expectedDateOfClosingBangla(isInvestigated.isPresent() ?
                        BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.makeExpectedDateOfClosing(grievance.getCreatedAt(), isInvestigated.get()))
                        : BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.makeExpectedDateOfClosing(grievance.getCreatedAt(), false)))
                .serviceNameEnglish(serviceNameEnglish)
                .serviceNameBangla(serviceNameBangla)
                .serviceOfficerPostEnglish(serviceOfficerPostEnglish)
                .serviceOfficerPostBangla(serviceOfficerPostBangla)
                .officeUnitNameBangla(officeUnitNameBangla)
                .officeUnitNameEnglish(officeUnitNameEnglish)
                .safetyNet(grievance.isSafetyNet())
                .complaintCategoryDetails(complaintCategoryDetails)
                .complaintGeoLocation(geoLocation)
                .build();
    }

    public GrievanceDTO convertToGrievanceDTOForComplainantInfo(Grievance grievance) {

        return GrievanceDTO.builder()
                .id(String.valueOf(grievance.getId()))
                .dateEnglish(DateTimeConverter.convertDateToString(grievance.getCreatedAt()))
                .dateBangla(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(grievance.getCreatedAt())))
                .submissionDateEnglish(DateTimeConverter.convertDateToString(grievance.getSubmissionDate()))
                .submissionDateBangla(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(grievance.getSubmissionDate())))
                .subject(grievance.getSubject())
                .trackingNumberEnglish(grievance.getTrackingNumber())
                .trackingNumberBangla(BanglaConverter.convertToBanglaDigit(Long.parseLong(grievance.getTrackingNumber())))
                .typeEnglish(grievance.getGrievanceType().toString())
                .typeBangla(BanglaConverter.convertServiceTypeToBangla(grievance.getGrievanceType()))
                .statusBangla(BanglaConverter.convertGrievanceStatusToBangla(grievance.getGrievanceCurrentStatus()))
                .statusEnglish(grievance.getGrievanceCurrentStatus().toString())
                .caseNumberEnglish(grievance.getCaseNumber() == null ? "" : grievance.getCaseNumber())
                .caseNumberBangla(BanglaConverter.convertToBanglaDigit(Long.parseLong(grievance.getCaseNumber() == null ? "-1" : grievance.getCaseNumber())))
                .officeUnitNameBangla("")
                .officeUnitNameEnglish("")
                .build();
    }

    public GrievanceComplainantInfoDTO convertToGrievanceComplainantInfoDTOForListView(Grievance grievance) {
        GrievanceDTO grievanceDTO = convertToGrievanceDTOForComplainantInfo(grievance);
//        if (grievance.getComplainantId() != 0L) {
//            System.out.println("## mr");
//            System.out.println(grievance.getId());
//            System.out.println(grievance.getComplainantId());
//        }
        ComplainantDTO complainantDTO = null;
        if (grievance.getComplainantId() == 0L) {
            complainantDTO = ComplainantDTO.builder()
                    .name("অজ্ঞাতনামা")
                    .phoneNumber("প্রযোজ্য নয়")
                    .build();
        }
        else if (Objects.equals(grievance.getSourceOfGrievance(), UserType.OISF_USER.name())) {
            EmployeeRecord employeeRecord = employeeService.findOne(grievance.getComplainantId());
            complainantDTO = ComplainantDTO.builder()
                    .name(employeeRecord.getNameBangla())
                    .phoneNumber(employeeRecord.getPersonalMobile())
                    .build();
        }
        else complainantDTO = this.complainantService.getComplaintDTO(grievance.getComplainantId());
        Office office = this.officeService.getOffice(grievance.getOfficeId());
        return GrievanceComplainantInfoDTO.builder()
                .id(grievanceDTO.getId())
                .name(complainantDTO.getName())
                .mobileNumber(complainantDTO.getPhoneNumber())
                .dateEnglish(grievanceDTO.getDateEnglish())
                .dateBangla(grievanceDTO.getDateBangla())
                .submissionDateEnglish(grievanceDTO.getSubmissionDateEnglish())
                .submissionDateBangla(grievanceDTO.getSubmissionDateBangla())
                .subject(grievanceDTO.getSubject())
                .trackingNumberEnglish(grievanceDTO.getTrackingNumberEnglish())
                .trackingNumberBangla(grievanceDTO.getTrackingNumberBangla())
                .typeEnglish(grievanceDTO.getTypeEnglish())
                .typeBangla(grievanceDTO.getTypeBangla())
                .statusBangla(grievanceDTO.getStatusBangla())
                .statusEnglish(grievanceDTO.getStatusEnglish())
                .caseNumberEnglish(grievanceDTO.getCaseNumberEnglish())
                .caseNumberBangla(grievanceDTO.getCaseNumberBangla())
                .expectedDateOfClosingEnglish(grievanceDTO.getExpectedDateOfClosingEnglish())
                .expectedDateOfClosingBangla(grievanceDTO.getExpectedDateOfClosingBangla())
                .serviceNameEnglish(grievanceDTO.getServiceNameEnglish())
                .serviceNameBangla(grievanceDTO.getServiceNameBangla())
                .serviceOfficerPostEnglish(grievanceDTO.getServiceOfficerPostEnglish())
                .serviceOfficerPostBangla(grievanceDTO.getServiceOfficerPostBangla())
                .officeNameBangla(office != null ? office.getNameBangla() : "")
                .officeUnitNameBangla(grievanceDTO.getOfficeUnitNameBangla())
                .officeUnitNameEnglish(grievanceDTO.getOfficeUnitNameEnglish())
                .createdAt(DateTimeConverter.convertDateToString(grievance.getCreatedAt()))
                .build();
    }

    public GrievanceDTO convertToGrievanceDTOWithRatingAndFeedback(Grievance grievance) {
        GrievanceDTO grievanceDTO = convertToGrievanceDTO(grievance);
        grievanceDTO.setRating(grievance.getRating());
        grievanceDTO.setAppealRating(grievance.getAppealRating());
        grievanceDTO.setFeedbackComments(grievance.getFeedbackComments());
        grievanceDTO.setAppealFeedbackComments(grievance.getAppealFeedbackComments());
        return grievanceDTO;
    }

    public List<GrievanceDTO> getCurrentMonthComplaintsWithRatingsByOfficeIdAndType(Long officeId, Boolean isAppeal) {
        List<Long> grievanceIds = dashboardService.getComplaintIdsContainRatingInCurrentMonth(officeId, isAppeal);
        List<Grievance> grievanceList = grievanceDAO.findByIdIn(grievanceIds);
        return grievanceList.stream()
                .map(this::convertToGrievanceDTOWithRatingAndFeedback)
                .collect(Collectors.toList());
    }

    public GrievanceDetailsDTO getGrievanceDetailsWithMenuOptions(Authentication authentication, Long id) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Grievance grievance = this.grievanceDAO.findOne(id);
        String subType = null;
        if (grievance.isSafetyNet()) {
            String sql = "select * from safety_net_grievance where grievance_id=:grievanceId";
            Map<String, Object> params = new HashMap<>();
            params.put("grievanceId", id);

            SafetyNetGrievance safetyNetGrievance = entityManager.findSingleByQuery(sql, SafetyNetGrievance.class, params);
            if (safetyNetGrievance != null) {
                subType = Utils.isEmpty(safetyNetGrievance.getSubType()) ? "Undefined" : safetyNetGrievance.getSubType();
            }
        }
        GrievanceDetailsDTO grievanceDetailsDTO = Utils.isEmpty(subType) ? getGrievanceDetails(id) : getGrievanceDetails(id, subType);
        if(userInformation != null) {
            grievanceDetailsDTO.setUserType(userInformation.getUserType().name());
            grievanceDetailsDTO.setMenuOptions(getGrievanceDetailsMenu(userInformation, grievance));
        }
        return grievanceDetailsDTO;
    }

    public GrievanceDetailsDTO getGrievanceDetails(Long id) {
        return this.getGrievanceDetails(id, null);
    }
    public GrievanceDetailsDTO getGrievanceDetails(Long id, String subType) {
        Grievance grievance = this.grievanceDAO.findOne(id);
        GrievanceDTO grievanceDTO = convertToGrievanceDTO(grievance);
        grievanceDTO.setSubType(subType);
        Office office = this.officeService.getOffice(grievance.getOfficeId());
        ServiceOrigin serviceOrigin = grievance.getServiceOrigin();
        String soPost = null;
        CitizenCharter citizenCharter;
        ServiceOriginDTO serviceOriginDTO;

        if (serviceOrigin != null) {
            citizenCharter = citizenCharterService.findByOfficeAndService(grievance.getOfficeId(), serviceOrigin);
            if (citizenCharter != null) {
                EmployeeOrganogram serviceOfficer = this.getServiceOfficer(id);
                soPost = "post_" + serviceOfficer.getMinistryId() + "_" + serviceOfficer.getOfficeId() + "_" + serviceOfficer.getOfficeUnitOrganogramId();
                serviceOriginDTO = ServiceOriginDTO.builder()
                        .id(citizenCharter.getId())
                        .serviceNameBangla(citizenCharter.getServiceNameBangla())
                        .serviceNameEnglish(citizenCharter.getServiceNameEnglish())
                        .serviceProcedureBangla(citizenCharter.getServiceProcedureBangla())
                        .serviceProcedureEnglish(citizenCharter.getServiceProcedureEnglish())
                        .documentAndLocationBangla(citizenCharter.getDocumentAndLocationBangla())
                        .documentAndLocationEnglish(citizenCharter.getDocumentAndLocationEnglish())
                        .paymentMethodBangla(citizenCharter.getPaymentMethodBangla())
                        .paymentMethodEnglish(citizenCharter.getPaymentMethodEnglish())
                        .serviceTime(citizenCharter.getServiceTime())
                        .build();
            }
            else {
                serviceOriginDTO = ServiceOriginDTO.builder()
                        .serviceNameBangla(grievance.getOtherService())
                        .serviceNameEnglish(grievance.getOtherService())
                        .responsible(new ArrayList())
                        .build();
            }
        } else {
            serviceOriginDTO = ServiceOriginDTO.builder()
                    .serviceNameBangla(grievance.getOtherService())
                    .serviceNameEnglish(grievance.getOtherService())
                    .responsible(new ArrayList())
                    .build();
        }

        EmployeeOrganogram groOrganogram = this.getGRO(id);
        String groPOst = "post_" + groOrganogram.getMinistryId() + "_" + groOrganogram.getOfficeId() + "_" + groOrganogram.getOfficeUnitOrganogramId();

        return GrievanceDetailsDTO.builder()
                .details(grievance.getDetails())
                .service(serviceOriginDTO)
                .grievance(grievanceDTO)
                .officeNameBangla(office == null ? "অভিযোগ ব্যবস্থাপনা সেল" : office.getNameBangla())
                .officeNameEnglish(office == null ? "Cell" : office.getNameEnglish())
                .complainant(getComplainantInfo(grievance))
                .groPost(groPOst)
                .soPost(soPost)
                .build();
    }

    private GrievanceMenuOptionContainerDTO getGrievanceDetailsMenu(UserInformation userInformation, Grievance grievance) {
        if (grievance.getGrievanceCurrentStatus().equals(GrievanceCurrentStatus.REJECTED) ||
                grievance.getGrievanceCurrentStatus().toString().startsWith("CLOSED_")) {
            return null;
        }
        GrievanceForwarding grievanceForwarding = null;
        if (!(userInformation.getUserType().equals(UserType.COMPLAINANT))) {
            grievanceForwarding = this.grievanceForwardingDAO.getCurrentForwardingForGivenGrievanceAndUser(
                    grievance, userInformation.getOfficeInformation().getOfficeId(), userInformation.getOfficeInformation().getOfficeUnitOrganogramId()
            );
            if (grievanceForwarding == null) {
                return null;
            } else if (!grievanceForwarding.getToOfficeUnitOrganogramId().equals(userInformation.getOfficeInformation().getOfficeUnitOrganogramId())
                    || grievanceForwarding.getIsCC()) {
                return null;
            }
        } else if (userInformation.getUserId().equals(grievance.getComplainantId()) || userInformation.getOfficeInformation().getEmployeeRecordId().equals(grievance.getComplainantId())) {
            grievanceForwarding = this.grievanceForwardingDAO.getLatestComplainantMovement(grievance.getId());
            if (grievanceForwarding == null) {
                return null;
            }
        }
        return this.getDetailsMenu(grievance, grievanceForwarding);
    }

    public GrievanceMenuOptionContainerDTO getDetailsMenu(Grievance grievance, GrievanceForwarding grievanceForwarding) {
        GrievanceMenuOptionContainerDTO grievanceMenuOptionContainerDTO = null;
        GrievanceCurrentStatus currentStatus = grievance.getGrievanceCurrentStatus();
        GrievanceStatus grievanceStatus = this.actionToRoleService.findByName(currentStatus.name());
        GrsRole grsRole = this.actionToRoleService.getRolebyRoleName(grievanceForwarding.getAssignedRole().name());
        List<ActionToRole> actionToRoles = this.actionToRoleService.findByGrievanceStatusAndRoleType(grievanceStatus, grsRole);
        List<GrievanceMenuOptionDTO> archiveClose = new ArrayList<>();
        if (actionToRoles.size() != 0) {
            grievanceMenuOptionContainerDTO = GrievanceMenuOptionContainerDTO
                    .builder()
                    .grievanceMenus(new ArrayList<>())
                    .build();
            for (ActionToRole actionToRole : actionToRoles) {
                if (!officeService.hasChildOffice(grievanceForwarding.getToOfficeId()) &&
                        (actionToRole.getAction().getId() == 16L || actionToRole.getAction().getId() == 5L)) {
                    continue;
                }
                GrievanceMenuOptionDTO menuOptionDTO = buildMenuOptionDTO(actionToRole);
                if(menuOptionDTO.getNameEnglish() != null
                        && (menuOptionDTO.getNameEnglish().equalsIgnoreCase("Reject") || menuOptionDTO.getNameEnglish().equalsIgnoreCase("Close"))) {
                    archiveClose.add(menuOptionDTO);
                    continue;
                }
                grievanceMenuOptionContainerDTO.getGrievanceMenus().add(menuOptionDTO);
            }
        }
        if(archiveClose.size() >0) {
            grievanceMenuOptionContainerDTO.getGrievanceMenus().addAll(archiveClose);
        }

        return grievanceMenuOptionContainerDTO;
    }

    private GrievanceMenuOptionDTO buildMenuOptionDTO(ActionToRole actionToRole) {
        Action action = actionToRole.getAction();
        return GrievanceMenuOptionDTO.builder()
                .nameBangla(action.getActionBng())
                .nameEnglish(action.getActionEng())
                .link(action.getLink())
                .iconLink(action.getIconLink())
                .build();
    }

    public ComplainantInfoDTO getComplainantInfo(Grievance grievance) {
        ComplainantInfoDTO complainantInfoDTO;
        if (grievance.isGrsUser() || grievance.isAnonymous()) {
            complainantInfoDTO = this.complainantService.getComplainantInfo(grievance.getComplainantId());
        } else {
            EmployeeRecord userEmployeeRecord = this.officeService.findEmployeeRecordById(grievance.getComplainantId());
            complainantInfoDTO = userEmployeeRecord == null ? ComplainantInfoDTO.builder().build() : ComplainantInfoDTO.builder()
                    .name(userEmployeeRecord.getNameBangla())
                    .mobileNumber(userEmployeeRecord.getPersonalMobile() == null ? "" : BanglaConverter.convertToBanglaDigit(userEmployeeRecord.getPersonalMobile()))
                    .nationalId(userEmployeeRecord.getNationalId() == null ? "" : BanglaConverter.convertToBanglaDigit(userEmployeeRecord.getNationalId()))
                    .email(userEmployeeRecord.getPersonalEmail())
                    .presentAddress("")
                    .permanentAddress("")
                    .occupation("")
                    .dateOfBirth(userEmployeeRecord.getDateOfBirth() == null ? "" : BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(userEmployeeRecord.getDateOfBirth())))
                    .guardianName(userEmployeeRecord.getFatherNameBangla())
                    .motherName(userEmployeeRecord.getMotherNameBangla())
                    .build();
        }
        return complainantInfoDTO;
    }

    @Transactional(value = "transactionManager", rollbackFor = RuntimeException.class)
    public WeakHashMap<String, String> addGrievance(Authentication authentication, GrievanceRequestDTO grievanceRequestDTO) throws Exception {
        boolean isOisfUser = false;
        UserInformation userInformation;
        WeakHashMap<String, String> returnObject = new WeakHashMap<>();
        if (authentication == null) {
            userInformation = null;
        } else {
            userInformation = Utility.extractUserInformationFromAuthentication(authentication);
            if (checkIfHOOSubmitsOnSameOffice(userInformation, grievanceRequestDTO)) {
                String message = this.messageService.getMessage("error.message.same.office.official");
                returnObject.put("success", "false");
                returnObject.put("message", message);
                return returnObject;
            }
            isOisfUser = Utility.isUserAnOisfUser(authentication);
        }

        if (grievanceRequestDTO.getFiles() != null && grievanceRequestDTO.getFiles().size() >0) {
            if (!storageService.checkFileSize(grievanceRequestDTO.getFiles())) {
                returnObject.put("timestamp", String.valueOf(new Date().getTime()));
                returnObject.put("status", String.valueOf(700));
                returnObject.put("error", "Your uploaded files are too big.");
                returnObject.put("success", "false");
                returnObject.put("message", "Your uploaded files are too big.");
                return  returnObject;
            }
        }

        if(!StringUtil.isValidString(grievanceRequestDTO.getOfficeId())) {
            returnObject.put("timestamp", String.valueOf(new Date().getTime()));
            returnObject.put("status", String.valueOf(700));
            returnObject.put("error", "Missing required field: officeId");
            returnObject.put("success", "false");
            returnObject.put("message", "Missing required field: officeId");
            return  returnObject;
        }

        if(!StringUtil.isValidString(grievanceRequestDTO.getBody())) {
            returnObject.put("timestamp", String.valueOf(new Date().getTime()));
            returnObject.put("status", String.valueOf(700));
            returnObject.put("error", "Missing required field: body");
            returnObject.put("success", "false");
            returnObject.put("message", "Missing required field: body");
            return  returnObject;
        }

        if (!Utility.isNumber(grievanceRequestDTO.getOfficeId())) {
            returnObject.put("timestamp", String.valueOf(new Date().getTime()));
            returnObject.put("status", String.valueOf(600));
            returnObject.put("error", "Missing required field: officeId");
            returnObject.put("success", "false");
            returnObject.put("message", "Missing required field: officeId");
            return  returnObject;
        }

        if (!Utility.isNumber(grievanceRequestDTO.getPhoneNumber()) && !(grievanceRequestDTO.getIsAnonymous() != null && grievanceRequestDTO.getIsAnonymous()) && !isOisfUser) {
            returnObject.put("timestamp", String.valueOf(new Date().getTime()));
            returnObject.put("status", String.valueOf(600));
            returnObject.put("error", "Illegal format of number for: complainantPhoneNumber");
            returnObject.put("success", "false");
            returnObject.put("message", "Illegal format of number for: complainantPhoneNumber");
            return  returnObject;
        }

        if(StringUtil.isValidString(grievanceRequestDTO.getPhoneNumber())) {
            Complainant complainant = this.complainantService.findComplainantByPhoneNumber(grievanceRequestDTO.getPhoneNumber());
            if (complainant != null) {
                List<Long> blacklistInOfficeId  = complainantService.findBlacklistedOffices(complainant.getId());
                if (blacklistInOfficeId.contains(Long.parseLong(grievanceRequestDTO.getOfficeId()))) {
                    returnObject.put("timestamp", String.valueOf(new Date().getTime()));
                    returnObject.put("status", String.valueOf(800));
                    returnObject.put("error", "Sorry, this complainant cannot complain to this office!");
                    returnObject.put("success", "false");
                    returnObject.put("message", "Sorry, this complainant cannot complain to this office!");
                    return  returnObject;
                }
            }
        }



        String jsonString = gson.toJson(grievanceRequestDTO);

        GrievanceWithoutLoginRequestDTO grievanceWithoutLoginRequestDTO = gson.fromJson(jsonString, GrievanceWithoutLoginRequestDTO.class);
        grievanceWithoutLoginRequestDTO.setSubmittedThroughApi(0);

        if (!Utils.isEmpty(grievanceWithoutLoginRequestDTO.getComplainantPhoneNumber())) {
            grievanceRequestDTO.setServiceTrackingNumber(entityManager.getTrackingNumber(grievanceWithoutLoginRequestDTO.getComplainantPhoneNumber()));
        }

        Grievance grievance;
        try {
            grievance = this.grievanceDAO.addGrievance(userInformation, grievanceRequestDTO);
        }
        catch (Throwable ex) {
            throw new RuntimeException("Grievance processing error. Please contact with admin");
        }

        if (grievanceRequestDTO.getFiles() != null && grievanceRequestDTO.getFiles().size() > 0) {
            try {
                this.attachedFileService.addAttachedFiles(grievance, grievanceRequestDTO);
            } catch (Throwable t) {
                t.printStackTrace();
                throw new RuntimeException("Grievance file processing error. Please contact with admin");
            }
        }

        try {
            GrievanceForwarding grievanceForwarding = this.addNewHistory(grievance, userInformation);
            if (grievanceForwarding == null) {
                throw new RuntimeException(this.messageService.getMessageV2("gro.not.found"));
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw  new RuntimeException(this.messageService.getMessageV2("gro.not.found"));
        }
        returnObject.put("trackingNumber", grievance.getTrackingNumber());

        String header = "Grievance Submitted in GRS";
        String body = "আপনার অভিযোগটি গৃহীত হয়েছে। ট্র্যাকিং নম্বর " + grievance.getTrackingNumber();
        Long complainantId = grievance.getComplainantId();

        if (userInformation != null && userInformation.getUserType().equals(UserType.COMPLAINANT) && complainantId > 0L) {
            Complainant complainant = this.complainantService.findOne(complainantId);
            if (complainant.getEmail() != null) {
                emailService.sendEmail(complainant.getEmail(), header, body);
            }
            shortMessageService.sendSMS(complainant.getPhoneNumber(), body);
        }
        sendNotificationTOGRO(grievance);

        return returnObject;
    }

    @Transactional("transactionManager")
    public com.grs.api.model.response.grievance.SafetyNetGrievanceSummaryListDto getSafetyNetGrievanceSummary
            (SafetyNetGrievanceSummaryRequest request) {
        return grievanceDAO.getSafetyNetGrievanceSummary(request);
    }

    @Transactional(value = "transactionManager", rollbackFor = RuntimeException.class)
    public WeakHashMap<String, Object> addGrievanceWithoutLogin(Authentication authentication, GrievanceWithoutLoginRequestDTO grievanceWithoutLoginRequestDTO) {

        if (!(grievanceWithoutLoginRequestDTO.getIsAnonymous() != null && grievanceWithoutLoginRequestDTO.getIsAnonymous())) {
            grievanceWithoutLoginRequestDTO.setIsAnonymous(
                    !(StringUtil.isValidString(grievanceWithoutLoginRequestDTO.getComplainantPhoneNumber()) &&
                            StringUtil.isValidString(grievanceWithoutLoginRequestDTO.getName()))
            );
        }

        WeakHashMap<String, Object> returnObject = new WeakHashMap<>();

        if (grievanceWithoutLoginRequestDTO.getOfficeId() == null ||
                !com.grs.utils.StringUtil.isValidString(grievanceWithoutLoginRequestDTO.getOfficeId())) {

            if(!StringUtil.isValidString(grievanceWithoutLoginRequestDTO.getSpProgrammeId())) {
                returnObject.put("timestamp", String.valueOf(new Date().getTime()));
                returnObject.put("status", String.valueOf(700));
                returnObject.put("error", "Missing required field: program id");
                returnObject.put("success", "false");
                returnObject.put("message", "Missing required field: program id");
                return  returnObject;
            }

            if (!com.grs.utils.StringUtil.isValidString(grievanceWithoutLoginRequestDTO.getDivision())
                    || !com.grs.utils.StringUtil.isValidString(grievanceWithoutLoginRequestDTO.getDistrict())
                    || !com.grs.utils.StringUtil.isValidString(grievanceWithoutLoginRequestDTO.getUpazila())) {
                returnObject.put("timestamp", String.valueOf(new Date().getTime()));
                returnObject.put("status", String.valueOf(700));
                returnObject.put("error", "Provide all location info");
                returnObject.put("success", "false");
                returnObject.put("message", "Provide all location info");
                return  returnObject;
            }

            Optional<com.grs.core.domain.grs.SpProgramme> spProgramme = spProgrammeRepo.findById
                    (Integer.parseInt(grievanceWithoutLoginRequestDTO.getSpProgrammeId()));
            if (spProgramme != null && spProgramme.isPresent()) {
                if (spProgramme.get().getOfficeId() != null) {
                    System.out.println("Found office id: " + spProgramme.get().getOfficeId());
                    grievanceWithoutLoginRequestDTO.setOfficeId(String.valueOf(spProgramme.get().getOfficeId()));
                } else {
                    returnObject.put("timestamp", String.valueOf(new Date().getTime()));
                    returnObject.put("status", String.valueOf(700));
                    returnObject.put("error", "No office found against this program");
                    returnObject.put("success", "false");
                    returnObject.put("message", "No office found against this program");
                    return  returnObject;
                }
            } else {
                System.out.println("No program found");
                returnObject.put("timestamp", String.valueOf(new Date().getTime()));
                returnObject.put("status", String.valueOf(700));
                returnObject.put("error", "No programme found against this id");
                returnObject.put("success", "false");
                returnObject.put("message", "No programme found against this id");
                return  returnObject;
            }
        } else {
            grievanceWithoutLoginRequestDTO.setSpProgrammeId(null);
            grievanceWithoutLoginRequestDTO.setDivision(null);
            grievanceWithoutLoginRequestDTO.setDistrict(null);
            grievanceWithoutLoginRequestDTO.setUpazila(null);
        }

        String jsonString = gson.toJson(grievanceWithoutLoginRequestDTO);

        GrievanceRequestDTO grievanceRequestDTO = gson.fromJson(jsonString, GrievanceRequestDTO.class);
        grievanceRequestDTO.setPhoneNumber(grievanceWithoutLoginRequestDTO.getComplainantPhoneNumber());

        UserInformation userInformation = null;

        if (grievanceWithoutLoginRequestDTO.getFiles() != null && grievanceWithoutLoginRequestDTO.getFiles().size() >0) {
            if (!storageService.checkFileSize(grievanceWithoutLoginRequestDTO.getFiles())) {
                returnObject.put("timestamp", String.valueOf(new Date().getTime()));
                returnObject.put("status", String.valueOf(700));
                returnObject.put("error", "Your uploaded files are too large");
                returnObject.put("success", "false");
                returnObject.put("message", "Your uploaded files are too large");
                return  returnObject;
            }
        }

        if(!StringUtil.isValidString(grievanceRequestDTO.getOfficeId())) {
            returnObject.put("timestamp", String.valueOf(new Date().getTime()));
            returnObject.put("status", String.valueOf(700));
            returnObject.put("error", "Missing required field: officeId");
            returnObject.put("success", "false");
            returnObject.put("message", "Missing required field: officeId");
            return  returnObject;
        }

        if(!StringUtil.isValidString(grievanceRequestDTO.getBody())) {
            returnObject.put("timestamp", String.valueOf(new Date().getTime()));
            returnObject.put("status", String.valueOf(700));
            returnObject.put("error", "Missing required field: body");
            returnObject.put("success", "false");
            returnObject.put("message", "Missing required field: body");
            return  returnObject;
        }

        if (!Utility.isNumber(grievanceRequestDTO.getOfficeId())) {
            returnObject.put("timestamp", String.valueOf(new Date().getTime()));
            returnObject.put("status", String.valueOf(600));
            returnObject.put("error", "Missing required field: officeId");
            returnObject.put("success", "false");
            returnObject.put("message", "Missing required field: officeId");
            return  returnObject;
        }

        if (!Utility.isNumber(grievanceWithoutLoginRequestDTO.getComplainantPhoneNumber()) && !(grievanceRequestDTO.getIsAnonymous() != null && grievanceRequestDTO.getIsAnonymous())) {
            returnObject.put("timestamp", String.valueOf(new Date().getTime()));
            returnObject.put("status", String.valueOf(600));
            returnObject.put("error", "Illegal format of number for: complainantPhoneNumber");
            returnObject.put("success", "false");
            returnObject.put("message", "Illegal format of number for: complainantPhoneNumber");
            return  returnObject;
        }

        if(StringUtil.isValidString(grievanceWithoutLoginRequestDTO.getComplainantPhoneNumber())) {
            Complainant complainant = this.complainantService.findComplainantByPhoneNumber(grievanceWithoutLoginRequestDTO.getComplainantPhoneNumber());
            if (complainant != null) {
                List<Long> blacklistInOfficeId  = complainantService.findBlacklistedOffices(complainant.getId());
                if (blacklistInOfficeId.contains(Long.parseLong(grievanceRequestDTO.getOfficeId()))) {
                    returnObject.put("timestamp", String.valueOf(new Date().getTime()));
                    returnObject.put("status", String.valueOf(800));
                    returnObject.put("error", "Sorry, this complainant cannot complain to this office!");
                    returnObject.put("success", "false");
                    returnObject.put("message", "Sorry, this complainant cannot complain to this office!");
                    return  returnObject;
                }
            }
        }

        if (!(grievanceWithoutLoginRequestDTO.getIsAnonymous() != null && grievanceWithoutLoginRequestDTO.getIsAnonymous())) {
            Complainant currentComplainant = null;
            ComplainantDTO complainantDTO = new ComplainantDTO();
            complainantDTO.setName(grievanceWithoutLoginRequestDTO.getName());
            complainantDTO.setEmail(grievanceWithoutLoginRequestDTO.getEmail());
            complainantDTO.setPhoneNumber(grievanceWithoutLoginRequestDTO.getComplainantPhoneNumber());
            complainantDTO.setIdentificationType(IdentificationType.NID.name());
            complainantDTO.setIdentificationValue(null);

            try {
                currentComplainant = this.complainantService.insertComplainantWithoutLogin(complainantDTO);
            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof DuplicateEmailException) {
                    returnObject.put("timestamp", String.valueOf(new Date().getTime()));
                    returnObject.put("status", String.valueOf(209));
                    returnObject.put("error", "Sorry, account is already created with this email!");
                    returnObject.put("success", "false");
                    returnObject.put("message", "Sorry, account is already created with this email!");
                    return  returnObject;
                }
            }

            userInformation = generateUserInformationForComplainant(currentComplainant);
        }
        grievanceRequestDTO.setServiceTrackingNumber(entityManager.getTrackingNumber(grievanceRequestDTO.getPhoneNumber()));
        Grievance grievance;
        try {
            grievance = this.grievanceDAO.addGrievance(userInformation, grievanceRequestDTO);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Complain insertion error. Please contact with admin!");
//            if (ex.getMessage() != null && ex.getMessage().contains("Incorrect")) {
//                returnObject.put("timestamp", String.valueOf(new Date().getTime()));
//                returnObject.put("status", String.valueOf(600));
//                returnObject.put("error", ex.getMessage().substring(0, ex.getMessage().indexOf( " at ")));
//                returnObject.put("success", "false");
//                returnObject.put("message", "ভুল ফরমেট");
//                return  returnObject;
//            } else {
//                returnObject.put("timestamp", String.valueOf(new Date().getTime()));
//                returnObject.put("status", String.valueOf(600));
//                returnObject.put("error", "Internal service error. Contact with admin");
//                returnObject.put("success", "false");
//                returnObject.put("message", "Internal service error. Contact with admin");
//                return  returnObject;
//            }
        }
        log.info("===Going to Insert Attachment for Tracking:{} Grievance Id:{}", grievance.getTrackingNumber(), grievance.getId());
        if (grievanceRequestDTO.getFiles() != null && grievanceRequestDTO.getFiles().size() > 0) {
            try {
                this.attachedFileService.addAttachedFiles(grievance, grievanceRequestDTO);
            } catch (Throwable e) {
                e.printStackTrace();
                throw new RuntimeException("Attachment error. Contact with admin!");
            }
        }
        log.info("===Insert Attachment Done for Tracking:{} Grievance Id:{}", grievance.getTrackingNumber(), grievance.getId());
        log.info("===Going to add movement and history for Tracking:{} Grievance Id:{}", grievance.getTrackingNumber(), grievance.getId());
        try {
            GrievanceForwarding grievanceForwarding = this.addNewHistory(grievance, userInformation);
            if (grievanceForwarding == null) {
                throw new RuntimeException("Movement could not be inserted");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException("Movement could not be inserted");
        }
        log.info("===Done for movement and history for Tracking:{} Grievance Id:{}", grievance.getTrackingNumber(), grievance.getId());
        returnObject.put("trackingNumber", grievance.getTrackingNumber());
        returnObject.put("mappedOfficeId", grievance.getOfficeId());

        String header = "Grievance Submitted in GRS";
        String body = "আপনার অভিযোগটি গৃহীত হয়েছে। ট্র্যাকিং নম্বর " + grievanceRequestDTO.getServiceTrackingNumber();
        Long complainantId = grievance.getComplainantId();

        log.info("===Going to add Email and SMS for Tracking:{} Grievance Id:{}", grievance.getTrackingNumber(), grievance.getId());
        if (userInformation != null && userInformation.getUserType().equals(UserType.COMPLAINANT) && complainantId > 0L) {
            Complainant complainant = this.complainantService.findOne(complainantId);
            if (complainant.getEmail() != null) {
                emailService.sendEmail(complainant.getEmail(), header, body);
            }
            shortMessageService.sendSMS(complainant.getPhoneNumber(), body);
        }
        log.info("===Email and SMS Done for Tracking:{} Grievance Id:{}", grievance.getTrackingNumber(), grievance.getId());
        log.info("===Going to Send GRO Notification for Tracking:{} Grievance Id:{}", grievance.getTrackingNumber(), grievance.getId());
        sendNotificationTOGRO(grievance);
        log.info("===GRO Notification Done for Tracking:{} Grievance Id:{}", grievance.getTrackingNumber(), grievance.getId());
        return returnObject;
    }


    @Transactional(value = "transactionManager", rollbackFor = RuntimeException.class)
    public WeakHashMap<String, Object> addGrievanceForOthers(Authentication authentication, GrievanceWithoutLoginRequestDTO grievanceWithoutLoginRequestDTO) throws Exception {
        boolean isOisfUser = false;
        boolean isAnGRSUser = false;
        boolean isUserOthersComplainant = false;

        if (authentication == null) {
        } else {
            isOisfUser = Utility.isUserAnOisfUser(authentication);
            isAnGRSUser = Utility.isUserAnGRSUser(authentication);
            isUserOthersComplainant = Utility.isUserAnOthersComplainant(authentication);
        }

        if ((isAnGRSUser && !grievanceWithoutLoginRequestDTO.getServiceType().equals(ServiceType.NAGORIK)) || isOisfUser) {
            grievanceWithoutLoginRequestDTO.setIsSelfMotivated(true);
        }

        if (!isOisfUser) {
            grievanceWithoutLoginRequestDTO.setIsAnonymous(
                    (grievanceWithoutLoginRequestDTO.getIsAnonymous() != null &&
                            grievanceWithoutLoginRequestDTO.getIsAnonymous())
                            ||
                            (!(
                                    StringUtil.isValidString(grievanceWithoutLoginRequestDTO.getPhoneNumber()) &&
                                            StringUtil.isValidString(grievanceWithoutLoginRequestDTO.getName())
                            ) && authentication == null)
            );
        }
        else {
            grievanceWithoutLoginRequestDTO.setIsAnonymous(grievanceWithoutLoginRequestDTO.getIsAnonymous() != null &&
                    grievanceWithoutLoginRequestDTO.getIsAnonymous());
        }

        WeakHashMap<String, Object> returnObject = new WeakHashMap<>();

        if (grievanceWithoutLoginRequestDTO.getOfficeId() == null ||
                !com.grs.utils.StringUtil.isValidString(grievanceWithoutLoginRequestDTO.getOfficeId())) {

            if(!StringUtil.isValidString(grievanceWithoutLoginRequestDTO.getSpProgrammeId())) {
                returnObject.put("timestamp", String.valueOf(new Date().getTime()));
                returnObject.put("status", String.valueOf(700));
                returnObject.put("error", "Missing required field: program id");
                returnObject.put("success", "false");
                returnObject.put("message", "Missing required field: program id");
                return  returnObject;
            }

            if (!com.grs.utils.StringUtil.isValidString(grievanceWithoutLoginRequestDTO.getDivision())
                    || !com.grs.utils.StringUtil.isValidString(grievanceWithoutLoginRequestDTO.getDistrict())
                    || !com.grs.utils.StringUtil.isValidString(grievanceWithoutLoginRequestDTO.getUpazila())) {
                returnObject.put("timestamp", String.valueOf(new Date().getTime()));
                returnObject.put("status", String.valueOf(700));
                returnObject.put("error", "Provide all location info");
                returnObject.put("success", "false");
                returnObject.put("message", "Provide all location info");
                return  returnObject;
            }

            Optional<com.grs.core.domain.grs.SpProgramme> spProgramme = spProgrammeRepo.findById
                    (Integer.parseInt(grievanceWithoutLoginRequestDTO.getSpProgrammeId()));
            if (spProgramme != null && spProgramme.isPresent()) {
                if (spProgramme.get().getOfficeId() != null) {
                    System.out.println("Found office id: " + spProgramme.get().getOfficeId());
                    grievanceWithoutLoginRequestDTO.setOfficeId(String.valueOf(spProgramme.get().getOfficeId()));
                } else {
                    returnObject.put("timestamp", String.valueOf(new Date().getTime()));
                    returnObject.put("status", String.valueOf(700));
                    returnObject.put("error", "No office found against this program");
                    returnObject.put("success", "false");
                    returnObject.put("message", "No office found against this program");
                    return  returnObject;
                }
            } else {
                System.out.println("No program found");
                returnObject.put("timestamp", String.valueOf(new Date().getTime()));
                returnObject.put("status", String.valueOf(700));
                returnObject.put("error", "No programme found against this id");
                returnObject.put("success", "false");
                returnObject.put("message", "No programme found against this id");
                return  returnObject;
            }
        } else {
            grievanceWithoutLoginRequestDTO.setSpProgrammeId(null);
            grievanceWithoutLoginRequestDTO.setDivision(null);
            grievanceWithoutLoginRequestDTO.setDistrict(null);
            grievanceWithoutLoginRequestDTO.setUpazila(null);
        }

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        if (!isOisfUser && (grievanceWithoutLoginRequestDTO.getIsAnonymous() == null || !grievanceWithoutLoginRequestDTO.getIsAnonymous()) && userInformation != null) {
            Complainant currentComplainant = this.complainantService.findOne(userInformation.getUserId());
            grievanceWithoutLoginRequestDTO.setPhoneNumber(currentComplainant.getPhoneNumber());
            grievanceWithoutLoginRequestDTO.setComplainantPhoneNumber(currentComplainant.getPhoneNumber());
            grievanceWithoutLoginRequestDTO.setName(currentComplainant.getName());
            grievanceWithoutLoginRequestDTO.setEmail(currentComplainant.getEmail());
        }

        String jsonString = gson.toJson(grievanceWithoutLoginRequestDTO);

        GrievanceRequestDTO grievanceRequestDTO = gson.fromJson(jsonString, GrievanceRequestDTO.class);
        grievanceRequestDTO.setPhoneNumber(grievanceWithoutLoginRequestDTO.getComplainantPhoneNumber());

        String sourceOfGrievance = isOisfUser ? UserType.OISF_USER.name() : (isAnGRSUser ? UserType.COMPLAINANT.name() : (isUserOthersComplainant ? GRSUserType.OTHERS_COMPLAINANT.name() : null));

        long userIdFromToken = userInformation != null ? userInformation.getUserId() : 0;

        if(!StringUtil.isValidString(grievanceRequestDTO.getOfficeId())) {
            returnObject.put("timestamp", String.valueOf(new Date().getTime()));
            returnObject.put("status", String.valueOf(700));
            returnObject.put("error", "Missing required field: officeId");
            returnObject.put("success", "false");
            returnObject.put("message", "Missing required field: officeId");
            return  returnObject;
        }

        if(!StringUtil.isValidString(grievanceRequestDTO.getBody())) {
            returnObject.put("timestamp", String.valueOf(new Date().getTime()));
            returnObject.put("status", String.valueOf(700));
            returnObject.put("error", "Missing required field: body");
            returnObject.put("success", "false");
            returnObject.put("message", "Missing required field: body");
            return  returnObject;
        }

        if (!Utility.isNumber(grievanceRequestDTO.getOfficeId())) {
            returnObject.put("timestamp", String.valueOf(new Date().getTime()));
            returnObject.put("status", String.valueOf(600));
            returnObject.put("error", "Missing required field: officeId");
            returnObject.put("success", "false");
            returnObject.put("message", "Missing required field: officeId");
            return  returnObject;
        }

        if (!isOisfUser && !Utility.isNumber(grievanceWithoutLoginRequestDTO.getComplainantPhoneNumber()) && !(grievanceRequestDTO.getIsAnonymous() != null && grievanceRequestDTO.getIsAnonymous())) {
            returnObject.put("timestamp", String.valueOf(new Date().getTime()));
            returnObject.put("status", String.valueOf(600));
            returnObject.put("error", "Illegal format of number for: complainantPhoneNumber");
            returnObject.put("success", "false");
            returnObject.put("message", "Illegal format of number for: complainantPhoneNumber");
            return  returnObject;
        }

        if(StringUtil.isValidString(grievanceWithoutLoginRequestDTO.getComplainantPhoneNumber())) {
            Complainant complainant = this.complainantService.findComplainantByPhoneNumber(grievanceWithoutLoginRequestDTO.getComplainantPhoneNumber());
            if (complainant != null) {
                List<Long> blacklistInOfficeId  = complainantService.findBlacklistedOffices(complainant.getId());
                if (blacklistInOfficeId.contains(Long.parseLong(grievanceRequestDTO.getOfficeId()))) {
                    returnObject.put("timestamp", String.valueOf(new Date().getTime()));
                    returnObject.put("status", String.valueOf(800));
                    returnObject.put("error", "Sorry, this complainant cannot complain to this office!");
                    returnObject.put("success", "false");
                    returnObject.put("message", "Sorry, this complainant cannot complain to this office!");
                    return  returnObject;
                }
            }
        }

        if (!(grievanceWithoutLoginRequestDTO.getIsAnonymous() != null && grievanceWithoutLoginRequestDTO.getIsAnonymous()) && !isOisfUser) {
            Complainant currentComplainant = null;
            ComplainantDTO complainantDTO = new ComplainantDTO();
            complainantDTO.setName(grievanceWithoutLoginRequestDTO.getName());
            complainantDTO.setEmail(grievanceWithoutLoginRequestDTO.getEmail());
            complainantDTO.setPhoneNumber(grievanceWithoutLoginRequestDTO.getComplainantPhoneNumber());
            complainantDTO.setIdentificationType(IdentificationType.NID.name());
            complainantDTO.setIdentificationValue(null);

            try {
                currentComplainant = this.complainantService.insertComplainantWithoutLogin(complainantDTO);
            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof DuplicateEmailException) {
                    returnObject.put("timestamp", String.valueOf(new Date().getTime()));
                    returnObject.put("status", String.valueOf(209));
                    returnObject.put("error", "Sorry, account is already created with this email!");
                    returnObject.put("success", "false");
                    returnObject.put("message", "Sorry, account is already created with this email!");
                    return  returnObject;
                }
            }

            userInformation = generateUserInformationForComplainant(currentComplainant);
        } else if(isOisfUser) {
            userInformation = generateUserInformationForOisfUser(userInformation);
        } else {
            userInformation = null;
        }

        grievanceRequestDTO.setServiceTrackingNumber(entityManager.getTrackingNumber(grievanceRequestDTO.getPhoneNumber()));
        Grievance grievance;
        try {
            grievance = this.grievanceDAO.addGrievanceForOthers(userInformation, grievanceRequestDTO, userIdFromToken, sourceOfGrievance);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Grievance movement error. Please contact with admin!");
        }

        if (grievanceRequestDTO.getFiles() != null && grievanceRequestDTO.getFiles().size() > 0) {
            try {
                this.attachedFileService.addAttachedFiles(grievance, grievanceRequestDTO);
            } catch (Throwable t) {
                t.printStackTrace();
                throw new RuntimeException("Grievance File storage error. Please contact with admin!");
            }
        }
        try {
            GrievanceForwarding grievanceForwarding = this.addNewHistory(grievance, userInformation);
            if (grievanceForwarding == null) {
                throw new RuntimeException("Grievance movement error. Please contact with admin!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Grievance movement error. Please contact with admin!");
        }


        returnObject.put("trackingNumber", grievance.getTrackingNumber());
        returnObject.put("mappedOfficeId", grievance.getOfficeId());

        String trackingNumber = (StringUtil.isValidString(grievanceRequestDTO.getServiceTrackingNumber())
                && (grievanceRequestDTO.getServiceTrackingNumber().startsWith("01"))) ?
                grievanceRequestDTO.getServiceTrackingNumber().substring(11) :
                grievanceRequestDTO.getServiceTrackingNumber();
        String header = "Grievance Submitted in GRS";
        String body = "আপনার অভিযোগটি গৃহীত হয়েছে। ট্র্যাকিং নম্বর " + trackingNumber;
        Long complainantId = grievance.getComplainantId();

        if (userInformation != null && userInformation.getUserType().equals(UserType.COMPLAINANT) && complainantId > 0L) {
            Complainant complainant = this.complainantService.findOne(complainantId);
            if (complainant.getEmail() != null) {
                emailService.sendEmail(complainant.getEmail(), header, body);
            }
            shortMessageService.sendSMS(complainant.getPhoneNumber(), body);
        }
        sendNotificationTOGRO(grievance);
        return returnObject;
    }

    @Transactional("transactionManager")
    public WeakHashMap<String, Object> changeSafetyNetCategory(Authentication authentication, ChangeSafetyNetCategoryDTO request) throws Exception {
        WeakHashMap<String, Object> returnObject = new WeakHashMap<>();
        if (Utils.isEmpty(request.getGrievanceId())) {
            returnObject.put("success", false);
            returnObject.put("message", "Grievance ID is required");
            return returnObject;
        }

        if (Utils.isEmpty(request.getSubCategory())) {
            returnObject.put("success", false);
            returnObject.put("message", "SafetyNet Sub category is required");
            return returnObject;
        }

        Grievance grievance = grievanceDAO.findOne(Long.parseLong(request.getGrievanceId()));
        if (grievance == null) {
            returnObject.put("success", false);
            returnObject.put("message", "Invalid grievance id!");
            return returnObject;
        }

        String subCategory;
        if (request.getSubCategory().equalsIgnoreCase("1")) {
            subCategory = "Exclusion Error";
        } else if (request.getSubCategory().equalsIgnoreCase("2")) {
            subCategory = "Inclusion Error";
        } else if (request.getSubCategory().equalsIgnoreCase("3")) {
            subCategory = "Money not Received";
        } else {
            returnObject.put("success", false);
            returnObject.put("message", "Invalid Safetynet sub category!");
            return returnObject;
        }

        String updateSQL = "update safety_net_grievance set sub_type=:subType where grievance_id=:grievanceId ";
        Map<String, Object> params = new HashMap<>();
        params.put("subType", subCategory);
        params.put("grievanceId", Long.parseLong(request.getGrievanceId()));

        try {
            int val = entityManager.updateByQuery(updateSQL, params);
            if (val >0) {
                returnObject.put("success", true);
                returnObject.put("message", "Sub category has been changed successfully");
                return returnObject;
            }
        } catch (Throwable t) {
            t.printStackTrace();
            returnObject.put("success", false);
            returnObject.put("message", "Internal service error. Please contact with admin!");
            return returnObject;
        }

        returnObject.put("success", false);
        returnObject.put("message", "Internal service error. Please contact with admin!");
        return returnObject;
    }
    public UserInformation generateUserInformationForComplainant(Complainant complainant) {

        UserInformation userInformation = new UserInformation();
        if (complainant != null) {
            userInformation.setUserId(complainant.getId());
            userInformation.setUsername(complainant.getName());
        }
        userInformation.setUserType(UserType.COMPLAINANT);
        userInformation.setIsAppealOfficer(false);
        userInformation.setIsOfficeAdmin(false);
        userInformation.setIsCentralDashboardUser(false);
        userInformation.setIsCellGRO(false);
        userInformation.setIsMobileLogin(false);

        return userInformation;

    }

    public UserInformation generateUserInformationForOisfUser(UserInformation userInformationFromToken) {

        UserInformation userInformation = new UserInformation();
        if (userInformationFromToken != null) {
            userInformation.setUserId(userInformationFromToken.getUserId());
            userInformation.setUsername(userInformationFromToken.getUsername());
            userInformation.setOfficeInformation(userInformationFromToken.getOfficeInformation());
        }
        userInformation.setUserType(UserType.OISF_USER);
        userInformation.setIsAppealOfficer(false);
        userInformation.setIsOfficeAdmin(false);
        userInformation.setIsCentralDashboardUser(false);
        userInformation.setIsCellGRO(false);
        userInformation.setIsMobileLogin(false);

        return userInformation;

    }

    public void sendNotificationTOGRO(Grievance grievance){
        Long officeId = grievance.getOfficeId();
        OfficesGRO officesGRO = officesGroService.findOfficesGroByOfficeId(officeId);
        Long officeUnitOrganogram = officesGRO.getGroOfficeUnitOrganogramId();
        EmployeeOffice employeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(officeId, officeUnitOrganogram, true);
        String groEmail = employeeOffice != null ? employeeOffice.getEmployeeRecord().getPersonalEmail() : "";
        String groMobile = employeeOffice != null ? employeeOffice.getEmployeeRecord().getPersonalMobile() : "";
        String trackingNumber = (StringUtil.isValidString(grievance.getTrackingNumber())
                && (grievance.getTrackingNumber().startsWith("01"))) ?
                grievance.getTrackingNumber().substring(11) :
                grievance.getTrackingNumber();
        String header = "Grievance Submitted in GRS";
        String body = "A new Grievance is submitted with tracking number:  " + trackingNumber;
        if(employeeOffice != null){
            emailService.sendEmail(groEmail, header, body);
            shortMessageService.sendSMS(groMobile, body);
        }
    }

    public GrievanceForwarding addNewHistory(Grievance grievance, UserInformation userInformation) {
        Long officeId = grievance.getOfficeId();
        OfficesGRO officesGRO = this.officesGroService.findOfficesGroByOfficeId(officeId);
        if (officesGRO == null) {
            return null;
        }
        Long groOrganogramId = officesGRO.getGroOfficeUnitOrganogramId();
        OfficeUnitOrganogram toOfficeUnitOrganogram;
        OfficeUnit toOfficeUnit;
        EmployeeRecord toEmployeeRecord;
        OfficeInformationFullDetails toInfo, fromInfo;

        if (officeId == 0L) {
            CellMember cellMember = this.cellService.getCellMemberEntry(groOrganogramId);
            toEmployeeRecord = this.getEmployeeRecordById(cellMember.getEmployeeRecordId());
            if (toEmployeeRecord == null) {
                return null;
            }

            String cellDesignation = cellMember.getIsAo() ? "সভাপতি" : (cellMember.getIsGro() ? "সদস্য সচিব" : "সদস্য");

            toInfo = OfficeInformationFullDetails.builder()
                    .officeId(grievance.getOfficeId())
                    .officeUnitId(0L)
                    .officeUnitOrganogramId(officesGRO.getGroOfficeUnitOrganogramId())
                    .employeeRecordId(toEmployeeRecord.getId())
                    .employeeDesignation(cellDesignation)
                    .employeeNameBangla(toEmployeeRecord.getNameBangla())
                    .employeeNameEnglish(toEmployeeRecord.getNameEnglish())
                    .officeNameBangla("অভিযোগ ব্যবস্থাপনা সেল")
                    .officeUnitNameBangla("")
                    .build();
        } else {
            toOfficeUnitOrganogram = this.officeService.getOfficeUnitOrganogramById(groOrganogramId);
            if (toOfficeUnitOrganogram == null) {
                return null;
            }
            toOfficeUnit = toOfficeUnitOrganogram.getOfficeUnit();

            EmployeeOffice employeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(
                    officeId,
                    groOrganogramId,
                    true
            );
            if (employeeOffice == null) {
                return null;
            }
            toEmployeeRecord = employeeOffice.getEmployeeRecord();

            toInfo = OfficeInformationFullDetails.builder()
                    .officeId(grievance.getOfficeId())
                    .officeUnitId(toOfficeUnit != null ? toOfficeUnit.getId() : null)
                    .officeUnitOrganogramId(officesGRO.getGroOfficeUnitOrganogramId())
                    .employeeRecordId(toEmployeeRecord.getId())
                    .employeeDesignation(employeeOffice.getDesignation())
                    .employeeNameBangla(toEmployeeRecord.getNameBangla())
                    .employeeNameEnglish(toEmployeeRecord.getNameEnglish())
                    .officeNameBangla(employeeOffice.getOffice().getNameBangla())
                    .officeUnitNameBangla(toOfficeUnit != null ? toOfficeUnit.getUnitNameBangla() : null)
                    .build();
        }

        boolean oisfSource = grievance.getSourceOfGrievance() != null && grievance.getSourceOfGrievance().equals(UserType.OISF_USER.name());

        if (userInformation != null && !grievance.isGrsUser() && !grievance.isAnonymous() && !oisfSource) {
            OfficeUnitOrganogram fromOfficeUnitOrganogram = this.officeOrganogramService.findOfficeUnitOrganogramById(userInformation.getOfficeInformation().getOfficeUnitOrganogramId());
            Long fromOfficeId = userInformation.getOfficeInformation().getOfficeId();
            OfficeUnit fromOfficeUnit = fromOfficeUnitOrganogram.getOfficeUnit();
            EmployeeRecord fromEmployeeRecord = this.officeService
                    .findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(fromOfficeId, userInformation.getOfficeInformation().getOfficeUnitOrganogramId(), true).getEmployeeRecord();

            fromInfo = OfficeInformationFullDetails.builder()
                    .officeId(userInformation.getOfficeInformation().getOfficeId())
                    .officeUnitId(fromOfficeUnit != null ? fromOfficeUnit.getId() : null)
                    .officeUnitOrganogramId(userInformation.getOfficeInformation().getOfficeUnitOrganogramId())
                    .employeeRecordId(userInformation.getOfficeInformation().getEmployeeRecordId())
                    .employeeDesignation(userInformation.getOfficeInformation().getDesignation())
                    .employeeNameBangla(fromEmployeeRecord.getNameBangla())
                    .employeeNameEnglish(fromEmployeeRecord.getNameEnglish())
                    .officeNameBangla(userInformation.getOfficeInformation().getOfficeNameBangla())
                    .officeUnitNameBangla(fromOfficeUnit != null ? fromOfficeUnit.getUnitNameBangla() : null)
                    .username(userInformation.getUsername())
                    .build();
        } else {
            fromInfo = toInfo;
        }
        return this.grievanceForwardingDAO.addNewHistory(grievance, fromInfo, toInfo);
    }

    public Page<GrievanceDTO> getInboxGrievanceList(UserInformation userInformation, Pageable pageable) {
        Date date = new Date();
        Long expTime = CalendarUtil.getWorkDaysCountBefore(date, (int) Constant.GRIEVANCE_EXPIRATION_TIME);
        date.setTime(date.getTime() - expTime * 24 * 60 * 60 * 1000);

        Page<GrievanceDTO> grievanceForwardings = this.grievanceForwardingDAO.getListViewDTOPage(userInformation, pageable, ListViewType.NORMAL_INBOX)
                .map(source -> {
                    Grievance grievance = source.getGrievance();
                    GrievanceDTO grievanceDTO = this.convertToGrievanceDTO(grievance);
                    grievanceDTO.setIsSeen(source.getIsSeen());
                    grievanceDTO.setIsCC(source.getIsCC());
                    grievanceDTO.setIsExpired(grievance.getCreatedAt().before(date));
                    return grievanceDTO;
                });
        return grievanceForwardings;
    }

    public Page<GrievanceDTO> getOutboxGrievance(UserInformation userInformation, Pageable pageable) {
        Page<GrievanceDTO> grievanceForwardings = this.grievanceForwardingDAO.getListViewDTOPage(userInformation, pageable, ListViewType.NORMAL_OUTBOX)
                .map(GrievanceForwarding::getGrievance)
                .map(this::convertToGrievanceDTO);
        return grievanceForwardings;
    }

    public Page<GrievanceDTO> getCCGrievance(UserInformation userInformation, Pageable pageable) {
        Page<GrievanceDTO> grievanceForwardings = this.grievanceForwardingDAO.getListViewDTOPage(userInformation, pageable, ListViewType.NORMAL_CC)
                .map(source -> source.getGrievance())
                .map(this::convertToGrievanceDTO);
        return grievanceForwardings;
    }

    public Page<GrievanceDTO> findGrievancesByUsers(UserInformation userInformation, Pageable pageable) {
        Boolean isGrsUser = false;
        if (userInformation.getUserType().equals(UserType.COMPLAINANT)) {
            isGrsUser = true;
        }
        Long userId = isGrsUser ? userInformation.getUserId() : userInformation.getOfficeInformation().getEmployeeRecordId();
        return this.grievanceDAO.findByComplainantId(userId, isGrsUser, pageable).map(this::convertToGrievanceDTO);
    }

    public List<GrievanceDTO> findGrievancesByOthersComplainant(long userId) {
        return this.grievanceDAO.findByCreatedByAndSourceOfGrievance(userId, GRSUserType.OTHERS_COMPLAINANT.name())
                .stream()
                .map(this::convertToGrievanceDTO)
                .collect(Collectors.toList());
    }

    public Page<GrievanceDTO> getInboxAppealGrievanceList(UserInformation userInformation, Pageable pageable) {
        Date date = new Date();
        Long expTime = CalendarUtil.getWorkDaysCountBefore(date, (int) Constant.GRIEVANCE_EXPIRATION_TIME);
        date.setTime(date.getTime() - expTime * 24 * 60 * 60 * 1000);
        Page<GrievanceDTO> grievanceForwardings = this.grievanceForwardingDAO.getListViewDTOPage(userInformation, pageable, ListViewType.APPEAL_INBOX)
                .map(source -> {
                    Grievance grievance = source.getGrievance();
                    GrievanceDTO grievanceDTO = this.convertToGrievanceDTO(grievance);
                    grievanceDTO.setIsSeen(source.getIsSeen());
                    grievanceDTO.setIsCC(source.getIsCC());
                    grievanceDTO.setIsExpired(grievance.getCreatedAt().before(date));
                    return grievanceDTO;
                });
        return grievanceForwardings;
    }

    public Page<GrievanceDTO> getOutboxAppealGrievanceList(UserInformation userInformation, Pageable pageable) {
        Page<GrievanceDTO> grievanceForwardings = this.grievanceForwardingDAO.getListViewDTOPage(userInformation, pageable, ListViewType.APPEAL_OUTBOX)
                .map(source -> source.getGrievance())
                .map(this::convertToGrievanceDTO);
        return grievanceForwardings;
    }

    public Page<GrievanceDTO> getForwardedGrievances(UserInformation userInformation, Pageable pageable) {
        Page<GrievanceDTO> grievanceForwardings = this.grievanceForwardingDAO.getListViewDTOPage(userInformation, pageable, ListViewType.NORMAL_FORWARDED)
                .map(source -> source.getGrievance())
                .map(this::convertToGrievanceDTO)
                .map((GrievanceDTO grievanceDTO) -> {
                    Grievance grievance = this.grievanceDAO.findOne(Long.parseLong(grievanceDTO.getId()));
                    GrievanceForwarding forwardEntry = this.grievanceForwardingDAO.getByActionAndFromOffice(grievance, "%FORWARD%", userInformation.getOfficeInformation().getOfficeId());
                    grievanceDTO.setExpectedDateOfClosingEnglish(forwardEntry == null ? "" : forwardEntry.getCreatedAt().toString());
                    grievanceDTO.setExpectedDateOfClosingBangla(forwardEntry == null ? "" : BanglaConverter.getDateBanglaFromEnglish(forwardEntry.getCreatedAt().toString()));
                    grievanceDTO.setStatusBangla("অন্য দপ্তরে প্রেরিত");
                    grievanceDTO.setStatusEnglish("Forwarded To Another Office");
                    return grievanceDTO;
                });
        return grievanceForwardings;
    }

    public Page<GrievanceDTO> getClosedGrievances(UserInformation userInformation, Pageable pageable) {
        Page<GrievanceDTO> grievanceForwardings = this.grievanceForwardingDAO.getListViewDTOPage(userInformation, pageable, ListViewType.NORMAL_CLOSED)
                .map(source -> source.getGrievance())
                .map(this::convertToGrievanceDTO)
                .map((GrievanceDTO grievanceDTO) -> {
                    GrievanceForwarding closeEntry = this.grievanceForwardingDAO.findRecentlyClosedOrRejectedOne(Long.parseLong(grievanceDTO.getId()));
                    grievanceDTO.setExpectedDateOfClosingEnglish(closeEntry.getCreatedAt().toString());
                    grievanceDTO.setExpectedDateOfClosingBangla(BanglaConverter.getDateBanglaFromEnglish(closeEntry.getCreatedAt().toString()));

                    if(!grievanceDTO.getStatusEnglish().contains("CLOSED")){
                        grievanceDTO.setStatusBangla("নিষ্পত্তি(" + grievanceDTO.getStatusBangla() + ")");
                        grievanceDTO.setStatusEnglish("Closed(" + grievanceDTO.getStatusEnglish() + ")");
                    }
                    return grievanceDTO;
                });
        return grievanceForwardings;
    }

    public String getCaseNumber(Long officeId) {
        return this.grievanceDAO.getCaseNumber(officeId);
    }

    public Page<GrievanceDTO> getClosedAppealGrievances(UserInformation userInformation, Pageable pageable) {
        Page<GrievanceDTO> grievanceForwardings = this.grievanceForwardingDAO.getListViewDTOPage(userInformation, pageable, ListViewType.APPEAL_CLOSED)
                .map(source -> source.getGrievance())
                .map(this::convertToGrievanceDTO);
        return grievanceForwardings;
    }

    public EmployeeOrganogram getServiceOfficer(Long grievanceId) {
        Grievance grievance = this.grievanceDAO.findOne(grievanceId);
        Office office = this.officeService.getOffice(grievance.getOfficeId());
        CitizenCharter citizenCharter = this.citizenCharterService.findByOfficeAndService(grievance.getOfficeId(), grievance.getServiceOrigin());
        return EmployeeOrganogram.builder()
                .officeId(grievance.getOfficeId())
                .officeUnitOrganogramId(citizenCharter.getSoOfficeUnitOrganogramId())
                .ministryId(office.getOfficeMinistry().getId())
                .build();
    }

    public EmployeeOrganogram getGRO(Long grievanceId) {
        Grievance grievance = this.grievanceDAO.findOne(grievanceId);
        Office office = this.officeService.getOffice(grievance.getOfficeId());
        OfficesGRO gro = this.officesGroService.findOfficesGroByOfficeId(grievance.getOfficeId());

        return EmployeeOrganogram.builder()
                .officeId(grievance.getOfficeId())
                .officeUnitOrganogramId(gro.getGroOfficeUnitOrganogramId())
                .ministryId(office == null ? 0L : office.getOfficeMinistry().getId())
                .build();
    }

    public EmployeeOrganogram getAppealOfficer(Long grievanceId) {
        Grievance grievance = this.grievanceDAO.findOne(grievanceId);
        if (grievance.getCurrentAppealOfficeId() == null || grievance.getCurrentAppealOfficerOfficeUnitOrganogramId() == null) {
            return null;
        }
        Office office = this.officeService.getOffice(grievance.getCurrentAppealOfficeId());
        return EmployeeOrganogram.builder()
                .officeId(grievance.getCurrentAppealOfficeId())
                .officeUnitOrganogramId(grievance.getCurrentAppealOfficerOfficeUnitOrganogramId())
                .ministryId(office.getOfficeMinistry().getId())
                .build();
    }

    public EmployeeOrganogramDTO getSODetail(Long grievanceId) {
        Grievance grievance = this.grievanceDAO.findOne(grievanceId);
        Office office = this.officeService.getOffice(grievance.getOfficeId());
        CitizenCharter citizenCharter = this.citizenCharterService.findByOfficeAndService(grievance.getOfficeId(), grievance.getServiceOrigin());
        if (citizenCharter == null) {
            return EmployeeOrganogramDTO.builder().build();
        }
        EmployeeOffice employeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(grievance.getOfficeId(), citizenCharter.getSoOfficeUnitOrganogramId(), true);
        return EmployeeOrganogramDTO.builder()
                .officeId(office.getId())
                .officeUnitOrganogramId(citizenCharter.getSoOfficeUnitOrganogramId())
                .ministryId(office.getOfficeMinistry().getId())
                .employeeDesignation(
                        this.officeService.getOfficeUnitOrganogramById(citizenCharter.getSoOfficeUnitOrganogramId())
                                .getDesignationBangla())
                .employeeName(employeeOffice == null ? "" : employeeOffice.getEmployeeRecord().getNameBangla())
                .officeUnitName(employeeOffice == null ? "" : employeeOffice.getOfficeUnit().getUnitNameBangla())
                .build();
    }

    public EmployeeOrganogramDTO getGroOfGrievance(Long grievanceId) {
        Grievance grievance = this.grievanceDAO.findOne(grievanceId);
        OfficesGRO officesGRO = this.officesGroService.findOfficesGroByOfficeId(grievance.getOfficeId());
        Office office = this.officeService.getOffice(grievance.getOfficeId());
        EmployeeOffice employeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(grievance.getOfficeId(), officesGRO.getGroOfficeUnitOrganogramId(), true);

        return EmployeeOrganogramDTO.builder()
                .officeId(officesGRO.getGroOfficeId())
                .officeUnitOrganogramId(officesGRO.getGroOfficeUnitOrganogramId())
                .employeeName(employeeOffice.getEmployeeRecord().getNameBangla())
                .employeeDesignation(employeeOffice.getDesignation())
                .officeUnitName(employeeOffice.getOfficeUnit().getUnitNameBangla())
                .ministryId(office.getOfficeMinistry().getId())
                .build();

    }

    public Map getGrievanceDataForGRODashboard(Long officeId) {
        Office office = officeService.findOne(officeId);
        Long resolvedComplaintsCount = grievanceDAO.getResolvedGrievancesCountByOfficeId(officeId);
        Long totalComplaintsCount = grievanceDAO.getSubmittedGrievancesCountByOffice(officeId);
        Map groDashboardData = new WeakHashMap();
        groDashboardData.put("totalSubmitted", new WeakHashMap() {{
            put("name", "প্রাপ্ত অভিযোগ");
            put("value", totalComplaintsCount.toString());
        }});
        groDashboardData.put("totalResolved", new WeakHashMap() {{
            put("name", "নিষ্পত্তিকৃত অভিযোগ");
            put("value", resolvedComplaintsCount.toString());
        }});
        groDashboardData.put("ratingGauge", new WeakHashMap() {{
            put("name", "ব্যবহারকারী সন্তুষ্টি");
            put("value", 4.23);
        }});
        groDashboardData.put("comparisonPie", new ArrayList() {{
            add(new WeakHashMap() {{
                put("name", "নিষ্পন্ন");
                put("value", 147);
            }});
            add(new WeakHashMap() {{
                put("name", "অনিষ্পন্ন");
                put("value", 88);
            }});
            add(new WeakHashMap() {{
                put("name", "চলমান");
                put("value", 206);
            }});
        }});
        groDashboardData.put("comparisonBar", new ArrayList() {{
            add(new WeakHashMap() {{
                put("name", "জুলাই");
                put("submitted", 104);
                put("resolved", 60);
            }});
            add(new WeakHashMap() {{
                put("name", "আগস্ট");
                put("submitted", 65);
                put("resolved", 73);
            }});
            add(new WeakHashMap() {{
                put("name", "সেপ্টেম্বর");
                put("submitted", 122);
                put("resolved", 86);
            }});
            add(new WeakHashMap() {{
                put("name", "অক্টোবর");
                put("submitted", 159);
                put("resolved", 85);
            }});
            add(new WeakHashMap() {{
                put("name", "নভেম্বর");
                put("submitted", 78);
                put("resolved", 78);
            }});
            add(new WeakHashMap() {{
                put("name", "ডিসেম্বর");
                put("submitted", 95);
                put("resolved", 80);
            }});
        }});
        return groDashboardData;
    }

    public Long getResolvedGrievancesCountByOfficeId(Long officeId) {
        return grievanceDAO.getResolvedGrievancesCountByOfficeId(officeId);

    }

    public Long getUnresolvedGrievancesCountByOfficeId(Long officeId) {

        return grievanceDAO.getCountOfUnresolvedGrievancesByOfficeId(officeId);
    }

    public Long getRunningGrievancesCountByOfficeId(Long officeId) {
        return grievanceDAO.getCountOfRunningGrievancesByOfficeId(officeId);
    }

    public Long getSubmittedGrievancesCountByOffice(Long officeId) {
        return grievanceDAO.getSubmittedGrievancesCountByOffice(officeId);
    }

    public Page<GrievanceDTO> getListViewWithSearching(UserInformation userInformation, String value, ListViewType listViewType, Pageable pageable) {
        Date date = new Date();
        Long expTime = CalendarUtil.getWorkDaysCountBefore(date, (int) Constant.GRIEVANCE_EXPIRATION_TIME);
        date.setTime(date.getTime() - expTime * 24 * 60 * 60 * 1000);
        Page<GrievanceForwarding> forwardings;
        if(listViewType.equals(ListViewType.NORMAL_OUTBOX)) {
            Long officeId = userInformation.getOfficeInformation().getOfficeId();
            Long officeUnitOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();
            Long userId = userInformation.getUserId();
            log.info("===STARTED STARTED=== ORG:{} Office:{} user:{}", officeUnitOrganogramId, officeId, userId);
            forwardings = grievanceForwardingRepo.findOutboxGrievance(officeUnitOrganogramId, officeId, userId, pageable);
            log.info("===OUTBOX ENDED===");
        } else {
            log.info("==NOT OUTBOX STARTED===");
            forwardings = this.grievanceForwardingDAO.getListViewDTOPageWithSearching(userInformation, pageable, listViewType, value);
            log.info("==DONE NOT OUTBOX===");
        }

        return forwardings.map(source -> {
            Grievance grievance = source.getGrievance();
            GrievanceDTO grievanceDTO = this.convertToGrievanceDTO(grievance);
            switch (listViewType) {
                case NORMAL_CLOSED:
                    GrievanceForwarding closeEntry = this.grievanceForwardingDAO.findRecentlyClosedOrRejectedOne(Long.parseLong(grievanceDTO.getId()));
                    grievanceDTO.setExpectedDateOfClosingEnglish(closeEntry.getCreatedAt().toString());
                    grievanceDTO.setExpectedDateOfClosingBangla(BanglaConverter.getDateBanglaFromEnglish(closeEntry.getCreatedAt().toString()));

                    if (!grievanceDTO.getStatusEnglish().contains("CLOSED")) {
                        grievanceDTO.setStatusBangla("নিষ্পত্তি(" + grievanceDTO.getStatusBangla() + ")");
                        grievanceDTO.setStatusEnglish("Closed(" + grievanceDTO.getStatusEnglish() + ")");
                    }
                    break;
                case NORMAL_FORWARDED:
                    GrievanceForwarding forwardEntry = this.grievanceForwardingDAO.getByActionAndFromOffice(grievance, "%FORWARD%", userInformation.getOfficeInformation().getOfficeId());
                    grievanceDTO.setExpectedDateOfClosingEnglish(forwardEntry == null ? "" : forwardEntry.getCreatedAt().toString());
                    grievanceDTO.setExpectedDateOfClosingBangla(forwardEntry == null ? "" : BanglaConverter.getDateBanglaFromEnglish(forwardEntry.getCreatedAt().toString()));
                    grievanceDTO.setStatusBangla("অন্য দপ্তরে প্রেরিত");
                    grievanceDTO.setStatusEnglish("Forwarded To Another Office");
                    break;
                case NORMAL_OUTBOX:
                case NORMAL_CC:
                case APPEAL_OUTBOX:
                case APPEAL_CLOSED:
                    break;

                default:
                    grievanceDTO.setIsSeen(source.getIsSeen());
                    grievanceDTO.setIsCC(source.getIsCC());
                    grievanceDTO.setIsExpired(grievance.getCreatedAt().before(date));
                    break;
            }

            return grievanceDTO;
        });
    }

    public Page<GrievanceComplainantInfoDTO> getComplainantViewForReport(List<Office> childOffices, Integer fromYear, Integer fromMonth, Integer toYear, Integer toMonth, Pageable pageable) {

        Page<GrievanceComplainantInfoDTO> inboxAppealGrievances = this.grievanceDAO.getComplainantViewForReport(childOffices, fromYear, fromMonth, toYear, toMonth, pageable)
                .map(grievance -> convertToGrievanceComplainantInfoDTOForListView(grievance));
        return inboxAppealGrievances;
    }

    public Long getInboxCount(UserInformation userInformation, ListViewType listViewType) {

        return this.grievanceForwardingDAO.getInboxCount(userInformation, listViewType);
    }

    public List<Grievance> getListViewWithOutSearching(long officeId, long userId, long officeOrganogramId) {
        Date date = new Date();
        Long expTime = CalendarUtil.getWorkDaysCountBefore(date, (int) Constant.GRIEVANCE_EXPIRATION_TIME);
        date.setTime(date.getTime() - expTime * 24 * 60 * 60 * 1000);

        List<Grievance> inboxAppealGrievances = this.grievanceForwardingDAO.getListViewDTOPageWithOutSearching(officeId, userId, officeOrganogramId)
                .stream()
                .map(source -> {
                    Grievance grievance = source.getGrievance();
//                    GrievanceDTO grievanceDTO = this.convertToGrievanceDTOForListView(grievance);
//                    grievanceDTO.setIsSeen(source.getIsSeen());
//                    grievanceDTO.setIsCC(source.getIsCC());
//                    grievanceDTO.setIsExpired(grievance.getCreatedAt().before(date));

                    return grievance;
                })
                .collect(Collectors.toList());
        return inboxAppealGrievances;
    }

    public Page<GrievanceDTO> getExpiredGrievances(UserInformation userInformation, Pageable pageable) {
        Date date = new Date();
        Long expTime = CalendarUtil.getWorkDaysCountBefore(date, (int) Constant.GRIEVANCE_EXPIRATION_TIME);
        date.setTime(date.getTime() - expTime * 24 * 60 * 60 * 1000);
        Page<GrievanceDTO> grievanceForwardings = this.grievanceForwardingDAO.getListViewDTOPage(userInformation, pageable, ListViewType.NORMAL_EXPIRED)
                .map(source -> {
                    Grievance grievance = source.getGrievance();
                    GrievanceDTO grievanceDTO = this.convertToGrievanceDTO(grievance);
                    grievanceDTO.setIsSeen(source.getIsSeen());
                    grievanceDTO.setIsExpired(grievance.getCreatedAt().before(date));
                    return grievanceDTO;
                });
        return grievanceForwardings;
    }

    public Page<GrievanceDTO> getAppealExpiredGrievances(UserInformation userInformation, Pageable pageable) {
        Page<GrievanceDTO> grievanceForwardings = this.grievanceForwardingDAO.getListViewDTOPage(userInformation, pageable, ListViewType.APPEAL_EXPIRED)
                .map(source -> {
                    Grievance grievance = source.getGrievance();
                    GrievanceDTO grievanceDTO = this.convertToGrievanceDTO(grievance);
                    grievanceDTO.setIsSeen(source.getIsSeen());
                    return grievanceDTO;
                });
        return grievanceForwardings;
    }

    public Object getStatusOfGrievance(String trackingNumber, String phoneNumber) {
        trackingNumber = BanglaConverter.convertToEnglish(trackingNumber);
        phoneNumber = BanglaConverter.convertToEnglish(phoneNumber);
        Complainant complainant = this.complainantService.findComplainantByPhoneNumber(phoneNumber);
        if (complainant == null) {
            return new EmptyJsonResponse();
        }
        Grievance grievance = this.grievanceDAO.findByTrackingNumberAndComplaintId(trackingNumber, complainant.getId());
        if (grievance == null) {
            return new EmptyJsonResponse();
        }

        GrievanceCurrentStatus currentStatus = grievance.getGrievanceCurrentStatus();
        boolean isClosed = false;
        String statusInTextBng;
        String statusInTextEng;
        switch (currentStatus) {
            case NEW:
                statusInTextBng = "নতুন";
                statusInTextEng = "New";
                break;
            case FORWARDED_OUT:
                statusInTextBng = "অন্য দপ্তরে প্রেরিত";
                statusInTextEng = "Forwarded to another office";
                break;
            case CLOSED_ACCUSATION_INCORRECT:
            case CLOSED_ANSWER_OK:
            case CLOSED_INSTRUCTION_EXECUTED:
            case CLOSED_ACCUSATION_PROVED:
            case CLOSED_SERVICE_GIVEN:
            case CLOSED_OTHERS:
            case APPEAL_CLOSED_OTHERS:
            case APPEAL_CLOSED_ACCUSATION_INCORRECT:
            case APPEAL_CLOSED_ACCUSATION_PROVED:
            case APPEAL_CLOSED_ANSWER_OK:
            case APPEAL_CLOSED_INSTRUCTION_EXECUTED:
            case APPEAL_CLOSED_SERVICE_GIVEN:
                statusInTextBng = "নিষ্পত্তিকৃত";
                statusInTextEng = "Closed";
                isClosed = true;
                break;
            case REJECTED:
            case APPEAL_REJECTED:
                statusInTextBng = "নথিজাত";
                statusInTextEng = "Rejected";
                break;
            default:
                statusInTextBng = "চলমান";
                statusInTextEng = "In progress";
                break;
        }
        Date date = new Date();
        Long closeTime = CalendarUtil.getWorkDaysCountAfter(date, (int) Constant.GRIEVANCE_EXPIRATION_TIME);
        date.setTime(grievance.getCreatedAt().getTime() + closeTime * 24 * 60 * 60 * 1000);
        return GrievanceStatusDTO.builder()
                .id(grievance.getId())
                .statusBng(statusInTextBng)
                .statusEng(statusInTextEng)
                .closeDateBng(statusInTextBng)
                .closeDateEng(statusInTextEng)
                .submissionDateBng(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(grievance.getCreatedAt())))
                .submissionDateEng(DateTimeConverter.convertDateToString(grievance.getCreatedAt()))
                .closeDateEng(isClosed ? "Closed" : DateTimeConverter.convertDateToString(date))
                .closeDateBng(isClosed ? "নিষ্পত্তিকৃত" : BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(date)))
                .serviceNameEng(grievance.getServiceOrigin() != null ? grievance.getServiceOrigin().getServiceNameEnglish() : grievance.getOtherService())
                .serviceNameBng(grievance.getServiceOrigin() != null ? grievance.getServiceOrigin().getServiceNameBangla() : grievance.getOtherService())
                .build();
    }

    public String getOfficeNameBanglaByOfficeId(Long officeId) {
        Office office = this.officeService.getOffice(officeId);
        return office.getNameBangla();
    }

    public String getServiceNameBanglaByOfficeCitizenCharterId(Long officeCitizenCharterId) {
        CitizenCharter citizenCharter = this.citizenCharterService.findOne(officeCitizenCharterId);
        return citizenCharter.getServiceNameBangla();
    }

    public ServiceRelatedInfoRequestDTO convertFromBase64encodedString(String base64EncodedString) throws IOException {
        String base64DecodedParameters = StringUtils.newStringUtf8(org.apache.tomcat.util.codec.binary.Base64.decodeBase64(base64EncodedString));
        ServiceRelatedInfoRequestDTO serviceRelatedInfoRequestDTO = objectMapper.readValue(base64DecodedParameters, ServiceRelatedInfoRequestDTO.class);
        String officeName = this.getOfficeNameBanglaByOfficeId(serviceRelatedInfoRequestDTO.getOfficeId());
        String serviceName = this.getServiceNameBanglaByOfficeCitizenCharterId(serviceRelatedInfoRequestDTO.getOfficeCitizenCharterId());
        serviceRelatedInfoRequestDTO.setServiceName(serviceName);
        serviceRelatedInfoRequestDTO.setOfficeName(officeName);
        return serviceRelatedInfoRequestDTO;
    }

    public Boolean appealActivationFlag(Long id) {
        Grievance grievance = this.grievanceDAO.findOne(id);
        Date today = new Date();
        if (grievance.getOfficeId() == 0) {
            return false;
        }
        if (grievance.getGrievanceCurrentStatus().toString().contains("CLOSED") || grievance.getGrievanceCurrentStatus().toString().contains("REJECTED")) {
            /*GrievanceForwarding grievanceForwarding = this.grievanceForwardingDAO.getLastClosedOrRejectedForwarding(grievance);
            Date closingDate = grievanceForwarding.getCreatedAt();

            Long days = TimeUnit.DAYS.convert((today.getTime() - closingDate.getTime()), TimeUnit.MILLISECONDS);
            if (days <= CalendarUtil.getWorkDaysCountAfter(closingDate, (int) Constant.APPEAL_EXPIRATION_TIME)) {
                return true;
            }*/
            return true;
        } else if(!grievance.getGrievanceCurrentStatus().toString().contains("APPEAL") &&
                TimeUnit.DAYS.convert((today.getTime() - grievance.getCreatedAt().getTime()), TimeUnit.MILLISECONDS) > CalendarUtil.getWorkDaysCountAfter(grievance.getCreatedAt(), (int) Constant.GRIEVANCE_EXPIRATION_TIME)){
            return true;
        }
        return false;
    }

    public Boolean isHeadOfOffice(Long officeId, UserInformation userInformation) {
        EmployeeOffice employeeOffice = this.officeService.findEmployeeOfficeByOfficeAndIsOfficeHead(officeId);
        if (employeeOffice == null) {
            return false;
        }
        return Objects.equals(userInformation.getOfficeInformation().getOfficeUnitOrganogramId(), employeeOffice.getOfficeUnitOrganogram().getId());

    }

    public Boolean isOISFComplainant(Authentication authentication, Long grievanceId) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Grievance grievance = this.grievanceDAO.findOne(grievanceId);
        if (!grievance.isGrsUser()
                && (grievance.getComplainantId().equals(userInformation.getUserId())
                || grievance.getComplainantId().equals(userInformation.getOfficeInformation().getEmployeeRecordId()))) {
            return true;
        }
        return false;
    }

    public Boolean serviceIsNull(Long grievanceId) {
        Grievance grievance = this.grievanceDAO.findOne(grievanceId);
        if (grievance.getServiceOrigin() == null) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean checkIfHOOSubmitsOnSameOffice(UserInformation userInformation, GrievanceRequestDTO grievanceRequestDTO) {
        if (userInformation.getUserType().equals(UserType.OISF_USER) &&
                userInformation.getOisfUserType().equals(OISFUserType.HEAD_OF_OFFICE) &&
                userInformation.getOfficeInformation().getOfficeId().equals(Long.valueOf(grievanceRequestDTO.getOfficeId()))) {
            return true;
        } else {
            return false;
        }
    }

    public List<EmployeeDetailsDTO> getAllRelatedUsers(Long grievanceId) {
        List<GrievanceForwarding> employees = this.grievanceForwardingDAO.getdistinctemployeRecordIds(grievanceId);
        List<Long> escapeList = new ArrayList<>();
        EmployeeOrganogram gro = this.getGRO(grievanceId);
        EmployeeOrganogram appealOfficer = this.getAppealOfficer(grievanceId);
        if (appealOfficer != null) {
            escapeList.add(appealOfficer.getOfficeUnitOrganogramId());
        }
        escapeList.add(gro.getOfficeUnitOrganogramId());
        List<EmployeeDetailsDTO> employeeRecordDTOS = employees.stream().filter(grievanceForwarding -> {
            return !escapeList.contains(grievanceForwarding.getToOfficeUnitOrganogramId());
        }).map(
                data -> {
                    return EmployeeDetailsDTO.builder()
                            .id(data.getToEmployeeRecordId().toString() + "_" + data.getToOfficeUnitOrganogramId() + "_" + data.getToOfficeId())
                            .name(data.getToEmployeeNameBangla())
                            .designation(data.getToEmployeeDesignationBangla())
                            .officeUnitNameBng(data.getToEmployeeUnitNameBangla())
                            .officeNameBng(data.getToOfficeNameBangla())
                            .build();
                }
        ).collect(Collectors.toList());
        return employeeRecordDTOS;
    }

    public List<GrievanceDTO> getGrievancesByComplainantId(Long complainantId) {
        List<GrievanceDTO> grievanceDTOList = this.grievanceDAO.findByComplainantId(complainantId, true,
                new PageRequest(0, Integer.MAX_VALUE))
                .map(x -> {
                    GrievanceDTO grievanceDTO = GrievanceDTO.builder()
                            .caseNumberBangla(x.getCaseNumber() == null ? "" : BanglaConverter.convertToBanglaDigit(Long.valueOf(x.getCaseNumber())))
                            .caseNumberEnglish(x.getCaseNumber() == null ? "" : x.getCaseNumber())
                            .dateBangla(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToStringForTimeline(x.getCreatedAt())))
                            .dateEnglish(DateTimeConverter.convertDateToStringForTimeline(x.getCreatedAt()))
                            .statusBangla(BanglaConverter.convertGrievanceStatusToBangla(x.getGrievanceCurrentStatus()))
                            .statusEnglish(BanglaConverter.convertGrievanceStatusToEnglish(x.getGrievanceCurrentStatus()))
                            .subject(x.getSubject())
                            .trackingNumberBangla(BanglaConverter.convertToBanglaDigit(Long.valueOf(x.getTrackingNumber())))
                            .trackingNumberEnglish(x.getTrackingNumber())
                            .build();
                    return grievanceDTO;
                })
                .getContent();
        return grievanceDTOList;
    }

    public List<GrievanceDTO> getGrievancesByComplainantIdForApi(Long complainantId) {
        Date date = new Date();
        Long expTime = CalendarUtil.getWorkDaysCountBefore(date, (int) Constant.GRIEVANCE_EXPIRATION_TIME);
        date.setTime(date.getTime() - expTime * 24 * 60 * 60 * 1000);

        List<GrievanceDTO> grievanceDTOList = this.grievanceDAO.findByComplainantId(complainantId, true,
                new PageRequest(0, Integer.MAX_VALUE))
                .map(x -> {
                    GrievanceDTO grievanceDTO = this.convertToGrievanceDTO(x);
                    grievanceDTO.setIsExpired(x.getCreatedAt().before(date));
                    return grievanceDTO;
                })
                .getContent();
        return grievanceDTOList;
    }

    public Object getGrievanceByTrackingNumber(String trackingNumber) {
        Date date = new Date();
        Long expTime = CalendarUtil.getWorkDaysCountBefore(date, (int) Constant.GRIEVANCE_EXPIRATION_TIME);
        date.setTime(date.getTime() - expTime * 24 * 60 * 60 * 1000);

        List<GrievanceDTO> grievanceDTOList = this.grievanceDAO.findByTrackingNumber(trackingNumber,
                new PageRequest(0, Integer.MAX_VALUE))
                .map(x -> {
                    GrievanceDTO grievanceDTO = this.convertToGrievanceDTO(x);
                    grievanceDTO.setIsExpired(x.getCreatedAt().before(date));
                    return grievanceDTO;
                })
                .getContent();

        if (!grievanceDTOList.isEmpty()) return grievanceDTOList.get(0);
        return new EmptyJsonResponse();
    }

    public Grievance getSingleGrievanceByTrackingNumber(String trackingNumber) {
        return this.grievanceDAO.findByTrackingNumber(trackingNumber);
    }

    public Boolean isNagorikTypeGrievance(Long grievanceId) {
        Grievance grievance = this.grievanceDAO.findOne(grievanceId);
        return grievance.getGrievanceType().equals(ServiceType.NAGORIK);
    }

    public Boolean isBlacklistedUserByGrievanceId(Long grievanceId) {
        Grievance grievance = this.grievanceDAO.findOne(grievanceId);
        if (!grievance.isGrsUser()) {
            return false;
        }
        Long complainantId = grievance.getComplainantId();
        return this.complainantService.isBlacklistedUserByComplainantId(complainantId);
    }

    public Boolean isFeedbackEnabled(Long grievanceId) {
        Grievance grievance = this.grievanceDAO.findOne(grievanceId);
        if (grievance.getGrievanceCurrentStatus().toString().matches("(.*)(CLOSED|REJECTED)(.*)")) {
            if (grievance.getGrievanceCurrentStatus().toString().matches("^((?!APPEAL).)*$")) {
                if (grievance.getIsRatingGiven() == null || grievance.getIsRatingGiven().equals(false)) {
                    return true;
                }
            } else if (grievance.getIsAppealRatingGiven() == null || grievance.getIsAppealRatingGiven().equals(false)) {
                return true;
            }
        }
        return false;
    }

    public Boolean isSubmittedByAnonymousUser(Long grievanceId) {
        Grievance grievance = grievanceDAO.findOne(grievanceId);
        return grievance.getComplainantId().equals(0L);
    }

    public Grievance getFeedbackByComplainant(FeedbackRequestDTO feedbackRequestDTO) {
        Grievance grievance = this.grievanceDAO.findOne(feedbackRequestDTO.getGrievanceId());
        if (grievance.getGrievanceCurrentStatus().toString().matches("^((?!APPEAL).)*$")) {
            grievance =  this.grievanceDAO.feedbackAgainstGrievance(grievance, feedbackRequestDTO);
        } else {
            grievance = this.grievanceDAO.feedbackAgainstAppealGrievance(grievance, feedbackRequestDTO);
        }
        return  grievance;
    }

    public List<FeedbackResponseDTO> getFeedbacks(Long id) {
        Grievance grievance = this.grievanceDAO.findOne(id);
        FeedbackResponseDTO feedback, appealFeedback;
        List<FeedbackResponseDTO> feedbacks = new ArrayList();
        if(grievance.getIsRatingGiven()!=null && grievance.getIsRatingGiven().equals(true)) {
            feedback = FeedbackResponseDTO.builder()
                    .title(this.messageService.getMessage("feedback.grievance"))
                    .rating(grievance.getRating())
                    .comments(grievance.getFeedbackComments())
                    .build();
            feedbacks.add(feedback);
        }
        if(grievance.getIsAppealRatingGiven()!=null && grievance.getIsAppealRatingGiven().equals(true)) {
            appealFeedback = FeedbackResponseDTO.builder()
                    .title(this.messageService.getMessage("feedback.grievance.appeal"))
                    .rating(grievance.getAppealRating())
                    .comments(grievance.getAppealFeedbackComments())
                    .build();
            feedbacks.add(appealFeedback);
        }
        return feedbacks;
    }

    public List<Grievance> getGrievancesByIds(List<Long> grievanceIds) {
        return this.grievanceDAO.findByIdIn(grievanceIds);
    }

    public Model addFileSettingsAttributesToModel(Model model) {
        Integer maxFileSize = generalSettingsService.getMaximumFileSize();
        String allowedFileTypes = generalSettingsService.getAllowedFileTypes();
        model.addAttribute("maxFileSize", maxFileSize);
        model.addAttribute("allowedFileTypes", allowedFileTypes);
        model.addAttribute("fileSizeLabel", generalSettingsService.getAllowedFileSizeLabel());
        model.addAttribute("fileTypesLabel", generalSettingsService.getAllowedFileTypesLabel());
        return model;
    }

    public Boolean soAppealActivationFlag(Long id) {
        Grievance grievance = this.grievanceDAO.findOne(id);
        if (grievance.getOfficeId() == 0) {
            return false;
        }
        if (grievance.getGrievanceCurrentStatus().toString().contentEquals("CLOSED_ACCUSATION_PROVED")) {
            GrievanceForwarding grievanceForwarding = this.grievanceForwardingDAO.getLastClosedOrRejectedForwarding(grievance);
            Date closingDate = grievanceForwarding.getCreatedAt();
            Date today = new Date();
            Long days = TimeUnit.DAYS.convert((today.getTime() - closingDate.getTime()), TimeUnit.MILLISECONDS);
            if (days <= 31) {
                return true;
            }
        }
        return false;
    }

    public Boolean isComplainantBlackListedByGrievanceId(Long id) {
        Grievance grievance = grievanceDAO.findOne(id);
        return complainantService.isBlacklistedUserByComplainantId(grievance.getComplainantId());
    }

    public List<FileDerivedDTO> getGrievancesFiles(Long id) {
        Grievance grievance = this.grievanceDAO.findOne(id);
        List<AttachedFile> attachedFiles = grievance.getAttachedFiles();
        List<FileDerivedDTO> files = new ArrayList<>();

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

    public int getCountOfAttachedFiles(Long grievanceId){
        Grievance grievance = this.grievanceDAO.findOne(grievanceId);
        List<AttachedFile> attachedFiles = grievance.getAttachedFiles();
        return attachedFiles.size();
    }

    public Boolean isComplaintRevivable(Long grievanceId, Authentication authentication) {
        Grievance grievance = this.grievanceDAO.findOne(grievanceId);
        if(grievance.getGrievanceCurrentStatus().equals(GrievanceCurrentStatus.REJECTED)){
            UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
            if(userInformation.getUserType().equals(UserType.OISF_USER)){
                if(userInformation.getOisfUserType().equals(OISFUserType.GRO)){
                    return true;
                }
            }
        }
        return false;
    }

    public ServiceOriginDTO getCitizenCharterAsServiceOriginDTO(UserInformation userInformation, Long citizensCharterId, Long grievanceId) {
        CitizenCharter citizenCharter = citizenCharterService.findOne(citizensCharterId);
        ServiceOriginDTO serviceOriginDTO = officeService.convertToService(citizenCharter);
        if(userInformation.getUserType().equals(UserType.OISF_USER)) {
            Long countGrievanceByOffice = grievanceDAO.countByOfficeIdAndServiceOriginId(citizenCharter.getOfficeId(), citizenCharter.getServiceOrigin().getId());
            serviceOriginDTO.setCountGrievanceByOffice(countGrievanceByOffice);
        }
        return serviceOriginDTO;
    }

    public List<Grievance> findByOfficeIdAndStatus(Long officeId){
        return this.grievanceDAO.findByOfficeIdAndStatus(officeId);
    }

    public List<GrievanceDTO> getAllCellComplaints() {
        List<Grievance> grievances = this.grievanceDAO.getAllGrievanceOfCell();
        return grievances.stream().map(this::convertToGrievanceDTO).collect(Collectors.toList());
    }

    public List<OISFGrievanceDTO> getUserInboxList(Long officeId, Long officeUnitOrganogramId, Long userId, ListViewType listViewType) {
        Pageable pageable = new Pageable() {
            @Override
            public int getPageNumber() {
                return 0;
            }

            @Override
            public int getPageSize() {
                return 100;
            }

            @Override
            public int getOffset() {
                return 0;
            }

            @Override
            public Sort getSort() {
                return null;
            }

            @Override
            public Pageable next() {
                return null;
            }

            @Override
            public Pageable previousOrFirst() {
                return null;
            }

            @Override
            public Pageable first() {
                return null;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }
        };
        List<OISFGrievanceDTO> dtos = new ArrayList<>();
        grievanceForwardingDAO.getListViewDTOPageWithSearching(officeUnitOrganogramId, officeId, userId, listViewType, "", pageable)
                .map(source -> {
                    Grievance grievance = source.getGrievance();
                    OISFGrievanceDTO grievanceDTO = this.convertToOISFGrievanceListDTO(grievance, listViewType);
                    dtos.add(grievanceDTO);

                    return grievanceDTO;
                });

        return dtos;

    }

    public OISFGrievanceDTO convertToOISFGrievanceListDTO(Grievance grievance, ListViewType listViewType) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Complainant complainant = complainantService.findOne(grievance.getComplainantId());
        String name = complainant == null ? "" : complainant.getName();
        return OISFGrievanceDTO.builder()
                .body(grievance.getDetails())
                .caseNumber(grievance.getCaseNumber())
                .datetime(formatter.format(grievance.getSubmissionDate() == null ? grievance.getCreatedAt() : grievance.getSubmissionDate()))
                .id(grievance.getId())
                .status(getStatusFromListViewType(listViewType))
                .subject(grievance.getSubject())
                .type(grievance.getGrievanceType().name())
                .trackingNumber(grievance.getTrackingNumber())
                .sender(name)
                .redirectURL("http://www.grs.gov.bd/login?a=1&redirectUrl=viewGrievances.do?id=" + grievance.getId())
                .build();

    }

    public String getStatusFromListViewType(ListViewType listViewType) {
        switch (listViewType) {
            case APPEAL_INBOX:
            case NORMAL_INBOX:
                return "আগত";
            case APPEAL_OUTBOX:
            case NORMAL_OUTBOX:
                return "প্রেরিত";
            case APPEAL_CLOSED:
            case NORMAL_CLOSED:
                return "নিষ্পত্তিকৃত";
            case NORMAL_FORWARDED:
                return "অন্য দপ্তরে প্রেরিত";
            case NORMAL_CC:
                return "অনুলিপি";
            case APPEAL_EXPIRED:
            case NORMAL_EXPIRED:
                return "সময় অতিক্রান্ত";
            default:
                return "";

        }
    }

    public OISFIntermediateDashboardDTO getInboxDataDTO(Long officeId, Long officeUnitOrganogramId, Long userId) {
        Long inbox, outbox, forwarded, resolved, expired, cc;
        ListViewConditionOnCurrentStatusGenerator viewConditionOnCurrentStatusGenerator = new ListViewConditionOnCurrentStatusGenerator();
        ListViewType listViewType;
        List<OISFGrievanceDTO> allGrievanceList = new ArrayList<>();

        listViewType = viewConditionOnCurrentStatusGenerator.getNormalListTypeByString("inbox");
        List<OISFGrievanceDTO> getUserInboxList = this.getUserInboxList(officeId, officeUnitOrganogramId, userId, listViewType);
        allGrievanceList.addAll(getUserInboxList);
        inbox = (long) getUserInboxList.size();

        listViewType = viewConditionOnCurrentStatusGenerator.getNormalListTypeByString("outbox");
        List<OISFGrievanceDTO> getUserOutboxList = this.getUserInboxList(officeId, officeUnitOrganogramId, userId, listViewType);
        allGrievanceList.addAll(getUserOutboxList);
        outbox = (long) getUserOutboxList.size();

        listViewType = viewConditionOnCurrentStatusGenerator.getNormalListTypeByString("forwarded");
        List<OISFGrievanceDTO> getUserForwardedList = this.getUserInboxList(officeId, officeUnitOrganogramId, userId, listViewType);
        allGrievanceList.addAll(getUserForwardedList);
        forwarded = (long) getUserForwardedList.size();

        listViewType = viewConditionOnCurrentStatusGenerator.getNormalListTypeByString("closed");
        List<OISFGrievanceDTO> getUserResolvedList = this.getUserInboxList(officeId, officeUnitOrganogramId, userId, listViewType);
        allGrievanceList.addAll(getUserResolvedList);
        resolved = (long) getUserResolvedList.size();

       /* listViewType = viewConditionOnCurrentStatusGenerator.getNormalListTypeByString("expired");
        List<OISFGrievanceDTO> getUserExpiredList = this.getUserInboxList(officeId, officeUnitOrganogramId, userId, listViewType);
        allGrievanceList.addAll(getUserExpiredList);
        expired = (long) getUserExpiredList.size();*/

        /*listViewType = viewConditionOnCurrentStatusGenerator.getNormalListTypeByString("cc");
        List<OISFGrievanceDTO> getUserCCList = this.getUserInboxList(officeId, officeUnitOrganogramId, userId, listViewType);
        allGrievanceList.addAll(getUserCCList);
        cc = (long) getUserCCList.size();*/


        GeneralInboxDataDTO generalDashboardDataDTO = GeneralInboxDataDTO.builder()
                .inbox(NameValuePairDTO.builder()
                        .name("আগত")
                        .value(inbox)
                        .build())
                .outbox(NameValuePairDTO.builder()
                        .name("প্রেরিত")
                        .value(outbox)
                        .color("#008000")
                        .build())
                .forwarded(NameValuePairDTO.builder()
                        .name("অন্য দপ্তরে প্রেরিত")
                        .value(forwarded)
                        .color("#8A2BE2")
                        .build())
                .resolved(NameValuePairDTO.builder()
                        .name("নিষ্পত্তিকৃত")
                        .value(resolved)
                        .color("#EED202")
                        .build())
                /*.expired(NameValuePairDTO.builder()
                        .name("সময় অতিক্রান্ত")
                        .value(expired)
                        .color("#ED2939")
                        .build())
                .cc(NameValuePairDTO.builder()
                        .name("অনুলিপি")
                        .value(cc)
                        .color("#ED2939")
                        .build())*/
                .build();
        return OISFIntermediateDashboardDTO.builder().generalInboxDataDTO(generalDashboardDataDTO).grievanceDTOS(allGrievanceList).build();
    }


    public Page<GrievanceAdminDTO> getAdminGrievances(UserInformation userInformation, Long officeId, String referenceNumber, Pageable pageable) {
        return this.grievanceDAO.getGrievanceAdminSearch(officeId, referenceNumber, pageable);
    }

    public WeakHashMap<String, Object> reassignGrievance(ReassignGrievanceDTO reassignGrievance, UserInformation userInformation) {

        WeakHashMap<String, Object> result = new WeakHashMap<>();
        if (!(userInformation.getUsername().equalsIgnoreCase("wonderwoman") || userInformation.getUsername().equalsIgnoreCase("wonderman"))) {
            result.put("success", false);
            result.put("message", "You don't have permission for this operation");
            return result;
        }

        if (reassignGrievance == null) {
            result.put("success", false);
            result.put("message", "Invalid request.");
            return result;
        }

        if (reassignGrievance.getCaseId() == null) {
            result.put("success", false);
            result.put("message", "Grievance ID is required");
            return result;
        }

        if (reassignGrievance.getTrackingNumber() == null) {
            result.put("success", false);
            result.put("message", "Grievance tracking number is required");
            return result;
        }

        if (reassignGrievance.getOfficeId() == null) {
            result.put("success", false);
            result.put("message", "Grievance office id is required");
            return result;
        }

        Grievance grievanceEO = grievanceDAO.findOne(reassignGrievance.getCaseId());
        if (grievanceEO == null) {
            result.put("success", false);
            result.put("message", "Grievance not found with id:"+reassignGrievance.getCaseId());
            return result;
        }

        if (!grievanceEO.getOfficeId().equals(reassignGrievance.getOfficeId())) {
            result.put("success", false);
            result.put("message", "Grievance not found with office id:"+reassignGrievance.getOfficeId());
            return result;
        }

        if (!grievanceEO.getTrackingNumber().contains(reassignGrievance.getTrackingNumber())) {
            result.put("success", false);
            result.put("message", "Grievance not found with tracking number:"+reassignGrievance.getTrackingNumber());
            return result;
        }


        OfficesGRO officesGRO = officesGroService.findOfficesGroByOfficeId(reassignGrievance.getOfficeId());
        if (officesGRO == null || officesGRO.getGroOfficeId() == null) {
            result.put("success", false);
            result.put("message", "Office is not correctly setup");
            return result;
        }

        String sql = "select max(id) from complaint_movements where complaint_id=:complaintId ";
        Map<String, Object> params = new HashMap<>();
        params.put("complaintId", reassignGrievance.getCaseId());

        Long maxMovement = entityManager.findMaxId(sql, params);
        if (maxMovement == null || maxMovement == 0L) {
            result.put("success", false);
            result.put("message", "Internal service error.");
            return result;
        }
        sql = "update complaint_movements set is_current=0 where complaint_id=:complaintId and id<>:id ";
        params.put("id", maxMovement);
        params.put("complaintId", reassignGrievance.getCaseId());

        if (grievanceEO.getGrievanceCurrentStatus().name().contains("APPEAL")) {
            try {
                entityManager.updateByQuery(sql, params);
                sql = "update complaint_movements set action ='APPEAL_STATEMENT_ANSWERED', to_office_unit_organogram_id=:orgId, to_office_id=:toOfficeId, is_current=1, is_cc=0,current_status='APPEAL_STATEMENT_ANSWERED', assigned_role='AO' where id = :id";

                params.remove("complaintId");
                params.put("orgId", officesGRO.getAppealOfficerOfficeUnitOrganogramId());
                params.put("toOfficeId", reassignGrievance.getOfficeId());
                entityManager.updateByQuery(sql, params);

                sql ="update complaints set current_status='APPEAL_STATEMENT_ANSWERED', current_appeal_office_id=:officeId, current_appeal_office_unit_organogram_id=:orgId where id =:id ";
                params.clear();
                params.put("officeId", officesGRO.getAppealOfficeId());
                params.put("orgId", officesGRO.getAppealOfficerOfficeUnitOrganogramId());
                params.put("id", reassignGrievance.getCaseId());
                entityManager.updateByQuery(sql, params);

            } catch (Throwable t) {
                t.printStackTrace();
                result.put("success", false);
                result.put("message", "Internal service error.");
                return result;
            }
        } else {
            try {
                entityManager.updateByQuery(sql, params);
                sql = "update complaint_movements set action ='RETAKE', to_office_unit_organogram_id=:orgId, to_office_id=:toOfficeId, is_current=1, is_cc=0,current_status='STATEMENT_ANSWERED', assigned_role='GRO' where id =:id ";
                params.clear();
                params.put("orgId", officesGRO.getGroOfficeUnitOrganogramId());
                params.put("toOfficeId", officesGRO.getGroOfficeId());
                params.put("id", maxMovement);
                entityManager.updateByQuery(sql, params);

                sql = "update complaints set current_status ='STATEMENT_ANSWERED' where id = :id";
                params.clear();
                params.put("id", reassignGrievance.getCaseId());
                entityManager.updateByQuery(sql, params);
            } catch (Throwable t) {
                t.printStackTrace();
                result.put("success", false);
                result.put("message", "Internal service error.");
                return result;

            }
        }

        result.put("success", true);
        result.put("message", "Operation successfully done!");
        return result;
    }
}
