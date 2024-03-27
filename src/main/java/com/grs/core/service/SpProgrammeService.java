package com.grs.core.service;

import com.grs.api.model.response.OccupationDTO;
import com.grs.api.model.response.SpProgramDto;
import com.grs.api.model.response.SpProgramGroDto;
import com.grs.core.domain.grs.Occupation;
import com.grs.core.domain.grs.SpProgramme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.WeakHashMap;
import java.util.List;

@Service
public class SpProgrammeService {
    @Autowired
    private com.grs.core.dao.SpProgrammeDAO spProgrammeDAO;

    public List<com.grs.core.domain.grs.SpProgramme> findAll() {
        return spProgrammeDAO.findAll();
    }

    public List<SpProgramme> findAllByStatusAndOfficeIdNotNull() {
        return spProgrammeDAO.findAllByStatusAndOfficeIdNotNull();
    }

    public List<SpProgramGroDto> getSpProgramGroDetailsList() {
        return spProgrammeDAO.getSpProgramGroDetailsList();
    }

    public SpProgramme getSpProgramme(Integer id) {
        return this.spProgrammeDAO.findOne(id);
    }

    public SpProgramDto convertToSpProgramDTO(SpProgramme entity) {
        return SpProgramDto.builder()
                .id(entity.getId())
                .nameBn(entity.getNameBn())
                .nameEn(entity.getNameEn())
                .officeId(entity.getOfficeId())
                .status(entity.getStatus())
                .build();
    }

    public Boolean createSpProgram(SpProgramDto dto) {
        try {
            Integer count = this.spProgrammeDAO.checkUniqueSpProgram(dto);
            if (count != null && !count.equals(0)) {
                return false;
            }
            if (dto.getOfficeId() == null) {
                return false;
            }
            Boolean checkGroExist = spProgrammeDAO.checkIfGroExistsForSpProgramOffice(dto.getOfficeId());
            if (!checkGroExist) {
                return false;
            }
            SpProgramme spProgramme = SpProgramme.builder()
                    .nameBn(dto.getNameBn())
                    .nameEn(dto.getNameEn())
                    .officeId(dto.getOfficeId())
                    .status(dto.getStatus())
                    .build();
            this.spProgrammeDAO.saveSpProgram(spProgramme);
            return true;
        } catch (Exception ex) {
            throw ex;
        }
    }

    public Boolean updateSpProgram(SpProgramDto dto) {
        try {
            if (dto.getOfficeId() == null) {
                return false;
            }
            Boolean checkGroExist = spProgrammeDAO.checkIfGroExistsForSpProgramOffice(dto.getOfficeId());
            if (!checkGroExist) {
                return false;
            }
            SpProgramme spProgramme = spProgrammeDAO.findOne(dto.getId());
            spProgramme.setNameBn(dto.getNameBn());
            spProgramme.setNameEn(dto.getNameEn());
            if (dto.getOfficeId() != null)  spProgramme.setOfficeId(dto.getOfficeId());
            spProgramme.setStatus(dto.getStatus());
            this.spProgrammeDAO.saveSpProgram(spProgramme);
            return true;
        } catch (Exception ex) {
            throw ex;
        }
    }
}