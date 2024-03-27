package com.grs.core.service;

import com.grs.core.domain.grs.Grievance;
import com.grs.core.domain.grs.OfficesGRO;
import com.grs.core.domain.projapoti.EmployeeOffice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class systemNotificationService {
    @Autowired
    private OfficeService officeService;
    @Autowired
    private OfficesGroService officesGroService;
    @Autowired
    private GrievanceService grievanceService;
    @Autowired
    private ShortMessageService shortMessageService;
    @Autowired
    private EmailService emailService;

    DecimalFormat df = new DecimalFormat("#.####");

    // second minute hour day-of-month month day-of-week
    @Scheduled(cron = "0 0 12 1 1/3 ?")
    public void sendGRONotificationOnRemainingGrievance(){
        List<OfficesGRO> officesGROES = new ArrayList<>();
//        List<OfficesGRO> officesGROES = officesGroService.findAll();
        for (OfficesGRO officesGRO : officesGROES){
            List<Grievance> grievances = grievanceService.getListViewWithOutSearching(officesGRO.getGroOfficeId(), -1, officesGRO.getGroOfficeUnitOrganogramId());
            if(grievances.size() > 0){
                log.info("sending email and sms to -> office_id: " + officesGRO.getGroOfficeId() + ", office_unit_organogram_id: " + officesGRO.getGroOfficeUnitOrganogramId());
                EmployeeOffice employeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(officesGRO.getGroOfficeId(), officesGRO.getGroOfficeUnitOrganogramId(), true);
                String groEmail = employeeOffice != null ? employeeOffice.getEmployeeRecord().getPersonalEmail() : "";
                String groMobile = employeeOffice != null ? employeeOffice.getEmployeeRecord().getPersonalMobile() : "";
                String body = "প্রিয় অনিক, আপনার ইনবক্সে " + grievances.size() + " টি অভিযোগ অনিষ্পন্ন অবস্থায় আছে। অনুগ্রহপূর্বক grs.gov.bd তে প্রশাসনিক লগইন করে অভিযোগ নিষ্পত্তি করুন।";
                shortMessageService.sendSMS(groMobile, body);
                emailService.sendEmail(groEmail, "অনিষ্পন্ন অভিযোগের নোটিফিকেশন", body);
            }
        }
    }

    // second minute hour day-of-month month day-of-week
    @Scheduled(fixedDelay = 60000, initialDelay = 240000)
    public void heapMemoryHealthStatusCheckUp(){
        String freeMemoryPercentage = df.format(Runtime.getRuntime().freeMemory() * 100.0 / Runtime.getRuntime().totalMemory());
        try {
            if (df.parse(freeMemoryPercentage).floatValue() < 10.0) {
                log.info( "Calling GC.......................");
                System.gc();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        log.info( " \t Free Memory(%) \t Free Memory \t \t Total Memory \t Max Memory");
        log.info( " \t " + freeMemoryPercentage +
                  " \t \t \t " + Runtime.getRuntime().freeMemory() +
                  " \t \t \t " + Runtime.getRuntime().totalMemory() +
                  " \t \t " + Runtime.getRuntime().maxMemory());

    }
}
