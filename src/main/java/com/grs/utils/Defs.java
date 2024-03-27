package com.grs.utils;

import java.util.HashMap;
import java.util.Map;

public class Defs {
    public static final String INCLUSION_ERROR = "Inclusion Error";
    public static final String EXCLUSION_ERROR = "Exclusion Error";
    public static final String MONEY_NOT_RECEIVED = "Money not Received";

    public static Map<String, String> ERROR_MAP = new HashMap<>();

    static {
        ERROR_MAP.put(EXCLUSION_ERROR, "বর্জন ত্রুটি");
        ERROR_MAP.put(INCLUSION_ERROR, "অন্তর্ভুক্তি ত্রুটি");
        ERROR_MAP.put(MONEY_NOT_RECEIVED,"টাকা প্রাপ্ত হয়নি");
    }
}
