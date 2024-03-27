package com.grs.core.dao;

import com.grs.core.domain.grs.CellMeeting;
import com.grs.core.domain.grs.Grievance;
import com.grs.core.repo.grs.CellMeetingRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Acer on 14-Mar-18.
 */
@Service
public class CellMeetingDAO {
    @Autowired
    private CellMeetingRepo cellMeetingRepo;

    public void save(CellMeeting cellMeeting) {
        this.cellMeetingRepo.save(cellMeeting);
    }

    public Long getCount() {
        return this.cellMeetingRepo.count();
    }

    public Long countByStatus() {
        return this.cellMeetingRepo.countByStatus(true);
    }

    public Page<CellMeeting> findAll(Pageable pageable){
        return this.cellMeetingRepo.findAll(pageable);
    }

    public Long countByGrievancesIn(List<Grievance> grievances){
        return this.cellMeetingRepo.countByGrievancesIn(grievances);
    }

    public CellMeeting findOne(Long meetingId) {
        return cellMeetingRepo.findOne(meetingId);
    }

    public Boolean deleteMeeting(CellMeeting cellMeeting) {
        if (cellMeeting.getStatus() == false){
            return false;
        }
        cellMeetingRepo.delete(cellMeeting);
        return true;
    }
}
