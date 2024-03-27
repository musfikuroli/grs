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
public class GrievanceForwardingInvestigationDTO {
    private Long grievanceId;
    private String note;
    private GrievanceCurrentStatus currentStatus;
    private List<String> committee;
    private String head;
}
