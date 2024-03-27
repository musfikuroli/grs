package com.grs.api.controller;

import com.grs.api.model.request.GenericCitizenCharterUploaderRequestDTO;
import com.grs.api.model.response.GenericResponse;
import com.grs.core.domain.grs.Education;
import com.grs.core.domain.grs.Occupation;
import com.grs.core.service.GRSSettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by Aftab on 2/20/2018.
 */
@Slf4j
@RestController
public class GRSSettingsController {
    @Autowired
    GRSSettingsService grsSettingsService;

    @RequestMapping(value = "/api/occupations", method = RequestMethod.GET)
    public List<Occupation> getOccupations() {
        return this.grsSettingsService.getActiveOccupations();
    }

    @RequestMapping(value = "/api/educations", method = RequestMethod.GET)
    public List<Education> getEducationalQualifications() {
        return this.grsSettingsService.getActiveEducationalQualifications();
    }

    @RequestMapping(value = "/api/origin-citizen-charter/upload", method = RequestMethod.POST)
    public GenericResponse uploadCitizenCharter(@RequestBody GenericCitizenCharterUploaderRequestDTO citizenCharterUploaderRequestDTO) {
        GenericResponse genericResponse = null;
        String message = "";
        try{
            genericResponse = this.grsSettingsService.uploadCitizenCharter(citizenCharterUploaderRequestDTO);
        }catch (Exception ex) {

            log.info(ex.getMessage());
            genericResponse = GenericResponse.builder()
                    .success(false)
                    .message("প্রক্রিয়াকরণ ব্যাহত হওয়ায় সেবা প্রদান প্রতিশ্রুতি আপলোড বিঘ্নিত হয়েছে")
                    .build();
            throw ex;
        }finally {
            return genericResponse;
        }
    }
}
