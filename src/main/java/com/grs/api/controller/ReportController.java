package com.grs.api.controller;

import com.grs.api.model.*;
import com.grs.api.model.response.GenericResponse;
import com.grs.api.model.response.grievance.GrievanceComplainantInfoDTO;
import com.grs.api.model.response.reports.GrievanceAndAppealDailyReportDTO;
import com.grs.api.model.response.reports.GrievanceAndAppealMonthlyReportDTO;
import com.grs.api.model.response.reports.GrievanceMonthlyReportsDTO;
import com.grs.core.service.ModelViewService;
import com.grs.core.service.ReportsService;
import com.grs.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.List;

@Slf4j
@RestController
public class ReportController {
    @Autowired
    private ReportsService reportsService;
    @Autowired
    private ModelViewService modelViewService;

    @RequestMapping(value = "/viewFieldCoordination.do", method = RequestMethod.GET)
    public ModelAndView getFieldCoordinationPage(HttpServletRequest request, Authentication authentication, Model model) {
        if (authentication == null) {
            return new ModelAndView("redirect:/error-page");
        }
        return modelViewService.addNecessaryAttributesAndReturnViewPage(model,
                authentication,
                request,
                "reports",
                "fieldCoordination",
                "admin"
        );
    }

    @RequestMapping(value = "/api/grievances/monthly/field/coordination/reports", method = RequestMethod.GET, params = "month")
    public List<GrievanceMonthlyReportsDTO> getMonthlyFieldCoordinationReports(Authentication authentication, @RequestParam String month) {
        log.info("View Page Request : /api/grievances/monthly/field/coordination/reports for month: {}", month);
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return this.reportsService.getMonthlyFieldCoordinationReports(month, userInformation);
    }

    @RequestMapping(value = "/api/offices/{office_id}/reports/{year}/{month}", method = RequestMethod.GET)
    public GrievanceAndAppealMonthlyReportDTO getMonthlyGrievanceReportsByOffice(Authentication authentication,
                                                                                 @PathVariable("office_id") Long officeId,
                                                                                 @PathVariable("year") int year,
                                                                                 @PathVariable("month") int month) {
        log.info("View Page Request : /api/offices/{}/reports/{}/{}", officeId, year, month);
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        OfficeInformation officeInformation = userInformation.getOfficeInformation();
        return reportsService.getMonthlyReport(officeId, year, month, officeInformation.getLayerLevel());
    }

