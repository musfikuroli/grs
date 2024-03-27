package com.grs.core.repo.grs;

import com.grs.api.model.GrsRoleToEmailDTO;
import com.grs.core.domain.grs.ActionToRole;
import com.grs.core.domain.grs.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by HP on 2/6/2018.
 */
@Repository
public interface EmailTemplateRepo extends JpaRepository<EmailTemplate, Long> {
    Integer countByEmailTemplateName(String emailTemplateName);
    EmailTemplate findByActionToRole(ActionToRole actionToRole);

}
