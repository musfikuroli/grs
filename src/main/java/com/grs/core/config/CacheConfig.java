package com.grs.core.config;

import com.grs.core.repo.grs.GrievanceRepo;
import com.grs.utils.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class CacheConfig {
    @Autowired
    private GrievanceRepo grievanceRepo;

    @PostConstruct
    private void initTrackingNumber(){
//        Long count = (long) this.grievanceRepo.findAll().size();
        Long count = (long) this.grievanceRepo.findMaxTrackingNumber();
        CacheUtil.setTrackingNumber(count);
    }
}

