package com.grs.core.dao;

import com.grs.core.domain.grs.CitizensCharterOrigin;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.domain.projapoti.OfficeLayer;
import com.grs.core.domain.projapoti.OfficeMinistry;
import com.grs.core.repo.grs.CitizensCharterOriginRepo;
import com.grs.core.repo.grs.OfficesGRORepo;
import com.grs.core.repo.projapoti.OfficeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Acer on 8/30/2017.
 */
@Service
public class OfficeDAO {
    @Autowired
    private OfficeRepo officeRepo;
    @Autowired
    private OfficesGRORepo officesGRORepo;
    @Autowired
    private CitizensCharterOriginRepo citizensCharterOriginRepo;

    public Integer getChildCountByParentOfficeId( Long parentOfficeId ) {
        return this.officeRepo.countByParentOfficeId(parentOfficeId);
    }

    public List<Office> findByMinistryAndParentOfficeId(OfficeMinistry ministry, Long parentOfficeId) {
        return this.officeRepo.findByParentOfficeIdAndStatusTrue(parentOfficeId);
    }

    public List<Office> findAll() {
        return officeRepo.findAllByStatusTrue();
    }

    public Office findOne(long id) {
        if (id == 0) {
            OfficeMinistry officeMinistry = OfficeMinistry.builder()
                    .id(0L)
                    .build();

            return Office.builder()
                    .id(0L)
                    .officeMinistry(officeMinistry)
                    .nameEnglish("Cell")
                    .nameBangla("অভিযোগ ব্যবস্থাপনা সেল")
                    .build();
        }
        return this.officeRepo.findOne(id);
    }

    public List<Office> getOfficesByOfficeLayer(List<OfficeLayer> officeLayers){
        return this.officeRepo.findByOfficeLayerInAndStatusTrueOrderByIdAsc(officeLayers);
    }

    public List<Office> getOfficesByDivisionIdAndOfficeLayers(Long divisionId, List<OfficeLayer> officeLayers){
        return this.officeRepo.findByDivisionIdAndOfficeLayerInAndStatusTrueOrderByIdAsc(divisionId.intValue(), officeLayers);
    }

    public List<Office> getOfficesByDivisionIdAndDistrictIdAndOfficeLayers(Integer divisionId, Integer districtId, List<OfficeLayer> officeLayers){
        return this.officeRepo.findByDivisionIdAndDistrictIdAndOfficeLayerInAndStatusTrueOrderByIdAsc(divisionId, districtId, officeLayers);
    }

    public List<Office> getOfficesByDivisionIdAndDistrictIdAndUpazilaIdAndOfficeLayers(Integer divisionId, Integer districtId, Integer upazilaId, List<OfficeLayer> officeLayers){
        return this.officeRepo.findByDivisionIdAndDistrictIdAndUpazilaIdAndOfficeLayerInAndStatusTrueOrderByIdAsc(divisionId, districtId, upazilaId, officeLayers);
    }

    public List<Office> getOfficesByDistrictIdAndOfficeLayers(Long districtId, List<OfficeLayer> officeLayers){
        return this.officeRepo.findByDistrictIdAndOfficeLayerInAndStatusTrueOrderByIdAsc(districtId.intValue(), officeLayers);
    }

    public List<Office> getOfficesByParentOfficeId(Long parentOfficeId) {
        return officeRepo.findByParentOfficeIdAndStatusTrue(parentOfficeId);
    }

    public List<Office> findByOfficeIdInList(List<Long> idList) {
        return officeRepo.findByIdIn(idList);
    }

    public Boolean checkIfOfficeLayerIn(Long officeId, List<OfficeLayer> officeLayers) {
        return officeRepo.countByIdEqualsAndOfficeLayerIn(officeId, officeLayers) > 0;
    }

    public List<Office> getGRSenabledOfficesFromOffices(List<Long> officeIdList) {
        return this.officeRepo.getGRSenabledOfficesFromOffices(officeIdList);
    }

    public List<Long> getOfficeIdListByGeoDivisionId(Long geoDivisionId, Long layerLevel) {
        return this.officeRepo.getOfficeIdListByGeoDivisionId(geoDivisionId, layerLevel);
    }

    public List<Long> getOfficeIdListByGeoDistrictId(Long geoDistrictId, Long layerLevel) {
        return this.officeRepo.getOfficeIdListByGeoDistrictId(geoDistrictId, layerLevel);
    }

    public List<Office> findByOfficeOriginId(Long officeoriginId) {
        return this.officeRepo.findByOfficeOriginIdAndStatusTrue(officeoriginId);
    }
    public List<Office> findByOfficeOriginIds(List<Long>  officeoriginIds) {
        return this.officeRepo.findByOfficeOriginIdInAndStatusTrue(officeoriginIds);
    }

    public List<Office> findByIdContainsInList(List<Long> idList) {
        return officeRepo.findByIdIn(idList);
    }

    public List<Office> findByDivisionId(Integer divisionId){
        return this.officeRepo.findByDivisionId(divisionId);
    }

    public List<Office> findByDivisionIdAndDistrictId(Integer divisionId, Integer districtId){
        return this.officeRepo.findByDivisionIdAndDistrictId(divisionId, districtId);
    }

    public List<Office> findByDivisionIdAndDistrictIdAndUpazilaId(Integer divisionId, Integer districtId, Integer upazilaId){
        return this.officeRepo.findByDivisionIdAndDistrictIdAndUpazilaId(divisionId, districtId, upazilaId);
    }

    public List<Office> findByDivisionIdAndOfficeMinistry(Integer divisionId, OfficeMinistry officeMinistry){
        return this.officeRepo.findByDivisionIdAndOfficeMinistry(divisionId, officeMinistry);
    }

    public List<Office> findByDivisionIdAndDistrictIdAndOfficeMinistry(Integer divisionId, Integer districtId, OfficeMinistry officeMinistry){
        return this.officeRepo.findByDivisionIdAndDistrictIdAndOfficeMinistry(divisionId, districtId, officeMinistry);
    }

    public List<Office> findByDivisionIdAndDistrictIdAndUpazilaIdAndOfficeMinistry(Integer divisionId, Integer districtId, Integer upazilaId, OfficeMinistry officeMinistry){
        return this.officeRepo.findByDivisionIdAndDistrictIdAndUpazilaIdAndOfficeMinistry(divisionId, districtId, upazilaId, officeMinistry);
    }
}
