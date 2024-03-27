package com.grs.core.service;

import com.grs.core.dao.SafeNetProgramDAO;
import com.grs.core.dao.SafetyNetDAO;
import com.grs.core.domain.grs.SafetyNetProgram;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.repo.grs.BaseEntityManager;
import com.grs.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

@Slf4j
@Service
public class SafetyNetProgramService {
    @Autowired
    private SafeNetProgramDAO safeNetProgramDAO;

    @Autowired
    private BaseEntityManager entityManager;

    @Autowired
    private OfficeService officeService;
    public List<SafetyNetProgram> getSafetyNetPrograms() {
        return this.safeNetProgramDAO.getSafetyNetPrograms();
    }
    public SafetyNetProgram findById(Long id) {
        return this.safeNetProgramDAO.findById(id);
    }

    public WeakHashMap<String, String> saveProgram(SafetyNetDAO safetyNetDAO) {
        WeakHashMap<String, String> map = new WeakHashMap<>();
        if (!StringUtil.isValidString(safetyNetDAO.getNameBn())) {
            map.put("success", "false");
            map.put("message", "Program name bangla required");
            return map;
        }
        if (!StringUtil.isValidString(safetyNetDAO.getNameEn())) {
            map.put("success", "false");
            map.put("message", "Program name english required");
            return map;
        }

        if (!StringUtil.isValidString(safetyNetDAO.getOfficeId())) {
            map.put("success", "false");
            map.put("message", "Office ID required");
            return map;
        }

        if (!StringUtil.isValidString(safetyNetDAO.getOfficeLayer())) {
            map.put("success", "false");
            map.put("message", "Office Layer required");
            return map;
        }

        SafetyNetProgram program = safeNetProgramDAO.findByNames(safetyNetDAO.getNameEn().trim().toUpperCase(), safetyNetDAO.getNameBn().trim());
        if (program != null && program.getId() >0) {
            map.put("success", "false");
            map.put("message", "This safetynet program already exists");
            return map;
        }

        program = new SafetyNetProgram();
        program.setNameEn(safetyNetDAO.getNameEn());
        program.setNameBn(safetyNetDAO.getNameBn());
        program.setOfficeId(Long.parseLong(safetyNetDAO.getOfficeId()));
        program.setOfficeLayer(Long.parseLong(safetyNetDAO.getOfficeLayer()));
        program.setActive(Boolean.TRUE);

        try {
            entityManager.save(program);
            map.put("success", "true");
            map.put("message", "This safetynet program has been saved");
            return map;
        } catch (Throwable t) {
            t.printStackTrace();
            map.put("success", "false");
            map.put("message", "Internal service error. Please contact with admin!");
            return map;
        }
    }

    public Page<SafetyNetDAO> safetyNetSearch(Pageable pageable) {
        List<SafetyNetProgram> programs = safeNetProgramDAO.findAllActive();
        List<SafetyNetDAO> list = new ArrayList<>();
        if (programs != null && programs.size() >0) {
            for (SafetyNetProgram program : programs) {
                if (program == null) {
                    continue;
                }
                SafetyNetDAO dao = new SafetyNetDAO(program);
                Office office = officeService.getOffice(program.getOfficeId());
                if (office != null) {
                    dao.setOfficeNameBn(office.getNameBangla());
                    dao.setOfficeNameEn(office.getNameEnglish());
                }
                list.add(dao);
            }
        }
        final int start = pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), list.size());
        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }
}