    @GetMapping("/api/reports/generate-last-month-report-data")
    public GenericResponse generateLastMonthReportData(Authentication authentication) {
        log.info("View Page Request : /api/reports/generate-last-month-report-data");
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        String message = "প্রতিবেদন প্রস্তুতকরণ সম্ভব হচ্ছেনা";
        boolean success = false;
        if (userInformation.getUserType().equals(UserType.SYSTEM_USER) && userInformation.getGrsUserType().equals(GRSUserType.SUPER_ADMIN)) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -1);
            Integer month = calendar.get(Calendar.MONTH) + 1;
            Integer year = calendar.get(Calendar.YEAR);
            Integer lastMonthReportCount = reportsService.countGeneratedReportByMonthAndYear(month, year);
            if (lastMonthReportCount == 0) {
                try {
                    reportsService.generateReportsAtEndOfMonth();
                    success = true;
                    message = "সর্বশেষ মাসের মাসিক প্রতিবেদন প্রস্তুত হয়েছে";
                } catch (Exception ex) {
                    message = "দুঃখিত! ত্রুটির কারণে মাসিক প্রতিবেদন প্রস্তুত করা সম্ভব হচ্ছেনা";
                    log.error(ex.getMessage());
                }
            } else {
                message = "মাসিক প্রতিবেদন ইতোমধ্যে প্রস্তুত করা হয়েছে";
            }
        } else {
            message = "দুঃখিত! আপনার মাসিক প্রতিবেদন প্রস্তুত করার অনুমতি নেই";
        }
        return GenericResponse.builder()
                .success(success)
                .message(message)
                .build();
    }

    @RequestMapping(value = "/api/offices/{office_id}/child-offices-report", method = RequestMethod.GET)
    public List<GrievanceAndAppealMonthlyReportDTO> getChildOfficesLastMonthReport(@PathVariable("office_id") Long officeId) {
        log.info("View Page Request : /api/offices/{}/child-offices-report", officeId);
        return reportsService.getChildOfficesLastMonthReport(officeId);
    }

    @RequestMapping(value = "/api/layer-level/{layerLevel}/offices/{officeId}/reports/from/{fromYear}/{fromMonth}/to/{toYear}/{toMonth}", method = RequestMethod.GET)
    public List<GrievanceAndAppealMonthlyReportDTO> getCustomReport(@PathVariable("layerLevel") Long layerLevel,
                                                                    @PathVariable("officeId") Long officeId,
                                                                    @PathVariable("fromYear") Integer fromYear,
                                                                    @PathVariable("fromMonth") Integer fromMonth,
                                                                    @PathVariable("toYear") Integer toYear,
                                                                    @PathVariable("toMonth") Integer toMonth) {
        log.info("View Page Request : /api/layer-level/{}/offices/{}/reports/from/{}/{}/to/{}/{}", layerLevel, officeId, fromYear, fromMonth, toYear, toMonth);
        return reportsService.getCustomReport(layerLevel, officeId, fromYear, fromMonth, toYear, toMonth);
    }

    @RequestMapping(value = "/api/layer-level/{layerLevel}/officeOrigin/{officeOrigin}/customLayer/{customLayer}/offices/{officeId}/reports/from/{fromYear}/{fromMonth}/{fromDay}/to/{toYear}/{toMonth}/{toDay}", method = RequestMethod.GET)
    public List<GrievanceAndAppealDailyReportDTO> getCustomReport(Authentication authentication,
                                                                  @PathVariable("layerLevel") Long layerLevel,
                                                                  @PathVariable("officeOrigin") String officeOrigin,
                                                                  @PathVariable("customLayer") Long customLayer,
                                                                  @PathVariable("officeId") Long officeId,
                                                                  @PathVariable("fromYear") Integer fromYear,
                                                                  @PathVariable("fromMonth") Integer fromMonth,
                                                                  @PathVariable("fromDay") Integer fromDay,
                                                                  @PathVariable("toYear") Integer toYear,
                                                                  @PathVariable("toMonth") Integer toMonth,
                                                                  @PathVariable("toDay") Integer toDay
    ) {
        log.info("View Page Request : /api/layer-level/{}/officeOrigin/{}/customLayer/{}" +
                        "/offices/{}/reports/from/{}/{}/{}/to/{}/{}/{}",
                layerLevel, officeOrigin, customLayer, officeId, fromYear, fromMonth, fromDay, toYear, toMonth, toDay);
        Long org = null;
        if (!officeOrigin.equalsIgnoreCase("null")) {
            org = Utility.getLongValue(officeOrigin);
        }
        return reportsService.getCustomReportDaily(layerLevel, org, customLayer, officeId, fromYear, fromMonth, fromDay, toYear, toMonth, toDay);
    }

    @RequestMapping(value = "/api/ministry/{officeId}/reports/from/{fromYear}/{fromMonth}/to/{toYear}/{toMonth}", method = RequestMethod.GET)
    public List<GrievanceAndAppealMonthlyReportDTO> getMinistryReport(@PathVariable("officeId") Long officeId,
                                                                      @PathVariable("fromYear") Integer fromYear,
                                                                      @PathVariable("fromMonth") Integer fromMonth,
                                                                      @PathVariable("toYear") Integer toYear,
                                                                      @PathVariable("toMonth") Integer toMonth) {
        log.info("View Page Request : /api/ministry/{}/reports/from/{}/{}/to/{}/{}", officeId, fromYear, fromMonth, toYear, toMonth);
        return reportsService.getMinistryBasedReport(officeId, fromYear, fromMonth, toYear, toMonth);
    }


    @RequestMapping(value = "/api/ministry/{officeId}/reports/from/{fromYear}/{fromMonth}/{fromDay}/to/{toYear}/{toMonth}/{toDay}", method = RequestMethod.GET)
    public List<GrievanceAndAppealDailyReportDTO> getMinistryReport(@PathVariable("officeId") Long officeId,
                                                                      @PathVariable("fromYear") Integer fromYear,
                                                                      @PathVariable("fromMonth") Integer fromMonth,
                                                                      @PathVariable("fromDay") Integer fromDay,
                                                                      @PathVariable("toYear") Integer toYear,
                                                                      @PathVariable("toMonth") Integer toMonth,
                                                                      @PathVariable("toDay") Integer toDay
    ) {
        log.info("View Page Request : /api/ministry/{}/reports/from/{}/{}/{}/to/{}/{}/{}", officeId, fromYear, fromMonth, fromDay, toYear, toMonth, toDay);
        return reportsService.getMinistryBasedReport(officeId, fromYear, fromMonth, fromDay, toYear, toMonth, toDay);
    }

    @RequestMapping(value = "/api/layerWise/{level}/reports/from/{fromYear}/{fromMonth}/to/{toYear}/{toMonth}", method = RequestMethod.GET)
    public List<GrievanceAndAppealMonthlyReportDTO> getLayerWiseReport(Authentication authentication,
                                                                       @PathVariable("level") Integer level,
                                                                       @PathVariable("fromYear") Integer fromYear,
                                                                       @PathVariable("fromMonth") Integer fromMonth,
                                                                       @PathVariable("toYear") Integer toYear,
                                                                       @PathVariable("toMonth") Integer toMonth) {
        log.info("View Page Request : /api/layerWise/{}/reports/from/{}/{}/to/{}/{}", level, fromYear, fromMonth, toYear, toMonth);
        return reportsService.getLayerWiseBasedReport(level, fromYear, fromMonth, toYear, toMonth, authentication);
    }

    @RequestMapping(value = "/api/layerWiseWithChildOffices/{level}/reports/from/{fromYear}/{fromMonth}/to/{toYear}/{toMonth}", method = RequestMethod.GET)
    public List<GrievanceAndAppealMonthlyReportDTO> getLayerWiseWithChildOfficesReport(Authentication authentication,
                                                                                       @PathVariable("level") Integer level,
                                                                                       @PathVariable("fromYear") Integer fromYear,
                                                                                       @PathVariable("fromMonth") Integer fromMonth,
                                                                                       @PathVariable("toYear") Integer toYear,
                                                                                       @PathVariable("toMonth") Integer toMonth,
                                                                                       @RequestParam(value = "firstSelection") Long firstSelection,
                                                                                       @RequestParam(value = "secondSelection") Long secondSelection
    ) {
        log.info("View Page Request : /api/layerWiseWithChildOffices/{}/reports/from/{}/{}/to/{}/{}", level, fromYear, fromMonth, toYear, toMonth);
        return reportsService.getLayerWiseWithChildOfficesBasedReport(level, firstSelection, secondSelection, fromYear, fromMonth, toYear, toMonth);
    }

    @RequestMapping(value = "/api/timeWiseComplainantWithChildOffices/reports/from/{fromYear}/{fromMonth}/to/{toYear}/{toMonth}", method = RequestMethod.GET)
    public Page<GrievanceComplainantInfoDTO> getTimeWiseComplainantsReport(Authentication authentication,
                                                                           @PathVariable("fromYear") Integer fromYear,
                                                                           @PathVariable("fromMonth") Integer fromMonth,
                                                                           @PathVariable("toYear") Integer toYear,
                                                                           @PathVariable("toMonth") Integer toMonth,
                                                                           @RequestParam(value = "level") Integer level,
                                                                           @RequestParam(value = "firstSelection") Long firstSelection,
                                                                           @RequestParam(value = "secondSelection") Long secondSelection,
                                                                           @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable
    ) {
        log.info("View Page Request : /api/timeWiseComplainantWithChildOffices/{}/reports/from/{}/{}/to/{}/{}", level, fromYear, fromMonth, toYear, toMonth);
        return reportsService.getTimeWiseComplainantsReport(level, firstSelection, secondSelection, fromYear, fromMonth, toYear, toMonth, pageable);
    }

    @RequestMapping(value = "/api/locationBased/division/{division}/district/{district}/upazilla/{upazilla}/reports/from/{fromYear}/{fromMonth}/to/{toYear}/{toMonth}", method = RequestMethod.GET)
    public List<GrievanceAndAppealMonthlyReportDTO> getLocationBasedReport(Authentication authentication,
                                                                           @PathVariable("division") Integer division,
                                                                           @PathVariable("district") Integer district,
                                                                           @PathVariable("upazilla") Integer upazilla,
                                                                           @PathVariable("fromYear") Integer fromYear,
                                                                           @PathVariable("fromMonth") Integer fromMonth,
                                                                           @PathVariable("toYear") Integer toYear,
                                                                           @PathVariable("toMonth") Integer toMonth) {
        log.info("View Page Request : /api/locationBased/division/{}/district/{}/upazilla/{}/reports/from/{}/{}/to/{}/{}", division, district, fromYear, upazilla, fromYear, fromMonth, toYear, toMonth);
        if (authentication != null) {
            return reportsService.getLocationBasedReport(authentication, division, district, upazilla, fromYear, fromMonth, toYear, toMonth);
        } else {
            return null;
        }
    }


    @RequestMapping (value = "/api/dcOfficeWise/office/{officeId}/reports/from/{fromYear}/{fromMonth}/to/{toYear}/{toMonth}", method = RequestMethod.GET)
    public List<GrievanceAndAppealMonthlyReportDTO> getDcOfficeWiseReport(
            Authentication authentication,
            @PathVariable("officeId") Long officeId,
            @PathVariable("fromYear") Integer fromYear,
            @PathVariable("fromMonth") Integer fromMonth,
            @PathVariable("toYear") Integer toYear,
            @PathVariable("toMonth") Integer toMonth
    ) {
        if(authentication != null){
            return reportsService.getDcOfficeWiseReport(officeId, fromYear, fromMonth, toYear, toMonth);
        }
        else {
            return null;
        }

    }


    @RequestMapping(value = "/api/report/regenerate/{year}/{month}", method = RequestMethod.GET)
    public void regenerateRepoort(Authentication authentication, @PathVariable("year") String year, @PathVariable("month") String month) {
        log.info("View Page Request : /api/report/regenerate/{}/{}", year, month);
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        if (userInformation.getGrsUserType().equals(GRSUserType.SUPER_ADMIN)) {
            reportsService.regenerateReports(year, month);
        }

    }

    @RequestMapping(value = "/api/report/safety-net-summary/{program_id}", method = RequestMethod.GET)
    public @ResponseBody SafetyNetSummaryResponse getSafetyNetSummary(Authentication authentication, @PathVariable(name = "program_id") Integer programId) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return this.reportsService.getSafetyNetSummary(userInformation, programId);
    }
}
