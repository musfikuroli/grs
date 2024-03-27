package com.grs.core.service;

import com.grs.api.model.response.ActionToRoleDTO;
import com.grs.api.model.response.EmailTemplateDTO;
import com.grs.core.dao.*;
import com.grs.core.domain.*;
import com.grs.core.domain.grs.*;
import com.grs.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Acer on 18-Dec-17.
 */
@Slf4j
@Service
public class EmailService {
    @Autowired
    private JavaMailSender emailSender;
    @Autowired
    private EmailTemplateDAO emailTemplateDAO;
    @Autowired
    private ActionToRoleDAO actionToRoleDAO;
    @Autowired
    private ActionDAO actionDAO;
    @Autowired
    private GrievanceStatusDAO grievanceStatusDAO;
    @Autowired
    private GrsRoleDAO grsRoleDAO;
    @Autowired
    private MessageService messageService;
    @Autowired
    private GrsRoleToEmailDAO grsRoleToEmailDAO;
    @Autowired
    private Environment environment;

    @Value("${spring.mail.username}")
    private String sentFrom;

    @Async("threadPoolTaskExecutor")
    public void sendEmail(String to, String subject, String text) {

        MimeMessage message = this.emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        try {
            if(!Boolean.valueOf(environment.getProperty("environment.production"))) {
                return;
            }
            helper.setFrom(sentFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            this.emailSender.send(message);
            log.info("email sent to " + to);
        } catch (Exception e) {
            log.info("email sending failed for: " + to);
            //e.printStackTrace();
        }

    }

    @Async("threadPoolTaskExecutor")
    public void sendEmailUsingDB(String to, EmailTemplate emailTemplate, Grievance grievance) {
        if(!Boolean.valueOf(environment.getProperty("environment.production"))) {
            return;
        }
        ActionToRole actionToRole = emailTemplate.getActionToRole();
        String body = null;
        String subject = null;
        String trackingNumber = (StringUtil.isValidString(grievance.getTrackingNumber())
                && (grievance.getTrackingNumber().startsWith("01"))) ?
                grievance.getTrackingNumber().substring(11) :
                grievance.getTrackingNumber();
        if (emailTemplate.getLanguage().toString().equals("BANGLA")) {
            body = findEmailTemplate(actionToRole).getEmailTemplateBodyBng() + " Tracking number: " + trackingNumber;
            subject = findEmailTemplate(actionToRole).getEmailTemplateSubjectBng();
        } else {
            body = findEmailTemplate(actionToRole).getEmailTemplateBodyEng() + " Tracking number: " + trackingNumber;
            subject = findEmailTemplate(actionToRole).getEmailTemplateSubjectEng();
        }
        MimeMessage message = this.emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        try {
            helper.setFrom(sentFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            this.emailSender.send(message);
            log.info("email sent to " + to);
        } catch (Exception e) {
            log.info("email sending failed for: " + to);
            //e.printStackTrace();
        }

    }

    public Page<EmailTemplateDTO> findAllEmailTemplates(Pageable pageable) {
        Page<EmailTemplateDTO> emailTemplateDTOS = this.emailTemplateDAO.findAll(pageable).map(this::convertToEmailTemplateDTO);
        return emailTemplateDTOS;
    }

    public EmailTemplateDTO convertToEmailTemplateDTO(EmailTemplate emailTemplate) {
        return EmailTemplateDTO.builder()
                .id(emailTemplate.getId())
                .emailTemplateName(emailTemplate.getEmailTemplateName())
                .emailTemplateSubjectBng(emailTemplate.getEmailTemplateSubjectBng())
                .emailTemplateSubjectEng(emailTemplate.getEmailTemplateSubjectEng())
                .emailTemplateBodyBng(emailTemplate.getEmailTemplateBodyBng())
                .emailTemplateBodyEng(emailTemplate.getEmailTemplateBodyEng())
                .status(emailTemplate.getStatus())
                .language(String.valueOf(emailTemplate.getLanguage()))
                //.actionToRoleId(emailTemplate.getActionToRole().getId())
                .build();
    }

    public EmailTemplateDTO convertToEmailTemplateDTOWithRecipient(EmailTemplate emailTemplate) {
        List<String> activeEmailRecipients = this.grsRoleToEmailDAO.findByEmailTemplateAndStatus(emailTemplate, true)
                .stream()
                .map(x->{
                    return x.getGrsRole();
                })
                .collect(Collectors.toList());
        return EmailTemplateDTO.builder()
                .id(emailTemplate.getId())
                .emailTemplateName(emailTemplate.getEmailTemplateName())
                .emailTemplateSubjectBng(emailTemplate.getEmailTemplateSubjectBng())
                .emailTemplateSubjectEng(emailTemplate.getEmailTemplateSubjectEng())
                .emailTemplateBodyBng(emailTemplate.getEmailTemplateBodyBng())
                .emailTemplateBodyEng(emailTemplate.getEmailTemplateBodyEng())
                .status(emailTemplate.getStatus())
                .language(String.valueOf(emailTemplate.getLanguage()))
                .recipient(activeEmailRecipients)
                .build();
    }

    public Boolean saveAllEmailTemplates(EmailTemplateDTO emailTemplateDTO, ActionToRoleDTO actionToRoleDTO) {
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
        Integer countEmailTemplate = this.emailTemplateDAO.countByEmailTemplateName(templateName);
        if (!countEmailTemplate.equals(0)) {
            return false;
        }

        EmailTemplate emailTemplate = EmailTemplate.builder()
                .emailTemplateName(templateName)
                .emailTemplateSubjectEng(emailTemplateDTO.getEmailTemplateSubjectEng())
                .emailTemplateSubjectBng(emailTemplateDTO.getEmailTemplateSubjectBng())
                .emailTemplateBodyEng(emailTemplateDTO.getEmailTemplateBodyEng())
                .emailTemplateBodyBng(emailTemplateDTO.getEmailTemplateBodyBng())
                .status(emailTemplateDTO.getStatus())
                .language(LanguageStatus.valueOf(emailTemplateDTO.getLanguage()))
                .actionToRole(actionToRole)
                .build();
        List<String> recipients = emailTemplateDTO.getRecipient();
        this.emailTemplateDAO.saveEmailTemplate(emailTemplate,recipients);
        return true;
    }

    public EmailTemplate getEmailTemplate(Long id) {
        return this.emailTemplateDAO.findOne(id);
    }

    public EmailTemplate findEmailTemplate(ActionToRole actionToRole) {
        return this.emailTemplateDAO.findByActionToRole(actionToRole);
    }

    public Boolean updateEmailTemplate(Long emailTemplateID, EmailTemplateDTO emailTemplateDTO) {
        List<String> recipients = emailTemplateDTO.getRecipient();
        EmailTemplate emailTemplate = this.getEmailTemplate(emailTemplateID);
        emailTemplate.setEmailTemplateBodyEng(emailTemplateDTO.getEmailTemplateBodyEng());
        emailTemplate.setEmailTemplateBodyBng(emailTemplateDTO.getEmailTemplateBodyBng());
        emailTemplate.setEmailTemplateSubjectEng(emailTemplateDTO.getEmailTemplateSubjectEng());
        emailTemplate.setEmailTemplateSubjectBng(emailTemplateDTO.getEmailTemplateSubjectBng());
        emailTemplate.setStatus(emailTemplateDTO.getStatus());
        emailTemplate.setLanguage(LanguageStatus.valueOf(emailTemplateDTO.getLanguage()));
        this.emailTemplateDAO.updateEmailTemplate(emailTemplate,recipients);
        return true;
    }

}
