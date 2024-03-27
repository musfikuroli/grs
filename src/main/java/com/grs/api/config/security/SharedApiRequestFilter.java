package com.grs.api.config.security;

import com.grs.utils.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Order(1)
public class SharedApiRequestFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        log.info("Shared API Request  {} : {}", request.getMethod(), request.getRequestURI());
        String auth = request.getHeader(Constant.API_CLIENT_TOKEN_HEADER);
        chain.doFilter(req, res);
        /*if(!StringUtil.isValidString(auth)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The token is not valid");
            return;
        }
        String[] authValues = auth.trim().replaceAll("\\s+", " ").split(" ");
        if(Constant.API_CLIENT_TOKEN_PREFIX.equals(authValues[0]) && StringUtil.isValidString(authValues[1])) {
            String accessToken = authValues[1];
            ApiClientDAO apiClientDAO = BeanUtil.bean(ApiClientDAO.class);
            ApiClient apiClient = apiClientDAO.getByAccessToken(accessToken);
            if(apiClient == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The token is not valid");
            } else if((apiClient.getExpiryTime().getTime() - (new Date()).getTime()) <= 0) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The token has been expired");
            } else {
                chain.doFilter(req, res);
            }
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The token is not valid");
        }*/
        log.info("Shared API Response :{}", response.getContentType());
    }

    @Override
    public void destroy() {}
}
