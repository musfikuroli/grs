package com.grs.api.myGov;


import com.grs.api.model.request.ComplainantDTO;
import com.grs.core.domain.Gender;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC)
@AllArgsConstructor
@ToString
public class MyGovUser implements Serializable {

    Integer id;
    String citizen_id;
    Integer mobile;
    String email;
    Integer user_type;
    Long nid;
    String name;
    String name_en;
    String mother_name;
    String mother_name_en;
    String father_name;
    String father_name_en;
    String spouse_name;
    String spouse_name_en;
    String gender;
    String nationality;
    String date_of_birth;
    String occupation;
    String religion;
    String pre_address;
    String per_address;
    String pre_division;
    String pre_district;
    String pre_upazila;
    String per_division;
    String per_district;
    String per_upazila;
    String photo;
    String brn;
    String passport;
    String tin;
    String bin;
    Integer email_verify;
    Integer nid_verify;
    Integer brn_verify;
    Integer passport_verify;
    Integer pds_emis_verify;
    Integer tin_verify;
    Integer bin_verify;
    String referer_uri;
    String source;

    public ComplainantDTO toComplainantDTO() {

        ComplainantDTO c = new ComplainantDTO();
//        c.setId(this.id);
//        c.setcitizen_id(this.citizen_id);
        c.setPhoneNumber((this.mobile != null) ? this.mobile.toString() : null);
        c.setEmail(this.email);
//        c.setuser_type(this.user_type);
        c.setIdentificationType("NID");
        c.setIdentificationValue((this.nid != null) ? this.nid.toString() : null);
        c.setName(this.name);
//        c.setname_en(this.name_en);
//        c.setmother_name(this.mother_name);
//        c.setmother_name_en(this.mother_name_en);
//        c.setfather_name(this.father_name);
//        c.setfather_name_en(this.father_name_en);
//        c.setspouse_name(this.spouse_name);
//        c.setspouse_name_en(this.spouse_name_en);
        c.setGender((this.gender != null) ? this.gender.toUpperCase().trim() : null);
        c.setNationality(this.nationality);
        c.setBirthDate(this.date_of_birth);
        c.setOccupation(this.occupation);
//        c.setreligion(this.religion);
        c.setPresentAddressHouse(this.pre_address);
        c.setPermanentAddressHouse(this.per_address);
        c.setPresentAddressDivisionNameBng(this.pre_division);
        c.setPresentAddressDivisionNameBng(this.pre_district);
        c.setPresentAddressTypeValue("UPAZILA");
        c.setPresentAddressTypeNameBng(this.pre_upazila);
        c.setPermanentAddressDivisionNameBng(this.per_division);
        c.setPermanentAddressDistrictNameBng(this.per_district);
        c.setPermanentAddressTypeValue("UPAZILA");
        c.setPermanentAddressTypeNameBng(this.per_upazila);
//        c.setphoto(this.photo);
//        c.setbrn(this.brn);
//        c.setPassport(this.passport);
//        c.settin(this.tin);
//        c.setbin(this.bin);
//        c.setemail_verify(this.email_verify);
//        c.setnid_verify(this.nid_verify);
//        c.setbrn_verify(this.brn_verify);
//        c.setpassport_verify(this.passport_verify);
//        c.setpds_emis_verify(this.pds_emis_verify);
//        c.settin_verify(this.tin_verify);
//        c.setbin_verify(this.bin_verify);
//        c.setreferer_uri(this.referer_uri);
//        c.setsource(this.source);

        return c;
    }
}
