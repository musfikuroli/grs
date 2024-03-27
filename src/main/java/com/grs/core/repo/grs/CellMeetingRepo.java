package com.grs.core.repo.grs;

import com.grs.core.domain.grs.CellMeeting;
import com.grs.core.domain.grs.Grievance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Acer on 14-Mar-18.
 */
@Repository
public interface CellMeetingRepo extends JpaRepository<CellMeeting, Long> {
    Long countByStatus(boolean status);

    Page<CellMeeting> findAll(Pageable pageable);

    Long countByGrievancesIn(List<Grievance> grievances);
}
