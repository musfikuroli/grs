package com.grs.api.model.response.grievanceForwarding;

import com.grs.core.domain.GrievanceCurrentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Created by User on 10/16/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrievanceForwardingWithFileDTO {
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
    private List<String> files;
    private Boolean isCommitteeHead = false;
    private Boolean isCommitteeMember = false;
}
