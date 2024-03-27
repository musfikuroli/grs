package com.grs.core.service;

import com.grs.api.model.response.OccupationDTO;
import com.grs.core.dao.OccupationDAO;
import com.grs.core.domain.grs.Occupation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by HP on 1/30/2018.
 */
@Service
public class OccupationService {
    @Autowired
    private OccupationDAO occupationDAO;

    public OccupationDTO convertToOccupationDTO(Occupation occupation) {
        return OccupationDTO.builder()
                .Id(occupation.getId())
                .occupationBangla(occupation.getOccupationBangla())
                .occupationEnglish(occupation.getOccupationEnglish())
                .status(occupation.getStatus())
                .build();
    }

    public Page<OccupationDTO> findAllOccupations (Pageable pageable) {
        Page<OccupationDTO> occupationDTOS = this.occupationDAO.findAll(pageable).map(this::convertToOccupationDTO);
        return occupationDTOS;
    }

    public Boolean saveAllOccupations(OccupationDTO occupationDTO) {
        Integer countOccupation = this.occupationDAO.countByOccupationBanglaAndOccupationEnglish(occupationDTO.getOccupationBangla(),occupationDTO.getOccupationEnglish());
        if (!countOccupation.equals(0)) {
            return false;
        }
        Occupation occupation = Occupation.builder()
                .occupationBangla(occupationDTO.getOccupationBangla())
                .occupationEnglish(occupationDTO.getOccupationEnglish())
                .status(occupationDTO.getStatus())
                .build();
        this.occupationDAO.saveOccupation(occupation);
        return true;
    }

    public Occupation getOccupation(Long id) {
        return this.occupationDAO.findOne(id);
    }

    public Boolean updateOccupation(Long occupationID, OccupationDTO occupationDTO) {
        Occupation occupation = this.getOccupation(occupationID);
        occupation.setOccupationBangla(occupationDTO.getOccupationBangla());
        occupation.setOccupationEnglish(occupationDTO.getOccupationEnglish());
        occupation.setStatus(occupationDTO.getStatus());
        this.occupationDAO.saveOccupation(occupation);
        return true;
    }

}
