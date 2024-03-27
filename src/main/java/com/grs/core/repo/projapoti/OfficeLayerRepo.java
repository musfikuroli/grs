package com.grs.core.repo.projapoti;

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
public interface OfficeLayerRepo extends JpaRepository<OfficeLayer, Long> {
    List<OfficeLayer> findByOfficeMinistry(OfficeMinistry officeMinistry);
    List<OfficeLayer> findIdByLayerLevel(Integer layerLevel);
    List<OfficeLayer> findIdByLayerLevelAndOfficeMinistryId(Integer layerLevel, Long ministryId );
    List<OfficeLayer> findByLayerLevelAndCustomLayerId(Integer layerLevel, Integer customLayerId);
    List<OfficeLayer> findByLayerLevelAndCustomLayerIdIn(Integer layerLevel, List<Integer> customLayerIdList);
    List<String> findLayerNameBngByLayerLevel(Integer layerLevel);
}
