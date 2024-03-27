package com.grs.api.exception_handler;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

@Data
public class ApiError {
    private long timestamp;
    private int status;
    private String error;
    private String message;
    private String path;


    public ApiError(int status, long timestamp, String error, String message, String path) {
        this.status = status;
        this.timestamp = timestamp;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
