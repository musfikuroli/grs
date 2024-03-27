package com.grs.core.domain;

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
public class GrievanceCountByOfficeUnit {
    private Long count;
    private Long officeUnitId;
}
