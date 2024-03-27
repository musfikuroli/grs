package com.grs.core.domain;

import com.grs.core.domain.grs.CitizenCharter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 01-Feb-18.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrievanceCountByService {
    private Long count;
    private Long citizenCharterId;
}
