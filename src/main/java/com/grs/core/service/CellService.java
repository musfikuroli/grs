package com.grs.core.service;

import com.grs.api.model.OfficeInformation;
import com.grs.api.model.UserInformation;
import com.grs.api.model.request.CellMeetingCloseDTO;
import com.grs.api.model.request.CellMeetingDTO;
import com.grs.api.model.request.CellMemberDTO;
import com.grs.api.model.request.MeetingDTO;
import com.grs.api.model.response.GenericResponse;
import com.grs.api.model.response.MeetingDetailsDTO;
import com.grs.api.model.response.file.FileDerivedDTO;
import com.grs.api.model.response.grievance.GrievanceDTO;
import com.grs.core.dao.CellMeetingDAO;
import com.grs.core.dao.CellMemberDAO;
import com.grs.core.domain.GrievanceCurrentStatus;
import com.grs.core.domain.grs.*;
import com.grs.utils.BanglaConverter;
import com.grs.utils.DateTimeConverter;
import com.grs.utils.StringUtil;
import com.grs.utils.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Acer on 11-Mar-18.
 */
@Service
public class CellService {
    @Autowired
    private CellMemberDAO cellMemberDAO;
    @Autowired
    private CellMeetingDAO cellMeetingDAO;
    @Autowired
    private GrievanceService grievanceService;
    @Autowired
    private GrievanceForwardingService grievanceForwardingService;
    @Autowired
    private AttachedFileService attachedFileService;
    @Autowired
    private OfficesGroService officesGroService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private OfficeService officeService;

    public CellMember getCellMemberEntry(Long officeUnitOrganogramId) {
        return this.cellMemberDAO.isACellMember(officeUnitOrganogramId);
    }

    public CellMember getCellMemberEntry(List<Long> officeIds, List<Long> officeUnitOrganogramIds) {
        return this.cellMemberDAO.isACellMember(officeIds, officeUnitOrganogramIds);
    }

    public boolean isCellGRO(OfficeInformation officeInformation) {
        if(officeInformation != null) {
            return cellMemberDAO.isCellGRO(officeInformation.getOfficeId(), officeInformation.getEmployeeRecordId());
        }
        return false;
    }

    @Transactional("transactionManager")
    public GenericResponse addNewMeeting(Authentication authentication, CellMeetingDTO cellMeetingDTO) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Long count = this.cellMeetingDAO.countByStatus();
        String msg;

        if (count != 0) {
            msg = messageService.isCurrentLanguageInEnglish() ?  "A meeting is currently running" : "একটি মিটিং চলমান রয়েছে।" ;
            return GenericResponse.builder()
                    .success(false)
                    .message(msg)
                    .build();
        }

        List<Long> grievanceIds = cellMeetingDTO.getGrievanceIds()
                .stream()
                .map(s -> Long.valueOf(s))
                .collect(Collectors.toList());

        List<Grievance> grievances = this.grievanceService.getGrievancesByIds(grievanceIds);
        count = this.cellMeetingDAO.countByGrievancesIn(grievances);
        if (count > 0) {
            msg = messageService.isCurrentLanguageInEnglish() ? "Chosen questions are selected in one or more other meetings" : "বাছাইকৃত অভিযোগসমুহ এর কোন একটি বা একাধিক অন্য কোন মিটিং এ নির্বাচিত আছে" ;
            return GenericResponse.builder()
                    .success(false)
                    .message(msg)
                    .build();
        }


        Date date = DateTimeConverter.convertToDate(cellMeetingDTO.getSubmissionDate());
        String meetingNumber = getMeetingNumber(date);

        CellMeeting cellMeeting = CellMeeting.builder()
                .meetingDate(date)
                .subject(cellMeetingDTO.getSubject())
                .grievances(grievances)
                .meetingNumber(meetingNumber)
                .build();

        cellMeeting.setStatus(true);
        this.cellMeetingDAO.save(cellMeeting);

        grievances.stream().
                forEach(grievance -> {
                    grievance.setGrievanceCurrentStatus(GrievanceCurrentStatus.CELL_MEETING_PRESENTED);
                    if (!StringUtil.isValidString(grievance.getCaseNumber())){
                        grievance.setCaseNumber(grievanceService.getCaseNumber(0L));
                    }
                });

