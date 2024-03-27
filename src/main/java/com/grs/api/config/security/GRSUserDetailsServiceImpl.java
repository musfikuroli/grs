package com.grs.api.config.security;

import com.grs.api.model.GRSUserType;
import com.grs.api.model.UserInformation;
import com.grs.api.model.UserType;
import com.grs.core.dao.SuperAdminDAO;
import com.grs.core.domain.grs.GrsRole;
import com.grs.core.domain.grs.SuperAdmin;
import com.grs.core.service.LoginTraceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GRSUserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private SuperAdminDAO superAdminDAO;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private LoginTraceService loginTraceService;

    @Override
    public UserDetailsImpl loadUserByUsername(String username) throws UsernameNotFoundException {
        SuperAdmin user = this.superAdminDAO.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Invalid credentials");
        }

        GrsRole role = user.getRole();
        List<GrantedAuthorityImpl> grantedAuthorities = role.getPermissions().stream()
                .map(permission -> {
                    return GrantedAuthorityImpl.builder()
                            .role(permission.getName())
                            .build();
                }).collect(Collectors.toList());

        UserInformation userInformation = this.getUserInfo(user);

        loginTraceService.saveGRSLogin(userInformation);

        return UserDetailsImpl.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .grantedAuthorities(grantedAuthorities)
                .userInformation(userInformation)
                .isAccountAuthenticated(true)
                .build();
    }

    public UserInformation getUserInfo(SuperAdmin user) {
        return UserInformation
                .builder()
                .userId(user.getId())
                .username(user.getUsername())
                .userType(UserType.SYSTEM_USER)
                .oisfUserType(null)
                .isOfficeAdmin(false)
                .isAppealOfficer(false)
                .isCellGRO(false)
                .grsUserType(getGRSUserTypeFromRole(user))
                .build();
    }

    public GRSUserType getGRSUserTypeFromRole(SuperAdmin user) {
        return GRSUserType.valueOf(user.getRole().getRole());
    }
}
