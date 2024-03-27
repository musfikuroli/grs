package com.grs.core.dao;

import com.grs.api.model.GrsRoleToEmailDTO;
import com.grs.core.domain.grs.ActionToRole;
import com.grs.core.domain.grs.EmailTemplate;
import com.grs.core.repo.grs.EmailTemplateRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by HP on 2/6/2018.
 */
@Service
public class EmailTemplateDAO {
    @Autowired
    private EmailTemplateRepo emailTemplateRepo;
    @Autowired
    private GrsRoleToEmailDAO grsRoleToEmailDAO;

    public Page<EmailTemplate> findAll(Pageable pageable) {
        return emailTemplateRepo.findAll(pageable);
    }

    @Transactional("transactionManager")
    public void saveEmailTemplate(EmailTemplate emailTemplate, List<String> recipients) {
        this.emailTemplateRepo.save(emailTemplate);
        for (String person : recipients) {
            GrsRoleToEmailDTO grsRoleToEmail = GrsRoleToEmailDTO.builder()
                    .grsRole(person)
                    .status(true)
                    .emailTemplateId(emailTemplate.getId())
                    .build();
            this.grsRoleToEmailDAO.saveEmailRecipient(grsRoleToEmail);
        }
    }

    int isSubscribedUser(List<GrsRoleToEmailDTO> notificationReceiver, String userRole) {
        int len = notificationReceiver.size();
        for (int idx=0; idx<len; ++idx) {
            if (notificationReceiver.get(idx).getGrsRole().equals(userRole)) {
                return idx;
            }
        }
        return -1;
    }

    List<GrsRoleToEmailDTO> unsubscribeAll(List<GrsRoleToEmailDTO> notificationReceiver) {
        int len = notificationReceiver.size();
        for (int idx=0; idx<len; ++idx) {
            notificationReceiver.get(idx).setStatus(false);
        }
        return notificationReceiver;
    }

    @Transactional("transactionManager")
    public void updateEmailTemplate(EmailTemplate emailTemplate, List<String> recipients) {
        this.emailTemplateRepo.save(emailTemplate);
        List<GrsRoleToEmailDTO> notificationReceiver = this.grsRoleToEmailDAO.findByEmailTemplate(emailTemplate);
        notificationReceiver = this.unsubscribeAll(notificationReceiver);
        if(recipients != null) {
            for (String person : recipients) {
                int index = this.isSubscribedUser(notificationReceiver, person);
                if (index!=-1) {
                    notificationReceiver.get(index).setStatus(true);
                }
                else {
                    notificationReceiver.add(GrsRoleToEmailDTO.builder()
                            .grsRole(person)
                            .status(true)
                            .emailTemplateId(emailTemplate.getId())
                            .build());
                }
            }
            this.grsRoleToEmailDAO.saveAll(notificationReceiver);
        }
    }

    public EmailTemplate findOne(Long id) {
        return this.emailTemplateRepo.findOne(id);
    }

    public Integer countByEmailTemplateName(String emailTemplateName) {
        return this.emailTemplateRepo.countByEmailTemplateName(emailTemplateName);
    }

    public EmailTemplate findByActionToRole(ActionToRole actionToRole) {
        return this.emailTemplateRepo.findByActionToRole(actionToRole);
    }

}
