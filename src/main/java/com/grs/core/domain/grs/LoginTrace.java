package com.grs.core.domain.grs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.grs.api.model.LoginType;
import com.grs.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "login_trace")
public class LoginTrace extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "login_type")
    private String loginType;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name")
    private String username;

    @Column(name = "mobile_no")
    private String mobileNo;

    @Column(name = "office_id")
    private Long officeId;

    @Column(name = "office_name_bangla")
    private String officeNameBangla;

    @Column(name = "office_name_english")
    private String officeNameEnglish;

    @Column(name = "office_ministry_id")
    private Long officeMinistryId;

    @Column(name = "office_origin_id")
    private Long officeOriginId;

    @Column(name = "designation")
    private String designation;

    @Column(name = "employee_record_id")
    private Long employeeRecordId;

    @Column(name = "office_unit_organogram_id")
    private Long officeUnitOrganogramId;

    @Column(name = "layer_level")
    private Long layerLevel;

    @Column(name = "geo_division_id")
    private Long geoDivisionId;

    @Column(name = "geo_district_id")
    private Long geoDistrictId;

}
