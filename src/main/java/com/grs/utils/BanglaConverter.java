package com.grs.utils;

import com.grs.core.domain.*;
import com.grs.core.domain.Feedback;

import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.Map;

/**
 * Created by Acer on 9/6/2017.
 */
public class BanglaConverter {

    private static final char[] banglaDigits = {'০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯'};
    private static final String[] banglaDigitsString = {"০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯"};

    private static WeakHashMap<String, String> amPm, days, months;
    public static Map<Integer, String> MONTH_INT;

    static {
        amPm = new WeakHashMap<String, String>() {{
            put("AM", "পুর্বাহ্ন");
            put("PM", "অপরাহ্ন");
        }};
        days = new WeakHashMap<String, String>() {{
            put("Sun", "রবিবার");
            put("Mon", "সোমবার");
            put("Tue", "মঙ্গলবার");
            put("Wed", "বুধবার");
            put("Thu", "বৃহস্পতি");
            put("Fri", "শুক্রবার");
            put("Sat", "শনিবার");
        }};
        months = new WeakHashMap<String, String>() {{
            put("Jan", "জানুয়ারী");
            put("Feb", "ফেব্রুয়ারি");
            put("Mar", "মার্চ");
            put("Apr", "এপ্রিল");
            put("May", "মে");
            put("Jun", "জুন");
            put("Jul", "জুলাই");
            put("Aug", "অগাস্ট");
            put("Sep", "সেপ্টেম্বর");
            put("Oct", "অক্টোবর");
            put("Nov", "নভেম্বর");
            put("Dec", "ডিসেম্বর");
        }};

        MONTH_INT = new HashMap<Integer, String>() {{
            put(1, "জানুয়ারী");
            put(2, "ফেব্রুয়ারি");
            put(3, "মার্চ");
            put(4, "এপ্রিল");
            put(5, "মে");
            put(6, "জুন");
            put(7, "জুলাই");
            put(8, "অগাস্ট");
            put(9, "সেপ্টেম্বর");
            put(10, "অক্টোবর");
            put(11, "নভেম্বর");
            put(12, "ডিসেম্বর");
        }};
    }

    public static final StringBuilder convertToBanglaDigitBuilder(String number) {
        if (number == null)
            return new StringBuilder("");
        StringBuilder builder = new StringBuilder();
        try {
            for (int i = 0; i < number.length(); i++) {
                if (Character.isDigit(number.charAt(i))) {
                    if (((int) (number.charAt(i)) - 48) <= 9) {
                        builder.append(banglaDigits[(int) (number.charAt(i)) - 48]);
                    } else {
                        builder.append(number.charAt(i));
                    }
                } else {
                    builder.append(number.charAt(i));
                }
            }
        } catch (Exception e) {
            return new StringBuilder("");
        }
        return builder;
    }

    public static final String getDateBanglaFromEnglish(String number) {
        StringBuilder builder = convertToBanglaDigitBuilder(number);
        String dateBangla = builder.toString();
        if (dateBangla.contains("AM")) {
            dateBangla = dateBangla.replaceAll("AM", amPm.get("AM"));
        } else if (dateBangla.contains("PM")) {
            dateBangla = dateBangla.replaceAll("PM", amPm.get("PM"));
        }
        return dateBangla;
    }

    public static final StringBuilder getDateBanglaFromEnglishBuilder(String number) {
        if (number == null)
            return new StringBuilder("");
        StringBuilder builder = new StringBuilder();
        try {
            for (int i = 0; i < number.length(); i++) {
                if (Character.isDigit(number.charAt(i))) {
                    if (((int) (number.charAt(i)) - 48) <= 9) {
                        builder.append(banglaDigits[(int) (number.charAt(i)) - 48]);
                    } else {
                        builder.append(number.charAt(i));
                    }
                } else {
                    builder.append(number.charAt(i));
                }
            }
        } catch (Exception e) {
            return new StringBuilder("");
        }
        if (builder.indexOf("AM") != -1) {
            StringUtil.replaceAll(builder, "AM", amPm.get("AM"));
        } else if (builder.indexOf("PM") != -1) {
            StringUtil.replaceAll(builder, "PM", amPm.get("PM"));
        }
        return builder;
    }

