package com.grs.core.dao;

import com.grs.core.domain.grs.Education;
import com.grs.core.domain.grs.Occupation;
import com.grs.core.repo.grs.EducationRepo;
import com.grs.core.repo.grs.OccupationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by User on 10/3/2017.
 */
@Service
public class GRSSettingsDAO {
    @Autowired
    private OccupationRepo occupationRepo;
    @Autowired
    private EducationRepo educationRepo;

    public List<Occupation> getOccupations() {
        return this.occupationRepo.findAll();
    }

    public List<Occupation> getActiveOccupations() {
        return this.occupationRepo.findByStatus(true);
    }

    public List<Education> getEducationalQualifications() {
        return this.educationRepo.findAll();
    }

    public List<Education> getActiveEducationalQualifications() {
        return this.educationRepo.findByStatus(true);
    }
}
