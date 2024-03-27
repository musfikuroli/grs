package com.grs.core.dao;

import com.grs.core.domain.grs.CitizensCharterOrigin;
import com.grs.core.repo.grs.CitizensCharterOriginRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Acer on 18-Apr-18.
 */
@Service
public class CitizenCharterOriginDAO {
    @Autowired
    private CitizensCharterOriginRepo citizenCharterOriginRepo;

    public CitizensCharterOrigin save(CitizensCharterOrigin citizenCharterOrigin) {
        return this.citizenCharterOriginRepo.save(citizenCharterOrigin);
    }

    public List<CitizensCharterOrigin> saveAll(List<CitizensCharterOrigin> citizenCharterOriginList) {
        return this.citizenCharterOriginRepo.save(citizenCharterOriginList);
    }

    public CitizensCharterOrigin findByOfficeOriginId(Long officeOriginId) {
        return citizenCharterOriginRepo.findByOfficeOriginId(officeOriginId);
    }
}
