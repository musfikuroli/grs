package com.grs.core.repo.grs;

import com.grs.core.domain.grs.CitizensCharterOrigin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by HP on 4/9/2018.
 */
@Repository
public interface CitizensCharterOriginRepo extends JpaRepository<CitizensCharterOrigin, Long> {
    CitizensCharterOrigin findByLayerLevelAndOfficeOriginId(Long layerLevel, Long officeOriginId);
    CitizensCharterOrigin findByOfficeOriginId(Long officeOriginId);
}
