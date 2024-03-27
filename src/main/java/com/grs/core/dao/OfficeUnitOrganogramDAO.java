package com.grs.core.dao;

import com.grs.api.model.response.organogram.OfficeUnitOrganogramDTO;
import com.grs.core.domain.projapoti.*;
import com.grs.core.repo.projapoti.OfficeOriginUnitOrganogramRepo;
import com.grs.core.repo.projapoti.OfficeUnitOrganogramRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Acer on 10/4/2017.
 */
@Service
public class OfficeUnitOrganogramDAO {
    @Autowired
    private OfficeUnitOrganogramRepo officeUnitOrganogramRepo;
    @Autowired
    private OfficeUnitDAO officeUnitDAO;
    @Autowired
    private OfficeOriginUnitOrganogramRepo officeOriginUnitOrganogramRepo;

    public List<OfficeOriginUnitOrganogram> getOfficeOriginUnitOrganogramsByOfficeOriginUnits(List<OfficeOriginUnit> officeOriginUnits) {
        List<Long> officeOriginUnitIds = officeOriginUnits.stream().map(officeOriginUnit -> officeOriginUnit.getId()).collect(Collectors.toList());
        return this.officeOriginUnitOrganogramRepo.findByOfficeOriginUnitIdIn(officeOriginUnitIds);
    }

    public OfficeUnitOrganogram findOne(Long id) {
        return this.officeUnitOrganogramRepo.findOne(id);
    }

    public OfficeUnitOrganogram findOfficeUnitOrganogramById(Long id) {
        return this.officeUnitOrganogramRepo.findOne(id);
    }

    public List<OfficeUnitOrganogramDTO> getPostsListByUnit(long parentOfficeUnitId) {
        List<OfficeUnitOrganogram> officeUnitOrganograms = this.officeUnitOrganogramRepo.findByOfficeUnitId(parentOfficeUnitId);
        return officeUnitOrganograms.stream()
                .map(this::constructOfficeUnitOrganogramDTO)
                .collect(Collectors.toList());
    }

    public OfficeUnitOrganogramDTO constructOfficeUnitOrganogramDTO(OfficeUnitOrganogram officeUnitOrganogram) {
        if (officeUnitOrganogram == null)
            return null;

        Office office = officeUnitOrganogram.getOffice();
        OfficeLayer officeLayer = (office == null) ? null : office.getOfficeLayer();
        OfficeMinistry officeMinistry = (office == null) ? null :office.getOfficeMinistry();
        OfficeUnit officeUnit = (office == null) ? null :officeUnitOrganogram.getOfficeUnit();

        return OfficeUnitOrganogramDTO.builder()
                .id(officeUnitOrganogram.getId())
                .designationBng(officeUnitOrganogram.getDesignationBangla())
                .designationEng(officeUnitOrganogram.getDesignationEnglish())
                .layerId(officeLayer == null ? 0L : officeLayer.getId())
                .layerName(officeLayer == null ? "" : officeLayer.getLayerNameBangla())
                .ministryId(officeMinistry == null ? 0L : officeMinistry.getId())
                .ministryName(officeMinistry == null ? "" : officeMinistry.getNameBangla())
                .officeId(office == null ? 0L : office.getId())
                .officeName(office == null ? "" : office.getNameBangla())
                .officeUnitId(office == null ? 0L : office.getId())
                .officeUnitName(officeUnit == null ? "" : officeUnit.getUnitNameBangla())
                .build();
    }

    public List<OfficeUnitOrganogram> findOfficeUnitOrganogramByOfficeOriginUnitOrgIdAndOfficeId(Long officeOriginUnitOrgId, Long officeId) {
        return officeUnitOrganogramRepo.findByOfficeOriginUnitOrgIdAndOfficeId(officeOriginUnitOrgId, officeId);
    }

    public OfficeUnitOrganogram getByOfficeIdAndIsAdmin(Long officeId) {
        return officeUnitOrganogramRepo.findByOfficeIdAndIsAdminAndStatus(officeId, true, true);
    }
}