package com.grs.core.repo.grs;

import com.grs.core.domain.grs.ActionToRole;
import com.grs.core.domain.grs.SmsTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by HP on 2/6/2018.
 */
@Repository
public interface SmsTemplateRepo extends JpaRepository<SmsTemplate,Long>{
    Integer countBySmsTemplateName(String smsTemplateName);

    SmsTemplate findByActionToRole(ActionToRole actionToRole);
}
