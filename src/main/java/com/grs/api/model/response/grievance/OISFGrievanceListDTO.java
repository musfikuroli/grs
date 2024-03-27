package com.grs.api.model.response.grievance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OISFGrievanceListDTO {
    private List<OISFGrievanceDTO> summary;
    private List<OISFDashboardDTO> dashboard;
    private Long designation_id;

}
