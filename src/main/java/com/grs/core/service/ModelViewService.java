package com.grs.core.service;

import com.grs.api.model.NotificationsDTO;
import com.grs.api.model.OISFUserType;
import com.grs.api.model.UserInformation;
import com.grs.api.model.UserType;
import com.grs.api.model.response.menu.SubMenuDTO;
import com.grs.utils.CookieUtil;
import com.grs.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Acer on 9/11/2017.
 */
@Slf4j
@Service
public class ModelViewService {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private Environment environment;

    public ModelAndView addNecessaryAttributesAndReturnViewPage(Model model,
                                                                       Authentication authentication,
                                                                       HttpServletRequest request,
                                                                       String mainPageFragmentName,
                                                                       String mainPageFragmentValue,
                                                                       String viewName) {

        List<String> permissions = authentication.getAuthorities()
                .stream()
                .map(grantedAuthorityImpl -> grantedAuthorityImpl.getAuthority())
                .collect(Collectors.toList());

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        String username = userInformation.getUsername();
        if (userInformation.getUserType().equals(UserType.OISF_USER) &&
                !userInformation.getOisfUserType().equals(OISFUserType.SUPER_ADMIN)) {
            username = userInformation.getOfficeInformation().getName();
        }
        String languageCode = CookieUtil.getValue(request, "lang");

        model.addAttribute("name", username);
        model.addAttribute("fragmentPath", mainPageFragmentName);
        model.addAttribute("fragmentName", mainPageFragmentValue);
        model.addAttribute("lang", languageCode);
        model.addAttribute("permissions", permissions);
        model.addAttribute("isProductionMode", Boolean.valueOf(environment.getProperty("environment.production")));

        Boolean isGrsUser = Utility.isUserAnGRSUser(authentication);
        Boolean isOthersComplainant = Utility.isUserAnOthersComplainant(authentication);
        if(!isGrsUser && !userInformation.getUserType().equals(UserType.SYSTEM_USER)){
            NotificationsDTO notifications = this.notificationService.findByOfficeIdAndEmployeeRecordIdAndOfficeUnitOrganogramIdOrderByIdDesc(
                    userInformation.getOfficeInformation().getOfficeId(), userInformation.getOfficeInformation().getEmployeeRecordId(), userInformation.getOfficeInformation().getOfficeUnitOrganogramId()
            );
            model.addAttribute("notificationList", notifications);
        }
        Boolean isMyGovLogin = userInformation.getIsMyGovLogin() != null && userInformation.getIsMyGovLogin();
        Boolean isMobileLogin = Utility.isLoggedInFromMobile(authentication);
        Boolean isCellGro = userInformation.getIsCellGRO() == null ? false : userInformation.getIsCellGRO();
        Boolean showDrawerMenu = isMobileLogin && !isGrsUser;
        model.addAttribute("grsUser", isGrsUser);
        model.addAttribute("isOthersComplainant", isOthersComplainant);
        model.addAttribute("isGro", Utility.isUserAnGROUser(authentication));
        model.addAttribute("isAppealOfficer", (userInformation.getIsAppealOfficer() || isCellGro));
        model.addAttribute("isOfficeAdmin", userInformation.getIsOfficeAdmin());
        model.addAttribute("isHoo", Utility.isUserAHOOUser(authentication));
        model.addAttribute("isFieldCoordinator", Utility.isFieldCoordinator(authentication));
        model.addAttribute("isCentralDashboardRecipient", Utility.isUserACentralDashboardRecipient(authentication));
        model.addAttribute("isMobileLogin", isMobileLogin);
        model.addAttribute("showDrawerMenu", showDrawerMenu);
        Boolean isMinistryLevel = false;
        Boolean isAdmin = false;

        if(!isGrsUser) {
            isMinistryLevel = userInformation.getIsOfficeAdmin() && userInformation.getOfficeInformation().getLayerLevel().equals(1L);
            isAdmin = userInformation.getIsOfficeAdmin();
        }

        model.addAttribute("isMinistryLevelAdmin",  isMinistryLevel);
        model.addAttribute("isAdmin",  isAdmin);

        if (!model.containsAttribute("superAdmin")){
            model.addAttribute("superAdmin",Utility.isUserASuperAdmin(authentication));
        }
        if(!model.containsAttribute("showAllOffices")) {
            model.addAttribute("showAllOffices",Utility.isUserASuperAdmin(authentication));
        }
        if(!model.containsAttribute("showChildOfficesOnly")) {
            model.addAttribute("showChildOfficesOnly",false);
        }
        Boolean isCellMember = false;
        if (userInformation.getUserType().equals(UserType.OISF_USER) && !userInformation.getOisfUserType().equals(OISFUserType.SUPER_ADMIN)
                && userInformation.getOfficeInformation().getOfficeId() == 0 ){
            isCellMember = true;
        }
        model.addAttribute("isMyGovLogin", isMyGovLogin);
        model.addAttribute("isCellMember", isCellMember);
        model.addAttribute("isCellGRO", Utility.isCellGRO(authentication));
        model.addAttribute("canViewDashboard", Utility.canViewDashboard(authentication));
        model.addAttribute("token", userInformation.getToken());

        viewName = showDrawerMenu ? "admin_mobile" : viewName;
        return new ModelAndView(viewName);
    }

