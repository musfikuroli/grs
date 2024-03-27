package com.grs.core.repo.projapoti;

import com.grs.core.domain.projapoti.Office;
import com.grs.core.domain.projapoti.OfficeMinistry;
import com.grs.core.domain.projapoti.OfficeUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Acer on 9/6/2017.
 */
@Repository
public interface OfficeUnitRepo extends JpaRepository<OfficeUnit, Long> {
    List<OfficeUnit> findByOfficeMinistryAndOfficeAndParentOfficeUnit(OfficeMinistry officeMinistry, Office office, OfficeUnit parentOfficeUnit);

    List<OfficeUnit> findByOfficeMinistryAndOffice(OfficeMinistry officeMinistry, Office office);

    List<OfficeUnit> findByOffice(Office office);

    OfficeUnit findByUnitNameBanglaAndOfficeId(String soOfficeUnitName, Long OfficeId);
}
