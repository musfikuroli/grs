package com.grs.core.repo.grs;

import com.grs.core.domain.grs.LanguageText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Created by HP on 2/7/2018.
 */
@Repository
public interface LanguageTextRepo extends JpaRepository <LanguageText, Long> {
    List<LanguageText> findByLanguageConstant(String languageConstant);
}
