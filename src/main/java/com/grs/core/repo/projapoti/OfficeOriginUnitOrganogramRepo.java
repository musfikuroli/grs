package com.grs.core.repo.projapoti;

import com.grs.core.domain.projapoti.OfficeOriginUnitOrganogram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OfficeOriginUnitOrganogramRepo extends JpaRepository<OfficeOriginUnitOrganogram, Long> {
    List<OfficeOriginUnitOrganogram> findByOfficeOriginUnitId(Long officeOriginUnitId);

    public List<OfficeOriginUnitOrganogram> findByOfficeOriginUnitIdIn(List<Long> officeOriginUnitIds);
}
