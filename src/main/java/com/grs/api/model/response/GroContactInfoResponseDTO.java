package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 29-Jan-18.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroContactInfoResponseDTO {
    private String nameBangla;
    private String nameEnglish;
    private String designationBangla;
    private String designationEnglish;
    private String phoneNumber;
    private String email;
    private String officeNameBangla;
    private String officeNameEnglish;
    private String officeUnitNameBangla;
    private String officeUnitNameEnglish;
}
