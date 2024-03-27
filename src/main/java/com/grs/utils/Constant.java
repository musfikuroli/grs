package com.grs.utils;

import com.grs.core.model.KeyValue;
import com.grs.core.model.MonthYear;
import java.util.*;

/**
 * Created by Acer on 9/28/2017.
 */
public class Constant {

    public static final long EXPIRATIONTIME = 60000;
    public static final String SECRET = "ThisIsASecretKey";
    public static final String TOKEN_PREFIX = "Bearer";
    public static final String HEADER_STRING = "Authorization";
    public static final String MYGOV_ACCESS_TOKEN = "AccessToken";
    public static final String HEADER_STRING_OISF_AUTH = "oisf_auth";
    public static final String AUTHORITY = "permissions";
    public static final String USER_INFO = "user_info";
    public static final Long roleIdForComplainant = 5L;
    public static final Long roleIdForSuperAdmin = 1L;
    public static final String storedFilesFolderName = "uploadedFiles";

    public static final String tempFilesDirectoryName = "temporary";
    public static final long GRIEVANCE_EXPIRATION_TIME = 30L;
    public static final long APPEAL_EXPIRATION_TIME = 30L;
    public static final long INVESTIGATION_ADDITIONAL_DAYS = 10L;
    public static final String inboxForwardingSeen = "Inbox Forwarding Seen Updated.";
    public static final String NO_INFO_FOUND = "দুঃখিত, কোনো তথ্য পাওয়া যায়নি।";
    public static final Long layerThree = 3L;
    public static final Long layerFour = 4L;
    public static final Long ministryIdFive = 5L;
    public static final String fileNameSuffix = "GRS_FILE";
    public static final String untitledFileName = "UNTITLED_GRS_FILE";
    public static final String fileTypeFieldName = "FILE_TYPES";
    public static final String fileSizeFieldName = "MAX_FILE_SIZE";
    public static final String SYSTEM_NOTIFICATION_EMAIL = "SYSTEM_NOTIFICATION_EMAIL";
    public static final String SYSTEM_NOTIFICATION_PHONE_NUMBER = "SYSTEM_NOTIFICATION_PHONE_NUMBER";
    public static final Integer COOKIE_EXPIRATION_TIME = 86400;
    public static String OISF_ACCESS_TOKEN = "";
    public static final String API_CLIENT_TOKEN_HEADER = "X-API-Authorization";
    public static final String API_CLIENT_TOKEN_PREFIX = "Bearer";
    public static final String API_CLIENT_SECRET_PREFIX = "Secret";
    public static final String DOPTOR_API_CLIENT_HEADER_ACCEPT = "accept";
    public static final String DOPTOR_API_CLIENT_HEADER_CONT_TYPE = "Content-Type";
    public static final Integer API_CLIENT_TOKEN_DURATION = 86400;
    public static final Long DIVISION_FIELD_COORDINATOR_OFFICE_ORIGIN_ID = 15L;
    public static final Long DISTRICT_FIELD_COORDINATOR_OFFICE_ORIGIN_ID = 16L;
    public static final Long ministryLayerLevel= 1L;
    public static final Long directorateLayerLevel = 2L;
    public static final Long organizationLayerLevel = 3L;
    public static final Long divisionLayerLevel = 4L;
    public static final Long districtLayerLevel = 5L;
    public static final Long upazilaLayerLevel = 6L;
    public static final Long regionalLayerLevel = 7L;
    public static final String myGovLoginRedirectSuffix = "/afterLoginFromMyGov";
    public static final String myGovLogoutRedirectSuffix = "/afterLogoutFromMyGov";

    public static Map<String, MonthYear> monthYearMap;
    static {
        monthYearMap = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        for (int i = calendar.get(Calendar.YEAR); i >=2017;i--) {
            if(i != calendar.get(Calendar.YEAR)) {
                for (int j =12;j>=1;j--) {
                    MonthYear yearMonth = new MonthYear();
                    yearMonth.setYear(i);
                    yearMonth.setMonth(j);
                    monthYearMap.put(i+"_"+j, yearMonth);
                }
            } else {
                for (int j =calendar.get(Calendar.MONTH)+1;j>=1;j--) {
                    MonthYear yearMonth = new MonthYear();
                    yearMonth.setYear(i);
                    yearMonth.setMonth(j);
                    monthYearMap.put(i+"_"+j, yearMonth);
                }
            }
        }

    }
}
