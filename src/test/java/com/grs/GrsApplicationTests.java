package com.grs;

import com.grs.api.model.UserInformation;
import com.grs.api.model.response.dashboard.latest.GRSStatisticDTO;
import com.grs.core.dao.GRSStatisticsDAO;
import com.grs.core.repo.grs.BaseEntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Calendar;
import java.util.Date;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class GrsApplicationTests {

//    @Autowired
    public BaseEntityManager baseEntityManager;
//    @Autowired
    public GRSStatisticsDAO grsStatisticsDAO;


//    @Test
    public void test(){
        Long officeId=8834L;
        Integer year=2023;
        Integer month=1;
        GRSStatisticDTO dto = grsStatisticsDAO.getGRSStatistics(UserInformation.builder().build(), officeId, year, month, false);
        GRSStatisticDTO dto2 = grsStatisticsDAO.getGRSStatistics(UserInformation.builder().build(), officeId, year, month, true);
        System.out.println(dto.toString());


        Long obj1 = new Long(124);
        Long obj2 = new Long(167);
        int compareValue = obj1.compareTo(obj2);




    }

//    @Test
    public void contextLoads() {
        Calendar calendar = Calendar.getInstance();

//        calendar.set(2021, 02, 01);
        calendar.set(2022, 8, 1, 0, 0, 0);
        Date fromDate = calendar.getTime();

        calendar.set(2022, 8, 31, 23, 59, 59);
        Date toDate = calendar.getTime();

//        Long officeId = 3247L;
//        Long officeId = 28L;
        Long officeId = null;
        Long layerLevel = 1L;
        Long officeOrigin = 9999L;
        Long customLayer = 9999L;
        Long[] dailyReport = baseEntityManager.getDailyReport(layerLevel, officeOrigin, customLayer, officeId, fromDate, toDate);

        String s = "\n onlineSubmissionCount: " + dailyReport[5]
                + "\n conventionalMethodSubmissionCount: " + dailyReport[6]
                + "\n selfMotivatedAccusationCount: " + dailyReport[7]
                + "\n inheritedFromLastMonthCount: " + dailyReport[8]
                + "\n totalSubmitted: " + dailyReport[0]
                + "\n sentToOtherOfficeCount: " + dailyReport[4]

                + "\n not_expired: " + (dailyReport[3] + dailyReport[4] + dailyReport[1]) //sum(mr.runningGrievanceCount+  mr.sent_to_other + mr.resolved) not_expired,

                + "\n runningGrievanceCount: " + dailyReport[3]
                + "\n timeExpiredCount: " + dailyReport[2]
                + "\n rate: " + (((double) (dailyReport[4] + dailyReport[1]) / (double) dailyReport[0]) * 100.0) //  sum(mr.sent_to_other + mr.resolved) * 100 / sum(mr.total) rate,
                + "\n resolvedCount: " + dailyReport[1]


                + "\n appealInheritedFromLastMonthCount: " + dailyReport[14]
                + "\n appealOnlineSubmission: " + dailyReport[13]
                + "\n appealTotalSubmitted: " + dailyReport[9]
                + "\n appealResolved: " + dailyReport[10]
                + "\n appealExpired: " + dailyReport[11]

                + "\n appeal_not_expired: " + (dailyReport[12] + dailyReport[10]) //sum(mr.appealRunning + mr.appealResolved) not_expired,

                + "\n appealRunning: " + dailyReport[12]
                + "\n rateAppeal: " + (((double) (dailyReport[10]) / (double) dailyReport[9]) * 100.0) //  sum(  mr.appealResolved) * 100 / sum(mr.appealTotalSubmitted) rate,
                ;


        System.out.println(s);
    }

}
