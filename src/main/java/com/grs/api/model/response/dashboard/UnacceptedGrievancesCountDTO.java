package com.grs.api.model.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnacceptedGrievancesCountDTO {
    Long newCount;
    Long sendToAOCount;
    Long sendToOtherOfficesCount;
    Long sendToChildOfficesCount;
    Long rejectedGrievanceCount;
}
