package com.grs.core.dao;

import com.grs.api.model.GrsRoleToSmsDTO;
import com.grs.core.domain.grs.ActionToRole;
import com.grs.core.domain.grs.SmsTemplate;
import com.grs.core.repo.grs.SmsTemplateRepo;
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
public class SmsTemplateDAO {
    @Autowired
    private SmsTemplateRepo smsTemplateRepo;
    @Autowired
    private GrsRoleToSmsDAO grsRoleToSmsDAO;

    public Page<SmsTemplate> findAll(Pageable pageable){
        return smsTemplateRepo.findAll(pageable);
    }


    public Integer countBySmsTemplateName(String smsTemplateName) {
        return this.smsTemplateRepo.countBySmsTemplateName(smsTemplateName);
    }

    @Transactional("transactionManager")
    public void saveSmsTemplate(SmsTemplate smsTemplate, List<String> recipients) {
        this.smsTemplateRepo.save(smsTemplate);
        if(recipients != null) {
            for (String person : recipients) {
                GrsRoleToSmsDTO grsRoleToSms = GrsRoleToSmsDTO.builder()
                        .grsRole(person)
                        .status(true)
                        .smsTemplateId(smsTemplate.getId())
                        .build();
                this.grsRoleToSmsDAO.saveSmsRecipient(grsRoleToSms);
            }
        }
    }

    public SmsTemplate findOne(Long smsTemplateID) {
        return this.smsTemplateRepo.findOne(smsTemplateID);
    }

    public SmsTemplate findByActionToRole(ActionToRole actionToRole) {
        return this.smsTemplateRepo.findByActionToRole(actionToRole);
    }

    int isSubscribedUser(List<GrsRoleToSmsDTO> notificationReceiver, String userRole) {
        int len = notificationReceiver.size();
        for (int idx=0; idx<len; ++idx) {
            if (notificationReceiver.get(idx).getGrsRole().equals(userRole)) {
                return idx;
            }
        }
        return -1;
    }

    List<GrsRoleToSmsDTO> unsubscribeAll(List<GrsRoleToSmsDTO> notificationReceiver) {
        int len = notificationReceiver.size();
        for (int idx=0; idx<len; ++idx) {
            notificationReceiver.get(idx).setStatus(false);
        }
        return notificationReceiver;
    }

    @Transactional("transactionManager")
    public void updateSmsTemplate(SmsTemplate smsTemplate, List<String> recipients) {
        this.smsTemplateRepo.save(smsTemplate);
        List<GrsRoleToSmsDTO> notificationReceiver = this.grsRoleToSmsDAO.findBySmsTemplate(smsTemplate);
        notificationReceiver = this.unsubscribeAll(notificationReceiver);
        if(recipients != null) {
            for (String person : recipients) {
                int index = this.isSubscribedUser(notificationReceiver, person);
                if (index!=-1) {
                    notificationReceiver.get(index).setStatus(true);
                }
                else {
                    notificationReceiver.add(GrsRoleToSmsDTO.builder()
                            .grsRole(person)
                            .status(true)
                            .smsTemplateId(smsTemplate.getId())
                            .build());
                }
            }
            this.grsRoleToSmsDAO.saveAll(notificationReceiver);
        }
    }

}
