package com.grs.core.domain.grs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Created by Acer on 08-Oct-17.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "offices_gro")
public class OfficesGRO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "office_id")
    private Long officeId;

    @Column(name = "office_name_bng")
    private String officeNameBangla;

    @Column(name = "office_name_eng")
    private String officeNameEnglish;

    @Column(name = "gro_office_id")
    private Long groOfficeId;

    @Column(name = "gro_office_unit_organogram_id")
    private Long groOfficeUnitOrganogramId;

    @Column(name = "ao_office_id")
    private Long appealOfficeId;

    @Column(name = "ao_office_unit_organogram_id")
    private Long appealOfficerOfficeUnitOrganogramId;

    @Column(name = "is_ao")
    private Boolean isAppealOfficer;

    @Column(name = "office_admin_office_id")
    private Long adminOfficeId;

    @Column(name = "office_admin_office_unit_organogram_id")
    private Long adminOfficeUnitOrganogramId;

    @Column(name = "gro_office_unit_name")
    private String groOfficeUnitName;

    @Column(name = "ao_office_unit_name")
    private String aoOfficeUnitName;

    @Column(name = "admin_office_unit_name")
    private String adminOfficeUnitName;

    @Column(name = "status")
    private Boolean status = false;





//************************  these are added for efficient searching  *****************************************************

    @Column(name = "layer_level")
    private Integer layerLevel;

    @Column(name = "custom_layer_level")
    private Integer customLayerLevel;

    @Column(name = "custom_layer_id")
    private Integer customLayerId;

    @Column(name = "office_layer_id")
    private Long officeLayerId;

    @Column(name = "office_origin_id")
    private Long officeOriginId;

    @Column(name = "office_ministry_id")
    private Long officeMinistryId;
}
