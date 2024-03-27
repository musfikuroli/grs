package com.grs.core.repo.grs;

import com.grs.core.domain.grs.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by HP on 1/30/2018.
 */
@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {

    public List<Notification> findByOfficeIdAndEmployeeRecordIdAndOfficeUnitOrganogramIdOrderByIdDesc(Long officeId, Long employeeRecordId, Long officeUnitOrganogramId);

    public Page<Notification> findByOfficeIdAndEmployeeRecordIdAndOfficeUnitOrganogramIdOrderByIdDesc(Long officeId, Long employeeRecordId, Long officeUnitOrganogramId, Pageable pageable);
}