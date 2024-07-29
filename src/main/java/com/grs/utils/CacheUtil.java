package com.grs.utils;

import com.grs.api.model.response.officeSelection.OfficeSearchDTO;

import java.util.*;
import java.util.stream.Collectors;

public class CacheUtil {
    public static WeakHashMap<String, Object> apiAccessTokens = new WeakHashMap();
    public static Long[][] officeOrder = new Long[][]{{2L, 2131L}, {3L, 2175L}, {4L, 53L}, {5L, 12368L}, {6L, 28L}, {7L, 2242L}, {8L, 222L}, {9L, 98L}, {10L, 89L}, {11L, 2101L}, {13L, 79L}, {14L, 80L}, {15L, 54L}, {16L, 2287L}, {17L, 81L}, {18L, 97L}, {19L, 5684L}, {20L, 4337L}, {22L, 221L}, {23L, 2212L}, {24L, 977L}, {25L, 8L}, {27L, 199L}, {29L, 113L}, {30L, 324L}, {31L, 2253L}, {32L, 1647L}, {33L, 2347L}, {35L, 5346L}, {36L, 6085L}, {37L, 2185L}, {38L, 4389L}, {39L, 4289L}, {40L, 533L}, {41L, 96L}, {42L, 206L}, {43L, 63L}, {44L, 2214L}, {46L, 2225L}, {47L, 2243L}, {48L, 2294L}, {49L, 241L}, {51L, 2240L}, {52L, 95L}, {54L, 143L}, {55L, 526L}, {56L, 94L}, {58L, 2171L}, {59L, 57L}, {60L, 311L}, {61L, 587L}, {62L, 59L}, {63L, 14L}, {64L, 84L}, {65L, 2226L}, {66L, 414L}, {67L, 2213L}, {68L, 2164L}, {69L, 142L}, {70L, 87L}};
    public static Map<Long, Long> officeOrderMap = new HashMap<Long, Long>();
    private static WeakHashMap<Long, List> ministryDescendantOffices = new WeakHashMap();
    private static WeakHashMap<Long, List> ministryDescendantOfficeOrigins = new WeakHashMap();
    private static List<OfficeSearchDTO> grsEnabledOfficeSearchDTOList = new ArrayList();
    private static List<OfficeSearchDTO> allOfficeSearchDTOList = new ArrayList();
    private static List<Date> yearlyHolidayMapping = new ArrayList<>();
    private static Long trackingNumber = 0L;
    public static final Long SELECT_ALL_OPTION_VALUE = 9999L;

    public static Long getOfficeOrder(Long officeId) {

        if (officeOrderMap.size() == 0) {
            officeOrderMap = Arrays.stream(officeOrder)

                    .collect(Collectors.toMap(e -> e[1], f -> f[0]));
        }
        if (officeOrderMap.containsKey(officeId)) {
            return officeOrderMap.get(officeId);
        } else {
            return null;
        }
    }

    public static synchronized void updateTrackingNumber() {
        trackingNumber += 1;
    }

    public static synchronized Long getTrackingNumber() {
        return trackingNumber;
    }

    public static synchronized void setTrackingNumber(Long number) {
        trackingNumber = number;
    }

    public static synchronized List<Date> getYearlyHolidayMapping() {
        return yearlyHolidayMapping;
    }

    public static synchronized void setYearlyHolidayMapping(List<Date> dates) {
        yearlyHolidayMapping = dates;
    }

    public static synchronized WeakHashMap<Long, List> getMinistryDescendantOffices() {
        return ministryDescendantOffices;
    }

    public static synchronized void setMinistryDescendantOffices(WeakHashMap<Long, List> descendants) {
        ministryDescendantOffices = descendants;
    }

    public static synchronized WeakHashMap<Long, List> getMinistryDescendantOfficeOrigins() {
        return ministryDescendantOfficeOrigins;
    }

    public static synchronized void setMinistryDescendantOfficeOrigins(WeakHashMap<Long, List> descendants) {
        ministryDescendantOfficeOrigins = descendants;
    }

    public static synchronized List<OfficeSearchDTO> getGrsEnabledOfficeSearchDTOList() {
        return grsEnabledOfficeSearchDTOList;
    }

    public static synchronized void setGrsEnabledOfficeSearchDTOList(List<OfficeSearchDTO> list) {
        grsEnabledOfficeSearchDTOList = list;
    }

    public static synchronized List<OfficeSearchDTO> getAllOfficeSearchDTOList() {
        return allOfficeSearchDTOList;
    }

    public static synchronized void setAllOfficeSearchDTOList(List<OfficeSearchDTO> list) {
        allOfficeSearchDTOList = list;
    }
}
