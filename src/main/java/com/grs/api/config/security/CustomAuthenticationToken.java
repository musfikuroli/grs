package com.grs.api.config.security;

import com.grs.api.model.UserInformation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Created by Acer on 10/4/2017.
 */
@Data
public class CustomAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private UserInformation userInformation;

    public CustomAuthenticationToken(Object principal,
                                     Object credentials,
                                     UserInformation userInformation) {
        super(principal, credentials);
        this.userInformation = userInformation;
    }

    public CustomAuthenticationToken(Object principal,
                                     Object credentials,
                                     Collection<? extends GrantedAuthority> authorities,
                                     UserInformation userInformation) {
        super(principal, credentials, authorities);
        this.userInformation = userInformation;
    }

}
