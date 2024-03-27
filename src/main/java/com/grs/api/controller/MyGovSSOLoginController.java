package com.grs.api.controller;

import com.google.gson.Gson;
import com.grs.api.config.security.GrantedAuthorityImpl;
import com.grs.api.config.security.OISFUserDetailsServiceImpl;
import com.grs.api.config.security.TokenAuthenticationServiceUtil;
import com.grs.api.config.security.UserDetailsImpl;
import com.grs.api.model.UserInformation;
import com.grs.api.model.request.ComplainantDTO;
import com.grs.api.myGov.MyGovTokenResponse;
import com.grs.api.myGov.MyGovUser;
import com.grs.api.sso.SSOPropertyReader;
import com.grs.core.config.IDP_Client;
import com.grs.core.dao.*;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.domain.grs.GrsRole;
import com.grs.core.service.*;
import com.grs.utils.Constant;
import com.grs.utils.CookieUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class MyGovSSOLoginController {
    @Autowired
    private CellService cellService;
    @Autowired
    private EmployeeOfficeDAO employeeOfficeDAO;
    @Autowired
    private OISFUserDetailsServiceImpl userDetailsService;
    @Autowired
    private GrsRoleDAO grsRoleDAO;
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
    private ComplainantService complainantService;
    @Autowired
    private GrievanceService grievanceService;
    @Autowired
    private GrsRoleDAO roleDAO;
    @Autowired
    private Gson gson;
    @Autowired
    private LoginTraceService loginTraceService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MyGovConnectorService myGovConnectorService;


    @RequestMapping(value = "/mygovlogout1", method = RequestMethod.GET)
    public ModelAndView getLogoutPage() throws Exception {


        IDP_Client idp = new IDP_Client(SSOPropertyReader.getInstance().getBaseUri() + Constant.myGovLogoutRedirectSuffix);
        String url = idp.logoutRequest();
        idp = null;
        return new ModelAndView("redirect:" + url);
    }


    @RequestMapping(value = "/afterLoginFromMyGov", method = RequestMethod.GET)
    public void redirectAfterMyGovLoginSuccessPOST(HttpServletResponse response, HttpServletRequest request, @RequestParam(value = "code") String code) throws IOException {

        try {
            log.info("MyGov Code : {} ", code);

            MyGovTokenResponse myGovTokenResponse = myGovConnectorService.getMyGovToken(code);

            if (myGovTokenResponse == null) {
                response.sendRedirect("/error-page");
            } else {

                HttpSession session = request.getSession();
                session.setAttribute("myGovTokenResponse", myGovTokenResponse);

                CookieUtil.create(response, Constant.MYGOV_ACCESS_TOKEN, myGovTokenResponse.getAccess_token());


                MyGovUser myGovUser = myGovConnectorService.getMyGovLoginUser(myGovTokenResponse.getAccess_token());

                if (myGovUser == null) {
                    response.sendRedirect("/error-page");
                } else {

                    ComplainantDTO complainantDTO = myGovUser.toComplainantDTO();

                    Complainant currentComplainant = this.complainantService.insertComplainantWithoutLogin(complainantDTO);

                    UserInformation userInformation = this.grievanceService.generateUserInformationForComplainant(currentComplainant);
                    userInformation.setIsMyGovLogin(true);
                    userInformation.setToken(myGovTokenResponse.getToken_type() + " " + myGovTokenResponse.getId_token());


                    loginTraceService.saveMyGovLogin(userInformation);

                    GrsRole role = roleDAO.findByRole("COMPLAINANT");
                    List<String> permissions = new ArrayList() {{
                        add("ADD_PUBLIC_GRIEVANCES");
                        add("DO_APPEAL");
                    }};
                    List<GrantedAuthorityImpl> grantedAuthorities = role.getPermissions().stream()

                            .map(permission -> {
                                return GrantedAuthorityImpl.builder().role(permission.getName()).build();
                            }).collect(Collectors.toList());

                    UserDetailsImpl userDetails = UserDetailsImpl.builder().username(currentComplainant.getUsername()).password(currentComplainant.getPassword()).isAccountAuthenticated(currentComplainant.isAuthenticated()).grantedAuthorities(grantedAuthorities).userInformation(userInformation).build();
                    try {
                        TokenAuthenticationServiceUtil.addAuthenticationForMyGov(userDetails, request, response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }


        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("/");
        }

    }

    @RequestMapping(value = "/afterLogoutFromMyGov", method = RequestMethod.POST)
    public void redirectAfterMyGovLogoutSuccessPOST(HttpServletResponse response, HttpServletRequest request, Authentication authentication) throws Exception {


        IDP_Client idp = new IDP_Client(SSOPropertyReader.getInstance().getBaseUri());
        String url = idp.responseRequest(request);
        idp = null;


        try {
//            SessionService sessionService = new SessionService();
//            AppSessionRepository appSessionRepository = AppSessionRepository.getInstance();
//            AppSessionDTO appSessionDTO = appSessionRepository.getSessionDetails(request.getSession(false).getId()).orElse(null);
//            if (appSessionDTO != null) {
//                sessionService.delete(appSessionDTO.getRealm(), appSessionDTO.getUsername());
//                AppSessionRepository.getAppSessions().remove(appSessionDTO.getId());
//            }
            request.getSession().invalidate();
            CookieUtil.clear(response, Constant.HEADER_STRING);

            if (url != null) response.sendRedirect(url);
            response.sendRedirect("/error-page");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("/error-page");
        }

    }

    @RequestMapping(value = "/mygovlogout", method = RequestMethod.GET)
    public void logoutFromMyGov(HttpServletResponse response, HttpServletRequest request, Authentication authentication) throws Exception {
        try {
//            MyGovTokenResponse myGovTokenResponse = (MyGovTokenResponse) request.getSession().getAttribute("myGovTokenResponse");
//            String access_token = myGovTokenResponse.getAccess_token();

            String access_token = CookieUtil.getValue(request, Constant.MYGOV_ACCESS_TOKEN);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + access_token);

            ResponseEntity<HashMap> myGovUserResponseEntity = restTemplate.exchange(SSOPropertyReader.getInstance().getMyGovApiBaseUri() + "/api/logout", HttpMethod.POST, new HttpEntity<String>(headers), HashMap.class);
        } catch (Exception e) {
            e.printStackTrace();
//            response.sendRedirect("/error-page");
        }

        request.getSession().invalidate();
        CookieUtil.clear(response, Constant.HEADER_STRING);
        CookieUtil.clear(response, Constant.MYGOV_ACCESS_TOKEN);

//            if (url != null) response.sendRedirect(url);
        response.sendRedirect("/");


    }
}
