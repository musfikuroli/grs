package com.grs.core.service;

import com.grs.utils.CookieUtil;
import com.grs.utils.DateTimeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class MessageService {
    @Autowired
    MessageSource messageSource;

    private String languageCode;

    public HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if(requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
            return servletRequest;
        }
        return null;
    }

    public String getCurrentLanguageCode() {
        HttpServletRequest servletRequest = this.getCurrentHttpRequest();
        if(servletRequest != null) {
            languageCode = CookieUtil.getValue(servletRequest, "lang");
        }
        return languageCode == null ? "fr" : languageCode;
    }

    public String getMessage(String code) {
        return getMessage(code, null);
    }

    public String getMessageV2(String code) {
        return getMessageV2(code, null);
    }

    public String getMessage(String code, String[] args) {
        Locale locale = new Locale(getCurrentLanguageCode());
        return messageSource.getMessage(code, (Object[]) args, code, locale);
    }

    public String getMessageV2(String code, String[] args) {
        Locale locale = new Locale(getCurrentLanguageCode());
        return messageSource.getMessage(code, args, locale);
    }

    /**
     * Returns i18n encoded message
     * e.g. getMessageWithArgs("x.send.to.y", "sms", "user") should return "SMS send to user"
     * */
    public String getMessageWithArgs(String code, String... args) {
        for(int index = 0; index < args.length; index++) {
            args[index] = getMessage(args[index]);
        }
        return getMessage(code, args);
    }

    public String getMessageWithArgsV2(String code, String... args) {
        Locale locale = new Locale(getCurrentLanguageCode());
        if (args != null) {
            for(int index = 0; index < args.length; index++) {
                args[index] = getMessageV2(args[index]);
            }
        }
        return messageSource.getMessage(code, args, locale);
    }

    public Boolean isCurrentLanguageInEnglish() {
        return getCurrentLanguageCode().equals("en");
    }

    public String getCurrentMonthYearAsString() {
        return isCurrentLanguageInEnglish() ? DateTimeConverter.getCurrentMonthYearStringInEnglish(false) : DateTimeConverter.getCurrentMonthYearStringInBangla(false);
    }

    public String getCurrentYearString(){
        return isCurrentLanguageInEnglish() ? DateTimeConverter.getCurrentYearStringInBangla() : DateTimeConverter.getCurrentYearStringInBangla();
    }

    public String getCurrentDateMonthYearAsString() {
        return isCurrentLanguageInEnglish() ? DateTimeConverter.getCurrentMonthYearStringInEnglish(true) : DateTimeConverter.getCurrentMonthYearStringInBangla(true);
    }
}
