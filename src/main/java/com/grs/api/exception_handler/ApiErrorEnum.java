package com.grs.api.exception_handler;

public enum ApiErrorEnum {
    NUMBER_FORMAT_EXCEPTION(600),
    MISSING_PARAMETER_EXCEPTION(700),
    BLACKLIST_EXCEPTION(800),
    MISSING_OBJECT_EXCEPTION(900),
    DUPLICATE_EMAIL_EXCEPTION(209);

    private final int value;

    ApiErrorEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
