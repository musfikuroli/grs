package com.grs.core.dao;

import com.grs.core.domain.grs.CentralDashboardRecipient;
import com.grs.core.repo.grs.CentralDashboardRecipientRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CentralDashboardRecipientDAO {
    @Autowired
    private CentralDashboardRecipientRepo centralDashboardRecipientRepo;

    public CentralDashboardRecipient findOne(Long id) {
        return centralDashboardRecipientRepo.findOne(id);
    }

    public List<CentralDashboardRecipient> findAll() {
        return centralDashboardRecipientRepo.findAll();
    }

    public List<CentralDashboardRecipient> findAllActiveRecipients() {
        return centralDashboardRecipientRepo.findAllByStatusTrue();
    }

    public CentralDashboardRecipient save(CentralDashboardRecipient recipient) {
        return centralDashboardRecipientRepo.save(recipient);
    }

    public void delete(Long id) {
        centralDashboardRecipientRepo.delete(id);
    }

    public CentralDashboardRecipient findActiveRecipientByOfficeIdAndOfficeUnitOrganogramId(Long officeId, Long officeUnitOrganogramId) {
        return centralDashboardRecipientRepo.findByOfficeIdAndOfficeUnitOrganogramIdAndStatus(officeId, officeUnitOrganogramId, true);
    }

    public CentralDashboardRecipient findByOfficeIdAndOfficeUnitOrganogramId(Long officeId, Long officeUnitOrganogramId) {
        return centralDashboardRecipientRepo.findByOfficeIdAndOfficeUnitOrganogramId(officeId, officeUnitOrganogramId);
    }

    public Boolean hasAccessToCentralDashboard(Long officeId, Long officeUnitOrganogramId) {
        CentralDashboardRecipient recipient = findActiveRecipientByOfficeIdAndOfficeUnitOrganogramId(officeId, officeUnitOrganogramId);
        return (recipient != null);
    }

}
