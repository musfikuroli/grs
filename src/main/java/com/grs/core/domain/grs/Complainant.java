package com.grs.core.domain.grs;

import com.grs.core.domain.AddressTypeValue;
import com.grs.core.domain.BaseEntity;
import com.grs.core.domain.Gender;
import com.grs.core.domain.IdentificationType;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Acer on 9/27/2017.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "complainants")
public class Complainant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "identification_value")
    private String identificationValue;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "identification_type")
    private IdentificationType identificationType;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nationality_id", referencedColumnName = "id")
    private CountryInfo countryInfo;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "educational_qualification")
    private String education;

    @Column(name = "present_address_country_id")
    private Long presentAddressCountryId;

    @Column(name = "permanent_address_country_id")
    private Long permanentAddressCountryId;

    @Column(name = "present_address_street")
    private String presentAddressStreet;

    @Column(name = "present_address_house")
    private String presentAddressHouse;

    @Column(name = "present_address_division_name_bng")
    private String presentAddressDivisionNameBng;

    @Column(name = "present_address_division_name_eng")
    private String presentAddressDivisionNameEng;

    @Column(name = "present_address_division_id")
    private Integer presentAddressDivisionId;

    @Column(name = "present_address_district_name_bng")
    private String presentAddressDistrictNameBng;

    @Column(name = "present_address_district_name_eng")
    private String presentAddressDistrictNameEng;

    @Column(name = "present_address_district_id")
    private Integer presentAddressDistrictId;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "present_address_type_value")
    private AddressTypeValue presentAddressTypeValue;

    @Column(name = "present_address_type_id")
    private Integer presentAddressTypeId;

    @Column(name = "present_address_type_name_bng")
    private String presentAddressTypeNameBng;

    @Column(name = "present_address_type_name_eng")
    private String presentAddressTypeNameEng;

    @Column(name = "present_address_postal_code")
    private String presentAddressPostalCode;

    @Column(name = "permanent_address_street")
    private String permanentAddressStreet;

    @Column(name = "permanent_address_house")
    private String permanentAddressHouse;

    @Column(name = "permanent_address_division_name_bng")
    private String permanentAddressDivisionNameBng;

    @Column(name = "permanent_address_division_name_eng")
    private String permanentAddressDivisionNameEng;

    @Column(name = "permanent_address_division_id")
    private Integer permanentAddressDivisionId;

    @Column(name = "permanent_address_district_name_bng")
    private String permanentAddressDistrictNameBng;

    @Column(name = "permanent_address_district_name_eng")
    private String permanentAddressDistrictNameEng;

    @Column(name = "permanent_address_district_id")
    private Integer permanentAddressDistrictId;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "permanent_address_type_value")
    private AddressTypeValue permanentAddressTypeValue;

    @Column(name = "permanent_address_type_id")
    private Integer permanentAddressTypeId;

    @Column(name = "permanent_address_type_name_eng")
    private String permanentAddressTypeNameEng;

    @Column(name = "permanent_address_type_name_bng")
    private String permanentAddressTypeNameBng;

    @Column(name = "permanent_address_postal_code")
    private String permanentAddressPostalCode;

    @Column(name = "birth_date")
    private Date birthDate;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "is_authenticated")
    private boolean authenticated;

    @Column(name = "mobile_number")
    private String phoneNumber;

    @Column(name = "foreign_permanent_address_line1")
    private String foreignPermanentAddressLine1;

    @Column(name = "foreign_permanent_address_line2")
    private String foreignPermanentAddressLine2;

    @Column(name = "foreign_permanent_address_city")
    private String foreignPermanentAddressCity;

    @Column(name = "foreign_permanent_address_state")
    private String foreignPermanentAddressState;

    @Column(name = "foreign_permanent_address_zipcode")
    private String foreignPermanentAddressZipCode;

    @Column(name = "foreign_present_address_line1")
    private String foreignPresentAddressLine1;

    @Column(name = "foreign_present_address_line2")
    private String foreignPresentAddressLine2;

    @Column(name = "foreign_present_address_city")
    private String foreignPresentAddressCity;

    @Column(name = "foreign_present_address_state")
    private String foreignPresentAddressState;

    @Column(name = "foreign_present_address_zipcode")
    private String foreignPresentAddressZipCode;
}
