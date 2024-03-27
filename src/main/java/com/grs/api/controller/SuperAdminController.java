package com.grs.api.controller;

import com.grs.api.model.request.FileSettingsDTO;
import com.grs.api.model.request.GrievanceRequestDTO;
import com.grs.api.model.request.GrsUserDTO;
import com.grs.api.model.request.SystemNotificationSettingsDTO;
import com.grs.api.model.response.*;
import com.grs.core.domain.grs.EmailSmsSettings;
import com.grs.core.domain.grs.SmsTemplate;
import com.grs.core.service.*;
import com.grs.utils.StringUtil;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.WeakHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HP on 1/30/2018.
 */
@RestController
@Slf4j
public class SuperAdminController {
    @Autowired
    private OccupationService occupationService;
    @Autowired
    private EducationService educationService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ShortMessageService shortMessageService;
    @Autowired
    private ActionToRoleService actionToRoleService;
    @Autowired
    private EmailSmsSettingsService emailSmsSettingsService;
    @Autowired
    private GeneralSettingsService generalSettingsService;
    @Autowired
    private GrsUserService grsUserService;
    @Autowired
    private SpProgrammeService spProgrammeService;

    @RequestMapping(value = "/api/settings/email-sms", method = RequestMethod.GET)
    public List<EmailSmsSettings> getEmailSmsSettings() {
        return this.emailSmsSettingsService.findEmailSmsSettings();
    }

    @RequestMapping(value = "/api/settings/email-sms/{id}", method = RequestMethod.PUT)
    public GenericResponse updateEmailSmsSettings(@PathVariable("id") Long emailSmsID,@RequestBody EmailSmsSettingsDTO emailSmsSettingsDTO) {
        EmailSmsSettings emailSmsSettings = this.emailSmsSettingsService.findOne(emailSmsID);
        String type = String.valueOf(emailSmsSettings.getType());
        String message = null;
        if (type.equals("SMS")) {
            if (!(StringUtil.isValidString(emailSmsSettingsDTO.getUrl()) && StringUtil.isValidString(emailSmsSettingsDTO.getUsername()) && StringUtil.isValidString(emailSmsSettingsDTO.getPassword()) && StringUtil.isValidString(emailSmsSettingsDTO.getMs_prefix()))) {
                message = messageService.getMessageWithArgsV2("x.cannot.be.blank", "setup.sms");
                return GenericResponse.builder().success(false).message(message).build();
            }
        } else if (type.equals("EMAIL")) {
            if (!(StringUtil.isValidString(emailSmsSettingsDTO.getHost()) && StringUtil.isValidString(emailSmsSettingsDTO.getUsername()) && StringUtil.isValidString(emailSmsSettingsDTO.getPassword()) && StringUtil.isValidString(emailSmsSettingsDTO.getPort().toString()) && StringUtil.isValidString(emailSmsSettingsDTO.getSmtpHost()))) {
                message = messageService.getMessageWithArgsV2("x.cannot.be.blank", "setup.email");
                return GenericResponse.builder().success(false).message(message).build();
            }
        }
        Boolean flag = emailSmsSettingsService.updateEmailSmsSettings(emailSmsID,emailSmsSettingsDTO);
        String code = flag ? "x.save.success" : "can.not.save.x";
        if(type.equals("SMS")){
            message = messageService.getMessageWithArgsV2(code, "setup.sms");
        } else if(type.equals("EMAIL")){
            message = messageService.getMessageWithArgsV2(code, "setup.email");
        }
        return GenericResponse.builder().success(flag).message(message).build();
    }

    @RequestMapping(value = "/api/{grievanceStatus}/action/takers", method = RequestMethod.GET)
    public WeakHashMap<String, String> getGrsRolesByGrievanceCurrent(@PathVariable("grievanceStatus") String grievanceStatus) {
        return this.actionToRoleService.findDistinctGRSRoleByGrievanceStatus(grievanceStatus);
    }

    @RequestMapping(value = "/api/{grievanceStatus}/{grsRole}/actions", method = RequestMethod.GET)
    public WeakHashMap<String, String> getActionsByGrievanceCurrentAndGrsRole(@PathVariable("grievanceStatus") String grievanceStatus,
                                                                      @PathVariable("grsRole") String grsRole) {
        return this.actionToRoleService.findActionsByGrievanceStatusAndGrsRole(grievanceStatus, grsRole);
    }

    @RequestMapping(value = "/api/occupation", method = RequestMethod.GET)
    public Page<OccupationDTO> getViewOccupationsPage(@PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        return this.occupationService.findAllOccupations(pageable);
    }

