package com.grs.core.repo.grs;

import com.grs.core.domain.grs.CellMeeting;
import com.grs.core.domain.grs.CellMeetingAttachedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Acer on 27-Mar-18.
 */
@Repository
public interface CellMeetingAttachedFileRepo extends JpaRepository<CellMeetingAttachedFile, Long> {
    List<CellMeetingAttachedFile> findByCellMeeting(CellMeeting cellMeeting);
}
