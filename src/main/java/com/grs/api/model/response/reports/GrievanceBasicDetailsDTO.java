package com.grs.api.model.response.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 20-Feb-18.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GrievanceBasicDetailsDTO {
    private String caseNumber;
    private String grievanceSubmissionDate;
    private String subject;
    private String serviceName;
}
