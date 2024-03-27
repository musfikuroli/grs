package com.grs.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by User on 11/14/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeOrganogram {
    private Long officeId;
    private Long ministryId;
    private Long officeUnitOrganogramId;
}
