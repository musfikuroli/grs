package com.grs.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Acer on 9/17/2017.
 */
public class DateTimeConverter {
    private static final String format = "dd-MM-yyyy hh:mm:ss:aa";
    public static final String formatForNotification = "dd-MM-yyyy hh:mm:ss";
    private static final String formatForDateTimePicker = "dd/MM/yyyy";
    private static final String formatForTimeline = "dd/MM/yyyy";
    private static final String formatForTimelineFull = "EEE, dd MMM yyyy : hh:mm aa";
    private static final String formatForMeeting = "ddMMYYYY";
    private static final List<String> MonthNameEnglish = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August", "Septmber", "October", "November", "December");
    private static final List<String> MonthNameBangla = Arrays.asList("জানুয়ারী", "ফেব্রুয়ারী", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর");


    public static Date convertToDate(String date) {
        if (date == null) {
            return null;
        }
        DateFormat dateFormat = new SimpleDateFormat(formatForDateTimePicker);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Dhaka"));
        Date convertedDate;
        try {
            convertedDate = dateFormat.parse(date);
        } catch (Exception e) {
            //throw new EntityNotFoundException("Date Format mismatch");
            return null;
        }
        return convertedDate;
    }

    public static String convertDateToString(Date date) {
        if(date == null) return null;
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public static String convertDateToStringformatForMeeting(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(formatForMeeting);
        return dateFormat.format(date);
    }

    public static String convertDateToStringForTimeline(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(formatForTimeline);
        return dateFormat.format(date);
    }

    public static String convertDateToStringForTimelineFull(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(formatForTimelineFull);
        return dateFormat.format(date);
    }

    public static String convertDateToString(Date date, String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public static String makeExpectedDateOfClosing(Date date, Boolean isInvestigated) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        Long expTime = CalendarUtil.getWorkDaysCountAfter(date, (int) (Constant.GRIEVANCE_EXPIRATION_TIME + (isInvestigated ? Constant.INVESTIGATION_ADDITIONAL_DAYS : 0L)));
        c.add(Calendar.DATE, expTime.intValue());

        Date closureDate = c.getTime();
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(closureDate);
    }

    public static String getCurrentMonthYearStringInEnglish(boolean includeDay) {
        Calendar calendar = Calendar.getInstance();
        Integer day = calendar.get(Calendar.DATE);
        Integer month = calendar.get(Calendar.MONTH);
        Integer year = calendar.get(Calendar.YEAR);
        String dayName = includeDay ? (day.toString() + " ") : "";
        String monthName = MonthNameEnglish.get(month) + " ";
        String yearName = year.toString();
        return dayName + monthName + yearName;
    }

    public static String getCurrentYearStringInEnglish(){
        Calendar calendar = Calendar.getInstance();
        Integer year = calendar.get(Calendar.YEAR);
        return year.toString();
    }

    public static String getCurrentYearStringInBangla(){
        Calendar calendar = Calendar.getInstance();
        Integer year = calendar.get(Calendar.YEAR);
        return BanglaConverter.convertToBanglaDigit(year.longValue());
    }

    public static String getCurrentMonthYearStringInBangla(boolean includeDay) {
        Calendar calendar = Calendar.getInstance();
        Integer day = calendar.get(Calendar.DATE);
        Integer month = calendar.get(Calendar.MONTH);
        Integer year = calendar.get(Calendar.YEAR);
        String dayName = includeDay ? (BanglaConverter.convertToBanglaDigit(day.longValue()) + " ") : "";
        String monthName = MonthNameBangla.get(month) + " ";
        String yearName = BanglaConverter.convertToBanglaDigit(year.longValue());
        return dayName + monthName + yearName;
    }
}
