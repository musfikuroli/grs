package com.grs.core.repo.projapoti;

import com.grs.core.domain.projapoti.Office;
import com.grs.core.domain.projapoti.OfficeLayer;
import com.grs.core.domain.projapoti.OfficeMinistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Acer on 8/30/2017.
 */
@Repository
public interface OfficeRepo extends JpaRepository<Office, Long> {
    List<Office> findByOfficeLayerInAndStatusTrueOrderByIdAsc(List<OfficeLayer> officeLayers);
    Integer countByParentOfficeId(Long parentOfficeId);
    List<Office> findByParentOfficeIdAndStatusTrue(Long parentOfficeId);
    Integer countByIdEqualsAndOfficeLayerIn(Long officeId, List<OfficeLayer> officeLayers);
    List<Office> findByIdIn(List<Long> idList);
    List<Office> findByOfficeOriginIdAndStatusTrue(Long officeoriginId);

    List<Office> findByOfficeOriginIdInAndStatusTrue(List<Long>  officeoriginId);

    List<Office> findByDistrictIdAndOfficeLayerInAndStatusTrueOrderByIdAsc(Integer districtId, List<OfficeLayer> officeLayers);
    List<Office> findByDivisionIdAndOfficeLayerInAndStatusTrueOrderByIdAsc(Integer divisionId, List<OfficeLayer> officeLayers);
    List<Office> findByDivisionIdAndDistrictIdAndOfficeLayerInAndStatusTrueOrderByIdAsc(Integer divisionId, Integer districtId, List<OfficeLayer> officeLayers);
    List<Office> findByDivisionIdAndDistrictIdAndUpazilaIdAndOfficeLayerInAndStatusTrueOrderByIdAsc(Integer divisionId, Integer districtId, Integer upazilaId, List<OfficeLayer> officeLayers);
    List<Office> findAllByStatusTrue();
    List<Office> findByDivisionId(Integer divisionId);
    List<Office> findByDivisionIdAndDistrictId(Integer divisionId, Integer districtId);
    List<Office> findByDivisionIdAndDistrictIdAndUpazilaId(Integer divisionId, Integer districtId, Integer upazilaId);
    List<Office> findByDivisionIdAndOfficeMinistry(Integer divisionId, OfficeMinistry officeMinistry);
    List<Office> findByDivisionIdAndDistrictIdAndOfficeMinistry(Integer divisionId, Integer districtId, OfficeMinistry officeMinistry);
    List<Office> findByDivisionIdAndDistrictIdAndUpazilaIdAndOfficeMinistry(Integer divisionId, Integer districtId, Integer upazilaId, OfficeMinistry officeMinistry);

    @Query(
            nativeQuery = true,
            value = "select * from offices \n" +
                    "where id in\n" +
                    "(select x.office_id as id\n" +
                    "from offices_gro as x\n" +
                    "where x.office_id in :officeIdList )")
    List<Office> getGRSenabledOfficesFromOffices(@Param("officeIdList") List<Long> officeIdList);

    @Query(
            nativeQuery = true,
            value = "select x.id \n" +
                    "from offices as x, office_layers as y\n" +
                    "where x.office_layer_id=y.id\n" +
                    "and\n" +
                    "x.geo_division_id=:geoDivisionId\n" +
                    "and y.layer_level=:layerLevel\n")
    List<Long> getOfficeIdListByGeoDivisionId(@Param("geoDivisionId") Long geoDivisionId, @Param("layerLevel") Long layerLevel);

    @Query(
            nativeQuery = true,
            value = "select x.id \n" +
                    "from offices as x, office_layers as y\n" +
                    "where x.office_layer_id=y.id\n" +
                    "and\n" +
                    "x.geo_district_id=:geoDistrictId\n" +
                    "and y.layer_level=:layerLevel\n")
    List<Long> getOfficeIdListByGeoDistrictId(@Param("geoDistrictId") Long geoDistrictId, @Param("layerLevel") Long layerLevel);
}
