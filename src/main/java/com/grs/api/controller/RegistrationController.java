package com.grs.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grs.api.config.security.TokenAuthenticationServiceUtil;
import com.grs.api.model.UserInformation;
import com.grs.api.model.UserType;
import com.grs.api.model.request.BlacklistRequestBodyDTO;
import com.grs.api.model.request.ComplainantDTO;
import com.grs.api.model.request.GrievanceRequestDTO;
import com.grs.api.model.request.PasswordChangeDTO;
import com.grs.api.model.response.BaseObjectDTO;
import com.grs.api.model.response.ComplainantResponseDTO;
import com.grs.api.model.response.GenericResponse;
import com.grs.api.model.response.IdPhoneMessageDTO;
import com.grs.api.model.response.grievance.ComplainantInfoBlacklistReqDTO;
import com.grs.api.model.response.grievance.ComplainantInfoDTO;
import com.grs.core.domain.ServicePair;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.domain.grs.SuperAdmin;
import com.grs.core.service.*;
import com.grs.utils.Constant;
import com.grs.utils.CookieUtil;
import com.grs.utils.StringUtil;
import com.grs.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Created by Acer on 9/19/2017.
 */
@Slf4j
@RestController
public class RegistrationController {
    @Autowired
    private ComplainantService complainantService;
    @Autowired
    private GrsUserService grsUserService;
    @Autowired
    private ShortMessageService shortMessageService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private MessageService messageService;
    @Autowired
    private ModelViewService modelViewService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private CitizenCharterService citizenCharterService;
    @Autowired
    private GrievanceService grievanceService;
    @Autowired
    private OfficeService officeService;

    @RequestMapping(value = "/register.do", method = RequestMethod.GET)
    public ModelAndView registrationPage(HttpServletRequest request, Authentication authentication, Model model) {
        return modelViewService.returnViewsForNormalPages(authentication, model, request, "grsRegistrationForm");
    }

