package com.grs.api.config.security;

import com.grs.api.model.UserInformation;
import com.grs.core.dao.GrsRoleDAO;
import com.grs.core.dao.UserDAO;
import com.grs.core.domain.grs.GrsRole;
import com.grs.core.domain.projapoti.User;
import com.grs.utils.BanglaConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Acer on 9/27/2017.
 */
@Slf4j
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserDAO userDAO;
    @Autowired
    private HttpServletResponse httpServletResponse;
    @Autowired
    private OISFPasswordService passwordService;
    @Autowired
    private OISFUserDetailsServiceImpl oisfUserDetailsService;
    @Autowired
    private GrsRoleDAO grsRoleDAO;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();
//        return  null;
        User user = this.userDAO.findByUsername(BanglaConverter.convertToEnglish(name));

        if (user != null
//                && passwordService.checkPassword(password, user.getPassword())
        ) {
            UserInformation userInformation = this.oisfUserDetailsService.getUserInfo(user);
            String roleName = null;
            if(userInformation.getGrsUserType() != null) {
                roleName = userInformation.getGrsUserType().name();
            } else {
                roleName = userInformation.getOisfUserType().name();
            }
            GrsRole grsRole = this.grsRoleDAO.findByRole(roleName);
            List<GrantedAuthorityImpl> grantedAuthorities = grsRole
                    .getPermissions()
                    .stream()
                    .map(permission -> {
                        return GrantedAuthorityImpl.builder()
                                .role(permission.getName())
                                .build();
                    }).collect(Collectors.toList());
            UsernamePasswordAuthenticationToken token = new CustomAuthenticationToken(name, password, grantedAuthorities, userInformation);
            return token;
        } else {
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
