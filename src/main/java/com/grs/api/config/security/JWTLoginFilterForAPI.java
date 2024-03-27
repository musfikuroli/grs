package com.grs.api.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grs.api.model.UserInformation;
import com.grs.api.model.response.ErrorDTO;
import com.grs.utils.Constant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static com.grs.api.config.security.TokenAuthenticationServiceUtil.constuctJwtToken;
import static com.grs.utils.Constant.HEADER_STRING;

/**
 * Created by Tanvir on 4/18/2017.
 */
public class JWTLoginFilterForAPI extends AbstractAuthenticationProcessingFilter {

    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private final String USERNAME_REQUEST_PARAM = "username";
    private final String PASSWORD_REQUEST_PARAM = "password";

    public JWTLoginFilterForAPI(String url, AuthenticationManager authManager, BCryptPasswordEncoder bCryptPasswordEncoder) {
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
            Authentication authentication) throws IOException, ServletException {

        UserInformation userInformation;
        String name;
        Set<String> permissionNamesSet;
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            name = authentication.getName();
            permissionNamesSet = authentication.getAuthorities()
                    .stream()
                    .map(permission -> permission.getAuthority())
                    .collect(Collectors.toSet());
            userInformation = userDetails.getUserInformation();

        } catch (Exception e) {
            CustomAuthenticationToken token = (CustomAuthenticationToken) authentication;
            name = token.getName();
            permissionNamesSet = token.getAuthorities()
                    .stream()
                    .map(permission -> permission.getAuthority())
                    .collect(Collectors.toSet());
            userInformation = token.getUserInformation();
        }
        String JWT = constuctJwtToken(name, permissionNamesSet, userInformation);
        response.addHeader(HEADER_STRING,  JWT);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ObjectMapper mapper = new ObjectMapper();
        response.addHeader("content-type", "application/json;charset=UTF-8");
        mapper.writeValue(response.getWriter(), userInformation);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {

        String[] message = failed.getMessage().split(" ");
        String lastMessage = message[message.length - 1];

        ErrorDTO error  = ErrorDTO.builder()
                .message("")
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();

        if (lastMessage.compareTo("credentials") == 0) {
            error.setMessage("Username or Password is incorrect");
        } else if (lastMessage.compareTo("disabled") == 0) {
            error.setMessage("User is disabled");
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.addHeader("content-type", "application/json;charset=UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), error);
    }
}
