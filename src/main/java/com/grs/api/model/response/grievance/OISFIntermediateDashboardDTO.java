package com.grs.api.model.response.grievance;

import com.grs.api.sso.GeneralInboxDataDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OISFIntermediateDashboardDTO {
    private List<OISFGrievanceDTO> grievanceDTOS;
    private GeneralInboxDataDTO generalInboxDataDTO;
}
