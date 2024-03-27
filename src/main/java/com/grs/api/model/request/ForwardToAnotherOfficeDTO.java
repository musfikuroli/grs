package com.grs.api.model.request;

import com.grs.core.domain.GrievanceCurrentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by User on 11/5/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForwardToAnotherOfficeDTO {
    private Long grievanceId;
    private Long officeId;
    private Long citizenCharterId;
    private String note;
    private String otherServiceName;
    private GrievanceCurrentStatus currentStatus;

}
