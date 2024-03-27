package com.grs.api.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by Acer on 06-Mar-18.
 */
@Slf4j
public class DuplicateEmailException
        extends RuntimeException {
    public DuplicateEmailException(String errorMessage) {
        super(errorMessage);
    }
}
