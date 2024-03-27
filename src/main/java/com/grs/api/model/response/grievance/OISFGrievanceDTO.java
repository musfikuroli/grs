package com.grs.api.model.response.grievance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OISFGrievanceDTO {
    private Long id;
    private String subject;
    private String body;
    private String datetime;
    private String trackingNumber;
    private String caseNumber;
    private String type;
    private String status;
    private String sender;
    private String redirectURL;

}
