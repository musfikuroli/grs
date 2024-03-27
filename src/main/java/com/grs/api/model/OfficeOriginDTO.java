package com.grs.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by HP on 4/9/2018.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficeOriginDTO {
    private Long id;
    private Long officeLayerId;
    private String officeNameEnglish;
    private String officeNameBangla;
}
