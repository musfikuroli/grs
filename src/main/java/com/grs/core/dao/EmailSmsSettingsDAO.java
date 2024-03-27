package com.grs.core.dao;

import com.grs.core.domain.grs.EmailSmsSettings;
import com.grs.core.repo.grs.EmailSmsSettingsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by HP on 3/13/2018.
 */
@Service
public class EmailSmsSettingsDAO {
    @Autowired
    private EmailSmsSettingsRepo emailSmsSettingsRepo;

    public List<EmailSmsSettings> findAll() {
        return this.emailSmsSettingsRepo.findAll();
    }

    public EmailSmsSettings findOne(Long id) {
        return emailSmsSettingsRepo.findOne(id);
    }

    public void saveSettings(EmailSmsSettings emailSmsSettings) {
        this.emailSmsSettingsRepo.save(emailSmsSettings);
    }
}