    @RequestMapping(value = "/api/occupation", method = RequestMethod.POST)
    public GenericResponse addOccupations(@RequestBody OccupationDTO occupationDTO) {
        if(!(StringUtil.isValidString(occupationDTO.getOccupationBangla()) && StringUtil.isValidString(occupationDTO.getOccupationEnglish()))) {
            String message = messageService.getMessageWithArgsV2("x.cannot.be.blank", "occupation");
            return GenericResponse.builder().success(false).message(message).build();
        }
        Boolean flag = occupationService.saveAllOccupations(occupationDTO);
        String code = flag ? "x.save.success" : "can.not.save.x";
        String message = messageService.getMessageWithArgsV2(code, "occupation");
        return GenericResponse.builder().success(flag).message(message).build();
    }

    @RequestMapping(value = "/api/occupation/{occupationId}", method = RequestMethod.PUT)
    public GenericResponse addOccupations(@PathVariable("occupationId") Long occupationID, @RequestBody OccupationDTO occupationDTO) {
        if(!(StringUtil.isValidString(occupationDTO.getOccupationBangla()) && StringUtil.isValidString(occupationDTO.getOccupationEnglish()))) {
            String message = messageService.getMessageWithArgsV2("x.cannot.be.blank", "occupation");
            return GenericResponse.builder().success(false).message(message).build();
        }
        Boolean flag = occupationService.updateOccupation(occupationID,occupationDTO);
        String code = flag ? "x.save.success" : "can.not.save.x";
        String message = messageService.getMessageWithArgsV2(code, "occupation");
        return GenericResponse.builder().success(flag).message(message).build();
    }

    @RequestMapping(value = "/api/sp-programme", method = RequestMethod.POST)
    public GenericResponse addSpProgram(@RequestBody SpProgramDto dto) {
        if(!(StringUtil.isValidString(dto.getNameEn()) && StringUtil.isValidString(dto.getNameBn()))) {
            String message = messageService.getMessageWithArgsV2("x.cannot.be.blank", "grievance.safetynet.program");
            return GenericResponse.builder().success(false).message(message).build();
        }
        Boolean flag = spProgrammeService.createSpProgram(dto);
        String code = flag ? "x.save.success" : "can.not.save.x";
        String message = messageService.getMessageWithArgsV2(code, "grievance.safetynet.program");
        return GenericResponse.builder().success(flag).message(message).build();
    }

    @RequestMapping(value = "/api/sp-programme/{spProgrammeId}", method = RequestMethod.PUT)
    public GenericResponse updateSpProgram(@PathVariable("spProgrammeId") Integer spProgrammeId, @RequestBody SpProgramDto dto) {
        if(dto.getId() == null || !(StringUtil.isValidString(dto.getNameBn()) && StringUtil.isValidString(dto.getNameEn()))) {
            String message = messageService.getMessageWithArgsV2("x.cannot.be.blank", "grievance.safetynet.program");
            return GenericResponse.builder().success(false).message(message).build();
        }
        Boolean flag = spProgrammeService.updateSpProgram(dto);
        String code = flag ? "x.save.success" : "can.not.save.x";
        String message = messageService.getMessageWithArgsV2(code, "grievance.safetynet.program");
        return GenericResponse.builder().success(flag).message(message).build();
    }

    @RequestMapping(value = "/api/template/email", method = RequestMethod.GET)
    public Page<EmailTemplateDTO> getViewEmailTemplatePage(@PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        return this.emailService.findAllEmailTemplates(pageable);
    }

    @RequestMapping(value = "/api/template/email", method = RequestMethod.POST)
    public GenericResponse addEmailTemplate(@ModelAttribute EmailTemplateDTO emailTemplateDTO,
                                            @ModelAttribute ActionToRoleDTO actionToRole) {
        if(!(StringUtil.isValidString(emailTemplateDTO.getEmailTemplateBodyBng()) && StringUtil.isValidString(emailTemplateDTO.getEmailTemplateSubjectBng()))) {
            String message = messageService.getMessageWithArgsV2("x.cannot.be.blank", "template.email");
            return GenericResponse.builder().success(false).message(message).build();
        }
        Boolean flag = emailService.saveAllEmailTemplates(emailTemplateDTO,actionToRole);
        String code = flag ? "x.save.success" : "can.not.save.x";
        String message = messageService.getMessageWithArgsV2(code, "template.email");
        return GenericResponse.builder().success(flag).message(message).build();
    }

    @RequestMapping(value = "/api/template/email/{emailTemplateId}", method = RequestMethod.PUT)
    public GenericResponse addEmailTemplate(@PathVariable ("emailTemplateId") Long emailTemplateID,
                                            @RequestBody EmailTemplateDTO emailTemplateDTO) {
        if(!(StringUtil.isValidString(emailTemplateDTO.getEmailTemplateBodyBng()) && StringUtil.isValidString(emailTemplateDTO.getEmailTemplateSubjectBng()))) {
            String message = messageService.getMessageWithArgsV2("x.cannot.be.blank", "template.email");
            return GenericResponse.builder().success(false).message(message).build();
        }
        Boolean flag = emailService.updateEmailTemplate(emailTemplateID,emailTemplateDTO);
        String code = flag ? "x.save.success" : "can.not.save.x";
        String message = messageService.getMessageWithArgsV2(code, "template.email");
        return GenericResponse.builder().success(flag).message(message).build();
    }

