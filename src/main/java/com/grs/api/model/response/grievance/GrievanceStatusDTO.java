package com.grs.api.model.response.grievance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 24-Jan-18.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GrievanceStatusDTO {
    private Long id;
    private String statusEng;
    private String statusBng;
    private String submissionDateEng;
    private String submissionDateBng;
    private String closeDateBng;
    private String closeDateEng;
    private String serviceNameEng;
    private String serviceNameBng;
}
