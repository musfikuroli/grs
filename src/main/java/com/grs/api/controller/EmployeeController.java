package com.grs.api.controller;

import com.grs.api.model.response.ComplainantResponseDTO;
import com.grs.core.service.EmployeeService;
import com.grs.core.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Acer on 08-Jan-18.
 */
@Slf4j
@RestController
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private UserService userService;

    @RequestMapping(value = "/api/employee/as/complainant/{employeeRecordId}", method = RequestMethod.GET)
    public ComplainantResponseDTO getPersonalInfoOfEmployeeIntoComplainantResponseDTO(@PathVariable("employeeRecordId") Long employeeRecordId) {
        return this.employeeService.getPersonalInfoOfEmployeeIntoComplainantResponseDTO(employeeRecordId);
    }

}
