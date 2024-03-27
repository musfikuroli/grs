package com.grs.api.config;

import com.grs.utils.Constant;
import com.grs.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import javax.servlet.FilterConfig;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Order(1)
public class ViewPageRequestFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        String queryString = request.getQueryString();
        if (!StringUtil.isValidString(queryString)) log.info("View Page Request : {}", request.getRequestURI());
        else log.info("View Page Request : {}", request.getRequestURI() + "?" + queryString);
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {}
}
