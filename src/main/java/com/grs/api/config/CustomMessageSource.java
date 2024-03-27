package com.grs.api.config;

import com.grs.core.dao.LanguageTextDAO;
import com.grs.core.domain.grs.LanguageText;
import com.grs.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

@Component
public class CustomMessageSource implements MessageSource {

    @Autowired
    private ReadFromProperty readFromProperty;

    @Autowired
    private LanguageTextDAO languageTextDAO;

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return null;
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        //TODO: use repository instead of database access.
        LanguageText languageText = languageTextDAO.getByLanguageConstant(code);
        String message;

        if(languageText == null){
            String fileName = Objects.equals(locale.getLanguage(), "fr") ? "messages_fr.properties" : "messages.properties";
            try {
                String value = readFromProperty.getPropValues(code, fileName);
                return value == null ? code : value;
            } catch (IOException e) {
                return code;
            }
        } else {
            switch (locale.getLanguage()){
                case "fr" :
                    message = languageText.getLanguageTextBangla();
                    break;
                default:
                    message = languageText.getLanguageTextEnglish();
            }
        }

        if (args != null) {
            StringBuilder stringBuilder = new StringBuilder(message);
            for(int i=0; i<args.length; i++){
                String arg = (String) args[i];
                String src = "{" + i + "}";
                StringUtil.replaceAll(stringBuilder, src, arg);
//                message = message.replace("{" + i + "}", arg);
            }
            message = stringBuilder.toString();
        }

        return message;
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        return null;
    }

}
