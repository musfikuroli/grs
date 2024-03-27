package com.grs.core.service;

import com.grs.api.model.request.FileSettingsDTO;
import com.grs.api.model.request.SystemNotificationSettingsDTO;
import com.grs.api.model.response.GenericResponse;
import com.grs.core.dao.GeneralSettingsDAO;
import com.grs.core.domain.grs.GeneralSettings;
import com.grs.utils.BanglaConverter;
import com.grs.utils.Constant;
import com.grs.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.WeakHashMap;
import java.util.Map;

@Service
public class GeneralSettingsService {
    @Autowired
    private GeneralSettingsDAO generalSettingsDAO;
    @Autowired
    private MessageService messageService;

    public String getAllowedFileTypes() {
        return generalSettingsDAO.getAllowedFileTypes();
    }

    public String getSettingsValueByFieldName(String name) {
        return generalSettingsDAO.getValueByFieldName(name);
    }

    public Integer getMaximumFileSize() {
        return generalSettingsDAO.getMaximumFileSize();
    }

    public String getAllowedFileSizeLabel() {
        Integer maximumFileSize = generalSettingsDAO.getMaximumFileSize();
        return messageService.isCurrentLanguageInEnglish() ? maximumFileSize.toString() : BanglaConverter.convertToBanglaDigit(maximumFileSize.longValue());
    }

    public String getAllowedFileTypesLabel() {
        String fileTypes = generalSettingsDAO.getAllowedFileTypes();
        if (fileTypes == null) return null;
        StringBuilder stringBuilder = new StringBuilder(fileTypes);
        StringUtil.replaceAll(stringBuilder ,"|", ", ");
        return StringUtil.replaceAll(stringBuilder ,"?", "").toString();
    }

    @Transactional("transactionManager")
    public GenericResponse saveFileSettings(FileSettingsDTO fileSettingsDTO) {
        GenericResponse genericResponse = GenericResponse.builder().build();
        Integer maxFileSize = fileSettingsDTO.getMaxFileSize();
        String allowedFileTypes = fileSettingsDTO.getAllowedFileTypes();
        if(maxFileSize == null || maxFileSize <= 0) {
            genericResponse.setSuccess(false);
            genericResponse.setMessage(messageService.getMessageWithArgsV2("x.should.be.greater.than.y", "file.size", "number.zero"));
        } else if(!StringUtil.isValidString(allowedFileTypes)) {
            genericResponse.setSuccess(false);
            genericResponse.setMessage(messageService.getMessageWithArgsV2("x.cannot.be.blank", "file.type"));
        } else {
            GeneralSettings fileSizeSettings = generalSettingsDAO.findByName(Constant.fileSizeFieldName);
            GeneralSettings fileTypeSettings = generalSettingsDAO.findByName(Constant.fileTypeFieldName);
            if(fileSizeSettings.getValue() != maxFileSize.toString()) {
                fileSizeSettings.setValue(maxFileSize.toString());
                generalSettingsDAO.save(fileSizeSettings);
            }
            if(fileTypeSettings.getValue() != allowedFileTypes) {
                fileTypeSettings.setValue(allowedFileTypes);
                generalSettingsDAO.save(fileTypeSettings);
            }
            genericResponse.setSuccess(true);
            genericResponse.setMessage(messageService.getMessageWithArgsV2("x.save.success", "file.settings"));
        }
        return genericResponse;
    }

    @Transactional("transactionManager")
    public GenericResponse saveSystemNotificationSettings(SystemNotificationSettingsDTO settingsDTO) {
        GenericResponse genericResponse = GenericResponse.builder().build();
        String newEmail = settingsDTO.getEmail();
        String newPhoneNumber = settingsDTO.getPhoneNumber();
        GeneralSettings emailSettings = generalSettingsDAO.findByName(Constant.SYSTEM_NOTIFICATION_EMAIL);
        GeneralSettings phoneNumberSettings = generalSettingsDAO.findByName(Constant.SYSTEM_NOTIFICATION_PHONE_NUMBER);
        if(emailSettings.getValue() != newEmail) {
            emailSettings.setValue(newEmail);
            generalSettingsDAO.save(emailSettings);
        }
        if(phoneNumberSettings.getValue() != newPhoneNumber) {
            phoneNumberSettings.setValue(newPhoneNumber);
            generalSettingsDAO.save(phoneNumberSettings);
        }
        genericResponse.setSuccess(true);
        genericResponse.setMessage(messageService.getMessageWithArgsV2("x.save.success", "report.notification.setup"));
        return genericResponse;
    }
}
