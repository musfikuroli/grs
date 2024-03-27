package com.grs.api.model.oisf_response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 30-Apr-18.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingleRoleDetailsDTO {
    String upazilaBbsCode;
    Long office_origin_id;
    Long ref_origin_unit_org_id;
    Long office_unit_organogram_id;
    Long is_admin;
    Long uid;
    Long office_id;
    String divisionBbsCode;
    String office_name_eng;
    String upazila_name_eng;
    String upazila_name_bng;
    Long layer_level;
    Long is_default_role;
    Long office_head;
    String office_name_bng;
    Long office_origin_unit_id;
    String designation_bng;
    String districtBbsCode;
    Long layer_sequence;
    Long employee_record_id;
    String name_bng;
    String division_name_bng;
    Long geo_district_id;
    Long office_ministry_id;
    Long office_unit_id;
    Long officeUnitOrganogramId;
    String unit_name_bng;
    Long geo_upazila_id;
    String district_name_bng;
    Long geo_division_id;
    Long designation_level;
    String district_name_eng;
    String personal_email;
}
