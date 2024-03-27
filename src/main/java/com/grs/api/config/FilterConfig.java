package com.grs.api.config;

import com.grs.api.config.security.SharedApiRequestFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean sharedApiRequestFilter() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new SharedApiRequestFilter());
        registrationBean.addUrlPatterns("/api/shared/public/*");
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean viewPageRequestFilter() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new ViewPageRequestFilter());
        registrationBean.addUrlPatterns(
                "*.do",
                "/api/dashboard/*"
        );
        return registrationBean;
    }
}
