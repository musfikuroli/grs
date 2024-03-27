package com.grs.core.dao;

import com.grs.core.domain.grs.SafetyNetProgram;
import com.grs.core.repo.grs.SafetyNetProgramRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SafeNetProgramDAO {
    @Autowired
    private SafetyNetProgramRepo safetyNetProgramRepo;

    public List<SafetyNetProgram> getSafetyNetPrograms() {
        return this.safetyNetProgramRepo.findAllByActive(Boolean.TRUE);
    }

    public SafetyNetProgram findById(Long id) {
        return this.safetyNetProgramRepo.findSafetyNetProgramByIdAndActive(id,Boolean.TRUE);
    }

    public SafetyNetProgram findByNames(String nameBn, String nameEn) {
        return this.safetyNetProgramRepo.findSafetyNetProgramByNameBnOrNameEn(nameEn, nameBn);
    }

    public List<SafetyNetProgram> findAllActive() {
        return this.safetyNetProgramRepo.findAllActive();
    }
}
