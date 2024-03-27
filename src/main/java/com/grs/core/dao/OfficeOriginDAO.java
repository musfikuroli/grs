package com.grs.core.dao;

import com.grs.api.model.response.OfficeOriginUnitDTO;
import com.grs.api.model.response.OfficeOriginUnitOrganogramDTO;
import com.grs.core.domain.projapoti.OfficeOrigin;
import com.grs.core.domain.grs.CitizensCharterOrigin;
import com.grs.core.repo.grs.CitizensCharterOriginRepo;
import com.grs.core.repo.projapoti.OfficeOriginRepo;
import com.grs.core.repo.projapoti.OfficeOriginUnitOrganogramRepo;
import com.grs.core.repo.projapoti.OfficeOriginUnitRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by HP on 4/9/2018.
 */
@Service
public class OfficeOriginDAO {
    @Autowired
    private OfficeOriginRepo officeOriginRepo;
    @Autowired
    private CitizensCharterOriginRepo citizensCharterOriginRepo;
    @Autowired
    private OfficeOriginUnitRepo officeOriginUnitRepo;
    @Autowired
    private OfficeOriginUnitOrganogramRepo officeOriginUnitOrganogramRepo;

    public OfficeOrigin findOfficeOriginById(Long officeOriginId) {
        return officeOriginRepo.findOne(officeOriginId);
    }

    public List<OfficeOrigin> findDistinctOfficeOrigins(List<Long> officeLayerIds) {
        return officeOriginRepo.findByOfficeLayerIdIn(officeLayerIds);
    }

    public CitizensCharterOrigin getOfficeOriginInfo(Long layerLevel, Long officeOriginId) {
        return this.citizensCharterOriginRepo.findByLayerLevelAndOfficeOriginId(layerLevel,officeOriginId);
    }

    public CitizensCharterOrigin findOne(Long officeOriginInfoId) {
        return this.citizensCharterOriginRepo.findOne(officeOriginInfoId);
    }

    public CitizensCharterOrigin save(CitizensCharterOrigin citizensCharterOrigin) {
        return citizensCharterOriginRepo.save(citizensCharterOrigin);
    }

    public CitizensCharterOrigin findByLayerLevelAndOfficeOriginId(Long layerLevel, Long officeOriginId) {
        return this.citizensCharterOriginRepo.findByLayerLevelAndOfficeOriginId(layerLevel,officeOriginId);
    }

    public List<OfficeOriginUnitDTO> getOfficeOriginUnitDTOListByOfficeOriginId(Long officeOriginId) {
        return officeOriginUnitRepo.findByOfficeOriginId(officeOriginId).stream()
                .map(officeOriginUnit -> {
                    return OfficeOriginUnitDTO.builder()
                            .id(officeOriginUnit.getId())
                            .nameBangla(officeOriginUnit.getNameBangla())
                            .nameEnglish(officeOriginUnit.getNameEnglish())
                            .officeOriginId(officeOriginUnit.getOfficeOriginId())
                            .build();
                }).collect(Collectors.toList());
    }

    public List<OfficeOriginUnitOrganogramDTO> getOfficeOriginUnitOrganogramDTOListByOfficeOriginUnitId(Long officeOriginUnitId) {
        return officeOriginUnitOrganogramRepo.findByOfficeOriginUnitId(officeOriginUnitId).stream()
                .map(officeOriginUnitOrganogram -> OfficeOriginUnitOrganogramDTO.builder()
                        .id(officeOriginUnitOrganogram.getId())
                        .nameBangla(officeOriginUnitOrganogram.getNameBangla())
                        .nameEnglish(officeOriginUnitOrganogram.getNameEnglish())
                        .officeOriginUnitId(officeOriginUnitOrganogram.getOfficeOriginUnitId())
                        .build()).collect(Collectors.toList());
    }
}
