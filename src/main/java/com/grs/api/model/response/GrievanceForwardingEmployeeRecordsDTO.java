package com.grs.api.model.response;

import com.grs.api.model.response.file.FileDerivedDTO;
import com.grs.core.domain.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Acer on 11-Oct-17.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrievanceForwardingEmployeeRecordsDTO {
    private String toGroNameBangla;
    private String fromGroNameBangla;
    private String toGroNameEnglish;
    private String fromGroNameEnglish;
    private String comment;
    private String action;
    private String createdAtBng;
    private String createdAtEng;
    private String createdAtFullBng;
    private String createdAtFullEng;
    private List<FileDerivedDTO> files;

    private String toOfficeNameBangla;
    private String toOfficeUnitNameBangla;
    private String toDesignationNameBangla;

    private String fromOfficeNameBangla;
    private String fromOfficeUnitNameBangla;
    private String fromDesignationNameBangla;

    private String fromGroUsername;

    private Boolean isCC;
    private Boolean isCommitteeHead;
    private Boolean isCommitteeMember;

    private RoleType assignedRole;
}
