package com.grs.core.repo.projapoti;

import com.grs.core.domain.projapoti.Municipality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Acer on 17-Oct-17.
 */
@Repository
public interface MunicipalityRepo extends JpaRepository<Municipality, Integer> {
    List<Municipality> findByDivisionIdAndDistrictId(Integer divisionId, Integer districtId);
}