    @RequestMapping(value = "/mobileRegistration.do", method = RequestMethod.GET)
    public ModelAndView mobileRegistrationPage(HttpServletRequest request, Authentication authentication, Model model){
        String languageCode = CookieUtil.getValue(request, "lang");
        model.addAttribute("lang", languageCode);
        model.addAttribute("isLoggedIn", false);
        model.addAttribute("isGrsUser", false);
        model.addAttribute("isOthersComplainant", false);
        model.addAttribute("isOisfUser", false);
        model.addAttribute("isMobileLogin", true);
        return new ModelAndView( "grsRegistrationForm");
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public GenericResponse registerUser(@RequestBody ComplainantDTO complainantDTO) {
        Complainant complainant = this.complainantService.insertComplainant(complainantDTO);
        return GenericResponse
                .builder()
                .message("নিবন্ধন সফল হয়েছে" +
                        "। কিছুক্ষনের মধ্যে আপনাকে স্বয়ংক্রিয়ভাবে লগইন পেজ এ নিয়ে যাওয়া হবে, অনুগ্রহ করে অপেক্ষা করুন।")
                .success(true)
                .build();
    }

    @RequestMapping(value = "/register/gro", method = RequestMethod.POST)
    public IdPhoneMessageDTO registerUserByGRO(@RequestBody ComplainantDTO complainantDTO) {
        Complainant complainant = this.complainantService.insertComplainant(complainantDTO);
        return IdPhoneMessageDTO
                .builder()
                .message("নিবন্ধন সফল হয়েছে" +
                        "। ।")
                .id(complainant.getId())
                .phone(complainant.getPhoneNumber())
                .build();
    }

//    @RequestMapping(value = "/api/profile", method = RequestMethod.PUT)
//    public void updateProfile(Authentication authentication, @RequestBody ComplainantDTO complainantDTO, HttpServletResponse response) {
//        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
//
//        Complainant existingComplainantByPhoneNumber = this.complainantService.findComplainantByPhoneNumber(complainantDTO.getPhoneNumber());
//        Complainant complainantByUserId = this.complainantService.findOne(userInformation.getUserId());
//
//        if (existingComplainantByPhoneNumber != null && existingComplainantByPhoneNumber.getId() != complainantByUserId.getId()) {
//            ObjectMapper objectMapper = new ObjectMapper();
//            try {
//                objectMapper.writeValue(response.getWriter(), GenericResponse
//                        .builder()
//                        .message("এই নম্বর দিয়ে অন্য একটি একাউন্ট বিদ্যমান\n")
//                        .success(false)
//                        .build());
//            } catch (Exception e) {
//                log.info(e.getMessage());
//            }
//            return;
//        }
//
//        Complainant complainant = this.complainantService.updateComplainant(userInformation, complainantDTO);
//        String extraMsg = "";
//        if(complainantDTO.getOldPassword() != null && complainantDTO.getNewPassword() != null){
//            PasswordChangeDTO passwordChangeDTO = PasswordChangeDTO.builder().oldPassword(complainantDTO.getOldPassword()).newPassword(complainantDTO.getNewPassword()).build();
//            Boolean changed = this.complainantService.updateComplainantPassword(userInformation, passwordChangeDTO);
//            if(!changed){
//                extraMsg += "তবে আপনার পাসওয়ার্ড পরিবর্তন করা হয়নি";
//            }
//        }
//        userInformation.setUsername(complainant.getName());
//
//        Set<String> authorities = authentication.getAuthorities()
//                .stream()
//                .map(o -> o.getAuthority())
//                .collect(Collectors.toSet());
//
//        String token = TokenAuthenticationServiceUtil.constuctJwtToken(authentication.getName(),
//                authorities,
//                userInformation);
//
//        Cookie cookie = new Cookie(Constant.HEADER_STRING, token);
//        cookie.setPath("/");
//        cookie.setMaxAge(Constant.COOKIE_EXPIRATION_TIME);
//        response.addCookie(cookie);
//
//        response.addHeader("Content-type","application/json; charset=utf-8");
//
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            objectMapper.writeValue(response.getWriter(), GenericResponse
//                    .builder()
//                    .message("প্রোফাইল আপডেট সফল হয়েছে\n" + extraMsg)
//                    .success(true)
//                    .build());
//        } catch (Exception e) {
//            log.info(e.getMessage());
//        }
//    }

    @RequestMapping(value = "/api/complainant/{phoneNumber}", method = RequestMethod.GET)
    public Object getComplainantWithPhoneNumber(@PathVariable("phoneNumber") String phoneNumber) {
        Complainant complainant = this.complainantService.findComplainantByPhoneNumber(phoneNumber);
        if (complainant == null) {
            return GenericResponse.builder()
                    .message("এই ফোন নম্বরের কোন ব্যবহারকারী নেই")
                    .success(false)
                    .build();
        }
        ComplainantResponseDTO responseDTO = this.complainantService.convertToComplainantResponseDTO(complainant);
        responseDTO.setBlacklistInOfficeId(complainantService.findBlacklistedOffices(complainant.getId()));
        return  responseDTO;    }


    @RequestMapping(value = "/api/grsUSer/{phoneNumber}", method = RequestMethod.GET)
    public Object getGrsUSerWithPhoneNumber(@PathVariable("phoneNumber") String phoneNumber) {
        SuperAdmin superAdmin = this.grsUserService.findGrsUserByPhoneNumber(phoneNumber);
        if (superAdmin == null) {
            return GenericResponse.builder()
                    .message("এই ফোন নম্বরের কোন ব্যবহারকারী নেই")
                    .success(false)
                    .build();
        }
        return this.grsUserService.convertToSuperAdminDTO(superAdmin);
    }


    @RequestMapping(value = "/api/complainant/", method = RequestMethod.GET)
    public Object getComplainantWithPhoneNumber() {
        return GenericResponse.builder()
                .message("এই ফোন নম্বরের কোন ব্যবহারকারী নেই")
                .success(false)
                .build();
    }

    @RequestMapping(value = "/api/citizen/reset/pincode/{phoneNumber}", method = RequestMethod.PUT)
    public GenericResponse resetCitizenPinCode(@PathVariable("phoneNumber") String  phoneNumber) {
        Complainant complainant = this.complainantService.findComplainantByPhoneNumber(phoneNumber);
        if (complainant == null) {
            return GenericResponse.builder()
                    .message("এই ফোন নম্বরের কোন ব্যবহারকারী নেই")
                    .success(false)
                    .build();
        }
        String newPincode = complainantService.getRandomPinNumber();
        complainant.setPassword(bCryptPasswordEncoder.encode(newPincode));
        this.complainantService.save(complainant);
//        shortMessageService.sendSMS(complainant.getPhoneNumber(), String.format("Your GRS login pincode has been reset, new pincode is %s.", newPincode));
        shortMessageService.sendSMS(complainant.getPhoneNumber(), String.format("আপনার জিআরএস লগইন পিনকোড রিসেট করা হয়েছে, নতুন পিনকোড : %s.", newPincode));
        if(StringUtil.isValidString(complainant.getEmail())) {
            emailService.sendEmail(complainant.getEmail(), "GRS login new pincode", "Dear " + complainant.getName() + ",\n\nYour GRS login pincode has been successfully reset. \nNew pincode is " + newPincode + ". \nPlease keep it secret and do not disclose to anyone.\n\n- From GRS System");
        }
        return GenericResponse.builder()
                .message("পিনকোড রিসেট সফল। নতুন পিনকোড এসএমএস ও ইমেইল এর মাধ্যমে পাঠানো হবে")
                .success(true)
                .build();
    }

    @RequestMapping(value = "/api/complainants/blacklist/{complainantId}/office/{officeId}", method = RequestMethod.PUT)
    public GenericResponse doBlacklistByComplainantId(Authentication authentication, @PathVariable("complainantId") Long complainantId, @PathVariable("officeId") Long officeId) {
        GenericResponse genericResponse = null;
        try {
            UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
            boolean done = this.complainantService.doBlacklistByComplainantId(complainantId, officeId);
            if (done) {
                genericResponse = GenericResponse.builder()
                        .message(this.messageService.getMessage("user.added.to.blacklist"))
                        .success(true)
                        .build();
            } else {
                genericResponse = GenericResponse.builder()
                        .message(this.messageService.getMessage("user.already.blacklisted"))
                        .success(false)
                        .build();
            }
        } catch (Exception ex) {
            log.info(ex.getMessage());
            genericResponse = GenericResponse.builder()
                    .message(this.messageService.getMessage("user.blacklisted.failed"))
                    .success(false)
                    .build();
        } finally {
            return genericResponse;
        }
    }

    @RequestMapping(value = "/api/complainants/blacklist/request", method = RequestMethod.PUT)
    public GenericResponse doBlacklistRequestByComplainantId(Authentication authentication, @RequestBody BlacklistRequestBodyDTO blacklistRequestBodyDTO) {
        GenericResponse genericResponse = null;
        try {
            UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
            boolean done = this.complainantService.doBlacklistRequestByComplainantId(blacklistRequestBodyDTO, userInformation);
            if (done) {
                genericResponse = GenericResponse.builder()
                        .message(this.messageService.getMessage("user.added.to.blacklist"))
                        .success(true)
                        .build();
            } else {
                genericResponse = GenericResponse.builder()
                        .message(this.messageService.getMessage("user.already.blacklisted"))
                        .success(false)
                        .build();
            }
        } catch (Exception ex) {
            log.info(ex.getMessage());
            genericResponse = GenericResponse.builder()
                    .message(this.messageService.getMessage("user.blacklisted.failed"))
                    .success(false)
                    .build();
        } finally {
            return genericResponse;
        }
    }

    @RequestMapping(value = "/api/complainants/blacklist/request/{complainantId}", method = RequestMethod.PUT)
    public GenericResponse doBlacklistRequestByComplainantId(Authentication authentication, @PathVariable("complainantId") Long complainantId) {
        GenericResponse genericResponse = null;
        try {
            UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
            boolean done = this.complainantService.doBlacklistRequestByComplainantId(complainantId, userInformation);
            if (done) {
                genericResponse = GenericResponse.builder()
                        .message(this.messageService.getMessage("user.added.to.blacklist"))
                        .success(true)
                        .build();
            } else {
                genericResponse = GenericResponse.builder()
                        .message(this.messageService.getMessage("user.already.blacklisted"))
                        .success(false)
                        .build();
            }
        } catch (Exception ex) {
            log.info(ex.getMessage());
            genericResponse = GenericResponse.builder()
                    .message(this.messageService.getMessage("user.blacklisted.failed"))
                    .success(false)
                    .build();
        } finally {
            return genericResponse;
        }
    }

    @RequestMapping(value = "/api/complainants/unblacklist/{complainantId}/office/{officeId}", method = RequestMethod.PUT)
    public GenericResponse doUnBlacklistByComplainantId(Authentication authentication, @PathVariable("complainantId") Long complainantId, @PathVariable("officeId") Long officeId) {
        GenericResponse genericResponse = null;
        try {
            boolean done = this.complainantService.doUnBlacklistByComplainantId(complainantId, officeId);
            if (done) {
                genericResponse = GenericResponse.builder()
                        .message(this.messageService.getMessage("user.removed.from.blacklist"))
                        .success(true)
                        .build();
            } else {
                genericResponse = GenericResponse.builder()
                        .message(this.messageService.getMessage("user.already.unblacklisted"))
                        .success(false)
                        .build();
            }
        } catch (Exception ex) {
            log.info(ex.getMessage());
            genericResponse = GenericResponse.builder()
                    .message(this.messageService.getMessage("user.unblacklisted.failed"))
                    .success(false)
                    .build();
        } finally {
            return genericResponse;
        }
    }

    @RequestMapping(value = "/api/complainants/unblacklist/request/{complainantId}", method = RequestMethod.PUT)
    public GenericResponse doUnBlacklistRequestByComplainantId(Authentication authentication, @PathVariable("complainantId") Long complainantId) {
        GenericResponse genericResponse = null;
        try {
            UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
            boolean done = this.complainantService.doUnBlacklistRequestByComplainantId(complainantId, userInformation.getOfficeInformation().getOfficeId());
            if (done) {
                genericResponse = GenericResponse.builder()
                        .message(this.messageService.getMessage("user.removed.from.blacklist"))
                        .success(true)
                        .build();
            } else {
                genericResponse = GenericResponse.builder()
                        .message(this.messageService.getMessage("user.already.unblacklisted"))
                        .success(false)
                        .build();
            }
        } catch (Exception ex) {
            log.info(ex.getMessage());
            genericResponse = GenericResponse.builder()
                    .message(this.messageService.getMessage("user.unblacklisted.failed"))
                    .success(false)
                    .build();
        } finally {
            return genericResponse;
        }
    }

    @RequestMapping(value = "/api/blacklists", method = RequestMethod.GET)
    public List<ComplainantInfoDTO> getBlacklistByOfficeId(Authentication authentication) {
        if (authentication == null) {
            return new ArrayList();
        }
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return this.complainantService.getBlacklistByOfficeId(userInformation.getOfficeInformation().getOfficeId());
    }

    @RequestMapping(value = "/api/blacklists/requests", method = RequestMethod.GET)
    public List<ComplainantInfoBlacklistReqDTO> getBlacklistRequestsFromChildOffices(Authentication authentication) {
        if (authentication == null) {
            return new ArrayList();
        }
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return this.complainantService.getBlacklistRequestByChildOffices(userInformation.getOfficeInformation().getOfficeId(), userInformation.getOfficeInformation().getOfficeUnitOrganogramId());
    }

    @RequestMapping(value = "/viewBlacklist.do", method = RequestMethod.GET)
    public ModelAndView getBlacklistPage(HttpServletRequest request, Authentication authentication, Model model) {
        if (authentication != null) {
            return modelViewService.addNecessaryAttributesAndReturnViewPage(model,
                    authentication,
                    request,
                    "blacklist",
                    "viewBlacklist",
                    "admin"
            );
        }
        return new ModelAndView("redirect:/error-page");
    }

    @RequestMapping(value = "/viewBlacklistRequests.do", method = RequestMethod.GET)
    public ModelAndView getBlacklistRequestPage(HttpServletRequest request, Authentication authentication, Model model) {
        if (authentication != null) {
            return modelViewService.addNecessaryAttributesAndReturnViewPage(model,
                    authentication,
                    request,
                    "blacklist",
                    "viewRequestedBlacklist",
                    "admin"
            );
        }
        return new ModelAndView("redirect:/error-page");
    }

//    @RequestMapping(value = "/api/complainant", method = RequestMethod.GET)
//    public ComplainantDTO getComplainant(Authentication authentication) {
//        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
//        return this.complainantService.getComplaintDTO(userInformation);
//    }

    @RequestMapping(value = "/update/password", method = RequestMethod.PUT)
    public GenericResponse updateComplainantPasswrd(Authentication authentication, @RequestBody PasswordChangeDTO passwordChangeDTO){
        if (authentication != null){
            UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
            if(userInformation.getUserType().equals(UserType.COMPLAINANT)){
                boolean changed = this.complainantService.updateComplainantPassword(userInformation, passwordChangeDTO);
                return GenericResponse.builder()
                        .success(changed)
                        .message(changed ? "The password has changed" : "Please provide correct password")
                        .build();
            }
        }
        return GenericResponse.builder()
                .success(false)
                .message("Sorry the password could not be changed")
                .build();
    }

}
