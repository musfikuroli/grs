package com.grs.core.service;

import com.google.gson.Gson;
import com.grs.api.model.request.*;
import com.grs.api.model.response.ActionToRoleDTO;
import com.grs.api.model.response.NewBulkSmsResponse;
import com.grs.api.model.response.SmsResponseDTO;
import com.grs.api.model.response.SmsTemplateDTO;
import com.grs.core.dao.*;
import com.grs.core.domain.*;
import com.grs.core.domain.grs.*;
import com.grs.utils.BanglaConverter;
import com.grs.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by Acer on 22-Dec-17.
 */
@Slf4j
@Service
public class ShortMessageService {
    @Autowired
    private SmsTemplateDAO smsTemplateDAO;
    @Autowired
    private ActionDAO actionDAO;
    @Autowired
    private GrievanceStatusDAO grievanceStatusDAO;
    @Autowired
    private GrsRoleDAO grsRoleDAO;
    @Autowired
    private ActionToRoleDAO actionToRoleDAO;
    @Autowired
    private MessageService messageService;
    @Autowired
    private GrsRoleToSmsDAO grsRoleToSmsDAO;
    @Autowired
    private Environment environment;
    @Autowired
    private Gson gson;

    private RestTemplate restTemplate;

    @Value("${sms.gateway.user}")
    private String smsUser;

    @Value("${sms.gateway.password}")
    private String smsPassword;

    @Value("${sms.gateway.encrypted-key}")
    private String encryptedKeyForCabinet;

    @Value("${sms.gateway.userid}")
    private String cabinetUserID;

