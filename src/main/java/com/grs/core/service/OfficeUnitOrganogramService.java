package com.grs.core.service;

import com.grs.core.dao.OfficeUnitOrganogramDAO;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.domain.projapoti.OfficeUnitOrganogram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * Created by Acer on 17-Dec-17.
 */
@Service
public class OfficeUnitOrganogramService {
    @Autowired
    private OfficeUnitOrganogramDAO officeUnitOrganogramDAO;

    public OfficeUnitOrganogram getOfficeUnitOrganogramById(Long id) {
        OfficeUnitOrganogram officeUnitOrganogram =  this.officeUnitOrganogramDAO.findOne(id);
        if (officeUnitOrganogram == null) {
            officeUnitOrganogram =  OfficeUnitOrganogram.builder().id(id).officeId(0L).officeUnitId(0L).build();
        }
        return officeUnitOrganogram;
    }

    public OfficeUnitOrganogram getAdminOrganogram(Long officeId){
        return this.officeUnitOrganogramDAO.getByOfficeIdAndIsAdmin(officeId);
    }
}
