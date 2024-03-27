package com.grs.api.config.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * Created by Tanvir on 4/18/2017.
 */
public class JWTLoginFilter extends AbstractAuthenticationProcessingFilter {

    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private final String USERNAME_REQUEST_PARAM = "username";
    private final String PASSWORD_REQUEST_PARAM = "password";

    public JWTLoginFilter(String url,
                          AuthenticationManager authManager,
                          BCryptPasswordEncoder bCryptPasswordEncoder) {
        super(new AntPathRequestMatcher(url, "POST"));
        setAuthenticationManager(authManager);
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest req, HttpServletResponse res)
            throws AuthenticationException, IOException, ServletException {

        String username = req.getParameter(USERNAME_REQUEST_PARAM);
        String password = req.getParameter(PASSWORD_REQUEST_PARAM);

        return getAuthenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password,
                        Collections.emptyList()
                )
        );
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response, FilterChain chain,
            Authentication auth) throws IOException, ServletException {

        TokenAuthenticationServiceUtil.addAuthentication(auth, request, response);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        String redirectUrl = "?a=" + request.getParameter("a");
        if (request.getParameter("redirectUrl") != null) {
            redirectUrl += "&redirectUrl=" + request.getParameter("redirectUrl");
        }
        if(failed.getMessage() == null) {
            response.sendRedirect("/login" + redirectUrl + "&error");
            return;
        }
        String[] message = failed.getMessage().split(" ");
        String lastMessage = message[message.length - 1];
        if (lastMessage.compareTo("credentials") == 0) {
            response.sendRedirect("/login" + redirectUrl + "&error");
        } else if (lastMessage.compareTo("disabled") == 0) {
            response.sendRedirect("/login" + redirectUrl + "&disabled");
        }
    }
}