    @PostConstruct
    public void init() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    @Async("threadPoolTaskExecutorForSMS")
    public void sendSMS(String phoneNumber, String content) {
        try {
            if(!Boolean.valueOf(environment.getProperty("environment.production"))) {
                return;
            }
            phoneNumber = BanglaConverter.convertToEnglish(phoneNumber);
            final String uri = "http://bulksms.teletalk.com.bd/jlinktbls.php";
            /*
            BulkSMSAuthDTO auth = new BulkSMSAuthDTO();
            BulkSMSInfoDTO smsInfo = new BulkSMSInfoDTO();
            BulkSMSRequestDTO requestDTO = new BulkSMSRequestDTO();

            List<String> msisdn = Arrays.asList(phoneNumber);

            auth.setUsername(smsUser);
            auth.setPassword(smsPassword);
            auth.setAcode("1005093");

            smsInfo.setMessage(content);
            smsInfo.setIs_unicode("1");
            smsInfo.setMasking("8801552146224");
            smsInfo.setMsisdn(msisdn);

            requestDTO.setAuth(auth);
            requestDTO.setSmsInfo(smsInfo);

            String requestString = gson.toJson(requestDTO);

            String resultString = restTemplate.postForObject(uri, requestString, String.class);
            SmsResponseDTO result = gson.fromJson(resultString, SmsResponseDTO.class);
            if(result == null || result.getDescription() == null || !result.getDescription().toUpperCase().contains("SUCCESS")) {
                log.error("SMS sending failed with response: " + result);
            }
            log.info("SMS Sent at: " + phoneNumber);

             */
            HashMap<String,String> aKeyPKey = getAKeyPKey();
            NewBulkSMSRequest request = new NewBulkSMSRequest();
            request.setUser(smsUser);
            request.setPass(smsPassword);
            request.setSms(content);
            request.setSms_id("8392829");
            request.setCharset("UTF-8");
            request.setMobile(phoneNumber);
            request.setA_key(aKeyPKey.get("akey"));
            request.setP_key(aKeyPKey.get("pkey"));
            request.setCid(String.valueOf(System.currentTimeMillis()).substring(0,9));
            String resultString = restTemplate.postForObject(uri, request, String.class);

            log.info("===SMS Response:{}", resultString);

            try {
                NewBulkSmsResponse result = gson.fromJson(resultString, NewBulkSmsResponse.class);

                if(result != null && result.status != null &&  result.status.toUpperCase().contains("SUCCESS")) {
                    log.error("SMS has been sent with response: " + result.status);
                } else {
                    log.info("SMS Sending Failed at: " + phoneNumber);
                }
            } catch (Throwable c) {
                c.printStackTrace();
                log.error("SMS sending failed for: " + phoneNumber);
            }


        } catch (Exception e) {
            log.error("SMS sending failed for: " + phoneNumber);
            e.printStackTrace();
        }
    }
    /* @Async("threadPoolTaskExecutorForSMS")
     public void sendSMSUsingDB(String phoneNumber, SmsTemplate smsTemplate, Grievance grievance) {
         ActionToRole actionToRole = smsTemplate.getActionToRole();
         String body = null;
         String trackingNumber = (StringUtil.isValidString(grievance.getTrackingNumber())
                 && (grievance.getTrackingNumber().startsWith("01"))) ?
                 grievance.getTrackingNumber().substring(11) :
                 grievance.getTrackingNumber();
         if (smsTemplate.getLanguage().toString().equals("BANGLA")) {
             body = findSmsTemplate(actionToRole).getSmsTemplateBodyBng() + " Tracking number: " + trackingNumber;
         } else {
             body = findSmsTemplate(actionToRole).getSmsTemplateBodyEng() + " Tracking number: " + trackingNumber;
         }
         try {
             final String uri = "http://bulkmsg.teletalk.com.bd/api/sendSMS";

             BulkSMSAuthDTO auth = new BulkSMSAuthDTO();
             BulkSMSInfoDTO smsInfo = new BulkSMSInfoDTO();
             BulkSMSRequestDTO requestDTO = new BulkSMSRequestDTO();

             List<String> msisdn = Arrays.asList(phoneNumber);

             auth.setUsername(smsUser);
             auth.setPassword(smsPassword);
             auth.setAcode("1005093");

             smsInfo.setMessage(body);
             smsInfo.setIs_unicode("1");
             smsInfo.setMasking("8801552146224");
             smsInfo.setMsisdn(msisdn);

             requestDTO.setAuth(auth);
             requestDTO.setSmsInfo(smsInfo);

             String requestString = gson.toJson(requestDTO);

             restTemplate.postForObject(uri, requestString, String.class);
             log.info("SMS Sent at: " + phoneNumber);
         } catch (Exception e) {
             log.error("SMS sending failed for: " + phoneNumber);
             e.printStackTrace();
         }

     }*/
    @Async("threadPoolTaskExecutorForSMS")
    public void sendSMSUsingDB(String phoneNumber, SmsTemplate smsTemplate, Grievance grievance) {
        ActionToRole actionToRole = smsTemplate.getActionToRole();
        String body = null;
        String trackingNumber = (StringUtil.isValidString(grievance.getTrackingNumber())
                && (grievance.getTrackingNumber().startsWith("01"))) ?
                grievance.getTrackingNumber().substring(11) :
                grievance.getTrackingNumber();
        if (smsTemplate.getLanguage().toString().equals("BANGLA")) {
            body = findSmsTemplate(actionToRole).getSmsTemplateBodyBng() + " Tracking number: " + trackingNumber;
        } else {
            body = findSmsTemplate(actionToRole).getSmsTemplateBodyEng() + " Tracking number: " + trackingNumber;
        }
        try {
            final String uri = "http://bulksms.teletalk.com.bd/jlinktbls.php";
            HashMap<String,String> aKeyPKey = getAKeyPKey();

            NewBulkSMSRequest request = new NewBulkSMSRequest();
            request.setUser(smsUser);
            request.setPass(smsPassword);
            request.setSms(body);
            request.setSms_id("8392829");
            request.setCharset("UTF-8");
            request.setMobile(phoneNumber);
            request.setA_key(aKeyPKey.get("akey"));
            request.setP_key(aKeyPKey.get("pkey"));
            request.setCid(String.valueOf(System.currentTimeMillis()).substring(0,9));
            String resultString = restTemplate.postForObject(uri, request, String.class);
            log.info("===SMS Response:{}", resultString);
            try {
                NewBulkSmsResponse result = gson.fromJson(resultString, NewBulkSmsResponse.class);
                if(result != null && result.status != null &&  result.status.toUpperCase().contains("SUCCESS")) {
                    log.error("SMS has been sent with response: " + result.status);
                } else {
                    log.info("SMS Sending Failed at: " + phoneNumber);
                }
            } catch (Throwable c) {
                c.printStackTrace();
                log.error("SMS sending failed for: " + phoneNumber);
            }
        } catch (Exception e) {
            log.error("SMS sending failed for: " + phoneNumber);
            e.printStackTrace();
        }

    }

    private HashMap<String,String> getAKeyPKey() {
        HashMap<String,String> aKeyPKey = new HashMap<>();
        Long currentTimeMillis = System.currentTimeMillis();
        String pKey = currentTimeMillis+"222";

        Long aKeyInteger = Long.parseLong(pKey) + Long.parseLong(cabinetUserID);
        String aKey = aKeyInteger+encryptedKeyForCabinet;

        String aKeyMd5 = DigestUtils.md5DigestAsHex(aKey.getBytes(StandardCharsets.UTF_8));
        aKeyPKey.put("akey",aKeyMd5);
        aKeyPKey.put("pkey",pKey);

        return aKeyPKey;
    }
    public Page<SmsTemplate> findAllSmsTemplates(Pageable pageable) {
        Page<SmsTemplate> smsTemplates = this.smsTemplateDAO.findAll(pageable);
        return smsTemplates;
    }

