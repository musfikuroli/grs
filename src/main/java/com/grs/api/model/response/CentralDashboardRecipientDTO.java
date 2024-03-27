package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CentralDashboardRecipientDTO {
    private Long id;
    private String nameBangla;
    private String nameEnglish;
    private String designation;
    private Long officeId;
    private Long officeUnitId;
    private Long officeUnitOrganogramId;
    private String officeNameBangla;
    private String officeNameEnglish;
    private String officeUnitNameBangla;
    private String officeUnitNameEnglish;
    private String phoneNumber;
    private String email;
    private Boolean status;
}
