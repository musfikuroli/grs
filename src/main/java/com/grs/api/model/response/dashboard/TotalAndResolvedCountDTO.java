package com.grs.api.model.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TotalAndResolvedCountDTO {
    Long officeId;
    String officeName;
    Long totalCount;
    Long resolvedCount;
    Long expiredCount;
    Double rate;
}
