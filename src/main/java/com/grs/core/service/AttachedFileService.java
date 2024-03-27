package com.grs.core.service;

import com.grs.api.model.request.FileDTO;
import com.grs.api.model.request.GrievanceRequestDTO;
import com.grs.core.dao.AttachedFileDAO;
import com.grs.core.domain.grs.CellMeeting;
import com.grs.core.domain.grs.CellMeetingAttachedFile;
import com.grs.core.domain.grs.Grievance;
import com.grs.core.domain.grs.GrievanceForwarding;
import com.grs.core.domain.grs.MovementAttachedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Acer on 10/4/2017.
 */
@Service
public class AttachedFileService {
    @Autowired
    private AttachedFileDAO attachedFileDAO;

    public void addAttachedFiles(Grievance grievance, GrievanceRequestDTO grievanceRequestDTO) {
        List<FileDTO> filePaths = grievanceRequestDTO.getFiles();
        this.attachedFileDAO.addAttachedFiles(grievance,filePaths);
    }

    public void addMovementAttachedFiles(GrievanceForwarding grievanceForwarding, List<FileDTO> filePaths) {
        this.attachedFileDAO.addMovementAttachedFiles(grievanceForwarding,filePaths);
    }

    public void addMovementAttachedFilesRef(GrievanceForwarding grievanceForwarding, List<MovementAttachedFile> movementAttachedFiles) {
        this.attachedFileDAO.addMovementAttachedFilesRef(grievanceForwarding, movementAttachedFiles);
    }

    public List<MovementAttachedFile> getAttachedFilesByIdList(List<Long> ids){
        return this.attachedFileDAO.getAttachedFilesByIds(ids);
    }

    public void addCellMeetingAttachedFiles(CellMeeting cellMeeting, List<FileDTO> filePaths){
        this.attachedFileDAO.addCellMeetingAttachedFiles(cellMeeting,filePaths);
    }

    public List<CellMeetingAttachedFile> getAttachedFilesForCellMeeting(CellMeeting cellMeeting){
        return this.attachedFileDAO.getAttachedFiles(cellMeeting);
    }

}
