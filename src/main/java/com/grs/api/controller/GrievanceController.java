package com.grs.api.controller;

import com.grs.api.model.OISFUserType;
import com.grs.api.model.UserInformation;
import com.grs.api.model.UserType;
import com.grs.api.model.request.*;
import com.grs.api.model.response.*;
import com.grs.api.model.response.file.ExistingFileDerivedDTO;
import com.grs.api.model.response.file.FileDerivedDTO;
import com.grs.api.model.response.grievance.GrievanceDTO;
import com.grs.api.model.response.grievance.GrievanceDetailsDTO;
import com.grs.core.config.CaptchaSettings;
import com.grs.core.domain.ServicePair;
import com.grs.core.domain.ServiceType;
import com.grs.core.domain.grs.Grievance;
import com.grs.core.domain.grs.Notification;
import com.grs.core.model.EmployeeOrganogram;
import com.grs.core.model.ListViewType;
import com.grs.core.service.*;
import com.grs.utils.ListViewConditionOnCurrentStatusGenerator;
import com.grs.utils.Utility;
import com.grs.core.dao.SpProgrammeDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * Created by Tanvir on 9/14/2017.
 */
@Slf4j
@RestController
public class GrievanceController {

    @Autowired
    private GrievanceService grievanceService;
    @Autowired
    private OfficeService officeService;
    @Autowired
    private GrievanceForwardingService grievanceForwardingService;
    @Autowired
    private CitizenCharterService citizenCharterService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private ModelViewService modelViewService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ComplainantService complainantService;
    @Autowired
    private AccessControlService accessControlService;
    @Autowired
    private SafetyNetProgramService safetyNetProgramService;

    @Autowired
    private com.grs.core.service.SpProgrammeService spProgrammeService;

    @Autowired
    private SpProgrammeDAO spProgrammeDAO;

    @Autowired
    private CaptchaSettings captchaSettings;


    @RequestMapping(value = "/viewGrievances.do", method = RequestMethod.GET)
    public ModelAndView getViewGrievancesPage(Authentication authentication, Model model, HttpServletRequest request) {
        if (authentication != null) {
            String fragmentFolder = Utility.isUserAnGRSUserOrOthersComplainant(authentication) ? "dashboard" : "grievances";
            String fragmentName = Utility.isUserAnGRSUserOrOthersComplainant(authentication) ? "dashboardCitizen" : "manageGrievances";
            Boolean isBlacklisted = complainantService.isBlacklistedUser(authentication);
            model.addAttribute("isBlacklisted", isBlacklisted);
            return modelViewService.addNecessaryAttributesAndReturnViewPage(model,
                    authentication,
                    request,
                    fragmentFolder,
                    fragmentName,
                    "admin");
        }
        return new ModelAndView("redirect:/error-page");
    }

    @RequestMapping(value = "/viewMyGrievances.do", method = RequestMethod.GET)
    public ModelAndView getMyViewGrievancesPage(Authentication authentication, Model model, HttpServletRequest request) {
        if (authentication != null) {
            String fragmentFolder = "grievances";
            String fragmentName = "viewMyGrievances";
            model.addAttribute("isHoo", Utility.isUserAHOOUser(authentication));
            return modelViewService.addNecessaryAttributesAndReturnViewPage(model,
                    authentication,
                    request,
                    fragmentFolder,
                    fragmentName,
                    "admin");
        }
        return new ModelAndView("redirect:/error-page");
    }

    @RequestMapping(value = "/viewAppealGrievances.do", method = RequestMethod.GET)
    public ModelAndView getViewAppealGrievancesPage(Authentication authentication, Model model, HttpServletRequest request) {
        if (authentication != null) {
            String fragmentFolder = "grievances";
            String fragmentName = "manageAppealGrievances";
            return modelViewService.addNecessaryAttributesAndReturnViewPage(model,
                    authentication,
                    request,
                    fragmentFolder,
                    fragmentName,
                    "admin");
        }
        return new ModelAndView("redirect:/error-page");
    }

