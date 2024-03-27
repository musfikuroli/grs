package com.grs.core.service;

import com.grs.api.model.response.EducationDTO;
import com.grs.core.dao.EducationDAO;
import com.grs.core.domain.grs.Education;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Created by HP on 1/31/2018.
 */
@Service
public class EducationService {
    @Autowired
    private EducationDAO educationDAO;

    public EducationDTO convertToEducationDTO(Education education) {
        return EducationDTO.builder()
                .Id(education.getId())
                .educationBangla(education.getEducationBangla())
                .educationEnglish(education.getEducationEnglish())
                .status(education.getStatus())
                .build();
    }

    public Page<EducationDTO> findAllEducation (Pageable pageable) {
        Page<EducationDTO> educationDTOS = this.educationDAO.findAll(pageable).map(this::convertToEducationDTO);
        return educationDTOS;
    }

    public Boolean saveAllEducation(EducationDTO educationDTO) {
        Integer countEducation = this.educationDAO.countByEducationBanglaAndEducationEnglish(educationDTO.getEducationBangla(),educationDTO.getEducationEnglish());
        if (!countEducation.equals(0)){
            return false;
        }
        Education education = Education.builder()
                .educationBangla(educationDTO.getEducationBangla())
                .educationEnglish(educationDTO.getEducationEnglish())
                .status(educationDTO.getStatus())
                .build();
        this.educationDAO.saveEducation(education);
        return true;
    }

    public Education getEducation(Long id) {
        return this.educationDAO.findOne(id);
    }

    public Boolean updateOccupation(Long educationID, EducationDTO educationDTO) {
        Education education = this.getEducation(educationID);
        education.setEducationBangla(educationDTO.getEducationBangla());
        education.setEducationEnglish(educationDTO.getEducationEnglish());
        education.setStatus(educationDTO.getStatus());
        this.educationDAO.saveEducation(education);
        return true;
    }

}
