package com.grs.core.dao;

import com.grs.core.domain.projapoti.OfficeOriginUnit;
import com.grs.core.repo.projapoti.OfficeOriginUnitRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Acer on 25-Apr-18.
 */
@Service
public class OfficeOriginUnitDAO {
    @Autowired
    private OfficeOriginUnitRepo officeOriginUnitRepo;

    public OfficeOriginUnit findOne(Long id){
        return officeOriginUnitRepo.findOne(id);
    }
}
