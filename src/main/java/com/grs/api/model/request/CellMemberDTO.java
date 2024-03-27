package com.grs.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 25-Jun-18.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CellMemberDTO {
    private Long cellMemberOfficeId;
    private Long cellMemberOfficeUnitOrganogramId;
    private Long cellMemberEmployeeRecordId;
}
