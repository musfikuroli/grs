package com.grs.api.model;

import com.grs.core.domain.GrievanceCurrentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * Created by User on 10/16/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrievanceForwardingDTO {
    private Long grievanceId;
    private String note;
    private Long toEmployeeRecordId;
    private Long fromEmployeeRecordId;
    private Long toOfficeId;
    private Long fromOfficeId;
    private Long toOfficeOrganogramId;
    private Long fromOfficeOrganogramId;
    private GrievanceCurrentStatus currentStatus;
    private Date deadlineDate;
    private String action;
    private Boolean isCommitteeHead = false;
    private Boolean isCommitteeMember = false;
    private Boolean isCC = false;
}
