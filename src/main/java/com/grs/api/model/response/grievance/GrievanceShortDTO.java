package com.grs.api.model.response.grievance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GrievanceShortDTO {
    private String grievanceId;
    private String subject;
    private String currentStatus;
}
