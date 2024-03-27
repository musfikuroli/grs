package com.grs.core.service;

import com.grs.api.model.response.EmailSmsSettingsDTO;
import com.grs.core.dao.ActionDAO;
import com.grs.core.dao.EmailSmsSettingsDAO;
import com.grs.core.dao.GrievanceStatusDAO;
import com.grs.core.dao.GrsRoleDAO;
import com.grs.core.domain.GrievanceCurrentStatus;
import com.grs.core.domain.grs.EmailSmsSettings;
import com.grs.core.domain.grs.GrievanceStatus;
import com.grs.core.domain.grs.GrsRole;
import com.grs.utils.BanglaConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.WeakHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HP on 3/13/2018.
 */
@Slf4j
@Service
public class EmailSmsSettingsService {
    @Autowired
    private EmailSmsSettingsDAO emailSmsSettingsDAO;
    @Autowired
    private MessageService messageService;
    @Autowired
    private GrievanceStatusDAO grievanceStatusDAO;
    @Autowired
    private ActionDAO actionDAO;
    @Autowired
    private GrsRoleDAO grsRoleDAO;

    public EmailSmsSettingsDTO convertToEmailSmsSettingsDTO(EmailSmsSettings emailSmsSettings) {
        return EmailSmsSettingsDTO.builder()
                .id(emailSmsSettings.getId())
                .type(String.valueOf(emailSmsSettings.getType()))
                .username(emailSmsSettings.getUsername())
                .password(emailSmsSettings.getPassword())
                .host(emailSmsSettings.getHost())
                .port(emailSmsSettings.getPort())
                .smtpHost(emailSmsSettings.getSmtpHost())
                .url(emailSmsSettings.getUrl())
                .ms_prefix(emailSmsSettings.getMs_prefix())
                .disabled(emailSmsSettings.getDisabled())
                .build();
    }

    public EmailSmsSettingsDTO getEmailSmsSettingsDTO(EmailSmsSettings emailSmsSettings) {
        return this.convertToEmailSmsSettingsDTO(emailSmsSettings);
    }

    public List<EmailSmsSettings> findEmailSmsSettings() {
        return this.emailSmsSettingsDAO.findAll();
    }

    public EmailSmsSettings getEmailSmsSettings(Long id) {
        return this.emailSmsSettingsDAO.findOne(id);
    }

    public Boolean updateEmailSmsSettings(Long smsID, EmailSmsSettingsDTO emailSmsSettingsDTO) {
        EmailSmsSettings emailSmsSettings = this.getEmailSmsSettings(smsID);
        emailSmsSettings.setUsername(emailSmsSettingsDTO.getUsername());
        emailSmsSettings.setPassword(emailSmsSettingsDTO.getPassword());
        emailSmsSettings.setHost(emailSmsSettingsDTO.getHost());
        emailSmsSettings.setPort(emailSmsSettingsDTO.getPort());
        emailSmsSettings.setSmtpHost(emailSmsSettingsDTO.getSmtpHost());
        emailSmsSettings.setUrl(emailSmsSettingsDTO.getUrl());
        emailSmsSettings.setMs_prefix(emailSmsSettingsDTO.getMs_prefix());
        if(emailSmsSettingsDTO.getDisabled() == null){
            emailSmsSettings.setDisabled(false);
        } else {
            emailSmsSettings.setDisabled(true);
        }
        this.emailSmsSettingsDAO.saveSettings(emailSmsSettings);
        return true;
    }

    public EmailSmsSettings findOne(Long emailSmsID) {
        return this.emailSmsSettingsDAO.findOne(emailSmsID);
    }

    public WeakHashMap<String, String> convertToGrievanceStatusList() {
        WeakHashMap<String, String> grievanceStatusList = new WeakHashMap<>();
        grievanceStatusDAO.findAll().forEach(grievanceStatus -> {
            String statusName = grievanceStatus.getStatusName();
//            String code = statusName.toLowerCase().replace('_', '.');
            grievanceStatusList.put(statusName, BanglaConverter.convertGrievanceStatusToBangla(GrievanceCurrentStatus.valueOf(grievanceStatus.getStatusName())));
        });
        return grievanceStatusList;
    }

    public WeakHashMap<String, String> convertToGrsRoleList() {
        WeakHashMap<String, String> grsRoleList = new WeakHashMap<>();
        grsRoleDAO.findAll().forEach(grsRole -> {
            String roleName = grsRole.getRole();
            String code = roleName.toLowerCase().replace('_', '.');
            grsRoleList.put(roleName, messageService.getMessage(code));
        });
        return grsRoleList;
    }

    public WeakHashMap<String, String> convertToActionList(){
        WeakHashMap<String,String> actionList = new WeakHashMap<>();
        actionDAO.findAll().forEach(action -> {
            String actionBng = action.getActionBng();
            String code = actionBng.toLowerCase().replace('_', '.');
            actionList.put(actionBng,messageService.getMessage(code));
        });
        return actionList;
    }
}