    @RequestMapping(value = "/searchGrievances.do", method = RequestMethod.GET, params = "id")
    public ModelAndView searchGrievancesPage(
            Authentication authentication,
            Model model,
            HttpServletRequest request,
            @RequestParam Long id,
            @RequestParam(required = false) String tab, // Add tab as an optional parameter
            @RequestParam(required = false) Integer page, // Add page as an optional parameter
            @RequestParam(required = false) Integer size // Add size as an optional parameter
    ) {
        if (authentication != null) {
            if(!accessControlService.hasPermissionToViewGrievanceDetails(authentication, id)) {
                return new ModelAndView("redirect:/error-page");
            }
            Boolean isGrsUser = Utility.isUserAnGRSUser(authentication);
            Boolean isOthersComplainant = Utility.isUserAnOthersComplainant(authentication);
            Boolean isGROUser = Utility.isUserAnGROUser(authentication);
            Boolean appealButtonFlag = this.grievanceService.appealActivationFlag(id);
            Boolean isOISFComplainant = this.grievanceService.isOISFComplainant(authentication, id);
            Boolean serviceIsNull = this.grievanceService.serviceIsNull(id);
            Boolean isNagorik = this.grievanceService.isNagorikTypeGrievance(id);
            Boolean isBlacklisted = complainantService.isBlacklistedUser(authentication);
            Boolean isFeedbackEnabled = this.grievanceService.isFeedbackEnabled(id);
            Boolean isAnonymousUser = this.grievanceService.isSubmittedByAnonymousUser(id);
            Boolean soAppealOption = null;
            Boolean isComplainantBlacklisted = false;
            Boolean canRetakeComplaint = false;
            if (Utility.isServiceOfficer(authentication)) {
                soAppealOption = this.grievanceService.soAppealActivationFlag(id);
            }
            if(!isGrsUser) {
                isComplainantBlacklisted = grievanceService.isComplainantBlackListedByGrievanceId(id);
                canRetakeComplaint = grievanceForwardingService.getComplaintRetakeFlag(id, authentication);
            }
            List<FeedbackResponseDTO> feedbacks = this.grievanceService.getFeedbacks(id);
            model = grievanceService.addFileSettingsAttributesToModel(model);
            model.addAttribute("grsUser", isGrsUser);
            model.addAttribute("isOthersComplainant", isOthersComplainant);
            model.addAttribute("isGRO", isGROUser);
            model.addAttribute("appealButtonFlag", appealButtonFlag);
            model.addAttribute("isOISFComplainant", isOISFComplainant);
            model.addAttribute("serviceIsNull", serviceIsNull);
            model.addAttribute("isNagorik", isNagorik);
            model.addAttribute("isBlacklisted", isBlacklisted);
            model.addAttribute("isAnonymousUser", isAnonymousUser);
            model.addAttribute("isFeedbackEnabled", isFeedbackEnabled);
            model.addAttribute("feedbacks", feedbacks);
            model.addAttribute("soAppealOption", soAppealOption);
            model.addAttribute("isComplainantBlacklisted", isComplainantBlacklisted);
            model.addAttribute("canRetakeComplaint", canRetakeComplaint);
            model.addAttribute("grievanceFileCount", this.grievanceService.getCountOfAttachedFiles(id));
            model.addAttribute("grievanceForwardingFileCount", this.grievanceForwardingService.getCountOfComplaintMovementAttachedFiles(authentication, id));
            Boolean reviveFlag = this.grievanceService.isComplaintRevivable(id, authentication);
            model.addAttribute("reviveFlag", reviveFlag);
            this.grievanceForwardingService.updateForwardSeenStatus(Utility.extractUserInformationFromAuthentication(authentication), id);
            model.addAttribute("searchableOffices", officeService.getGrsEnabledOfficeSearchingData());

            // Add tab, page, and size parameters to the model so they can be passed back
            model.addAttribute("currentTab", tab);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);

            return modelViewService.addNecessaryAttributesAndReturnViewPage(model,
                    authentication,
                    request,
                    "grievances",
                    "searchGrievance",
                    "admin");
        }
        return new ModelAndView("redirect:/error-page");
    }

    @RequestMapping(value = "/viewGrievances.do", method = RequestMethod.GET, params = "id")
    public ModelAndView getViewGrievancesPage(
            Authentication authentication,
            Model model,
            HttpServletRequest request,
            @RequestParam Long id,
            @RequestParam(required = false) String tab, // Add tab as an optional parameter
            @RequestParam(required = false) Integer page, // Add page as an optional parameter
            @RequestParam(required = false) Integer size // Add size as an optional parameter
    ) {
        if (authentication != null) {
            if(!accessControlService.hasPermissionToViewGrievanceDetails(authentication, id)) {
                return new ModelAndView("redirect:/error-page");
            }
            Boolean isGrsUser = Utility.isUserAnGRSUser(authentication);
            Boolean isOthersComplainant = Utility.isUserAnOthersComplainant(authentication);
            Boolean isGROUser = Utility.isUserAnGROUser(authentication);
            Boolean appealButtonFlag = this.grievanceService.appealActivationFlag(id);
            Boolean isOISFComplainant = this.grievanceService.isOISFComplainant(authentication, id);
            Boolean serviceIsNull = this.grievanceService.serviceIsNull(id);
            Boolean isNagorik = this.grievanceService.isNagorikTypeGrievance(id);
            Boolean isBlacklisted = complainantService.isBlacklistedUser(authentication);
            Boolean isFeedbackEnabled = this.grievanceService.isFeedbackEnabled(id);
            Boolean isAnonymousUser = this.grievanceService.isSubmittedByAnonymousUser(id);
            Boolean soAppealOption = null;
            Boolean isComplainantBlacklisted = false;
            Boolean canRetakeComplaint = false;
            if (Utility.isServiceOfficer(authentication)) {
                soAppealOption = this.grievanceService.soAppealActivationFlag(id);
            }
            if(!isGrsUser) {
                isComplainantBlacklisted = grievanceService.isComplainantBlackListedByGrievanceId(id);
                canRetakeComplaint = grievanceForwardingService.getComplaintRetakeFlag(id, authentication);
            }
            List<FeedbackResponseDTO> feedbacks = this.grievanceService.getFeedbacks(id);
            model = grievanceService.addFileSettingsAttributesToModel(model);
            model.addAttribute("grsUser", isGrsUser);
            model.addAttribute("isOthersComplainant", isOthersComplainant);
            model.addAttribute("isGRO", isGROUser);
            model.addAttribute("appealButtonFlag", appealButtonFlag);
            model.addAttribute("isOISFComplainant", isOISFComplainant);
            model.addAttribute("serviceIsNull", serviceIsNull);
            model.addAttribute("isNagorik", isNagorik);
            model.addAttribute("isBlacklisted", isBlacklisted);
            model.addAttribute("isAnonymousUser", isAnonymousUser);
            model.addAttribute("isFeedbackEnabled", isFeedbackEnabled);
            model.addAttribute("feedbacks", feedbacks);
            model.addAttribute("soAppealOption", soAppealOption);
            model.addAttribute("isComplainantBlacklisted", isComplainantBlacklisted);
            model.addAttribute("canRetakeComplaint", canRetakeComplaint);
            model.addAttribute("grievanceFileCount", this.grievanceService.getCountOfAttachedFiles(id));
            model.addAttribute("grievanceForwardingFileCount", this.grievanceForwardingService.getCountOfComplaintMovementAttachedFiles(authentication, id));
            Boolean reviveFlag = this.grievanceService.isComplaintRevivable(id, authentication);
            model.addAttribute("reviveFlag", reviveFlag);
            this.grievanceForwardingService.updateForwardSeenStatus(Utility.extractUserInformationFromAuthentication(authentication), id);
            model.addAttribute("searchableOffices", officeService.getGrsEnabledOfficeSearchingData());

            // Add tab, page, and size parameters to the model so they can be passed back
            model.addAttribute("currentTab", tab);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);

            return modelViewService.addNecessaryAttributesAndReturnViewPage(model,
                    authentication,
                    request,
                    "grievances",
                    "viewGrievancesDetails",
                    "admin");
        }
        return new ModelAndView("redirect:/error-page");
    }

    @RequestMapping(value = "/viewAppealGrievances.do", method = RequestMethod.GET, params = "id")
    public ModelAndView getViewAppealGrievancesPage(Authentication authentication, Model model, HttpServletRequest request, @RequestParam Long id) {
        if (authentication != null) {
            Boolean isGrsUser = Utility.isUserAnGRSUser(authentication);
            Boolean isOthersComplainant = Utility.isUserAnOthersComplainant(authentication);
            Boolean isGROUser = Utility.isUserAnGROUser(authentication);
            Boolean serviceIsNull = this.grievanceService.serviceIsNull(id);
            Boolean isFeedbackEnabled = this.grievanceService.isFeedbackEnabled(id);
            List<FeedbackResponseDTO> feedbacks = this.grievanceService.getFeedbacks(id);
            model = grievanceService.addFileSettingsAttributesToModel(model);
            model.addAttribute("isGRO", isGROUser);
            model.addAttribute("serviceIsNull", serviceIsNull);
            model.addAttribute("isFeedbackEnabled", isFeedbackEnabled);
            model.addAttribute("feedbacks", feedbacks);

            Boolean appealButtonFlag = this.grievanceService.appealActivationFlag(id);
            Boolean isOISFComplainant = this.grievanceService.isOISFComplainant(authentication, id);
            Boolean isNagorik = this.grievanceService.isNagorikTypeGrievance(id);
            Boolean isBlacklisted = complainantService.isBlacklistedUser(authentication);
            Boolean isAnonymousUser = this.grievanceService.isSubmittedByAnonymousUser(id);
            Boolean soAppealOption = null;
            Boolean isComplainantBlacklisted = false;
            Boolean canRetakeComplaint = false;
            if (Utility.isServiceOfficer(authentication)) {
                soAppealOption = this.grievanceService.soAppealActivationFlag(id);
            }
            if(!isGrsUser) {
                isComplainantBlacklisted = grievanceService.isComplainantBlackListedByGrievanceId(id);
                canRetakeComplaint = grievanceForwardingService.getComplaintRetakeFlag(id, authentication);
            }
            model = grievanceService.addFileSettingsAttributesToModel(model);
            model.addAttribute("grsUser", isGrsUser);
            model.addAttribute("isOthersComplainant", isOthersComplainant);
            model.addAttribute("isGRO", isGROUser);
            model.addAttribute("appealButtonFlag", appealButtonFlag);
            model.addAttribute("isOISFComplainant", isOISFComplainant);
            model.addAttribute("serviceIsNull", serviceIsNull);
            model.addAttribute("isNagorik", isNagorik);
            model.addAttribute("isBlacklisted", isBlacklisted);
            model.addAttribute("isAnonymousUser", isAnonymousUser);
            model.addAttribute("isFeedbackEnabled", isFeedbackEnabled);
            model.addAttribute("feedbacks", feedbacks);
            model.addAttribute("soAppealOption", soAppealOption);
            model.addAttribute("isComplainantBlacklisted", isComplainantBlacklisted);
            model.addAttribute("canRetakeComplaint", canRetakeComplaint);
            model.addAttribute("grievanceFileCount", this.grievanceService.getCountOfAttachedFiles(id));
            model.addAttribute("grievanceForwardingFileCount", this.grievanceForwardingService.getCountOfComplaintMovementAttachedFiles(authentication, id));
            Boolean reviveFlag = this.grievanceService.isComplaintRevivable(id, authentication);
            model.addAttribute("reviveFlag", reviveFlag);
            this.grievanceForwardingService.updateForwardSeenStatus(Utility.extractUserInformationFromAuthentication(authentication), id);
            model.addAttribute("searchableOffices", officeService.getGrsEnabledOfficeSearchingData());

            this.grievanceForwardingService.updateForwardSeenStatus(Utility.extractUserInformationFromAuthentication(authentication), id);
            return modelViewService.addNecessaryAttributesAndReturnViewPage(model,
                    authentication,
                    request,
                    "grievances",
                    "viewAppealGrievancesDetails",
                    "admin");
        }
        return new ModelAndView("redirect:/error-page");
    }

    @RequestMapping(value = "/addPublicGrievances.do", method = RequestMethod.GET)
    public ModelAndView getAddGrievancesPage(Authentication authentication, Model model, HttpServletRequest request,
                                             @RequestParam(value = "serviceInfo", defaultValue = "-1") Optional<String> serviceInfo
    ) throws IOException {
        model.addAttribute("grievanceDTO", new GrievanceRequestDTO());
        if (!serviceInfo.get().equals("-1")) {
            ServiceRelatedInfoRequestDTO serviceRelatedInfoRequestDTO = this.grievanceService.convertFromBase64encodedString(serviceInfo.get());
            model.addAttribute("serviceId", serviceRelatedInfoRequestDTO.getServiceId());
            model.addAttribute("officeId", serviceRelatedInfoRequestDTO.getOfficeId());
            model.addAttribute("officeName", serviceRelatedInfoRequestDTO.getOfficeName());
            model.addAttribute("serviceName", serviceRelatedInfoRequestDTO.getServiceName());
        }
        if (authentication != null) {
            UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
            boolean isComplainant = userInformation.getUserType().equals(UserType.COMPLAINANT);
            List<Long> blackLiterOffice = new ArrayList<>();
            if(!isComplainant) {
                return new ModelAndView("redirect:/error-page");
            }

            if(complainantService.isBlacklistedUser(authentication)){
                blackLiterOffice = complainantService.findBlacklistedOffices(userInformation.getUserId());
            }
            model.addAttribute("grievanceDTO", new GrievanceRequestDTO());
            List<ServicePair> servicePairs = citizenCharterService.getDefaultAllowedServiceTypes(request);
            model = grievanceService.addFileSettingsAttributesToModel(model);
            model.addAttribute("servicePairs", servicePairs);
            model.addAttribute("searchableOffices", officeService.getGrsEnabledOfficeSearchingData());
            model.addAttribute("safetyNetPrograms", safetyNetProgramService.getSafetyNetPrograms());
            model.addAttribute("complaintType", ServiceType.NAGORIK.name());

            model.addAttribute("blacklistInOfficeId", blackLiterOffice);

            return modelViewService.returnViewsForComplainWithoutLogin(
                    authentication,
                    model,
                    request,
                    "grievances",
                    "addGrievancesForOthers",
                    "adminForOthers");

        }
        return new ModelAndView("redirect:/login?a=0&redirectUrl=addPublicGrievances.do?serviceInfo=" + serviceInfo.get());
    }


    @RequestMapping(value = "/addSelfMotivatedGrievances.do", method = RequestMethod.GET)
    public ModelAndView getAddSelfMotivatedGrievancesPage(Authentication authentication, Model model, HttpServletRequest request) throws IOException {
        model.addAttribute("grievanceDTO", new GrievanceRequestDTO());

        if (authentication != null) {
            UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
            if(userInformation.getUserType().equals(UserType.COMPLAINANT)) {
                return new ModelAndView("redirect:/error-page");
            }

            model.addAttribute("grievanceDTO", new GrievanceRequestDTO());
            List<ServicePair> servicePairs = citizenCharterService.getDefaultAllowedServiceTypes(request);
            model = grievanceService.addFileSettingsAttributesToModel(model);
            model.addAttribute("servicePairs", servicePairs);
            model.addAttribute("searchableOffices", officeService.getGrsEnabledOfficeSearchingData());
            model.addAttribute("safetyNetPrograms", safetyNetProgramService.getSafetyNetPrograms());
            model.addAttribute("complaintType", ServiceType.STAFF.name());

            List<Long> blackLiterOffice = new ArrayList<>();

            model.addAttribute("blacklistInOfficeId", blackLiterOffice);

            return modelViewService.returnViewsForComplainWithoutLogin(
                    authentication,
                    model,
                    request,
                    "grievances",
                    "addGrievancesForOthers",
                    "adminForOthers");

        }
        return new ModelAndView("redirect:/login?a=0&redirectUrl=addSelfMotivatedGrievances.do");
    }

    @RequestMapping(value = "/addStaffGrievances.do", method = RequestMethod.GET)
    public ModelAndView getAddStaffGrievancesPage(Authentication authentication, Model model, HttpServletRequest request) throws IOException {
        model.addAttribute("grievanceDTO", new GrievanceRequestDTO());
        if (authentication != null) {
            UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
            if(!userInformation.getUserType().equals(UserType.OISF_USER)) {
                return new ModelAndView("redirect:/error-page");
            }

            model.addAttribute("grievanceDTO", new GrievanceRequestDTO());
            List<ServicePair> servicePairs = citizenCharterService.getDefaultAllowedServiceTypes(request);
            model = grievanceService.addFileSettingsAttributesToModel(model);
            model.addAttribute("servicePairs", servicePairs);
            model.addAttribute("searchableOffices", officeService.getGrsEnabledOfficeSearchingData());
            model.addAttribute("safetyNetPrograms", safetyNetProgramService.getSafetyNetPrograms());
            model.addAttribute("complaintType", ServiceType.STAFF.name());

            List<Long> blackLiterOffice = new ArrayList<>();

            model.addAttribute("blacklistInOfficeId", blackLiterOffice);

            return modelViewService.returnViewsForComplainWithoutLogin(
                    authentication,
                    model,
                    request,
                    "grievances",
                    "addGrievancesForOthers",
                    "adminForOthers");

        }
        return new ModelAndView("redirect:/error-page");
    }

    @RequestMapping(value = "/addOfficialGrievances.do", method = RequestMethod.GET)
    public ModelAndView getAddOfficialGrievancesPage(Authentication authentication, Model model, HttpServletRequest request) throws IOException {
        model.addAttribute("grievanceDTO", new GrievanceRequestDTO());
        if (authentication != null) {
            if(!Utility.isUserAHOOUser(authentication)) {
                return new ModelAndView("redirect:/error-page");
            }


            model.addAttribute("grievanceDTO", new GrievanceRequestDTO());
            List<ServicePair> servicePairs = citizenCharterService.getDefaultAllowedServiceTypes(request);
            model = grievanceService.addFileSettingsAttributesToModel(model);
            model.addAttribute("servicePairs", servicePairs);
            model.addAttribute("searchableOffices", officeService.getGrsEnabledOfficeSearchingData());
            model.addAttribute("safetyNetPrograms", safetyNetProgramService.getSafetyNetPrograms());
            model.addAttribute("complaintType", ServiceType.DAPTORIK.name());

            List<Long> blackLiterOffice = new ArrayList<>();

            model.addAttribute("blacklistInOfficeId", blackLiterOffice);

            return modelViewService.returnViewsForComplainWithoutLogin(
                    authentication,
                    model,
                    request,
                    "grievances",
                    "addGrievancesForOthers",
                    "adminForOthers");

        }
        return new ModelAndView("redirect:/error-page");
    }

    @RequestMapping(value = "/anonymousAddGrievance.do", method = RequestMethod.GET)
    public ModelAndView getAnonymousAddGrievancePage(HttpServletRequest request, Authentication authentication, Model model) {

        model.addAttribute("anonymousForOthers", true);

        String htmlFragment = authentication == null ? "addGrievancesWithoutLogin" : "addGrievancesForOthers";

        model.addAttribute("grievanceDTO", new GrievanceRequestDTO());
        List<ServicePair> servicePairs = citizenCharterService.getDefaultAllowedServiceTypes(request);
        model = grievanceService.addFileSettingsAttributesToModel(model);
        model.addAttribute("servicePairs", servicePairs);
        model.addAttribute("searchableOffices", officeService.getGrsEnabledOfficeSearchingData());
        model.addAttribute("safetyNetPrograms", safetyNetProgramService.getSafetyNetPrograms());
        model.addAttribute("complaintType", ServiceType.NAGORIK.name());

        List<Long> blackLiterOffice = new ArrayList<>();

        model.addAttribute("blacklistInOfficeId", blackLiterOffice);

        return modelViewService.returnViewsForComplainWithoutLogin(
                authentication,
                model,
                request,
                "grievances",
                htmlFragment,
                "adminForOthers");
    }


    @RequestMapping(value = "/complainWithoutLogin.do", method = RequestMethod.GET)
    public ModelAndView complainWithoutLoginPage(HttpServletRequest request, Authentication authentication, Model model) {
        String sn = request.getParameter("sn");
        model.addAttribute("grievanceDTO", new GrievanceRequestDTO());
        List<ServicePair> servicePairs = citizenCharterService.getDefaultAllowedServiceTypes(request);
        model = grievanceService.addFileSettingsAttributesToModel(model);
        model.addAttribute("servicePairs", servicePairs);
        model.addAttribute("searchableOffices", officeService.getGrsEnabledOfficeSearchingData());
        model.addAttribute("sn", sn);
        model.addAttribute("safetyNetPrograms", safetyNetProgramService.getSafetyNetPrograms());
        model.addAttribute("complaintType", ServiceType.NAGORIK.name());
        model.addAttribute("captchaSettings", captchaSettings);

        List<Long> blackLiterOffice = new ArrayList<>();

        model.addAttribute("blacklistInOfficeId", blackLiterOffice);

        return modelViewService.returnViewsForComplainWithoutLogin(
                authentication,
                model,
                request,
                "grievances",
                "addGrievancesWithoutLogin",
                "adminWithoutLogin");

    }

    @RequestMapping(value = "/spProgrammeList/get", method = RequestMethod.GET)
    public List<com.grs.core.domain.grs.SpProgramme> getSpProgrammeList() {
        List<com.grs.core.domain.grs.SpProgramme> spProgrammeList = spProgrammeService.findAllByStatusAndOfficeIdNotNull();
        return spProgrammeList;
    }

    @RequestMapping(value = "/spProgrammeList/get/all", method = RequestMethod.GET)
    public List<com.grs.core.domain.grs.SpProgramme> getAllSpProgrammeList() {
        List<com.grs.core.domain.grs.SpProgramme> spProgrammeList = spProgrammeService.findAll();
        return spProgrammeList;
    }

    @RequestMapping(value = "/spProgramme/get/{programId}", method = RequestMethod.GET)
    public SpProgramGroDto getSpProgrammeOfficeNameBn(@PathVariable("programId") Integer programId) {
        try {
            SpProgramGroDto dto = new SpProgramGroDto();
            dto.setOfficeName(officeService.getOffice(spProgrammeService.getSpProgramme(programId).
                    getOfficeId()).getNameBangla());
            return dto;
        } catch (Exception ex) {
            throw ex;
        }
    }


    @RequestMapping(value = "/complainForOthers.do", method = RequestMethod.GET)
    public ModelAndView complainForOthersPage(HttpServletRequest request, Authentication authentication, Model model) {
        model.addAttribute("grievanceDTO", new GrievanceRequestDTO());
        List<ServicePair> servicePairs = citizenCharterService.getDefaultAllowedServiceTypes(request);
        model = grievanceService.addFileSettingsAttributesToModel(model);
        model.addAttribute("servicePairs", servicePairs);
        model.addAttribute("searchableOffices", officeService.getGrsEnabledOfficeSearchingData());
        model.addAttribute("safetyNetPrograms", safetyNetProgramService.getSafetyNetPrograms());
        model.addAttribute("complaintType", ServiceType.NAGORIK.name());

        List<Long> blackLiterOffice = new ArrayList<>();

        model.addAttribute("blacklistInOfficeId", blackLiterOffice);

        return modelViewService.returnViewsForComplainWithoutLogin(
                authentication,
                model,
                request,
                "grievances",
                "addGrievancesForOthers",
                "adminForOthers");

    }

    @RequestMapping(value = "/safetynetgrievancedata.do", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public com.grs.api.model.response.grievance.SafetyNetGrievanceSummaryListDto getSafetyNetGrievanceList
            (@RequestBody com.grs.api.model.request.SafetyNetGrievanceSummaryRequest request, Model model) {
        com.grs.api.model.response.grievance.SafetyNetGrievanceSummaryListDto safetyNetGrievanceSummaryListDto
                = this.grievanceService.getSafetyNetGrievanceSummary(request);
        model.addAttribute("resultList", safetyNetGrievanceSummaryListDto.safetyNetGrievanceSummaryList);
        return safetyNetGrievanceSummaryListDto;
    }

    @RequestMapping(value = "/getSpProgrammeGroDetailList", method = RequestMethod.GET)
    public List<SpProgramGroDto> getSpProgramGroDetailsList(@PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        return spProgrammeService.getSpProgramGroDetailsList();
    }

    @RequestMapping(value = "/api/grievance", method = RequestMethod.GET)
    public Page<GrievanceDTO> getGrievances(Authentication authentication, @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return grievanceService.findGrievancesByUsers(userInformation, pageable);
    }

    @RequestMapping(value = "/api/grievance", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public WeakHashMap<String, String> addGrievance(Authentication authentication, @RequestBody GrievanceRequestDTO grievanceRequestDTO) throws Exception {
        return this.grievanceService.addGrievance(authentication, grievanceRequestDTO);
    }

    @RequestMapping(value = "/api/grievanceWithoutLogin", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public WeakHashMap<String, Object> addGrievanceWithoutLogin(Authentication authentication, @RequestBody GrievanceWithoutLoginRequestDTO grievanceRequestDTO) throws Exception {
        return this.grievanceService.addGrievanceWithoutLogin(authentication, grievanceRequestDTO);
    }

    @RequestMapping(value = "/api/grievanceForOthers", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public WeakHashMap<String, Object> addGrievanceForOthers(Authentication authentication, @RequestBody GrievanceWithoutLoginRequestDTO grievanceRequestDTO) throws Exception {
        return this.grievanceService.addGrievanceForOthers(authentication, grievanceRequestDTO);
    }

    @RequestMapping(value = "/api/grievance/safety-net-category-change", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public WeakHashMap<String, Object> changeSafetyNetCategory(Authentication authentication, @RequestBody ChangeSafetyNetCategoryDTO request) throws Exception {
        return this.grievanceService.changeSafetyNetCategory(authentication, request);
    }

    @RequestMapping(value = "/api/grievance/{id}", method = RequestMethod.GET)
    public GrievanceDetailsDTO getGrievanceDetails(Authentication authentication, @PathVariable("id") Long id) {
        return grievanceService.getGrievanceDetailsWithMenuOptions(authentication, id);
    }

    @RequestMapping(value = "/api/grievance/{id}/files", method = RequestMethod.GET)
    public List<FileDerivedDTO> getGrievancesFiles(@PathVariable("id") Long id){
        return grievanceService.getGrievancesFiles(id);
    }

    @RequestMapping(value = "/api/grievance/register/{office_id}", method = RequestMethod.GET)
    public Page<RegisterDTO> getIncomingRegister(Authentication authentication,
                                                 @PathVariable("office_id") Long officeId,
                                                 @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        return grievanceService.getGrievanceByOfficeID(pageable, authentication, officeId);
    }

    @RequestMapping(value = "/api/grievance/inbox/cell", method = RequestMethod.GET)
    public Page<GrievanceDTO> getCellInboxGrievances(Authentication authentication, @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return grievanceService.getInboxGrievanceList(userInformation, pageable);
    }

    @RequestMapping(value = "/api/grievance/inbox", method = RequestMethod.GET)
    public Page<GrievanceDTO> getInboxGrievances(Authentication authentication, @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return grievanceService.getInboxGrievanceList(userInformation, pageable);
    }

    @RequestMapping(value = "/api/grievance/outbox", method = RequestMethod.GET)
    public Page<GrievanceDTO> getOutboxGrievances(Authentication authentication, @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return grievanceService.getOutboxGrievance(userInformation, pageable);
    }

    @RequestMapping(value = "/api/grievance/cc", method = RequestMethod.GET)
    public Page<GrievanceDTO> getCCGrievances(Authentication authentication, @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return grievanceService.getCCGrievance(userInformation, pageable);
    }

    @RequestMapping(value = "/api/grievance/forwarded", method = RequestMethod.GET)
    public Page<GrievanceDTO> getRejectedGrievances(Authentication authentication, @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return grievanceService.getForwardedGrievances(userInformation, pageable);
    }

    @RequestMapping(value = "/api/grievance/closed", method = RequestMethod.GET)
    public Page<GrievanceDTO> getClosedGrievances(Authentication authentication, @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return grievanceService.getClosedGrievances(userInformation, pageable);
    }

    @RequestMapping(value = "/api/grievance/appeal/inbox", method = RequestMethod.GET)
    public Page<GrievanceDTO> getInboxAppealGrievances(Authentication authentication, @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return grievanceService.getInboxAppealGrievanceList(userInformation, pageable);
    }

    @RequestMapping(value = "/api/grievance/appeal/outbox", method = RequestMethod.GET)
    public Page<GrievanceDTO> getOutboxAppealGrievances(Authentication authentication, @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return grievanceService.getOutboxAppealGrievanceList(userInformation, pageable);
    }

    @RequestMapping(value = "/api/employee/{employee_record_id}", method = RequestMethod.GET)
    public EmployeeRecordDTO getEmployeeRecord(@PathVariable("employee_record_id") Long id) {
        return grievanceService.getEmployeeRecord(id);
    }

    @RequestMapping(value = "/api/grievance/appeal/closed", method = RequestMethod.GET)
    public Page<GrievanceDTO> getClosedAppealGrievances(Authentication authentication, @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return this.grievanceService.getClosedAppealGrievances(userInformation, pageable);
    }

    @RequestMapping(value = "/api/grievance/forward/latest/gro/{grievanceId}", method = RequestMethod.GET)
    public EmployeeOrganogram getGroOfRecentmostGrievanceForwarding(@PathVariable("grievanceId") Long grievanceId) {
        return this.grievanceForwardingService.getGroOfRecentmostGrievanceForwarding(grievanceId);
    }

    @RequestMapping(value = "/api/grievance/gro/{grievanceId}", method = RequestMethod.GET)
    public EmployeeOrganogramDTO getGroOfGrievance(@PathVariable("grievanceId") Long grievanceId) {
        return this.grievanceService.getGroOfGrievance(grievanceId);
    }

    @RequestMapping(value = "api/grievance/so/{grievanceId}")
    public EmployeeOrganogramDTO getSOOfGrievance(@PathVariable("grievanceId") Long grievanceId) {
        return this.grievanceService.getSODetail(grievanceId);
    }

    @RequestMapping(value = "api/grievance/{listType}/search", method = RequestMethod.GET)
    public Page<GrievanceDTO> searchNormalGrievances(Authentication authentication,
                                                     @PathVariable(value = "listType") String listType,
                                                     @RequestParam(value = "value") String value,
                                                     @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        ListViewConditionOnCurrentStatusGenerator viewConditionOnCurrentStatusGenerator = new ListViewConditionOnCurrentStatusGenerator();
        ListViewType listViewType = viewConditionOnCurrentStatusGenerator.getNormalListTypeByString(listType);
        return this.grievanceService.getListViewWithSearching(userInformation, value, listViewType, pageable);
    }

    @RequestMapping(value = "api/grievance/appeal/{listType}/search", method = RequestMethod.GET)
    public Page<GrievanceDTO> searchAppealGrievances(Authentication authentication,
                                                     @PathVariable(value = "listType") String listType,
                                                     @RequestParam(value = "value") String value,
                                                     @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        ListViewConditionOnCurrentStatusGenerator viewConditionOnCurrentStatusGenerator = new ListViewConditionOnCurrentStatusGenerator();
        ListViewType listViewType = viewConditionOnCurrentStatusGenerator.getAppealListTypeByString(listType);
        return this.grievanceService.getListViewWithSearching(userInformation, value, listViewType, pageable);
    }


    @RequestMapping(value = "/api/grievance/expired", method = RequestMethod.GET)
    public Page<GrievanceDTO> getExpiredGrievances(Authentication authentication, @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return grievanceService.getExpiredGrievances(userInformation, pageable);
    }

    @RequestMapping(value = "/api/grievance/appeal/expired", method = RequestMethod.GET)
    public Page<GrievanceDTO> getExpiredAppealGrievances(Authentication authentication, @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return grievanceService.getAppealExpiredGrievances(userInformation, pageable);
    }

    @RequestMapping(value = "/api/grievance/status", method = RequestMethod.GET)
    public Object getStatusOfGrievance(@RequestParam("trackingNumber") String trackingNumber, @RequestParam("phoneNumber") String phoneNumber) {
        return this.grievanceService.getStatusOfGrievance(trackingNumber, phoneNumber);
    }

    @RequestMapping(value = "/api/grievance/existingfiles/{grievanceId}", method = RequestMethod.GET)
    public List<ExistingFileDerivedDTO> getStatusOfGrievance(Authentication authentication, @PathVariable("grievanceId") Long grievanceId) {
        return this.grievanceForwardingService.getAllFilesList(grievanceId, authentication);
    }

    @RequestMapping(value = "/api/grievances/{complainantId}", method = RequestMethod.GET)
    public List<GrievanceDTO> getGrievancesByComplainantId(Authentication authentication, @PathVariable("complainantId") Long complainantId) {
        return this.grievanceService.getGrievancesByComplainantId(complainantId);
    }

    @RequestMapping(value = "/api/grievance/provide-nudge", method = RequestMethod.POST)
    public GenericResponse provideNudgeOfAGrievance(Authentication authentication, @RequestBody GrievanceForwardingMessageDTO forwardingMessageDTO) {
        return this.grievanceForwardingService.provideNudgeAgainstGrievance(forwardingMessageDTO, authentication);
    }

    @RequestMapping(value = "api/grievance/users/{grievanceId}", method = RequestMethod.GET)
    public List<EmployeeDetailsDTO> getRelatedUserToGrievance(@PathVariable("grievanceId") Long grievanceId) {
        return this.grievanceService.getAllRelatedUsers(grievanceId);
    }

    @RequestMapping(value = "/api/grievance/feedback", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse getFeedbackByComplainant(Authentication authentication, @RequestBody FeedbackRequestDTO feedbackRequestDTO) {
        boolean status = true;
        String msg = "";
        try {
            Grievance grievance = this.grievanceService.getFeedbackByComplainant(feedbackRequestDTO);
            msg = this.messageService.getMessage("feedback.saved");
        } catch (Exception ex) {
            status = false;
            msg = this.messageService.getMessage("feedback.saved.failed");
            log.info(ex.getMessage());
        } finally {
            return GenericResponse.builder()
                    .success(status)
                    .message(msg)
                    .build();
        }
    }

    @RequestMapping(value = "/api/notification/update", method = RequestMethod.PUT)
    public NotificationUrlDTO updateNotification(@RequestParam("id") Long id){
        Notification notification = this.notificationService.updateNotification(id);
        return NotificationUrlDTO.builder().url(notification.getUrl()).build();
    }

    @RequestMapping(value = "/api/grievances/{grievanceId}/citizens-charter/{citizensCharterId}", method = RequestMethod.GET)
    public ServiceOriginDTO getCitizensCharterServiceDetailsOfGrievance(Authentication authentication,@PathVariable("grievanceId") Long grievanceId, @PathVariable("citizensCharterId") Long citizensCharterId) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return grievanceService.getCitizenCharterAsServiceOriginDTO(userInformation, citizensCharterId, grievanceId);
    }

    @RequestMapping(value = "addOfflineGrievance.do", method = RequestMethod.GET)
    public ModelAndView getOfflineGrievanceSubmissionPage(Authentication authentication, Model model, HttpServletRequest request){
        if (authentication != null) {
            model.addAttribute("grievanceDTO", new GrievanceRequestDTO());
            List<ServicePair> servicePairs = citizenCharterService.getDefaultAllowedServiceTypes(request);
            model = grievanceService.addFileSettingsAttributesToModel(model);
            model.addAttribute("servicePairs", servicePairs);
            model.addAttribute("searchableOffices", officeService.getGrsEnabledOfficeSearchingData());
            model.addAttribute("safetyNetPrograms", safetyNetProgramService.getSafetyNetPrograms());
            model.addAttribute("complaintType", ServiceType.NAGORIK.name());

            List<Long> blackLiterOffice = new ArrayList<>();

            model.addAttribute("blacklistInOfficeId", blackLiterOffice);

            return modelViewService.returnViewsForComplainWithoutLogin(
                    authentication,
                    model,
                    request,
                    "grievances",
                    "addGrievancesForOthers",
                    "adminForOthers");
        }
        return new ModelAndView("redirect:/error-page");
    }

    @RequestMapping(value = "/registrationFromUpload.do", method = RequestMethod.GET)
    public ModelAndView addModelAndRedirect(Authentication authentication, Model model, HttpServletRequest request) {
        if(authentication!=null){//TODO: check if the user is gro.
            model.addAttribute("fromGrievanceUpload", true);
            return modelViewService.returnViewsForNormalPages(authentication, model, request, "grsRegistrationForm");
        }
        return new ModelAndView("redirect:/error-page");
    }

    @RequestMapping(value = "/addPublicGrievancesByGRO.do", method = RequestMethod.GET)
    public ModelAndView addPublicGrievanceByGRO(Authentication authentication, Model model, HttpServletRequest request, @RequestParam("phone") String phoneNumber){
        if (authentication != null) {
            UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
            if(!userInformation.getUserType().equals(UserType.OISF_USER) ) {
                return new ModelAndView("redirect:/error-page");
            }
            if(!userInformation.getOisfUserType().equals(OISFUserType.GRO) || phoneNumber==null){
                return new ModelAndView("redirect:/error-page");
            }

            model.addAttribute("grievanceDTO", new GrievanceRequestDTO());
            List<ServicePair> servicePairs = citizenCharterService.getDefaultAllowedServiceTypes(request);
            model = grievanceService.addFileSettingsAttributesToModel(model);
            model.addAttribute("servicePairs", servicePairs);
            model.addAttribute("searchableOffices", officeService.getGrsEnabledOfficeSearchingData());
            model.addAttribute("safetyNetPrograms", safetyNetProgramService.getSafetyNetPrograms());
            model.addAttribute("complaintType", ServiceType.NAGORIK.name());

            List<Long> blackLiterOffice = new ArrayList<>();

            model.addAttribute("blacklistInOfficeId", blackLiterOffice);

            return modelViewService.returnViewsForComplainWithoutLogin(
                    authentication,
                    model,
                    request,
                    "grievances",
                    "addGrievancesForOthers",
                    "adminForOthers");
        }
        return new ModelAndView("redirect:/error-page");
    }

    @RequestMapping(value = "/addAnonymousGrievanceGRO.do", method = RequestMethod.GET)
    public ModelAndView getAddAnonymousGrievanceGROPage(HttpServletRequest request, Authentication authentication, Model model) {

        model.addAttribute("anonymousForOthers", true);

        String htmlFragment = authentication == null ? "addGrievancesWithoutLogin" : "addGrievancesForOthers";

        model.addAttribute("grievanceDTO", new GrievanceRequestDTO());
        List<ServicePair> servicePairs = citizenCharterService.getDefaultAllowedServiceTypes(request);
        model = grievanceService.addFileSettingsAttributesToModel(model);
        model.addAttribute("servicePairs", servicePairs);
        model.addAttribute("searchableOffices", officeService.getGrsEnabledOfficeSearchingData());
        model.addAttribute("safetyNetPrograms", safetyNetProgramService.getSafetyNetPrograms());
        model.addAttribute("complaintType", ServiceType.NAGORIK.name());

        List<Long> blackLiterOffice = new ArrayList<>();

        model.addAttribute("blacklistInOfficeId", blackLiterOffice);

        return modelViewService.returnViewsForComplainWithoutLogin(
                authentication,
                model,
                request,
                "grievances",
                htmlFragment,
                "adminForOthers");
    }

    @RequestMapping(value = "/api/unseen/count/{inboxType}", method = RequestMethod.GET)
    public UnseenCountDTO getUnseenCount(Authentication authentication, @PathVariable("inboxType") String inboxType){
       return authentication == null ? UnseenCountDTO.builder().build() : this.grievanceForwardingService.getUnseenCountForUser(authentication, inboxType);
    }

    @RequestMapping(value = "/api/total/count/{inboxType}", method = RequestMethod.GET)
    public UnseenCountDTO getTotalCount(Authentication authentication, @PathVariable("inboxType") String inboxType){
        return authentication == null ? UnseenCountDTO.builder().build() : this.grievanceForwardingService.getTotalCountForUser(authentication, inboxType);
    }

    @RequestMapping(value = "/api/grievance/cell", method = RequestMethod.GET)
    public List<GrievanceDTO> getAllCellGrievance(Authentication authentication){
        if (authentication != null){
            UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
            if (Objects.equals(userInformation.getOfficeInformation().getOfficeId(), 0L)){
                return this.grievanceService.getAllCellComplaints();
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    @RequestMapping(value = "api/user/details", method = RequestMethod.GET)
    public Page<UserDetailsDTO> getPaginatedUsersData(@PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        return complainantService.getPaginatedUsersData(pageable);
    }

    @RequestMapping(value = "/api/grievance/admin", method = RequestMethod.GET)
    public Page<GrievanceAdminDTO> getAdminGrievances(Authentication authentication,
                                                      @RequestParam(value = "officeId", required = false) Long officeId,
                                                      @RequestParam(value = "referenceNumber",required = false) String referenceNumber,
                                                      @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return grievanceService.getAdminGrievances(userInformation,officeId, referenceNumber, pageable);
    }

    @RequestMapping(value = "/api/grievance/re-assign", method = RequestMethod.POST)
    public WeakHashMap<String, Object> reassignGrievance(Authentication authentication, @RequestBody ReassignGrievanceDTO reassignGrievance) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return grievanceService.reassignGrievance(reassignGrievance, userInformation);
    }
}
