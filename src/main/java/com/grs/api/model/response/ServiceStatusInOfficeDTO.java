package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceStatusInOfficeDTO {
    Long Id;
    String officeNameBangla;
    String officeNameEnglish;
    Boolean status;
}
