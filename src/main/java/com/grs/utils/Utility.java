package com.grs.utils;

import com.grs.api.config.security.CustomAuthenticationToken;
import com.grs.api.model.GRSUserType;
import com.grs.api.model.OISFUserType;
import com.grs.api.model.UserInformation;
import com.grs.api.model.UserType;
import com.grs.api.model.request.FileDTO;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.security.core.Authentication;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.InflaterOutputStream;

/**
 * Created by Acer on 16-Oct-17.
 */
public class Utility {
    public static UserInformation extractUserInformationFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        CustomAuthenticationToken customAuthenticationToken = (CustomAuthenticationToken) authentication;
        UserInformation userInformation = customAuthenticationToken.getUserInformation();
        return userInformation;
    }

    public static Boolean isUserAnGRSUser(Authentication authentication) {
        UserInformation userInformation = extractUserInformationFromAuthentication(authentication);
        return userInformation.getUserType().equals(UserType.COMPLAINANT);
    }

    public static Boolean isUserAnOthersComplainant(Authentication authentication) {
        UserInformation userInformation = extractUserInformationFromAuthentication(authentication);
        GRSUserType grsUserType = userInformation.getGrsUserType();
        if (grsUserType == null) {
            return false;
        }
        return grsUserType.equals(GRSUserType.OTHERS_COMPLAINANT);
    }

    public static Boolean isUserAnGRSUserOrOthersComplainant(Authentication authentication) {
        UserInformation userInformation = extractUserInformationFromAuthentication(authentication);
        GRSUserType grsUserType = userInformation.getGrsUserType();
        if (grsUserType == null) {
            return userInformation.getUserType().equals(UserType.COMPLAINANT);
        }
        return grsUserType.equals(GRSUserType.OTHERS_COMPLAINANT);
    }

    public static Boolean isUserAnOisfUser(Authentication authentication) {
        UserInformation userInformation = extractUserInformationFromAuthentication(authentication);
        return (userInformation.getUserType().equals(UserType.OISF_USER));
    }

    public static Boolean isUserAnGROUser(Authentication authentication) {
        UserInformation userInformation = extractUserInformationFromAuthentication(authentication);
        if (userInformation.getOfficeInformation() != null && userInformation.getOisfUserType() == OISFUserType.GRO) {
            return true;
        }
        return false;
    }

    public static Boolean isCellGRO(Authentication authentication) {
        UserInformation userInformation = extractUserInformationFromAuthentication(authentication);
        if (userInformation.getUserType().equals(UserType.OISF_USER)) {
            return userInformation.getIsCellGRO();
        }
        return false;
    }

    public static Boolean canViewDashboard(Authentication authentication) {
        return !isUserAnGRSUser(authentication) && (isUserAnGROUser(authentication)
                || isUserAHOOUser(authentication)
                || isUserACentralDashboardRecipient(authentication)
                || isCellGRO(authentication));
    }

    public static Boolean isServiceOfficer(Authentication authentication) {
        UserInformation userInformation = extractUserInformationFromAuthentication(authentication);
        if (userInformation.getOfficeInformation() != null && userInformation.getOisfUserType() == OISFUserType.SERVICE_OFFICER) {
            return true;
        }
        return false;
    }

    public static Boolean isUserAHOOUser(Authentication authentication) {
        UserInformation userInformation = extractUserInformationFromAuthentication(authentication);
        if (userInformation.getOfficeInformation() != null && userInformation.getOisfUserType() == OISFUserType.HEAD_OF_OFFICE) {
            return true;
        }
        return false;
    }

    public static Boolean isDivisionLevelFC(Authentication authentication) {
        UserInformation userInformation = extractUserInformationFromAuthentication(authentication);
        if (userInformation.getOfficeInformation() != null && userInformation.getOisfUserType() == OISFUserType.HEAD_OF_OFFICE) {
            Long officeOriginId = userInformation.getOfficeInformation().getOfficeOriginId();
            return officeOriginId != null && officeOriginId.equals(Constant.DIVISION_FIELD_COORDINATOR_OFFICE_ORIGIN_ID);
        }
        return false;
    }

    public static Boolean isDistrictLevelFC(Authentication authentication) {
        UserInformation userInformation = extractUserInformationFromAuthentication(authentication);
        if (userInformation.getOfficeInformation() != null && userInformation.getOisfUserType() == OISFUserType.HEAD_OF_OFFICE) {
            Long officeOriginId = userInformation.getOfficeInformation().getOfficeOriginId();
            return officeOriginId != null && officeOriginId.equals(Constant.DISTRICT_FIELD_COORDINATOR_OFFICE_ORIGIN_ID);
        }
        return false;
    }

    public static Boolean isUserACentralDashboardRecipient(Authentication authentication) {
        UserInformation userInformation = extractUserInformationFromAuthentication(authentication);
        if (userInformation.getIsCentralDashboardUser() != null) {
            return userInformation.getIsCentralDashboardUser();
        }
        return false;
    }

    public static Boolean isLoggedInFromMobile(Authentication authentication) {
        UserInformation userInformation = extractUserInformationFromAuthentication(authentication);
        if (userInformation.getIsMobileLogin() != null) {
            return userInformation.getIsMobileLogin();
        }
        return false;
    }

    public static Boolean isFieldCoordinator(Authentication authentication) {
        UserInformation userInformation = extractUserInformationFromAuthentication(authentication);
        if (userInformation.getOfficeInformation() != null && userInformation.getOfficeInformation().getOfficeMinistryId() != null && userInformation.getOfficeInformation().getLayerLevel() != null &&
                userInformation.getOfficeInformation().getOfficeMinistryId().equals(Constant.ministryIdFive) &&
                (userInformation.getOfficeInformation().getLayerLevel().equals(Constant.layerThree) || userInformation.getOfficeInformation().getLayerLevel().equals(Constant.layerFour)) &&
                userInformation.getOisfUserType() == OISFUserType.HEAD_OF_OFFICE) {
            return true;
        }
        return false;
    }

    public static Boolean isUserASuperAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        UserInformation userInformation = extractUserInformationFromAuthentication(authentication);
        return userInformation.getUserType().equals(UserType.SYSTEM_USER);
    }

    public static boolean isNumber(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            int d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static final String toBase64(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        return Base64.getEncoder().encodeToString(value.getBytes());
    }

    public static String decompress(String b64Compressed) {

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            OutputStream ios = new InflaterOutputStream(os);
            ios.write(Base64.getDecoder().decode(b64Compressed));
            return new String(os.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Date getDate(int day, int month, int year, boolean maxTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, day);
        calendar.set(Calendar.MONTH, month-1);
        calendar.set(Calendar.YEAR, year);
        if (maxTime) {
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }

        return calendar.getTime();
    }

    public static Date getDate(Date date, boolean maxTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (maxTime) {
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 1);
        }

        return calendar.getTime();
    }

    public static Long getLongValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof BigInteger) {
            return ((BigInteger) value).longValue();
        }

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).longValue();
        }

        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }

        if (value instanceof Float) {
            return ((Float) value).longValue();
        }

        if (value instanceof Double) {
            return ((Double)value).longValue();
        }

        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (Throwable t) {
                return null;
            }
        }

        return 0L;
    }

    public static long addDay(Date date, int i) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, i);
        return calendar.getTime().getTime();
    }

    public static String leftPad(Long value, int size) {
        if (size ==0) {
            size = 4;
        }

        StringBuilder seq = new StringBuilder(String.valueOf(value));
        while (seq.length() <size) {
            seq.insert(0, "0");
        }

        return seq.toString();
    }

    public static boolean valueExists(Object[] values, int index) {
        if (values == null || values.length ==0) {
            return false;
        }
        if (values.length <=index) {
            return false;
        }
        if (values[index] == null) {
            return false;
        }
        return true;
    }

    public static boolean isInList(String name, String ...values) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        if (values == null) {
            return false;
        }
        for (String val : values) {
            if (val == null || val.isEmpty()) {
                continue;
            }
            if (val.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
