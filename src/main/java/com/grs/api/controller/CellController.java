package com.grs.api.controller;

import com.grs.api.model.request.CellMeetingCloseDTO;
import com.grs.api.model.request.CellMeetingDTO;
import com.grs.api.model.request.CellMemberDTO;
import com.grs.api.model.request.MeetingDTO;
import com.grs.api.model.response.CellMemberInfoDTO;
import com.grs.api.model.response.GenericResponse;
import com.grs.api.model.response.MeetingDetailsDTO;
import com.grs.core.domain.grs.CellMember;
import com.grs.core.service.CellService;
import com.grs.core.service.GeneralSettingsService;
import com.grs.core.service.OfficeService;
import com.grs.utils.Utility;
import com.grs.core.service.ModelViewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by Acer on 12-Mar-18.
 */
@Slf4j
@RestController
public class CellController {
    @Autowired
    private CellService cellService;
    @Autowired
    private OfficeService officeService;
    @Autowired
    private ModelViewService modelViewService;
    @Autowired
    private GeneralSettingsService generalSettingsService;

    @RequestMapping(value = "/viewMeetings.do", method = RequestMethod.GET)
    public ModelAndView getViewMeetingsPage(Authentication authentication, Model model, HttpServletRequest request) {
        if (authentication != null) {
            String fragmentFolder = "cell_meeting";
            String fragmentName = "viewMeetings";
            return modelViewService.addNecessaryAttributesAndReturnViewPage(model,
                    authentication,
                    request,
                    fragmentFolder,
                    fragmentName,
                    "admin");
        }
        return new ModelAndView("redirect:/error-page");
    }

    @RequestMapping(value = "/addMeetings.do", method = RequestMethod.GET)
    public ModelAndView getAddMeetingsPage(Authentication authentication, Model model, HttpServletRequest request) {
        if (authentication != null) {
            String fragmentFolder = "cell_meeting";
            String fragmentName = "addMeetings";
            return modelViewService.addNecessaryAttributesAndReturnViewPage(model,
                    authentication,
                    request,
                    fragmentFolder,
                    fragmentName,
                    "admin");
        }
        return new ModelAndView("redirect:/error-page");
    }

    @RequestMapping(value = "/viewMeetings.do", method = RequestMethod.GET, params = "id")
    public ModelAndView getViewMeetingDetailsPage(Authentication authentication, Model model, HttpServletRequest request, @RequestParam Long id) {
        if (authentication != null) {
            Integer maxFileSize = generalSettingsService.getMaximumFileSize();
            String allowedFileTypes = generalSettingsService.getAllowedFileTypes();
            model.addAttribute("maxFileSize", maxFileSize);
            model.addAttribute("allowedFileTypes", allowedFileTypes);
            model.addAttribute("fileSizeLabel", generalSettingsService.getAllowedFileSizeLabel());
            model.addAttribute("fileTypesLabel", generalSettingsService.getAllowedFileTypesLabel());
            return modelViewService.addNecessaryAttributesAndReturnViewPage(model,
                    authentication,
                    request,
                    "cell_meeting",
                    "viewMeetingsDetails",
                    "admin");
        }
        return new ModelAndView("redirect:/error-page");
    }

    @RequestMapping(value = "/api/cell/meeting", method = RequestMethod.POST)
    public GenericResponse addNewMeeting(Authentication authentication, @RequestBody CellMeetingDTO cellMeetingDTO) {
        return this.cellService.addNewMeeting(authentication, cellMeetingDTO);
    }

    @RequestMapping(value = "/api/cell/meeting", method = RequestMethod.GET)
    public Page<MeetingDTO> getMeetingList(@PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {
        return this.cellService.getMeetingList(pageable);
    }

    @RequestMapping(value = "/api/cell/meeting/{meeting_id}", method = RequestMethod.GET)
    public MeetingDetailsDTO getMeetingDetails(@PathVariable("meeting_id") Long meetingId) {
        return this.cellService.getMeetingDetails(meetingId);
    }

    @RequestMapping(value = "/api/cell/meeting/{meeting_id}", method = RequestMethod.DELETE)
    public GenericResponse deleteMeeting(@PathVariable("meeting_id") Long meetingId) {
        return this.cellService.deleteMeeting(meetingId);
    }

    @RequestMapping(value = "/api/cell/meeting/close", method = RequestMethod.POST)
    public GenericResponse closeMeeting(Authentication authentication, @RequestBody CellMeetingCloseDTO cellMeetingCloseDTO) {
        return this.cellService.closeMeeting(authentication, cellMeetingCloseDTO);
    }

    @RequestMapping(value = "/api/cell/members", method = RequestMethod.GET)
    public List<CellMemberInfoDTO> getCellMembers() {
        List<CellMember> cellMembers = this.cellService.getCellMembers();
        return this.officeService.getCellMembersInfo(cellMembers);
    }

    @RequestMapping(value = "/api/cell/members", method = RequestMethod.POST)
    public GenericResponse addCellMember(Authentication authentication, @RequestBody CellMemberDTO cellMemberDTO) {
        return this.cellService.addNewCellMember(cellMemberDTO);
    }

    @RequestMapping(value = "/api/cell/members/{id}", method = RequestMethod.DELETE)
    public GenericResponse removeCellMember(Authentication authentication, @PathVariable("id") Long memberId) {
        return this.cellService.removeCellMember(memberId);
    }

    @RequestMapping(value = "/api/cell/members/gro/{id}", method = RequestMethod.PUT)
    public GenericResponse assignCellGRO(Authentication authentication, @PathVariable("id") Long memberId) {
        return this.cellService.assignCellGRO(memberId);
    }
    @RequestMapping(value = "/api/cell/members/ao/{id}", method = RequestMethod.PUT)
    public GenericResponse assignCellAO(Authentication authentication, @PathVariable("id") Long memberId) {
        return this.cellService.assignCellAO(memberId);
    }
}
