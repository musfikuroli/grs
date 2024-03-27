package com.grs.api.controller;

import com.grs.api.config.security.TokenAuthenticationServiceUtil;
import com.grs.core.service.ModelViewService;
import com.grs.utils.BanglaConverter;
import com.grs.utils.CookieUtil;
import com.grs.core.service.ModelViewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Acer on 9/14/2017.
 */
@Slf4j
@RestController
public class CustomErrorController extends AbstractErrorController {

    private static final String PATH = "/error";

    public CustomErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    @RequestMapping(value = PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> handleError(HttpServletRequest request) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, true);
        return errorAttributes;
    }

    @RequestMapping(value = "/error-page")
    public ModelAndView error(HttpServletRequest request, Model model, HttpServletResponse response) throws IOException {
        if(TokenAuthenticationServiceUtil.getAuthentication(request) == null) {
            response.sendRedirect("/");
        }
        int errorCode = response.getStatus();
        if (errorCode == 200) {
            errorCode = 404;
        }
        model.addAttribute("lang", CookieUtil.getValue(request, "lang"));
        model.addAttribute("errorCode", errorCode);
        model.addAttribute("errorCodeBng", BanglaConverter.convertToBanglaDigit(errorCode));
        return new ModelAndView("error");
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}