    @RequestMapping(value = "/api/template/sms", method = RequestMethod.GET)
    public Page<SmsTemplate> getViewSmsTemplatePage(@PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        return this.shortMessageService.findAllSmsTemplates(pageable);
    }

    @RequestMapping(value = "/api/template/sms", method = RequestMethod.POST)
    public GenericResponse addSmsTemplate(@ModelAttribute SmsTemplateDTO smsTemplateDTO,
                                          @ModelAttribute ActionToRoleDTO actionToRole) {
        if(!(StringUtil.isValidString(smsTemplateDTO.getSmsTemplateBodyBng()) )) {
            String message = messageService.getMessageWithArgsV2("x.cannot.be.blank", "template.sms");
            return GenericResponse.builder().success(false).message(message).build();
        }
        Boolean flag = shortMessageService.saveAllSmsTemplates(smsTemplateDTO,actionToRole);
        String code = flag ? "x.save.success" : "can.not.save.x";
        String message = messageService.getMessageWithArgsV2(code, "template.sms");
        return GenericResponse.builder().success(flag).message(message).build();
    }

    @RequestMapping(value = "/api/template/sms/{smsTemplateId}", method = RequestMethod.PUT)
    public GenericResponse addSmsTemplate(@PathVariable ("smsTemplateId") Long smsTemplateID,
                                          @RequestBody SmsTemplateDTO smsTemplateDTO) {
        if(!(StringUtil.isValidString(smsTemplateDTO.getSmsTemplateBodyBng()) )) {
            String message = messageService.getMessageWithArgsV2("x.cannot.be.blank", "template.sms");
            return GenericResponse.builder().success(false).message(message).build();
        }
        Boolean flag = shortMessageService.updateSmsTemplate(smsTemplateID,smsTemplateDTO);
        String code = flag ? "x.save.success" : "can.not.save.x";
        String message = messageService.getMessageWithArgsV2(code, "template.sms");
        return GenericResponse.builder().success(flag).message(message).build();
    }

    @RequestMapping(value = "/api/education", method = RequestMethod.GET)
    public Page<EducationDTO> getViewEducationPage(@PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        return this.educationService.findAllEducation(pageable);
    }

    @RequestMapping(value = "/api/education", method = RequestMethod.POST)
    public GenericResponse addEducation(@RequestBody EducationDTO educationDTO) {
        if(!(StringUtil.isValidString(educationDTO.getEducationBangla()) && StringUtil.isValidString(educationDTO.getEducationEnglish()))) {
            String message = messageService.getMessageWithArgsV2("x.cannot.be.blank", "education");
            return GenericResponse.builder().success(false).message(message).build();
        }
        Boolean flag = educationService.saveAllEducation(educationDTO);
        String code = flag ? "x.save.success" : "can.not.save.x";
        String message = messageService.getMessageWithArgsV2(code, "education");
        return GenericResponse.builder().success(flag).message(message).build();
    }

    @RequestMapping(value = "/api/education/{educationId}", method = RequestMethod.PUT)
    public GenericResponse addEducation(@PathVariable("educationId") Long educationID, @RequestBody EducationDTO educationDTO) {
        if(!(StringUtil.isValidString(educationDTO.getEducationBangla()) && StringUtil.isValidString(educationDTO.getEducationEnglish()))) {
            String message = messageService.getMessageWithArgsV2("x.cannot.be.blank", "education");
            return GenericResponse.builder().success(false).message(message).build();
        }
        Boolean flag = educationService.updateOccupation(educationID,educationDTO);
        String code = flag ? "x.save.success" : "can.not.save.x";
        String message = messageService.getMessageWithArgsV2(code, "education");
        return GenericResponse.builder().success(flag).message(message).build();
    }

    @RequestMapping(value = "/api/settings/file-upload-settings", method = RequestMethod.PUT)
    public GenericResponse updateFileSettings(@RequestBody FileSettingsDTO fileSettingsDTO) {
        return generalSettingsService.saveFileSettings(fileSettingsDTO);
    }

    @RequestMapping(value = "/api/settings/system-notification-settings", method = RequestMethod.PUT)
    public GenericResponse updateSystemNotificationSettings(@RequestBody SystemNotificationSettingsDTO settingsDTO) {
        return generalSettingsService.saveSystemNotificationSettings(settingsDTO);
    }

    @RequestMapping(value = "/api/addGrsUser", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public GenericResponse addGrsUser(Authentication authentication, @RequestBody GrsUserDTO grsUserDTO) {
        Boolean flag = this.grsUserService.register(grsUserDTO);
        String code = flag ? "x.save.success" : "can.not.save.x";
        String message = messageService.getMessageWithArgsV2(code, "grsuser");
        return GenericResponse.builder().success(flag).message(message).build();
    }
}
