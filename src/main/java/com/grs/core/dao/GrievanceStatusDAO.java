package com.grs.core.dao;

import com.grs.core.domain.grs.GrievanceStatus;
import com.grs.core.repo.grs.GrievanceStatusRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Acer on 9/28/2017.
 */
@Service
public class GrievanceStatusDAO {
    @Autowired
    private GrievanceStatusRepo grievanceStatusRepo;

    public GrievanceStatus findByName(String statusName){
        return this.grievanceStatusRepo.findByStatusName(statusName);
    }

    public GrievanceStatus findOne(Long id) {
        return grievanceStatusRepo.findOne(id);
    }

    public List<GrievanceStatus> findAll() {
        return grievanceStatusRepo.findAll();
    }

}
