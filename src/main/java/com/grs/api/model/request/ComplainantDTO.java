package com.grs.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 18-Oct-17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComplainantDTO {
    private String pinNumber;
    private String phoneNumber;
    private String name;
    private String nationality;
    private String email;
    private String identificationType;
    private String identificationValue;
    private String birthDate;
    private String occupation;
    private String education;
    private String gender;

    private String oldPassword;
    private String newPassword;

    private String permanentAddressCountryId;

    private String permanentAddressHouse;
    private String permanentAddressStreet;
    private String permanentAddressDivisionId;
    private String permanentAddressDivisionNameBng;
    private String permanentAddressDivisionNameEng;
    private String permanentAddressDistrictId;
    private String permanentAddressDistrictNameBng;
    private String permanentAddressDistrictNameEng;
    private String permanentAddressTypeValue;
    private String permanentAddressTypeId;
    private String permanentAddressTypeNameBng;
    private String permanentAddressTypeNameEng;
    private String permanentAddressPostalCode;

    private String presentAddressCountryId;

    private String presentAddressHouse;
    private String presentAddressStreet;
    private String presentAddressDivisionId;
    private String presentAddressDivisionNameBng;
    private String presentAddressDivisionNameEng;
    private String presentAddressDistrictId;
    private String presentAddressDistrictNameBng;
    private String presentAddressDistrictNameEng;
    private String presentAddressTypeValue;
    private String presentAddressTypeId;
    private String presentAddressTypeNameBng;
    private String presentAddressTypeNameEng;
    private String presentAddressPostalCode;

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
}