    public ModelAndView returnViewsForNormalPages(Authentication authentication,
                                                         Model model,
                                                         HttpServletRequest request,
                                                         String viewName) {
        SubMenuDTO subMenuDTO;
        boolean isLoggedIn, isGrsUser, isOisfUser, isMobileLogin, isOthersComplainant, isMyGovLogin;
        String token = "";
        if(authentication != null) {
            subMenuDTO = SubMenuDTO.builder()
                    .nameEnglish("Panel")
                    .nameBangla("প্যানেল")
                    .link("/dashboard.do")
                    .build();
            UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
            token = userInformation.getToken();
            isLoggedIn = true;
            isGrsUser = userInformation.getUserType().equals(UserType.COMPLAINANT);
            isOthersComplainant = Utility.isUserAnOthersComplainant(authentication);
            isOisfUser = userInformation.getUserType().equals(UserType.OISF_USER);
            isMobileLogin = userInformation.getIsMobileLogin();
            isMyGovLogin = userInformation.getIsMyGovLogin() != null && userInformation.getIsMyGovLogin();
        } else {
            isLoggedIn = false;
            isGrsUser = false;
            isOthersComplainant = false;
            isOisfUser = false;
            isMobileLogin = false;
            isMyGovLogin = false;
            subMenuDTO = SubMenuDTO.builder()
                    .nameEnglish("Log In")
                    .nameBangla("লগইন")
                    .link("/login?a=0")
                    .build();
        }
        String languageCode = CookieUtil.getValue(request, "lang");
        model.addAttribute("lang", languageCode);
        model.addAttribute("menu", subMenuDTO);
        model.addAttribute("isLoggedIn", isLoggedIn);
        if(authentication!=null) {
            isGrsUser = Utility.isUserAnGRSUser(authentication);
            isOthersComplainant = Utility.isUserAnOthersComplainant(authentication);
            model.addAttribute("grsUser", isGrsUser);
            if (!model.containsAttribute("superAdmin")) {
                model.addAttribute("superAdmin", Utility.isUserASuperAdmin(authentication));
            }
        }
        model.addAttribute("token", token);
        model.addAttribute("isGrsUser", isGrsUser);
        model.addAttribute("isOthersComplainant", isOthersComplainant);
        model.addAttribute("isOisfUser", isOisfUser);
        model.addAttribute("isMyGovLogin", isMyGovLogin);
        model.addAttribute("isMobileLogin", isMobileLogin);
        model.addAttribute("isProductionMode", Boolean.valueOf(environment.getProperty("environment.production")));
        return new ModelAndView(viewName);
    }

