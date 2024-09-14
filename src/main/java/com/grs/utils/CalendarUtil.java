package com.grs.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CalendarUtil {

    @Value("${service.gov.bd.calendar.url}")
    private String baseUrl;

    @Value("${service.gov.bd.secret.key}")
    private String secretKey;

    public static Date getDateBefore(Date fromDate, Integer workDays){
        Date beforeDate = getDateAllochronically(fromDate, -workDays);
        Integer holidayCount = getHolidayCountBetweenDate(beforeDate, fromDate);
        fromDate = beforeDate;
        while (holidayCount > 0){
            beforeDate = getDateAllochronically(fromDate, -holidayCount);
            holidayCount = getHolidayCountBetweenDate(beforeDate, fromDate);
            fromDate = beforeDate;
        }
        return fromDate;
    }

    public static Date getDateAfter(Date fromDate, Integer workDays){
        Date afterDate = getDateAllochronically(fromDate, workDays);
        Integer holidayCount = getHolidayCountBetweenDate(fromDate, afterDate);
        fromDate = afterDate;
        while (holidayCount > 0){
            afterDate = getDateAllochronically(fromDate, holidayCount);
            holidayCount = getHolidayCountBetweenDate(fromDate, afterDate);
            fromDate = afterDate;
        }
        return fromDate;
    }

    public static final Long getWorkDaysCountBefore(Date fromDate, Integer days){
        Date finalDate = getDateBefore(fromDate, days);
        long diff = fromDate.getTime() - finalDate.getTime();
        return (diff / (1000*60*60*24));
    }

    public static Long getWorkDaysCountAfter(Date fromDate, Integer days){
        Date finalDate = getDateAfter(fromDate, days);
        long diff = finalDate.getTime() - fromDate.getTime();
        return (diff / (1000*60*60*24));
    }

    public static Date getDateAllochronically(Date date, Integer count){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, count);
        return calendar.getTime();
    }

    public static Integer getHolidayCountBetweenDate(Date fromDate, Date toDate){
        List<Date> holidays = CacheUtil.getYearlyHolidayMapping();
        List<Date> sandwichDates = holidays.stream().filter(date -> date.after(fromDate) && date.before(toDate)).collect(Collectors.toList());
        return sandwichDates.size();
    }

    public static Date truncateDate(Date fromDate){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.parse(sdf.format(fromDate));
        }catch (ParseException pe){}
        return fromDate;
    }


    public List<Date> getHolidays() throws ParseException {
        ArrayList<Date> holidays = new ArrayList<>();
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        int startingYear = 2018;
        int thisYear = calendar.get(Calendar.YEAR);

        for (int year= startingYear; year <= thisYear; year++){

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = formatter.parse(year + "-01-01");
            Date endDate = formatter.parse(year + 1 + "-01-01");
            Calendar start = Calendar.getInstance();
            start.setTime(startDate);
            Calendar end = Calendar.getInstance();
            end.setTime(endDate);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();

            body.add("key", secretKey);
            body.add("year", "" + year);
            ObjectFromAPIService objectFromAPIService = new ObjectFromAPIService();
            Object listOfDays = objectFromAPIService.getObject(baseUrl, Object.class, body);
            if (listOfDays == null) {
                return new ArrayList<>();
            }
            Object allYearMap = ((LinkedHashMap) listOfDays).get("Calender");

            for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
                Object thisDay = ((LinkedHashMap) allYearMap).get(formatter.format(date));
                if(((LinkedHashMap) thisDay).get("day").toString().contains("Holiday")){
                    holidays.add(date);
                }
                ((LinkedHashMap<?, ?>) thisDay).clear();
                thisDay = null;
            }
            ((LinkedHashMap<?, ?>) listOfDays).clear();
            ((LinkedHashMap<?, ?>) allYearMap).clear();
            listOfDays = null;
            allYearMap = null;
        }

        return holidays;
    }
}
