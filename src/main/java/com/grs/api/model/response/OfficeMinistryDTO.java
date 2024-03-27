package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OfficeMinistryDTO {
    private Long id;
    private Integer officeType;
    private String officeTypeString;
    private String nameBangla;
    private String nameEnglish;
    private String nameEnglishShort;
    private String referenceCode;
}
