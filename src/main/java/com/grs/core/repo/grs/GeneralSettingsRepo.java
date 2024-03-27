package com.grs.core.repo.grs;

import com.grs.core.domain.grs.GeneralSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneralSettingsRepo extends JpaRepository<GeneralSettings, Long> {
    GeneralSettings findByName(String name);
}
