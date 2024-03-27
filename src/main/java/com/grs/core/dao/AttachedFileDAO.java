package com.grs.core.dao;


import com.grs.api.model.request.FileDTO;
import com.grs.core.domain.grs.*;
import com.grs.core.repo.grs.CellMeetingAttachedFileRepo;
import com.grs.core.repo.grs.AttachedFileRepo;
import com.grs.core.repo.grs.MovementAttachedFileRepo;

import com.grs.core.service.StorageService;
import com.grs.utils.Constant;

import com.grs.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by Acer on 10/4/2017.
 */
@Service
public class AttachedFileDAO {
    @Autowired
    private AttachedFileRepo attachedFileRepo;
    @Autowired
    private MovementAttachedFileRepo movementattachedFileRepo;
    @Autowired
    private CellMeetingAttachedFileRepo cellMeetingAttachedFileRepo;

    @Autowired
    private StorageService storageService;


    @Value("${upload.file.directory}")
    String uploadDirectory;

    public AttachedFile findOne(Long id) {
        return this.attachedFileRepo.findOne(id);
    }

    public AttachedFile save(AttachedFile attachedFile) {
        return this.attachedFileRepo.save(attachedFile);
    }

    public void addAttachedFiles(Grievance grievance, List<FileDTO> fileDTOs) {
        for (FileDTO fileDTO : fileDTOs) {
            if (fileDTO == null || !StringUtil.isValidString(fileDTO.getUrl())) continue;
            AttachedFile attachedFile = constructAttachedFileFromString(fileDTO);
            attachedFile.setGrievance(grievance);
            attachedFile.setStatus(true);
            this.save(attachedFile);
        }
    }

    public AttachedFile constructAttachedFileFromString(FileDTO fileDTO) {
        String filePathParts[] = fileDTO.getUrl().split("/");
        String fileName = filePathParts[filePathParts.length - 1];
        String folderName = filePathParts[filePathParts.length - 2];

        String extension = "";
        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            extension = fileName.substring(index + 1).toUpperCase();
        }
        String filePath = "." + File.separator + Constant.storedFilesFolderName + File.separator + folderName + File.separator + fileName;

        String uploadedFilePath = this.storageService.getRootLocation()+ File.separator + folderName + File.separator;

        String tempFile = this.storageService.getTempLocation()+ File.separator + folderName + File.separator + fileName;

        File f = new File(tempFile);
        if (f.exists()) {
            try {
                Path path = Paths.get(uploadedFilePath);
                if (!Files.exists(path)) {
                    Files.createDirectory(path);
                    Files.createFile(Paths.get(uploadedFilePath+fileName));
                }
                FileCopyUtils.copy(f, new File(uploadedFilePath+fileName));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        fileName = StringUtil.isValidString(fileDTO.getName()) ? fileDTO.getName() : Constant.untitledFileName;
        return AttachedFile.builder()
                .fileName(fileName)
                .fileType(extension)
                .filePath(filePath)
                .build();
    }

    public MovementAttachedFile constructMovementAttachedFileFromString(FileDTO fileDTO) {
        String filePathParts[] = fileDTO.getUrl().split("/");
        String fileName = filePathParts[filePathParts.length - 1];
        String folderName = filePathParts[filePathParts.length - 2];

        String extension = "";
        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            extension = fileName.substring(index + 1).toUpperCase();
        }
        String filePath = "." + File.separator + Constant.storedFilesFolderName + File.separator + folderName + File.separator + fileName;
        fileName = StringUtil.isValidString(fileDTO.getName()) ? fileDTO.getName() : Constant.untitledFileName;
        return MovementAttachedFile.builder()
                .fileName(fileName)
                .fileType(extension)
                .filePath(filePath)
                .build();
    }

    public CellMeetingAttachedFile constructCellMeetingAttachedFileFromString(FileDTO fileDTO) {
        String filePathParts[] = fileDTO.getUrl().split("/");
        String fileName = filePathParts[filePathParts.length - 1];
        String folderName = filePathParts[filePathParts.length - 2];

        String extension = "";
        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            extension = fileName.substring(index + 1).toUpperCase();
        }
        String filePath = "." + File.separator + Constant.storedFilesFolderName + File.separator + folderName + File.separator + fileName;
        fileName = StringUtil.isValidString(fileDTO.getName()) ? fileDTO.getName() : Constant.untitledFileName;

        return CellMeetingAttachedFile.builder()
                .fileName(fileName)
                .fileType(extension)
                .filePath(filePath)
                .build();
    }

    public void addMovementAttachedFiles(GrievanceForwarding grievanceForwarding, List<FileDTO> fileDTOs) {
        for (FileDTO fileDTO : fileDTOs) {
            MovementAttachedFile movementattachedFile = constructMovementAttachedFileFromString(fileDTO);
            movementattachedFile.setGrievanceForwarding(grievanceForwarding);
            movementattachedFile.setStatus(true);
            this.movementattachedFileRepo.save(movementattachedFile);
        }
    }

    public void addMovementAttachedFilesRef(GrievanceForwarding grievanceForwarding, List<MovementAttachedFile> movementAttachedFiles) {
        for (MovementAttachedFile movementAttachedFile : movementAttachedFiles) {
            movementAttachedFile.setGrievanceForwarding(grievanceForwarding);
            movementAttachedFile.setStatus(true);
            this.movementattachedFileRepo.save(movementAttachedFile);
        }
    }


    public List<MovementAttachedFile> getAttachedFilesByIds(List<Long> ids){
        return this.movementattachedFileRepo.findByIdIn(ids);
    }

    public void addCellMeetingAttachedFiles(CellMeeting cellMeeting, List<FileDTO> fileDTOs) {
        for (FileDTO fileDTO : fileDTOs) {
            CellMeetingAttachedFile attachedFile = constructCellMeetingAttachedFileFromString(fileDTO);
            attachedFile.setCellMeeting(cellMeeting);
            attachedFile.setStatus(true);
            this.cellMeetingAttachedFileRepo.save(attachedFile);
        }
    }

    public List<CellMeetingAttachedFile> getAttachedFiles(CellMeeting cellMeeting){
        return this.cellMeetingAttachedFileRepo.findByCellMeeting(cellMeeting);
    }

}
