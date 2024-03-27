package com.grs.core.repo.grs;

import com.grs.core.domain.grs.EmailSmsSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by HP on 3/13/2018.
 */
@Repository
public interface EmailSmsSettingsRepo extends JpaRepository<EmailSmsSettings, Long> {

}
