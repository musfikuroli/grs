package com.grs.api.model.response;

import com.grs.core.domain.GrievanceCurrentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by User on 10/16/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OpinionReceiverDTO {
    private String employeeName;
    private String employeeDesignation;
    private Long toEmployeeRecordId;
    private Long fromEmployeeRecordId;
    private Long toOfficeId;
    private Long fromOfficeId;
    private Long toOfficeOrganogramId;
    private Long fromOfficeOrganogramId;
    private GrievanceCurrentStatus currentStatus;
}
