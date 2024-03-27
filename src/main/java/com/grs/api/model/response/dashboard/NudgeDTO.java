package com.grs.api.model.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NudgeDTO {
    private Long id;
    private String trackingNumber;
    private String subject;
    private String grievanceSubmissionDate;
    private String officeName;
    private String dateOfNudge;
    private String currentStatus;
}
