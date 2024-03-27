package com.grs.core.repo.grs;

import com.grs.core.domain.grs.CentralDashboardRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CentralDashboardRecipientRepo extends JpaRepository<CentralDashboardRecipient, Long> {

    CentralDashboardRecipient findByOfficeIdAndOfficeUnitOrganogramIdAndStatus(Long officeId, Long officeUnitOrganogramId, Boolean status);

    CentralDashboardRecipient findByOfficeIdAndOfficeUnitOrganogramId(Long officeId, Long officeUnitOrganogramId);

    List<CentralDashboardRecipient> findAllByStatusTrue();
}
