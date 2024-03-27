package com.grs.core.dao;

import com.grs.core.domain.grs.Education;
import com.grs.core.repo.grs.EducationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Created by HP on 1/31/2018.
 */
@Service
public class EducationDAO {
    @Autowired
    private EducationRepo educationRepo;

    public Education findOne(Long id) {
        return this.educationRepo.findOne(id);
    }

    public void saveEducation(Education education){
        this.educationRepo.save(education);
    }

    public Page<Education> findAll(Pageable pageable) {
        return educationRepo.findAll(pageable);
    }

    public Integer countByEducationBanglaAndEducationEnglish(String educationNameBng, String educationNameEng){
        return this.educationRepo.countByEducationBanglaAndEducationEnglish(educationNameBng, educationNameEng);
    }

}
