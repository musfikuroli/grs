package com.grs.core.dao;

import com.grs.api.model.OfficeLayerDTO;
import com.grs.api.model.response.CitizensCharterOriginDTO;
import com.grs.core.domain.grs.CitizensCharterOrigin;
import com.grs.core.domain.projapoti.CustomOfficeLayer;
import com.grs.core.domain.projapoti.OfficeLayer;
import com.grs.core.domain.projapoti.OfficeMinistry;
import com.grs.core.repo.projapoti.CustomOfficeLayerRepo;
import com.grs.core.repo.projapoti.OfficeLayerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Acer on 8/30/2017.
 */
@Service
public class OfficeLayerDAO {
    @Autowired
    private OfficeLayerRepo officeLayerRepo;
    @Autowired
    private CustomOfficeLayerRepo customOfficeLayerRepo;

    public OfficeLayer findOne(Long id) {
        return this.officeLayerRepo.findOne(id);
    }

    public List<OfficeLayer> findAll() {
        return this.officeLayerRepo.findAll();
    }

    public List<CustomOfficeLayer> findAllCustomLayers() {
        return customOfficeLayerRepo.findAll();
    }

    public List<OfficeLayer> findByMinistry(OfficeMinistry officeMinistry) {
        return this.officeLayerRepo.findByOfficeMinistry(officeMinistry);
    }

    public List<OfficeLayer> getOfficeLayersIdByLayerLevel(Integer layerLevel) {
        return this.officeLayerRepo.findIdByLayerLevel(layerLevel);
    }

    public List<OfficeLayer> getOfficeLayersByLayerLevelAndMinistryId(Integer layerLevel, Long ministryId) {
        return this.officeLayerRepo.findIdByLayerLevelAndOfficeMinistryId(layerLevel, ministryId);
    }

    public List<OfficeLayer> getOfficeByLayerLevelAndCustomLayerId(Integer layerLevel, Integer customLayerId) {
        return this.officeLayerRepo.findByLayerLevelAndCustomLayerId(layerLevel, customLayerId);
    }

    public List<OfficeLayer> getOfficeByLayerLevelAndCustomLayerIdInList(Integer layerLevel, List<Integer> customLayerIdList) {
        return this.officeLayerRepo.findByLayerLevelAndCustomLayerIdIn(layerLevel, customLayerIdList);
    }

    public List<CustomOfficeLayer> getCustomOfficeLayersByLayerLevel(Integer layerLevel) {
        return customOfficeLayerRepo.findByLayerLevel(layerLevel);
    }

    public CustomOfficeLayer getCustomOfficeLayerById(Long id) {
        return customOfficeLayerRepo.findById(id);
    }

    public OfficeLayerDTO convertToOfficeLayerDTO(OfficeLayer officeLayer) {
        return OfficeLayerDTO.builder()
                .id(officeLayer.getId())
                .layerLevel(officeLayer.getLayerLevel())
                .layerNameBangla(officeLayer.getLayerNameBangla())
                .layerNameEnglish(officeLayer.getLayerNameEnglish())
                .build();
    }

    public CitizensCharterOriginDTO convertToOfficeOriginInfoDTO(CitizensCharterOrigin citizensCharterOrigin) {
        if(citizensCharterOrigin == null) return null;
        return CitizensCharterOriginDTO.builder()
                .id(citizensCharterOrigin.getId())
                .missionBangla(citizensCharterOrigin.getMissionBangla())
                .missionEnglish(citizensCharterOrigin.getMissionEnglish())
                .visionBangla(citizensCharterOrigin.getVisionBangla())
                .visionEnglish(citizensCharterOrigin.getVisionEnglish())
                .expectationBangla(citizensCharterOrigin.getExpectationBangla())
                .expectationEnglish(citizensCharterOrigin.getExpectationEnglish())
                .layerLevel(citizensCharterOrigin.getLayerLevel())
                .officeOriginId(citizensCharterOrigin.getOfficeOriginId())
                .build();
    }
}
