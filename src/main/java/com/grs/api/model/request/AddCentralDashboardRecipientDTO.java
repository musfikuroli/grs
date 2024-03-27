package com.grs.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCentralDashboardRecipientDTO {
    private Long id;
    private Long officeId;
    private Long officeUnitOrganogramId;
    private Boolean status;
}