    public static final StringBuilder getDateBanglaFromEnglishFullBuilder(String number) {
        StringBuilder banglaDateBuilder = getDateBanglaFromEnglishBuilder(number);
        for (String day : days.keySet()) {
            if (banglaDateBuilder.indexOf(day) != -1) {
                StringUtil.replaceAll(banglaDateBuilder, day, days.get(day));
                break;
            }
        }
        for (String month : months.keySet()) {
            if (banglaDateBuilder.indexOf(month) != -1) {
                StringUtil.replaceAll(banglaDateBuilder, month, months.get(month));
                break;
            }
        }

        return banglaDateBuilder;
    }

    public static final String getDateBanglaFromEnglishFull(String number) {

        return getDateBanglaFromEnglishFullBuilder(number).toString();
    }

    public static final String getDateBanglaFromEnglishFull24HourFormat(String number) {
        String banglaDate = getDateBanglaFromEnglishFull(number);

        String[] dates = banglaDate.split(" ");
        String date = "";
        String[] time = dates[3].split(":");
        date += dates[1] + "-" + dates[2] + "-" + dates[5] + " সময়: " + time[0] + "-" + time[1];

        return date;
    }

    public static String convertToBanglaDigit(long id) {
        if (id == -1){
            return null;
        }
        String idInString = String.valueOf(id);
        return convertToBanglaDigit(idInString);
    }

    public static String convertToBanglaDigit(String idInString) {

        StringBuilder builder = convertToBanglaDigitBuilder(idInString);
        return builder.toString();
    }

    public static String convertToEnglish(String id) {
        StringBuilder idInStringBuilder = new StringBuilder(String.valueOf(id));

        String englishDigit = "";
        String banglaDigit = "";

        for (int i = 0; i < banglaDigits.length; i++) {
            englishDigit = String.valueOf(i);
            banglaDigit = banglaDigitsString[i];
            StringUtil.replaceAll(idInStringBuilder, banglaDigit, englishDigit);
        }
        return idInStringBuilder.toString();
    }

    public static String convertAllToEnglish(String id) {

        return convertToEnglish(id);
    }

