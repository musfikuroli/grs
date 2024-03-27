package com.grs.core.dao;

import com.grs.core.domain.grs.Occupation;
import com.grs.core.repo.grs.OccupationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by HP on 1/30/2018.
 */
@Service
public class OccupationDAO {
    @Autowired
    private OccupationRepo occupationRepo;

    public Occupation findOne(Long id) {
        return this.occupationRepo.findOne(id);
    }

    public void saveOccupation(Occupation occupation){
        this.occupationRepo.save(occupation);
    }

    public Page<Occupation> findAll(Pageable pageable) {
        return occupationRepo.findAll(pageable);
    }

    public Integer countByOccupationBanglaAndOccupationEnglish(String occupationNameBng, String occcupationNameEng){
        return this.occupationRepo.countByOccupationBanglaAndOccupationEnglish(occupationNameBng, occcupationNameEng);
    }

}