    public ModelAndView returnViewsForComplainWithoutLogin(Authentication authentication,
                                                           Model model,
                                                           HttpServletRequest request,
                                                           String mainPageFragmentName,
                                                           String mainPageFragmentValue,
                                                           String viewName) {

        SubMenuDTO subMenuDTO;
        boolean isLoggedIn, isGrsUser, isOisfUser, isOthersComplainant, isMobileLogin, isMyGovLogin;
        String token = "";
        isLoggedIn = false;
        isGrsUser = false;
        isOisfUser = false;
        isMyGovLogin = false;
        isMobileLogin = false;
        isOthersComplainant = false;
        subMenuDTO = SubMenuDTO.builder()
                .nameEnglish("Log In")
                .nameBangla("লগইন")
                .link("/login?a=0")
                .build();
        String languageCode = CookieUtil.getValue(request, "lang");
        model.addAttribute("lang", languageCode);
        model.addAttribute("menu", subMenuDTO);


        if(authentication!=null) {
            isLoggedIn = true;
            UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
            String username = userInformation.getUsername();
            if (userInformation.getUserType().equals(UserType.OISF_USER) &&
                    !userInformation.getOisfUserType().equals(OISFUserType.SUPER_ADMIN)) {
                username = userInformation.getOfficeInformation().getName();
            }
            model.addAttribute("name", username);

            token = userInformation.getToken();
            isGrsUser = Utility.isUserAnGRSUser(authentication);
            isOisfUser = Utility.isUserAnOisfUser(authentication);
            isOthersComplainant = Utility.isUserAnOthersComplainant(authentication);
            isMyGovLogin = userInformation.getIsMyGovLogin() != null && userInformation.getIsMyGovLogin();

            if(!isGrsUser && !userInformation.getUserType().equals(UserType.SYSTEM_USER)){
                NotificationsDTO notifications = this.notificationService.findByOfficeIdAndEmployeeRecordIdAndOfficeUnitOrganogramIdOrderByIdDesc(
                        userInformation.getOfficeInformation().getOfficeId(), userInformation.getOfficeInformation().getEmployeeRecordId(), userInformation.getOfficeInformation().getOfficeUnitOrganogramId()
                );
                model.addAttribute("notificationList", notifications);
            }
        }

        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("fragmentPath", mainPageFragmentName);
        model.addAttribute("fragmentName", mainPageFragmentValue);
//        model.addAttribute("permissions", permissions);


        model.addAttribute("isGro", false);
        model.addAttribute("isAppealOfficer", false);
        model.addAttribute("isOfficeAdmin", false);
        model.addAttribute("isHoo", false);
        model.addAttribute("isFieldCoordinator", false);
        model.addAttribute("isCentralDashboardRecipient", false);
        model.addAttribute("isMobileLogin", isMobileLogin);
        model.addAttribute("showDrawerMenu", false);

        Boolean isMinistryLevel = false;

        model.addAttribute("isMinistryLevelAdmin",  isMinistryLevel);

        if (!model.containsAttribute("superAdmin")){
            model.addAttribute("superAdmin",false);
        }
        if(!model.containsAttribute("showAllOffices")) {
            model.addAttribute("showAllOffices",false);
        }
        if(!model.containsAttribute("showChildOfficesOnly")) {
            model.addAttribute("showChildOfficesOnly",false);
        }
        Boolean isCellMember = false;

        model.addAttribute("isCellMember", isCellMember);
        model.addAttribute("isCellGRO", false);
        model.addAttribute("canViewDashboard", false);

        model.addAttribute("token", token);
        model.addAttribute("fromGrievanceUpload", false);
        model.addAttribute("grsUser", !isOisfUser);
        model.addAttribute("isGrsUser", isGrsUser);
        model.addAttribute("isOisfUser", isOisfUser);
        model.addAttribute("isOthersComplainant", isOthersComplainant);
        model.addAttribute("isMyGovLogin", isMyGovLogin);
        model.addAttribute("isMobileLogin", isMobileLogin);
        model.addAttribute("isProductionMode", Boolean.valueOf(environment.getProperty("environment.production")));
        return new ModelAndView(viewName);
    }

}
