package com.grs.api.exception_handler;

import java.util.WeakHashMap;

public class ApiErrorMessages {
    public static final WeakHashMap<Integer, String> ApiErrorMessagesMap = new WeakHashMap<Integer, String>() {{
        put(ApiErrorEnum.NUMBER_FORMAT_EXCEPTION.getValue(), "Illegal format of number for: ");
        put(ApiErrorEnum.MISSING_PARAMETER_EXCEPTION.getValue(), "Missing required parameter: ");
        put(ApiErrorEnum.MISSING_OBJECT_EXCEPTION.getValue(), "Missing required object: ");
        put(ApiErrorEnum.BLACKLIST_EXCEPTION.getValue(), "Sorry, this complainant cannot complain to this office!");
    }};
}
