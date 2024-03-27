package com.grs.api.config.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

/**
 * Created by Acer on 8/13/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GrantedAuthorityImpl implements GrantedAuthority {
    private String role;

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String getAuthority() {
        return this.role;
    }
}
