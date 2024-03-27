package com.grs.core.repo.projapoti;

import com.grs.core.domain.projapoti.OfficeOriginUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfficeOriginUnitRepo extends JpaRepository<OfficeOriginUnit, Long> {
    List<OfficeOriginUnit> findByOfficeOriginId(Long officeOriginId);

    List<OfficeOriginUnit> findIdByOfficeOriginId(Long officeOriginId);
}
