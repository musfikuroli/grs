package com.grs.core.repo.grs;

import com.grs.core.domain.grs.GrsRoleToSms;
import com.grs.core.domain.grs.SmsTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by HP on 3/12/2018.
 */
@Repository
public interface GrsRoleToSmsRepo extends JpaRepository <GrsRoleToSms,Long> {
    public List<GrsRoleToSms> findBySmsTemplateAndStatus(SmsTemplate smsTemplate, Boolean status);
    public List<GrsRoleToSms> findBySmsTemplate(SmsTemplate smsTemplate);
}
