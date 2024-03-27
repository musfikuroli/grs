package com.grs.core.dao;

import com.grs.api.model.response.dashboard.GrievanceCountByItemDTO;
import com.grs.api.model.response.organogram.OfficeUnitDTO;
import com.grs.core.domain.projapoti.*;
import com.grs.core.repo.projapoti.OfficeOriginUnitRepo;
import com.grs.core.repo.projapoti.OfficeUnitRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OfficeUnitDAO {
    @Autowired
    private OfficeUnitRepo officeUnitRepo;
    @Autowired
    private OfficeMinistryDAO officeMinistryDAO;
    @Autowired
    private OfficeDAO officeDAO;
    @Autowired
    private OfficeOriginUnitRepo officeOriginUnitRepo;

    public List<OfficeOriginUnit> getOfficeOriginUnitsByOfficeOriginId(Long officeOriginId) {
        return this.officeOriginUnitRepo.findIdByOfficeOriginId(officeOriginId);
    }

    public OfficeUnit findOne(Long id) {
        return this.officeUnitRepo.findOne(id);
    }

    public List<OfficeUnitDTO> getDTOsByOfficeMinistryIDAndOfficeIdAndParentUnitId(long officeMinistryId, long officeId, long parentOfficeUnitId) {
        OfficeMinistry officeMinistry = this.officeMinistryDAO.findOne(officeMinistryId);
        Office office = this.officeDAO.findOne(officeId);
        OfficeUnit parentOfficeUnit = this.officeUnitRepo.findOne(parentOfficeUnitId);
        List<OfficeUnit> officeUnits;
        if (officeId == 0) {
            OfficeUnit cellOfficeUnit = this.findOne(0L);
            officeUnits = new ArrayList<OfficeUnit>() {{
                add(cellOfficeUnit);
            }};
        } else if (officeId != 0 && parentOfficeUnitId == 0) {
            officeUnits = this.officeUnitRepo.findByOffice(office);
        } else {
            officeUnits = this.officeUnitRepo.findByOfficeMinistryAndOfficeAndParentOfficeUnit(officeMinistry, office, parentOfficeUnit);
        }

        return officeUnits.stream()
                .map(this::constructOfficeUnitDTO)
                .collect(Collectors.toList());
    }

    public OfficeUnitDTO constructOfficeUnitDTO(OfficeUnit officeUnit) {
        Long parentOfficeUnitId = officeUnit.getParentOfficeUnit() == null ? 0 : officeUnit.getParentOfficeUnit().getId();
        String parentOfficeUnitName = officeUnit.getParentOfficeUnit() == null ? "" : officeUnit.getParentOfficeUnit().getUnitNameBangla();
        OfficeLayer officeLayer = officeUnit.getOfficeLayer();
        OfficeMinistry officeMinistry = officeUnit.getOfficeMinistry();
        Office office = officeUnit.getOffice();

        return OfficeUnitDTO.builder()
                .id(officeUnit.getId())
                .unitNameBng(officeUnit.getUnitNameBangla())
                .unitNameEng(officeUnit.getUnitNameEnglish())
                .officeId(officeUnit.getOffice().getId())
                .officeLayerId(officeLayer != null ? officeLayer.getId() : 0)
                .officeMinistryId(officeMinistry != null ? officeUnit.getOfficeMinistry().getId() : 0)
                .officeMinistryName(officeMinistry != null ? officeUnit.getOfficeMinistry().getNameBangla() : "")
                .officeLayerName(officeLayer != null ? officeUnit.getOfficeLayer().getLayerNameBangla() : "")
                .officeName(office != null ? officeUnit.getOffice().getNameBangla() : "")
                .parentUnitId(parentOfficeUnitId)
                .parentUnitName(parentOfficeUnitName)
                .build();
    }

    public List<GrievanceCountByItemDTO> getListOfOfficeUnitsByOfficeId(Long officeId) {
        Office office = officeDAO.findOne(officeId);
        List<OfficeUnit> officeUnits = officeUnitRepo.findByOffice(office);
        return officeUnits.stream()
                .map(this::convertToGrievanceCountByItemDTO)
                .collect(Collectors.toList());
    }

    public GrievanceCountByItemDTO convertToGrievanceCountByItemDTO(OfficeUnit officeUnit) {
        return GrievanceCountByItemDTO.builder()
                .id(officeUnit.getId())
                .nameBangla(officeUnit.getUnitNameBangla())
                .nameEnglish(officeUnit.getUnitNameEnglish())
                .grievanceCount(0L)
                .build();
    }

    public OfficeUnit findById(Long id) {
        return this.officeUnitRepo.findOne(id);
    }

    public OfficeUnit getByNameAndOfficeId(String soOfficeUnitName, Long officeId) {
        return this.officeUnitRepo.findByUnitNameBanglaAndOfficeId(soOfficeUnitName, officeId);
    }
}