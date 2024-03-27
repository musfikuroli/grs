package com.grs.core.domain.doptor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OfficeOrganogram {
    private Integer id;
    private Integer office_id;
    private Integer office_unit_id;
    private Integer superior_unit_id;
    private Integer superior_designation_id;
    private Integer ref_origin_unit_org_id;
    private Integer ref_sup_origin_unit_desig_id;
    private Integer ref_sup_origin_unit_id;
    private String designation_eng;
    private String designation_bng;
    private String short_name_eng;
    private String short_name_bng;
    private Integer designation_level;
    private Integer designation_sequence;
    private String designation_description;
    private Boolean status;
    private Boolean is_admin;
    private Boolean is_unit_admin;
    private Boolean is_unit_head;
    private Boolean is_office_head;
    private Integer created_by;
    private Integer modified_by;
    private Date created;
    private Date modified;
}
