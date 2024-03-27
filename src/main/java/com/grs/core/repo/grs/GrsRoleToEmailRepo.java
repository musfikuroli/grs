package com.grs.core.repo.grs;

import com.grs.core.domain.grs.EmailTemplate;
import com.grs.core.domain.grs.GrsRoleToEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by HP on 3/11/2018.
 */
@Repository
public interface GrsRoleToEmailRepo extends JpaRepository <GrsRoleToEmail, Long> {
    public List<GrsRoleToEmail> findByEmailTemplate(EmailTemplate emailTemplate);
    public List<GrsRoleToEmail> findByEmailTemplateAndStatus(EmailTemplate emailTemplate, Boolean status);
}
