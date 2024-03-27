package com.grs.core.service;

import com.grs.core.dao.OfficeMinistryDAO;
import com.grs.core.domain.projapoti.OfficeMinistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Acer on 19-Dec-17.
 */
@Service
public class OfficeMinistryService {
    @Autowired
    private OfficeMinistryDAO officeMinistryDAO;

    public OfficeMinistry getOfficeMinistry(Long id) {
        if (id == 0){
            return OfficeMinistry.builder()
                    .id(0L)
                    .nameBangla("অভিযোগ ব্যবস্থাপনা সেল")
                    .nameEnglish("Grievance Redress Cell")
                    .build();
        }
        return officeMinistryDAO.findOne(id);
    }
}
