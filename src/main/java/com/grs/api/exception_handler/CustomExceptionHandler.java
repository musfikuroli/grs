package com.grs.api.exception_handler;

import com.grs.api.model.response.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;

/**
 * Created by Acer on 06-Mar-18.
 */
@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public GenericResponse handleSizeExceededException(final WebRequest request, final MultipartException ex) {
        GenericResponse GenericResponse = new GenericResponse();
        GenericResponse.setSuccess(false);
        GenericResponse.setMessage(ex.getMessage());
        return GenericResponse;
    }

}
