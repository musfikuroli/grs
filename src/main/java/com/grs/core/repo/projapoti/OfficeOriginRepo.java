package com.grs.core.repo.projapoti;

import com.grs.core.domain.projapoti.OfficeOrigin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by HP on 4/9/2018.
 */
@Repository
public interface OfficeOriginRepo extends JpaRepository<OfficeOrigin, Long> {
    List<OfficeOrigin> findByOfficeLayerIdIn(List<Long> officeLayerIds);
}
