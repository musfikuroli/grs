package com.grs.api.config.security;

import com.grs.api.model.UserInformation;
import com.grs.api.model.UserType;
import com.grs.core.dao.ComplainantDAO;
import com.grs.core.dao.GrsRoleDAO;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.domain.grs.GrsRole;
import com.grs.core.service.LoginTraceService;
import com.grs.utils.BanglaConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Acer on 8/13/2017.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private ComplainantDAO userDAO;
    @Autowired
    private GrsRoleDAO roleDAO;
    @Autowired
    private LoginTraceService loginTraceService;

    @Override
    public UserDetailsImpl loadUserByUsername(String username) throws UsernameNotFoundException {
        Complainant user = this.userDAO.findByUsername(BanglaConverter.convertToEnglish(username));
        if (user == null) {
            throw new UsernameNotFoundException("Invalid credentials");
        }

        GrsRole role  = roleDAO.findByRole("COMPLAINANT");
        List<String> permissions = new ArrayList(){{
            add("ADD_PUBLIC_GRIEVANCES");
            add("DO_APPEAL");
        }};
        List<GrantedAuthorityImpl> grantedAuthorities = role.getPermissions().stream()
//                .filter(x ->(!permissions.contains(x.getName()))).collect(Collectors.toList())
//                .stream()
                .map(permission -> {
                    return GrantedAuthorityImpl.builder()
                            .role(permission.getName())
                            .build();
                }).collect(Collectors.toList());

        UserInformation userInformation = UserInformation
                .builder()
                .userId(user.getId())
                .username(user.getName())
                .userType(UserType.COMPLAINANT)
                .officeInformation(null)
                .oisfUserType(null)
                .isAppealOfficer(false)
                .build();

        loginTraceService.saveGRSLogin(userInformation);

        return UserDetailsImpl.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .isAccountAuthenticated(user.isAuthenticated())
                .grantedAuthorities(grantedAuthorities)
                .userInformation(userInformation)
                .build();
    }
}
