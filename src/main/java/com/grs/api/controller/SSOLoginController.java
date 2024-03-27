package com.grs.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grs.api.config.security.OISFUserDetailsServiceImpl;
import com.grs.api.config.security.TokenAuthenticationServiceUtil;
import com.grs.api.model.OfficeInformation;
import com.grs.api.model.UserInformation;
import com.grs.api.sso.*;
import com.grs.core.dao.*;
import com.grs.core.domain.RedirectMap;
import com.grs.core.domain.grs.GrsRole;
import com.grs.core.domain.grs.OfficesGRO;
import com.grs.core.domain.grs.Permission;
import com.grs.core.service.CellService;
import com.grs.core.service.ESBConnectorService;
import com.grs.core.service.FcmService;
import com.grs.core.service.LoginTraceService;
import com.grs.utils.Constant;
import com.grs.utils.CookieUtil;
import com.grs.utils.StringUtil;
import com.grs.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class SSOLoginController {
    @Autowired
    private CellService cellService;
    @Autowired
    private EmployeeOfficeDAO employeeOfficeDAO;
    @Autowired
    private OISFUserDetailsServiceImpl userDetailsService;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private GrsRoleDAO grsRoleDAO;
    @Autowired
    private PermissionsToUsersDAO permissionsToUsersDAO;
    @Autowired
    private ESBConnectorService esbConnectorService;
    @Autowired
    private OfficesGroDAO officesGroDAO;
    @Autowired
    private CentralDashboardRecipientDAO centralDashboardRecipientDAO;
    @Autowired
    private FcmService fcmService;
    @Autowired
    private CellMemberDAO cellMemberDAO;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private LoginTraceService loginTraceService;

    @RequestMapping(value = "/applogin", method = RequestMethod.POST)
    public void jwtSSO(HttpServletRequest request, HttpServletResponse response, Principal principal) {
        try {
            String token = request.getParameter("token");
            log.info("Token from oisf response: " + token);
            LoginResponse loginResponse = new AppLoginResponse();
            String nonce = (String) request.getSession().getAttribute("nonce");
            loginResponse.setSessionNonce(nonce);
            log.info("Nonce from request session: " + nonce);
            SSOResponseDTO ssoResponseDTO = loginResponse.parseResponse(token);
            log.info("Login Parsed response: " + ssoResponseDTO.toString());
            UserInformation userInformation = this.userDetailsService.getUserInformationByApi(ssoResponseDTO.getUsername());

            loginTraceService.saveSSOLogin(userInformation);

            /*User user = userDAO.findByUsername(ssoResponseDTO.getUsername());
            UserInformation userInformation = this.userDetailsService.getUserInfo(user);*/

            if (userInformation == null) {
                log.info("User information null ");
                request.getSession().invalidate();
                response.sendRedirect("/ssologout");
                return;
            }
            log.info("user name: " + userInformation.getUsername());

            OfficeInformation officeInformation = userInformation.getOfficeInformation();
            Long officeUnitOrganogramId = officeInformation.getOfficeUnitOrganogramId();
            List<OfficesGRO> userAsAppealOfficerList = this.officesGroDAO.findByAppealOfficeUnitOrganogramId(officeUnitOrganogramId);
            List<OfficesGRO> userAsOfficeAdminList = officesGroDAO.findByAdminOfficeUnitOrganogramId(officeUnitOrganogramId);
            Boolean hasCentralDashboardAccess = centralDashboardRecipientDAO.hasAccessToCentralDashboard(userInformation.getOfficeInformation().getOfficeId(), officeUnitOrganogramId);
            Boolean isCellGRO = cellService.isCellGRO(officeInformation);

            userInformation.setIsAppealOfficer(userAsAppealOfficerList.size() > 0 || isCellGRO);
            userInformation.setIsOfficeAdmin(userAsOfficeAdminList.size() > 0);
            userInformation.setIsCentralDashboardUser(hasCentralDashboardAccess);
            userInformation.setIsCellGRO(isCellGRO);

            log.info("cell gro info : " + isCellGRO);

            GrsRole grsRole = this.grsRoleDAO.findByRole(userInformation.getOisfUserType().toString());
            Set<String> grantedAuthorities = grsRole
                    .getPermissions()
                    .stream()
                    .map(Permission::getName).collect(Collectors.toSet());

            grantedAuthorities.addAll(
                    this.permissionsToUsersDAO.findByOisfUserId(userInformation.getUserId())
                            .stream()
                            .map(permissionsToUsers -> permissionsToUsers.getPermission().getName())
                            .collect(Collectors.toSet())
            );

            String grsAuthToken = TokenAuthenticationServiceUtil.constuctJwtToken(ssoResponseDTO.getUsername(), grantedAuthorities, userInformation);
            response.addCookie(new Cookie(Constant.HEADER_STRING, grsAuthToken));

            if(ssoResponseDTO.getRedirectMap().equals(RedirectMap.DASHBOARD)){
                response.sendRedirect(ssoResponseDTO.getLandingPageUrl());
            } else {
                log.info(ssoResponseDTO.getRedirectMap().toString());
                response.sendRedirect(ssoResponseDTO.getRedirectMap().getRedirectUrl());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            request.getSession().invalidate();
            log.error(" Could not redirect to idp login page : " + ex.getLocalizedMessage());
        }
    }

    @RequestMapping(value = "/ssologout", method = RequestMethod.GET)
    public void ssologout(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.getSession().invalidate();
            AppLogoutRequest appLogoutRequest = new AppLogoutRequest();
            CookieUtil.clear(response, Constant.HEADER_STRING);
            response.sendRedirect(appLogoutRequest.buildLogoutRequest());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    @ResponseBody
    @RequestMapping(value = "/oisf/sso/user/apps", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getPermittedApps(Authentication authentication) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        if (userInformation.getOfficeInformation() == null) {
            return "{}";
        }

        Object token = esbConnectorService.getAuthenticationToken();
        String auth = token== null ? null : token.toString();
        Constant.OISF_ACCESS_TOKEN = auth;
        HttpHeaders headers = new HttpHeaders();
        headers.clear();
        headers.add("Authorization", "Bearer " + Constant.OISF_ACCESS_TOKEN);
        HttpEntity<String> newEntity = new HttpEntity<String>("parameters", headers);

        String url = esbConnectorService.getBaseUrlWithPort() +
                "/identity/designation/" +
                String.valueOf(userInformation.getOfficeInformation().getOfficeUnitOrganogramId()) +
                "/apps";

//        ResponseEntity responseEntity = restTemplate.getForEntity(url, Object[].class);
        ResponseEntity responseEntity = restTemplate.exchange(url, HttpMethod.GET, newEntity, Object[].class);
        try {
            String jsonInString = mapper.writeValueAsString(responseEntity.getBody());
            return jsonInString;
        } catch (Exception e) {
            return "{}";
        }
    }

    @RequestMapping(value = "/mobile-login", method = RequestMethod.POST)
    public void mobileLogin(HttpServletRequest request, HttpServletResponse response, @ModelAttribute LoginRequest loginRequest) {
        WeakHashMap<String, String> jsonVal = null;
        try {
            log.info("mobile login");

            Object token = this.esbConnectorService.getAuthenticationToken();
            log.info(token == null ? "No token found" : token.toString());
            if(token != null){
                Constant.OISF_ACCESS_TOKEN = token.toString();
            }

            jsonVal = this.esbConnectorService.getLoginAuthorization(token.toString(), loginRequest.getUsername(), loginRequest.getPassword(), "nonce");

            log.info("mobile uname-> " + jsonVal.get("username"));
            UserInformation userInformation = this.userDetailsService.getUserInformationByApi(jsonVal.get("username"));

            if (userInformation == null) {
                request.getSession().invalidate();
                response.sendRedirect("/login?a=2");
                return;
            }

            String deviceToken = request.getParameter("device_token");
            if(StringUtil.isValidString(deviceToken)) {
                fcmService.registerDeviceToken(deviceToken, userInformation.getOfficeInformation().getEmployeeRecordId().toString());
                userInformation.setIsMobileLogin(true);
            }

            OfficeInformation officeInformation = userInformation.getOfficeInformation();
            Long officeUnitOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();
            List<OfficesGRO> userAsAppealOfficerList = this.officesGroDAO.findByAppealOfficeUnitOrganogramId(officeUnitOrganogramId);
            List<OfficesGRO> userAsOfficeAdminList = officesGroDAO.findByAdminOfficeUnitOrganogramId(officeUnitOrganogramId);
            Boolean hasCentralDashboardAccess = centralDashboardRecipientDAO.hasAccessToCentralDashboard(userInformation.getOfficeInformation().getOfficeId(), officeUnitOrganogramId);
            Boolean isCellGRO = cellMemberDAO.isCellGRO(officeInformation.getOfficeId(), officeInformation.getEmployeeRecordId());

            log.info("officeUnitOrganogramId -> " + officeUnitOrganogramId);
            userInformation.setIsAppealOfficer(userAsAppealOfficerList.size() > 0);
            userInformation.setIsOfficeAdmin(userAsOfficeAdminList.size() > 0);
            userInformation.setIsCentralDashboardUser(hasCentralDashboardAccess);
            userInformation.setIsCellGRO(isCellGRO);

            GrsRole grsRole = this.grsRoleDAO.findByRole(userInformation.getOisfUserType().toString());
            Set<String> grantedAuthorities = grsRole
                    .getPermissions()
                    .stream()
                    .map(Permission::getName).collect(Collectors.toSet());

            grantedAuthorities.addAll(
                    this.permissionsToUsersDAO.findByOisfUserId(userInformation.getUserId())
                            .stream()
                            .map(permissionsToUsers -> permissionsToUsers.getPermission().getName())
                            .collect(Collectors.toSet())
            );

            String grsAuthToken = TokenAuthenticationServiceUtil.constuctJwtToken(jsonVal.get("username"),
                    grantedAuthorities, userInformation);
            response.addCookie(new Cookie(Constant.HEADER_STRING, grsAuthToken));
            response.sendRedirect("/login/success");

        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().invalidate();
            try {
                response.sendRedirect("/login?a=2");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