        this.grievanceService.SaveGrievancesList(grievances);
        this.grievanceForwardingService.addEntryForCellMeetingStart(userInformation, grievances);
        msg = messageService.isCurrentLanguageInEnglish() ? "The meeting was successfully created" : "মিটিং সফলভাবে সৃজন হয়েছে ";
        return GenericResponse.builder()
                .success(true)
                .message(msg)
                .build();
    }

    public String getMeetingNumber(Date date) {
        String meetingNumber = DateTimeConverter.convertDateToStringformatForMeeting(date);
        Long count = this.cellMeetingDAO.getCount();
        meetingNumber = String.format("%04d", count) + meetingNumber;
        return meetingNumber;
    }


    public Page<MeetingDTO> getMeetingList(Pageable pageable) {
        Page<CellMeeting> cellMeetings = this.cellMeetingDAO.findAll(pageable);
        return cellMeetings.map(this::convertToMeetingDTO);
    }

    public MeetingDTO convertToMeetingDTO(CellMeeting cellMeeting) {
        return MeetingDTO.builder()
                .id(String.valueOf(cellMeeting.getId()))
                .meetingDate(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(cellMeeting.getMeetingDate())))
                .subject(cellMeeting.getSubject())
                .active(cellMeeting.getStatus())
                .build();
    }

    public MeetingDetailsDTO getMeetingDetails(Long meetingId) {
        CellMeeting cellMeeting = this.cellMeetingDAO.findOne(meetingId);
        List<GrievanceDTO> grievances = cellMeeting.getGrievances()
                .stream()
                .map(grievanceService::convertToGrievanceDTO)
                .collect(Collectors.toList());

        String meetingDate = BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(cellMeeting.getMeetingDate())).split(" ")[0];
        MeetingDTO meeting = MeetingDTO.builder()
                .active(cellMeeting.getStatus())
                .id(String.valueOf(cellMeeting.getId()))
                .subject(cellMeeting.getSubject())
                .meetingDate(meetingDate)
                .build();

        List<FileDerivedDTO> files = this.getCellMeetingAttachedFile(cellMeeting);

        return MeetingDetailsDTO.builder()
                .meeting(meeting)
                .grievances(grievances)
                .note(cellMeeting.getNote())
                .files(files)
                .build();
    }

    @Transactional("transactionManager")
    public GenericResponse closeMeeting(Authentication authentication, CellMeetingCloseDTO cellMeetingCloseDTO) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        CellMeeting cellMeeting = this.cellMeetingDAO.findOne(cellMeetingCloseDTO.getMeetingId());
        String msg;
        if (!cellMeeting.getStatus()) {
            msg = messageService.isCurrentLanguageInEnglish() ? "The meeting is not active" : "মিটিং টি চলমান নয়" ;
            return GenericResponse.builder()
                    .success(false)
                    .message(msg)
                    .build();
        }

        List<Grievance> grievances = cellMeeting.getGrievances();
        grievances.forEach(grievance -> grievance.setGrievanceCurrentStatus(GrievanceCurrentStatus.CELL_MEETING_ACCEPTED));

        cellMeeting.setStatus(false);
        cellMeeting.setNote(cellMeetingCloseDTO.getNote());

        this.grievanceService.SaveGrievancesList(grievances);
        this.grievanceForwardingService.addEntryForCellMeetingClose(userInformation, grievances, cellMeetingCloseDTO);
        this.cellMeetingDAO.save(cellMeeting);
        this.attachedFileService.addCellMeetingAttachedFiles(cellMeeting, cellMeetingCloseDTO.getFiles());
        msg = messageService.isCurrentLanguageInEnglish() ? "The meeting is successfully ended" : "মিটিং টি সফল ভাবে সমাপ্ত হয়েছে" ;
        return GenericResponse.builder()
                .success(true)
                .message(msg)
                .build();
    }

    public List<FileDerivedDTO> getCellMeetingAttachedFile(CellMeeting cellMeeting) {
        List<CellMeetingAttachedFile> attachedFiles = this.attachedFileService.getAttachedFilesForCellMeeting(cellMeeting);
        List<FileDerivedDTO> files = null;

        if (attachedFiles.size() > 0) {
            files = attachedFiles.stream().map(attachedFile -> {
                StringBuilder stringBuilderFirst = StringUtil.replaceAll(
                        new StringBuilder(attachedFile.getFilePath().substring(1)),
                        "uploadedFiles", "api/file/upload");
                String link = StringUtil.replaceAll(stringBuilderFirst, "\\", "/").append("/").toString();
                return FileDerivedDTO.builder()
                        .url(link)
                        .name(attachedFile.getFileName())
                        .build();
            }).collect(Collectors.toList());
            return files;
        }
        return null;

    }

    public GenericResponse deleteMeeting(Long meetingId) {
        CellMeeting cellMeeting = cellMeetingDAO.findOne(meetingId);

        List<Grievance> grievances = cellMeeting.getGrievances();
        grievances.stream().
                forEach(grievance -> grievance.setGrievanceCurrentStatus(GrievanceCurrentStatus.CELL_NEW));
        grievanceService.SaveGrievancesList(grievances);

        Boolean success = this.cellMeetingDAO.deleteMeeting(cellMeeting);
        String msg;
        if (success) {
            msg = messageService.isCurrentLanguageInEnglish() ? "The meeting is successfully deleted" : "মিটিং টি সফলভাবে অপসারণ হয়েছে" ;
        } else {
            msg = messageService.isCurrentLanguageInEnglish() ? "The meeting could not be deleted" : "মিটিং টি সফলভাবে অপসারণ হয়নি " ;
        }
        return GenericResponse.builder()
                .message(msg)
                .success(success)
                .build();
    }

    public List<CellMember> getCellMembers() {
        return this.cellMemberDAO.getAllCellMembers();
    }

    public GenericResponse addNewCellMember(CellMemberDTO cellMemberDTO) {
        Long cellOfficeUnitOrganogramId = this.cellMemberDAO.getNextOrganogramId();
        String msg;
        if (cellMemberDAO.ifCellMemberAlreadyExists(cellMemberDTO.getCellMemberEmployeeRecordId())){
            msg = messageService.isCurrentLanguageInEnglish() ?  "Member is already added" : "সদস্য টি উপস্থিত আছে" ;
            return GenericResponse.builder()
                    .message(msg)
                    .success(false)
                    .build();
        }

        CellMember cellMember = CellMember.builder()
                .employeeRecordId(cellMemberDTO.getCellMemberEmployeeRecordId())
                .officeId(cellMemberDTO.getCellMemberOfficeId())
                .cellOfficeUnitOrganogramId(cellOfficeUnitOrganogramId)
                .officeUnitOrganogramId(cellMemberDTO.getCellMemberOfficeUnitOrganogramId())
                .isAo(false)
                .isGro(false)
                .build();

        this.cellMemberDAO.save(cellMember);
        msg = messageService.isCurrentLanguageInEnglish() ? "Member is successfully added" : "সদস্য সফল ভাবে যোগ করা হয়েছে " ;
        return GenericResponse.builder()
                .message(msg)
                .success(true)
                .build();
    }

    public GenericResponse removeCellMember(Long memberId) {
        Boolean success =  this.cellMemberDAO.deleteOne(memberId);
        String msg;

        if (success) {
            msg = messageService.isCurrentLanguageInEnglish() ? "The member is successfully deleted" : "সদস্য টি সফলভাবে অপসারণ হয়েছে" ;
        } else {
            msg = messageService.isCurrentLanguageInEnglish() ? "The member could not be deleted" : "সদস্য টি সফলভাবে অপসারণ হয়নি " ;
        }
        return GenericResponse.builder()
                .message(msg)
                .success(success)
                .build();
    }


    @Transactional("transactionManager")
    public GenericResponse assignCellGRO(Long memberId) {
        CellMember cellMember = this.cellMemberDAO.findOne(memberId);
        String msg;
        if (cellMember.getIsGro() || cellMember.getIsAo()){
            msg = messageService.isCurrentLanguageInEnglish() ? "সদস্য কে সফল ভাবে সদস্য সচিব নির্বাচন করা যায় নি  " : "The member could not be successfully elected as member secretary";
            return GenericResponse.builder().success(false).message(msg).build();
        }

        CellMember oldCellMember = this.cellMemberDAO.findByIsGro();
        if (oldCellMember != null) {
            oldCellMember.setIsGro(false);
            this.cellMemberDAO.save(oldCellMember);
        }

        cellMember.setIsGro(true);
        this.cellMemberDAO.save(cellMember);

        OfficesGRO officesGRO = officesGroService.findOfficesGroByOfficeId(0L);
        officesGRO.setGroOfficeId(0L);
        officesGRO.setGroOfficeUnitOrganogramId(cellMember.getCellOfficeUnitOrganogramId());
        officesGroService.save(officesGRO);
        msg = messageService.isCurrentLanguageInEnglish() ? "সদস্য কে সফল ভাবে সদস্য সচিব নির্বাচন করা হয়েছে " : "Member has been successfully elected as Member Secretary";
        return GenericResponse.builder().success(true).message(msg).build();
    }

    @Transactional("transactionManager")
    public GenericResponse assignCellAO(Long memberId) {
        CellMember cellMember = this.cellMemberDAO.findOne(memberId);
        String msg;
        if (cellMember.getIsAo() || cellMember.getIsGro()){
            msg = messageService.isCurrentLanguageInEnglish() ? "সদস্য কে সফল ভাবে সভাপতি নির্বাচন করা যায় নি  " : "The member could not be successfully elected as chairman";
            return GenericResponse.builder().success(false).message(msg).build();
        }

        CellMember oldCellMember = this.cellMemberDAO.findByIsAo();
        if (oldCellMember != null) {
            oldCellMember.setIsAo(false);
            this.cellMemberDAO.save(oldCellMember);
        }

        cellMember.setIsAo(true);
        this.cellMemberDAO.save(cellMember);

        OfficesGRO officesGRO = officesGroService.findOfficesGroByOfficeId(0L);
        officesGRO.setAppealOfficeId(0L);
        officesGRO.setAppealOfficerOfficeUnitOrganogramId(cellMember.getCellOfficeUnitOrganogramId());
        officesGroService.save(officesGRO);
        msg = messageService.isCurrentLanguageInEnglish() ? "সদস্য কে সফল ভাবে সভাপতি  নির্বাচন করা যায় নি  " : "The member could not be successfully elected as chairman";
        return GenericResponse.builder().success(true).message(msg).build();
    }
}
