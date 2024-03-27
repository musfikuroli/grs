package com.grs.api.config.security;

import com.grs.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Morshed on 4/13/2017.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private OISFUserDetailsServiceImpl oisfUserDetailsService;
    @Autowired
    private GRSUserDetailsServiceImpl grsUserDetailsService;
    @Autowired
    private CustomAuthenticationProvider customAuthenticationProvider;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().configurationSource(request -> {
            CorsConfiguration cors = new CorsConfiguration();
                    cors.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
                    cors.setAllowedOrigins(Collections.singletonList("*"));
                    cors.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PUT","OPTIONS","PATCH", "DELETE"));
                    cors.setAllowCredentials(true);
                    cors.setExposedHeaders(Collections.singletonList("Authorization"));
            return cors;
        }).and().csrf().disable().authorizeRequests()
                .antMatchers("/register", "/").permitAll()
                .antMatchers("/api/**").permitAll()
                .antMatchers("/viewCitizenCharter.do").hasAnyAuthority("VIEW_CITIZEN_CHARTER")
                .antMatchers("/addCitizenCharter.do").hasAnyAuthority("ADD_CITIZEN_CHARTER")
                .antMatchers("/editCitizenCharter.do").hasAnyAuthority("EDIT_CITIZEN_CHARTER")
                .antMatchers("/viewServices.do").hasAnyAuthority("VIEW_SERVICES")
                .antMatchers("/addServices.do").hasAnyAuthority("ADD_SERVICES")
                .antMatchers("/editServices.do").hasAnyAuthority("EDIT_SERVICES")
                .antMatchers("/viewComplaints.do").hasAnyAuthority("VIEW_SERVICES")
                .antMatchers("/addComplaints.do").hasAnyAuthority("ADD_SERVICES")
                .antMatchers("/editComplaints.do").hasAnyAuthority("EDIT_SERVICES")
                .antMatchers("/addPublicGrievances.do").hasAnyAuthority("ADD_PUBLIC_GRIEVANCES")
                .antMatchers("/complainForOthers.do").hasAnyAuthority("ADD_PUBLIC_GRIEVANCES")
                .antMatchers("/api/grievance/forward/appeal").hasAnyAuthority("APPEAL")
                .antMatchers("/addOfflineGrievance.do").hasAnyAuthority("OFFLINE_GRIEVANCE_UPLOAD")
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
                .logoutSuccessHandler((httpServletRequest, httpServletResponse, authentication) -> {
                    CookieUtil.clear(httpServletResponse, "Authorization");
                    httpServletResponse.sendRedirect("/");
                })
                .and()
                .addFilterBefore(new JWTLoginFilter("/login", authenticationManager(), bCryptPasswordEncoder), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JWTLoginFilterForAPI("/api/login", authenticationManager(), bCryptPasswordEncoder), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JWTAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(this.userDetailsService).passwordEncoder(bCryptPasswordEncoder);
        auth.userDetailsService(this.grsUserDetailsService).passwordEncoder(bCryptPasswordEncoder);
        auth.authenticationProvider(customAuthenticationProvider);
    }
}