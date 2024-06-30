package com.grs.core.service;

import com.google.gson.Gson;
import com.grs.api.model.UserInformation;
import com.grs.api.model.response.dashboard.TotalAndResolvedCountDTO;
import com.grs.api.model.response.dashboard.latest.GRSStatisticDTO;
import com.grs.api.model.response.officeSelection.OfficeSearchDTO;
import com.grs.core.dao.GRSStatisticsDAO;
import com.grs.core.domain.grs.DashboardTotalResolved;
import com.grs.core.domain.grs.GrsStatistics;
import com.grs.core.model.MonthYear;
import com.grs.core.repo.grs.BaseEntityManager;
import com.grs.core.repo.grs.DashboardTotalResolvedRepo;
import com.grs.core.repo.grs.GrsStatisticsRepo;
import com.grs.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class CacheService {
    @Autowired
    private OfficeService officeService;
    @Autowired
    private CalendarUtil calendarUtil;
    @Autowired
    private OfficesGroService officesGroService;

    @Autowired
    private BaseEntityManager baseEntityManager;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private DashboardTotalResolvedRepo dashboardTotalResolvedRepo;

    @Autowired
    private GrsStatisticsRepo grsStatisticsRepo;

    @Autowired
    private GRSStatisticsDAO grsStatisticsDAO;

    @Autowired
    private StorageService storageService;

    private RestTemplate restTemplate;
    @Autowired
    Gson gson;

    @PostConstruct
    public void init() {
        restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            ClientHttpResponse response = execution.execute(request, body);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response;
        });

        try {
            DisableSSLCertificateCheckUtil.disableChecks();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    //    @Scheduled(fixedDelay = 3300000, initialDelay = 0) //Default delay 55 minutes
    @Scheduled(initialDelay = 10 * 1000, fixedDelay = 60*60*1000) //Default delay 55 minutes
    @Async("s1")
    public void updateOfficeSearchCacheContents() {
        log.info("Started Office Search cache updating on: " + (new Date()));
        List<OfficeSearchDTO> allOfficeSearchDTOs = officeService.generateOfficeSearchingData(false);

        List<Long> officeIdsInOfficesGro = this.officesGroService.findAllOffficeIds();
        List<OfficeSearchDTO> grsEnabledOfficeSearchDTOs = allOfficeSearchDTOs.stream()
                .filter(office -> officeIdsInOfficesGro.contains(office.getId()))
                .collect(Collectors.toList());

        CacheUtil.setGrsEnabledOfficeSearchDTOList(grsEnabledOfficeSearchDTOs);
        CacheUtil.setAllOfficeSearchDTOList(allOfficeSearchDTOs);
        log.info("Finished Office Search cache updating on: " + (new Date()).toString());
    }

    //    @Scheduled(fixedDelay = 10500000, initialDelay = 2000) //Default delay 2hr 55 minutes
    @Scheduled(initialDelay = 2000, fixedDelay =  3*60*60*1000) //Default delay 2hr 55 minutes
    @Async("s2")
    public void updateMinistryDescendantsCacheContents() {
        log.info("Started Ministry Descendants cache updating on: " + (new Date()).toString());
        WeakHashMap<String, WeakHashMap> ministriesDescendantIds = officeService.generateDescendantOfficesIdListOfMinistries();
        WeakHashMap<Long, List> officeIdMap = ministriesDescendantIds.get("officeIds");
        WeakHashMap<Long, List> originIdMap = ministriesDescendantIds.get("originIds");
        ;
        CacheUtil.setMinistryDescendantOffices(officeIdMap);
        CacheUtil.setMinistryDescendantOfficeOrigins(originIdMap);
        log.info("Finished Ministry Descendants cache updating on: " + (new Date()).toString());
    }

    //    @Scheduled(fixedDelay = 33000000, initialDelay = 0)
    @Scheduled(initialDelay = 0, fixedDelay = 24*60*60*1000)
    @Async("s3")
    public void updateGovtHolidays() {
        log.info("Started Government Holidays cashe, updation on" + (new Date()).toString());
        List<Date> holidays = null;
        try {
            holidays = calendarUtil.getHolidays();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        CacheUtil.setYearlyHolidayMapping(holidays);
        log.info("Finished Government Holidays cashe, updation on" + (new Date()).toString());
    }

    @Scheduled(initialDelay = 10000, fixedDelay =  86400000)
    public void updateYearlyStatistics() {
        log.info("Started Yearly Statistics cache at" + (new Date()));
        baseEntityManager.updateYearlyStatistics();
        log.info("Ended Yearly Statistics cache at" + (new Date()));
    }


    // second minute hour day-of-month month day-of-week
    @Scheduled(cron = "0 20 0 1 */1 *")
    public void updateAllYearlyStatistics() {
        log.info("Started ALL Yearly Statistics cache at" + (new Date()));
        baseEntityManager.updateAllYearlyStatistics();
        log.info("Ended ALL Yearly Statistics cache at" + (new Date()));
    }

    @Scheduled(initialDelay = 2 * 60 * 1000, fixedDelay = 86400000)
    public void refreshMonthYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Constant.monthYearMap = new HashMap<>();
        for (int i = calendar.get(Calendar.YEAR); i >= 2017; i--) {
            if (i != calendar.get(Calendar.YEAR)) {
                for (int j = 12; j >= 1; j--) {
                    Constant.monthYearMap.put(i + "_" + j, new MonthYear(i, j));
                }
            } else {
                for (int j = calendar.get(Calendar.MONTH) + 1; j >= 1; j--) {
                    Constant.monthYearMap.put(i + "_" + j, new MonthYear(i, j));
                }
            }
        }
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void deleteTempFiles() {
        log.info("Started Delete Temp File started at" + (new Date()));

        Path path = this.storageService.getTempLocation();

        try (Stream<Path> walkStream = Files.walk(path)) {
            walkStream.filter(p -> p.toFile().isFile()).forEach(f -> {
                if (f.toFile().lastModified() >= Utility.addDay(new Date(), -1)) {
                    boolean status = f.toFile().delete();
                    log.info("==FILE DELETED:{}", status);
                }
            });
        } catch (IOException e) {
            log.error("ERROR:", e);
        }
    }


    @Scheduled(initialDelay = 2 * 60 * 1000, fixedDelay = 3600000)
    public void updateGeneralDashboardData() {
        log.info("Started updateGeneralDashboardData cache at" + (new Date()));
        List<TotalAndResolvedCountDTO> resolvedCountDTOS = dashboardService.getSubordinateTotalSubmittedAndResolvedCountsOfMinistries(false);
        String sql = "delete from grs_dashboard_total_resolved where 1=1 ";
        if (baseEntityManager.deleteByQuery(sql)) {
            List<DashboardTotalResolved> list = new ArrayList<>();
            for (TotalAndResolvedCountDTO dto : resolvedCountDTOS) {
                DashboardTotalResolved eo = new DashboardTotalResolved();
                eo.setOfficeId(dto.getOfficeId());
                eo.setOfficeName(dto.getOfficeName());
                eo.setTotalCount(dto.getTotalCount());
                eo.setResolvedCount(dto.getResolvedCount());
                eo.setExpiredCount(dto.getExpiredCount());
                eo.setRate(dto.getRate());
                list.add(eo);
            }
            dashboardTotalResolvedRepo.save(list);
        }
        log.info("Ended updateGeneralDashboardData cache at" + (new Date()));
    }

   // @Scheduled(initialDelay = 5 * 60 * 1000, fixedDelay = 86400000)
    public void updateGrsStatistics() {
        log.info("Started updateGrsStatistics cache at" + (new Date()));
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;

        List<Long> offices = officesGroService.findAllOffficeIds();
        for (Long officeId : offices) {

            log.info("==YEAR:{} MONTH:{} Office:{}", year, month, officeId);
            if (officeId == null || officeId <= 0) {
                continue;
            }

            GRSStatisticDTO dto = grsStatisticsDAO.getGRSStatistics(UserInformation.builder().build(), officeId, year, month, false);
            if (dto == null) {
                continue;
            }

            GrsStatistics grsStatistics = grsStatisticsRepo.findByOfficeIdAndYearAndMonth(officeId, year, month);
            if (grsStatistics == null) {
                grsStatistics = new GrsStatistics();
            }

            grsStatistics.setOfficeId(dto.officeId);
            grsStatistics.setYear(dto.year);
            grsStatistics.setMonth(dto.month);
            grsStatistics.setTotalSubmittedGrievance(dto.totalSubmittedGrievance);
            grsStatistics.setCurrentMonthAcceptance(dto.currentMonthAcceptance);
            grsStatistics.setAscertainOfLastMonth(dto.ascertainOfLastMonth);
            grsStatistics.setRunningGrievances(dto.runningGrievances);
            grsStatistics.setForwardedGrievances(dto.forwardedGrievances);
            grsStatistics.setTimeExpiredGrievances(dto.timeExpiredGrievances);
            grsStatistics.setResolvedGrievances(dto.resolvedGrievances);
            grsStatistics.setResolveRate(dto.resolveRate);
            grsStatistics.setRateOfAppealedGrievance(dto.rateOfAppealedGrievance);
            grsStatistics.setTotalRating(dto.totalRating);
            grsStatistics.setAverageRating(dto.averageRating);
            grsStatistics.setAppealTotal(dto.appealTotal);
            grsStatistics.setAppealCurrentMonthAcceptance(dto.appealCurrentMonthAcceptance);
            grsStatistics.setAppealAscertain(dto.appealAscertain);
            grsStatistics.setAppealRunning(dto.appealRunning);
            grsStatistics.setAppealTimeExpired(dto.appealTimeExpired);
            grsStatistics.setAppealResolved(dto.appealResolved);
            grsStatistics.setAppealResolveRate(dto.appealResolveRate);
            grsStatistics.setSubOfficesTotalGrievance(dto.subOfficesTotalGrievance);
            grsStatistics.setSubOfficesTimeExpiredGrievance(dto.subOfficesTimeExpiredGrievance);
            grsStatistics.setSubOfficesResolvedGrievance(dto.subOfficesResolvedGrievance);
            grsStatistics.setSubOfficesTotalAppeal(dto.subOfficesTotalAppeal);
            grsStatistics.setSubOfficesTimeExpiredAppeal(dto.subOfficesTimeExpiredAppeal);
            grsStatistics.setSubOfficesResolvedAppeal(dto.subOfficesResolvedAppeal);
            grsStatistics.setSubOfficesGrievanceResolveRate(dto.subOfficesGrievanceResolveRate);
            grsStatistics.setSubOfficesAppealResolveRate(dto.subOfficesAppealResolveRate);
            grsStatisticsRepo.save(grsStatistics);
        }
        log.info("Ended updateGrsStatistics cache at" + (new Date()));
    }
}
