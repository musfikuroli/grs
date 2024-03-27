package com.grs.api.model.response;

import com.grs.core.domain.AddressTypeValue;
import com.grs.core.domain.Gender;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aftab on 12/25/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplainantResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String nidOrBcn;
    private Gender gender;
    private String nationality;
    private String occupation;
    private String education;
    private String presentAddressStreet;
    private String presentAddressHouse;
    private Integer presentAddressDivisionId;
    private String presentAddressDivisionNameBng;
    private String presentAddressDivisionNameEng;
    private Integer presentAddressDistrictId;
    private String presentAddressDistrictNameBng;
    private String presentAddressDistrictNameEng;
    private AddressTypeValue presentAddressTypeValue;
    private Integer presentAddressTypeId;
    private String presentAddressTypeNameBng;
    private String presentAddressTypeNameEng;
    private String permanentAddressStreet;
    private String permanentAddressHouse;
    private Integer permanentAddressDivisionId;
    private String permanentAddressDivisionNameBng;
    private String permanentAddressDivisionNameEng;
    private Integer permanentAddressDistrictId;
    private String permanentAddressDistrictNameBng;
    private String permanentAddressDistrictNameEng;
    private AddressTypeValue permanentAddressTypeValue;
    private Integer permanentAddressTypeId;
    private String permanentAddressTypeNameBng;
    private String permanentAddressTypeNameEng;
    private String birthDate;
    private String phoneNumber;

    private Long permanentAddressCountryId;
    private Long presentAddressCountryId;
    private String permanentAddressCountryName;
    private String presentAddressCountryName;

    private String foreignPermanentAddressLine1;
    private String foreignPermanentAddressLine2;
    private String foreignPermanentAddressCity;
    private String foreignPermanentAddressState;
    private String foreignPermanentAddressZipCode;

    private String foreignPresentAddressLine1;
    private String foreignPresentAddressLine2;
    private String foreignPresentAddressCity;
    private String foreignPresentAddressState;
    private String foreignPresentAddressZipCode;

    private List<Long> blacklistInOfficeId = new ArrayList<>();
}
