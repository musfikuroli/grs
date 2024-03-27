package com.grs.core.dao;

import com.grs.core.domain.grs.Notification;
import com.grs.core.repo.grs.NotificationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Acer on 9/28/2017.
 */
@Service
public class NotificationDAO {
    @Autowired
    private NotificationRepo notificationRepo;

    public Notification findOne(Long id) {
        return this.notificationRepo.findOne(id);
    }

    public List<Notification> findByOfficeIdAndEmployeeRecordIdAndOfficeUnitOrganogramIdOrderByIdDesc(Long officeId, Long employeeRecordId, Long officeUnitOrganogramId){
        return this.notificationRepo.findByOfficeIdAndEmployeeRecordIdAndOfficeUnitOrganogramIdOrderByIdDesc(officeId, employeeRecordId, officeUnitOrganogramId);
    }

    public Page<Notification> findByOfficeIdAndEmployeeRecordIdAndOfficeUnitOrganogramIdOrderByIdDesc(Long officeId, Long employeeRecordId, Long officeUnitOrganogramId, Pageable pageable){
        return this.notificationRepo.findByOfficeIdAndEmployeeRecordIdAndOfficeUnitOrganogramIdOrderByIdDesc(officeId, employeeRecordId, officeUnitOrganogramId, pageable);
    }

    public Notification saveNotification(Notification notification){
        return this.notificationRepo.save(notification);
    }
}