    public static Boolean isABanglaDigit(String id) {

        String englishDigit = "";
        String banglaDigit = "";

        StringBuilder builder = new StringBuilder(id);

        for (int i = 0; i < banglaDigits.length; i++) {
            englishDigit = String.valueOf(i);
            banglaDigit = banglaDigitsString[i];
            StringUtil.replaceAll(builder, englishDigit, banglaDigit);
        }
        try {
            Long.parseLong(builder.toString());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static String convertToBanglaMenuName(String input) {
        StringBuilder stringBuilderReplaced = new StringBuilder(input);

        StringUtil.replaceAll(stringBuilderReplaced, "Citizen Charter", "সেবা প্রদান প্রতিশ্রুতি");
        StringUtil.replaceAll(stringBuilderReplaced, "Suggestion", "পরামর্শ");
        StringUtil.replaceAll(stringBuilderReplaced, "Register", "রেজিস্টার");
        StringUtil.replaceAll(stringBuilderReplaced, "Services", "সেবা");
        StringUtil.replaceAll(stringBuilderReplaced, "Grievances", "অভিযোগ");
        StringUtil.replaceAll(stringBuilderReplaced, "Public", "নাগরিক");
        StringUtil.replaceAll(stringBuilderReplaced, "Staff", "কর্মচারী");
        StringUtil.replaceAll(stringBuilderReplaced, "Official", "দাপ্তরিক");
        StringUtil.replaceAll(stringBuilderReplaced, "Add", "নতুন");
        StringUtil.replaceAll(stringBuilderReplaced, "Edit", "সম্পাদন");
        StringUtil.replaceAll(stringBuilderReplaced, "View", " এর তালিকা");

        StringUtil.replaceAll(stringBuilderReplaced, "Ministries", "মন্ত্রণালয়সমূহ");
        StringUtil.replaceAll(stringBuilderReplaced, "Layers", "পর্যায়সমূহ");
        StringUtil.replaceAll(stringBuilderReplaced, "Office", "দপ্তর");
        StringUtil.replaceAll(stringBuilderReplaced, "Basic", "মৌলিক");
        StringUtil.replaceAll(stringBuilderReplaced, "Information", "তথ্যসমূহ");
        StringUtil.replaceAll(stringBuilderReplaced, "Branches", "শাখাসমূহ");
        StringUtil.replaceAll(stringBuilderReplaced, "Structure", "কাঠামো");
        StringUtil.replaceAll(stringBuilderReplaced, "List", "তালিকা");
        StringUtil.replaceAll(stringBuilderReplaced, "All", "সমস্ত");
        StringUtil.replaceAll(stringBuilderReplaced, "Manage", "ব্যবস্থাপনা");
        StringUtil.replaceAll(stringBuilderReplaced, "Local Admin", "লোকাল অ্যাডমিন");
        StringUtil.replaceAll(stringBuilderReplaced, "Search", "অনুসন্ধান");
        StringUtil.replaceAll(stringBuilderReplaced, "Message", "বার্তা/পত্র");

        return stringBuilderReplaced.toString();
    }

    public static String convertSuggestionTypeToBangla(SuggestionType suggestionType) {
        String banglaText = "";
        if (suggestionType == null)
            return banglaText;
        switch (suggestionType) {
            case SERVICE_IMPROVEMENT_SUGGESTION:
                banglaText = "সেবার মানোন্নয়নে পরামর্শ";
                break;
            case FEEDBACK:
                banglaText = "ফিডব্যাক/প্রশংসা/মতামত";
                break;
        }
        return banglaText;
    }

    public static String convertImprovementSuggestionTypeToBangla(ImprovementSuggestion improvementSuggestion) {
        String banglaText = "";
        if (improvementSuggestion == null)
            return banglaText;
        switch (improvementSuggestion) {
            case SERVICE_SIMPLIFICATION:
                banglaText = "সেবা সহজিকরণ";
                break;
            case LAW_REFORMS:
                banglaText = "আইন, বিধি সংস্কার";
                break;
            case NEW_IDEA:
                banglaText = "নতুন আইডিয়া";
                break;
        }
        return banglaText;
    }

    public static String convertFeedbackTypeToBangla(Feedback feedback) {
        String banglaText = "";
        if (feedback == null)
            return banglaText;
        switch (feedback) {
            case CASE_CLEARANCE:
                banglaText = "অভিযোগ নিষ্পত্তি";
                break;
            case SERVICE_DELIVERY:
                banglaText = "সেবা প্রাপ্তি";
                break;
        }
        return banglaText;
    }

    public static String convertEffectTypeToBangla(EffectsTowardsSolution effectsTowardsSolution) {
        String banglaText = "";
        if (effectsTowardsSolution == null)
            return banglaText;
        switch (effectsTowardsSolution) {
            case BETTER_SERVICE:
                banglaText = "সেবার গুণগত মান বৃদ্ধি পাবে ";
                break;
            case LESS_CORRUPTION:
                banglaText = "দুর্নীতি হ্রাস পাবে ";
                break;
            case LESS_TIME_EXPENSE:
                banglaText = "সময়, ব্যয় ও যাতায়াত হ্রাস পাবে  ";
                break;
            case OTHER:
                banglaText = "অন্যান্য";
                break;
        }
        return banglaText;
    }

    public static String convertGrievanceStatusToBangla(GrievanceCurrentStatus currentStatus) {
        String banglaText = "";
        switch (currentStatus) {
            case NEW:
            case CELL_NEW:
                banglaText = "নতুন";
                break;
            case FORWARDED_OUT:
                banglaText = "অন্য দপ্তরে প্রেরিত";
                break;
            case FORWARDED_IN:
                banglaText = "আওতাধীন দপ্তরে প্রেরণ ";
                break;
            case ACCEPTED:
                banglaText = "গৃহীত";
                break;
            case REJECTED:
                banglaText = "নথিজাত";
                break;
            case IN_REVIEW:
            case APPEAL_IN_REVIEW:
                banglaText = "পর্যালোচনা";
                break;
            case CLOSED_ANSWER_OK:
                banglaText = "নিষ্পত্তি";
                break;
            case CLOSED_SERVICE_GIVEN:
                banglaText = "নিষ্পত্তি";
                break;
            case CLOSED_ACCUSATION_PROVED:
                banglaText = "নিষ্পত্তি";
                break;
            case CLOSED_ACCUSATION_INCORRECT:
                banglaText = "নিষ্পত্তি";
                break;
            case CLOSED_OTHERS:
                banglaText = "নিষ্পত্তি";
                break;
            case CLOSED_INSTRUCTION_EXECUTED:
                banglaText = "নিষ্পত্তি";
                break;
            case APPEAL:
                banglaText = "আপিলকৃত";
                break;
            case INVESTIGATION_APPEAL:
            case INVESTIGATION:
                banglaText = "তদন্ত";
                break;
            case INV_NOTICE_FILE_APPEAL:
            case INV_NOTICE_FILE:
                banglaText = "অতিরিক্ত সংযুক্তি";
                break;
            case INV_NOTICE_HEARING_APPEAL:
            case INV_NOTICE_HEARING:
                banglaText = "তদন্ত শুনানি নোটিশ";
                break;
            case INV_HEARING:
            case INV_HEARING_APPEAL:
                banglaText = "তদন্ত শুনানি গৃহীত";
                break;
            case INV_REPORT:
            case INV_REPORT_APPEAL:
                banglaText = "তদন্ত প্রতিবেদন";
                break;
            case APPEAL_CLOSED_ACCUSATION_INCORRECT:
            case APPEAL_CLOSED_OTHERS:
            case APPEAL_CLOSED_ACCUSATION_PROVED:
            case APPEAL_CLOSED_ANSWER_OK:
            case APPEAL_CLOSED_INSTRUCTION_EXECUTED:
            case APPEAL_CLOSED_SERVICE_GIVEN:
                banglaText = "নিষ্পত্তি";
                break;
            case APPEAL_REJECTED:
                banglaText = "নথিজাত";
                break;
            case APPEAL_STATEMENT_ANSWERED:
                banglaText = "আপিলকৃত";
                break;
            case APPEAL_STATEMENT_ASKED:
                banglaText = "আপিলকৃত";
                break;
            case STATEMENT_ASKED:
                banglaText = "মতামতের জন্য প্রেরিত";
                break;
            case APPEAL_GIVE_GUIDANCE:
            case GIVE_GUIDANCE:
                banglaText = "সেবা প্রদানের জন্য নির্দেশিত ";
                break;
            case PERMISSION_ASKED:
                banglaText = "অনুমতির জন্য প্রেরিত";
                break;
            case PERMISSION_REPLIED:
                banglaText = "অনুমতি উত্তর প্রাপ্ত";
                break;
            case STATEMENT_ANSWERED:
                banglaText = "মতামত প্রাপ্ত";
                break;
            case FORWARDED_TO_AO:
                banglaText = "আপিল অফিসারের কাছে প্রেরিত";
                break;
            case APPEAL_RECOMMMEND_DETARTMENTAL_ACTION:
            case RECOMMEND_DEPARTMENTAL_ACTION:
                banglaText = "বিভাগীয় ব্যবস্থা গ্রহণের সুপারিশকৃত";
                break;
            case TESTIMONY_GIVEN:
                banglaText = "সাক্ষ্য-প্রমাণ প্রেরিত";
                break;
            case APPEAL_REQUEST_TESTIMONY:
            case REQUEST_TESTIMONY:
                banglaText = "সাক্ষ্য-প্রমাণের নির্দেশ";
                break;
            case CELL_MEETING_ACCEPTED:
                banglaText = "সেল সভায় গৃহীত";
                break;
            case CELL_MEETING_PRESENTED:
                banglaText = "সেল মিটিং এ উপস্থাপিত";
                break;
            case GIVE_GUIDANCE_POST_INVESTIGATION:
                banglaText = "তদন্তের জন্য নির্দেশিকা";
                break;
            case APPEAL_GIVE_GUIDANCE_POST_INVESTIGATION:
                banglaText = "আপীল তদন্তের জন্য নির্দেশিকা";
        }
        return banglaText;
    }

    public static String convertGrievanceStatusToEnglish(GrievanceCurrentStatus currentStatus) {
        String englishText = "";
        switch (currentStatus) {
            case NEW:
                englishText = "New";
                break;
            case FORWARDED_OUT:
                englishText = "Sent to another office";
                break;
            case FORWARDED_IN:
                englishText = "Sent to subordinate office";
                break;
            case ACCEPTED:
                englishText = "Accepted";
                break;
            case REJECTED:
            case APPEAL_REJECTED:
                englishText = "Rejected";
                break;
            case IN_REVIEW:
            case APPEAL_IN_REVIEW:
                englishText = "Review ongoing";
                break;
            case CLOSED_ANSWER_OK:
            case CLOSED_SERVICE_GIVEN:
            case CLOSED_ACCUSATION_PROVED:
            case CLOSED_ACCUSATION_INCORRECT:
            case CLOSED_INSTRUCTION_EXECUTED:
            case CLOSED_OTHERS:
            case APPEAL_CLOSED_OTHERS:
            case APPEAL_CLOSED_ACCUSATION_INCORRECT:
            case APPEAL_CLOSED_ACCUSATION_PROVED:
            case APPEAL_CLOSED_ANSWER_OK:
            case APPEAL_CLOSED_INSTRUCTION_EXECUTED:
            case APPEAL_CLOSED_SERVICE_GIVEN:
                englishText = "Closed";
                break;
            case APPEAL:
            case APPEAL_STATEMENT_ANSWERED:
            case APPEAL_STATEMENT_ASKED:
                englishText = "Appeal";
                break;
            case INVESTIGATION_APPEAL:
            case INVESTIGATION:
                englishText = "Investigation ongoing";
                break;
            case INV_NOTICE_FILE_APPEAL:
            case INV_NOTICE_FILE:
                englishText = "Additional attachments";
                break;
            case INV_NOTICE_HEARING_APPEAL:
            case INV_NOTICE_HEARING:
                englishText = "Investigation hearing notice";
                break;
            case INV_HEARING:
            case INV_HEARING_APPEAL:
                englishText = "Investigation hearing done";
                break;
            case INV_REPORT:
            case INV_REPORT_APPEAL:
                englishText = "Investigation report";
                break;
            case STATEMENT_ASKED:
                englishText = "Sent for opinion";
                break;
            case APPEAL_GIVE_GUIDANCE:
            case GIVE_GUIDANCE:
                englishText = "Service officer has been asked to give service";
                break;
            case PERMISSION_ASKED:
                englishText = "Sent for permission";
                break;
            case PERMISSION_REPLIED:
                englishText = "Permission asking is answered";
                break;
            case STATEMENT_ANSWERED:
                englishText = "Opinion received";
                break;
            case FORWARDED_TO_AO:
                englishText = "Sent to Appeal Officer";
                break;
            case APPEAL_RECOMMMEND_DETARTMENTAL_ACTION:
            case RECOMMEND_DEPARTMENTAL_ACTION:
                englishText = "Request to take disciplinary action";
                break;
            case TESTIMONY_GIVEN:
                englishText = "Testimony sent";
                break;
            case APPEAL_REQUEST_TESTIMONY:
            case REQUEST_TESTIMONY:
                englishText = "Request for testimony";
                break;
        }
        return englishText;
    }

    public static String convertServiceTypeToBangla(ServiceType serviceType) {
        switch (serviceType) {
            case NAGORIK:
                return "নাগরিক";
            case DAPTORIK:
                return "দাপ্তরিক";
            case STAFF:
                return "কর্মকর্তা-কর্মচারী";
            default:
                return "";
        }
    }
}
