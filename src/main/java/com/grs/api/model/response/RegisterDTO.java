package com.grs.api.model.response;

import com.grs.core.domain.GrievanceCurrentStatus;
import com.grs.core.domain.MediumOfSubmission;
import com.grs.core.domain.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 9/17/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterDTO {
    private Long id;
    private String dateBng;
    private String dateEng;
    private Long grievanceId;
    private String caseNumber;
    private String trackingNumber;
    private String complainantName;
    private String complainantEmail;
    private String complainantMobile;
    private String subject;
    private String service;
    private MediumOfSubmission medium;
    private ServiceType serviceType;
    private String serviceTypeEng;
    private String serviceTypeBng;
    private String closingOrRejectingDateBng;
    private String closingOrRejectingDateEng;
    private String closingDateInGrievancePhase;
    private String rootCause;
    private String remedyMeasures;
    private String preventionMeasures;
    private GrievanceCurrentStatus currentStatus;
}
