package com.grs.core.dao;

import com.grs.core.domain.grs.LanguageText;
import com.grs.core.repo.grs.LanguageTextRepo;
import com.grs.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by HP on 2/7/2018.
 */
@Service
public class LanguageTextDAO {
    @Autowired
    private LanguageTextRepo languageTextRepo;

    public List<LanguageText> getAll(){
        return this.languageTextRepo.findAll();
    }

    public LanguageText getByLanguageConstant(String languageConstant){
        if (!StringUtil.isValidString(languageConstant)) return null;
        String replacedConstant = languageConstant.replace(".", "_").toUpperCase();
        /*return LanguageText.builder()
                .languageTextBangla(languageConstant)
                .languageTextEnglish(replacedConstant)
                .build();*/
        List<LanguageText> languageTexts = languageTextRepo.findByLanguageConstant(replacedConstant);
        return languageTexts.size() == 0 ? null : languageTexts.get(0);
    }
}