    public Boolean saveAllSmsTemplates(SmsTemplateDTO smsTemplateDTO, ActionToRoleDTO actionToRoleDTO) {
        Action action = this.actionDAO.getActionByActionName(actionToRoleDTO.getAction());
        GrievanceStatus grievanceStatus = this.grievanceStatusDAO.findByName(actionToRoleDTO.getGrievanceStatus());
        GrsRole grsRole = this.grsRoleDAO.findByRole(actionToRoleDTO.getRole());
        ActionToRole actionToRole = this.actionToRoleDAO.findByGrievanceStatusAndRoleAndAction(grievanceStatus, grsRole, action);

        String actionBng = actionToRoleDTO.getAction();
        String codeAction = actionBng.toLowerCase().replace('_', '.');
        String roleName = actionToRoleDTO.getRole();
        String codeRole = roleName.toLowerCase().replace('_', '.');
        String statusName = actionToRoleDTO.getGrievanceStatus();
        String codeStatus = statusName.toLowerCase().replace('_', '.');
        String templateName = messageService.getMessage(codeStatus) + " অভিযোগের জন্য " + messageService.getMessage(codeRole) + " এর " + messageService.getMessage(codeAction) + " পদক্ষেপ";
        Integer countSmsTemplate = this.smsTemplateDAO.countBySmsTemplateName(templateName);
        if (!countSmsTemplate.equals(0)) {
            return false;
        }
        SmsTemplate smsTemplate = SmsTemplate.builder()
                .smsTemplateName(templateName)
                .smsTemplateBodyEng(smsTemplateDTO.getSmsTemplateBodyEng())
                .smsTemplateBodyBng(smsTemplateDTO.getSmsTemplateBodyBng())
                .status(smsTemplateDTO.getStatus())
                .language(LanguageStatus.valueOf(smsTemplateDTO.getLanguage()))
                .actionToRole(actionToRole)
                .build();
        List<String> recipients = smsTemplateDTO.getRecipient();
        this.smsTemplateDAO.saveSmsTemplate(smsTemplate, recipients);
        return true;
    }

    public Boolean updateSmsTemplate(Long smsTemplateID, SmsTemplateDTO smsTemplateDTO) {
        List<String> recipients = smsTemplateDTO.getRecipient();
        SmsTemplate smsTemplate = this.getSmsTemplate(smsTemplateID);
        smsTemplate.setSmsTemplateBodyBng(smsTemplateDTO.getSmsTemplateBodyBng());
        smsTemplate.setSmsTemplateBodyEng(smsTemplateDTO.getSmsTemplateBodyEng());
        smsTemplate.setStatus(smsTemplateDTO.getStatus());
        smsTemplate.setLanguage(LanguageStatus.valueOf(smsTemplateDTO.getLanguage()));
        this.smsTemplateDAO.updateSmsTemplate(smsTemplate, recipients);
        return true;
    }

    public SmsTemplate getSmsTemplate(Long smsTemplateID) {
        return this.smsTemplateDAO.findOne(smsTemplateID);
    }

    public SmsTemplate findSmsTemplate(ActionToRole actionToRole) {
        return this.smsTemplateDAO.findByActionToRole(actionToRole);
    }

    public SmsTemplateDTO convertToSmsTemplateDTO(SmsTemplate smsTemplate) {
        return SmsTemplateDTO.builder()
                .id(smsTemplate.getId())
                .smsTemplateName(smsTemplate.getSmsTemplateName())
                .smsTemplateBodyBng(smsTemplate.getSmsTemplateBodyBng())
                .smsTemplateBodyEng(smsTemplate.getSmsTemplateBodyEng())
                .status(smsTemplate.getStatus())
                .language(String.valueOf(smsTemplate.getLanguage()))
                //.actionToRoleId(smsTemplate.getActionToRole().getId())
                .build();
    }

    public SmsTemplateDTO convertToSmsTemplateDTOWithRecipient(SmsTemplate smsTemplate) {
        List<String> activeSmsRecipients = this.grsRoleToSmsDAO.findBySmsTemplateAndStatus(smsTemplate, true)
                .stream()
                .map(x -> {
                    return x.getGrsRole();
                })
                .collect(Collectors.toList());
        return SmsTemplateDTO.builder()
                .id(smsTemplate.getId())
                .smsTemplateName(smsTemplate.getSmsTemplateName())
                .smsTemplateBodyBng(smsTemplate.getSmsTemplateBodyBng())
                .smsTemplateBodyEng(smsTemplate.getSmsTemplateBodyEng())
                .status(smsTemplate.getStatus())
                .language(String.valueOf(smsTemplate.getLanguage()))
                .recipient(activeSmsRecipients)
                .build();

    }
}
