package com.grs.api.model.response;

import com.grs.api.model.response.dashboard.GrievanceCurrentLocationDTO;
import com.grs.api.model.response.grievance.GrievanceDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NudgeableGrievanceDTO {
    GrievanceDTO grievance;
    List<GrievanceCurrentLocationDTO> grievanceCurrentLocationList;
}
